(ns tcp.server
  (:require
   [arcadia.util :as u]
   [arcadia.network :as n]
   [clojure.string :as s])
  (:import
   [clojure.lang PersistentQueue]
   [System.Net IPAddress]
   [System.Net.Sockets TcpListener])
  (:gen-class))

(def interval 100)
(def joined (atom nil))

(defn make-server [host port]
  (TcpListener. (IPAddress/Parse host) port))

(defn- handle-connect! [result]
  (let [listener (.get_AsyncState result)
        client (.EndAcceptTcpClient listener result)]
    (reset! joined client)))

(defn begin-connect [{:keys [server] :as env}]
  (println "waiting for client")
  (assoc env :server
         (doto server (.BeginAcceptTcpClient handle-connect! server))))

(defn start-server [{:keys [host port] :as env}]
  (println "starting server")
  (let [server (make-server host port)]
    (assoc env :server (doto server .Start))))

(defn client-connect! [env]
  (let [client @joined]
    (if client
      (do
        (println "client joined")
        (reset! joined nil)
        (assoc env :client client))
      env)))

(defn client-dropped [env]
  (println "client dropped")
  (begin-connect (assoc env :client nil)))

(defn respond [msg] (s/upper-case msg))
(defn process [{:keys [messages responses] :as env}]
  (merge env
   (loop [msg (peek messages) rem (pop messages) rsp responses]
     (if (seq rem)
       (recur (peek rem) (pop rem) (conj rsp (respond msg)))
       {:messages rem :responses rsp}))))

(defn serve-client [{:keys [client messages responses] :as env}]
  (let [stream (.GetStream client)
        msg-q (n/try-receive stream messages)
        rsp-q (n/try-send stream responses)]
    (when (not (empty? msg-q))
      (print (str msg-q)))
    (when (not (empty? rsp-q))
      (print (str rsp-q)))
    (process
     (assoc env
       :messages msg-q :responses rsp-q))))

(defn state [{:keys [server client]}]
  (cond
   (not server)                :dormant
   (not client)                :waiting
   (not (.Connected client))   :dropped
   :else                       :serving))

(defn server! [env]
  (u/sleep! interval)
  (case (state env)
    :dormant (recur (begin-connect (start-server env)))
    :waiting (recur (client-connect! env))
    :dropped (recur (client-dropped env))
    :serving (recur (serve-client env))))

(defn -main [& [host port]]
  (server! {:host (or host "127.0.0.1")
            :port (or port 11212)
            :messages (PersistentQueue/EMPTY)
            :responses (PersistentQueue/EMPTY)}))
