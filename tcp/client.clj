(ns tcp.client
  (:require
   [arcadia.util :as u]
   [arcadia.network :as n]
   [clojure.string :as s])
  (:import
   [clojure.lang PersistentQueue]
   [System.Net.Sockets TcpClient])
  (:gen-class))

(def interval 100)

(defn handle-connect! [result]
  (println "connected to server")
  (let [client (.get_AsyncState result)]
    (.EndConnect client result)))

(defn begin-connect [{:keys [host port client] :as env}]
  (println "trying connection")
  (.BeginConnect client host port handle-connect! client)
  env)

(defn balanced? [code]
  (s/blank?
   (-> code
       (s/replace #"[^\(\)\[\]\{\}]" "")
       (s/replace #"(\(\)|\[\]|\{\})" ""))))

(defn read-input []
  (loop [line "" code ""]
    (if (not (s/blank? code))
      code
      (recur (read-line) (str code line)))))

(defn communicate [{:keys [client messages responses] :as env}]
  (let [code (read-input)
        stream (.GetStream client)
        rsp-q (n/try-send stream (conj responses code))
        msg-q (n/try-receive stream messages)]
    (println code)
    (doseq [m msg-q] (print m)) env))

(defn state [{:keys [client result]}]
  (cond
   (not client)                :dormant
   (not (.Connected client))   :disconnected
   :else                       :connected))

(defn client! [env]
  (u/sleep! interval)
  (case (state env)
    :dormant (recur (begin-connect
                     (assoc env :client (TcpClient.))))
    :disconnected (recur env)
    :connected (recur (communicate env))))

(defn -main [& [host port]]
  (client! {:host (or host "localhost")
            :port (or port 11212)
            :messages (PersistentQueue/EMPTY)
            :responses (PersistentQueue/EMPTY)})
  ;; connect to server
  ;; when connected
  ;;  read input
  ;;  send and receive
  )
