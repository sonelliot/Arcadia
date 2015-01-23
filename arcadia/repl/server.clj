(ns arcadia.repl.server
  (:require [arcadia.config :refer [configuration]]
            [arcadia.network :as n]
            [arcadia.repl :refer [repl-eval-string]]
            [arcadia.util :as u]
            [clojure.string :as s])
  (:import
   ;[clojure.lang PersistentQueue]
   ;[System.Net.Sockets IPAddress TcpListener]
   [UnityEngine Debug]))

(def client (atom nil))
(def server-thread (atom nil))
(def message-queue (atom (PersistentQueue/EMPTY)))

(defn is-running? [] @server-thread)

(defn log [msg] (Debug/Log msg))

(defn- log-client [client msg]
  (letfn [(endpoint [c]
            (->> c .get_Client .get_RemoteEndPoint))]
    (let [addr (.get_Address (endpoint client))
          port (.get_Port (endpoint client))]
      (log (format "TCP client '%s:%s' -> " addr port) msg))))

(defn- handle-connect! [result]
  (let [listener (.get_AsyncState result)
        new-client (.EndAcceptTcpClient listener result)]
    (log-client new-client "connected")
    (reset! client new-client)))

(defn make-server [host port]
  (doto (TcpListener.
         (IPAddress/Parse host) port)
    .Start))

(defn read-messages! [client]
  (let [stream (.GetStream client)]
    (log "receiving messages")
    (swap! message-queue conj (n/receive stream))))

(defn listener [server interval]
  (try
   (do
     (log "waiting for clients")
     (.BeginAcceptTcpClient server handle-connect! server)
     (while true
       (and @client (read-messages! @client))
       (u/sleep! interval)))
   (finally (.Stop server) (and @client (.Close @client)))))

(defn repl-command [code]
  (try
    (repl-eval-string code)
    (catch EndOfStreamException e "")
    (catch Exception e (str e))))

(defn update
  "Evaluate the queue of pending messages from the client."
  []
  (doseq [code @message-queue]
    (try
      (let [result (repl-command code)
            stream (.GetStream @client)]
        (n/send stream result))
      (catch Exception e
        (log (str "Send error: " e)))))
  (reset! @message-queue (PersistentQueue/EMPTY)))

(defn start [port]
  (when (not (is-running?))
    (reset!
     server-thread
     (u/do-thread! (partial listener (make-server "127.0.0.1" port) 50)))))

(defn stop []
  (reset! server-thread (doto @server-thread .Abort .Join)))

;; (defn- listener
;;   "Server listener thread."
;;   [port]
;;   (letfn [(start-server [port]
;;             (transition! :started)
;;             (reset! server (doto (TcpListener.
;;                                   (IPAddress/Parse "127.0.0.1") port)
;;                              .Start)))
;;           (accept-client [server]
;;             (transition! :waiting)
;;             (.BeginAcceptTcpClient server handle-connect server))
;;           (read-messages [client]
;;             (let [stream (.GetStream client)]
;;               (swap! queue conj (n/receive stream))))
;;           (shutdown []
;;             (when @server (.Stop @server))
;;             (when @client (.Close @client))
;;             (transition! :dormant))]
;;     (while true
;;       (try
;;         (case @state
;;           :dormant (start-server port)
;;           :started (accept-client @server)
;;           :waiting nil ; do nothing, just spin
;;           :serving (read-messages @client))
;;         (catch Exception e
;;           (log "Error caught on listener: " (str e))
;;           (shutdown))))))

;; (defn- abort
;;   "Kill the listener thread."
;;   []
;;   (reset! state :exiting)
;;   (reset! listener-thread nil))

;; (defn start
;;   "Start the TCP REPL server."
;;   [^long port]
;;   (when (not (is-running?))
;;     (log "Starting TCP REPL server ...")
;;     (u/named-thread
;;       "REPL Server" (partial listener port))
;;     ;; (reset! listener-thread
;;     ;;  (u/named-thread
;;     ;;   "REPL Server" (partial listener port)))
;;     ))

;; (defn stop
;;   "Stop the TCP REPL server."
;;   []
;;   (log "Stopping TCP REPL server ...")
;;   (transition! state :shutdown))
