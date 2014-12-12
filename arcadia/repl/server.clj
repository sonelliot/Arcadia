(ns arcadia.repl.server
  (:require [arcadia.config :refer [configuration]]
            [arcadia.log :refer [log]]
            [arcadia.network :as n]
            [arcadia.repl :refer [repl-eval-string]]
            [arcadia.util :as u]
            [clojure.string :as s])
  (:import
   [System AsyncCallback]
   ;; [System.Net.Sockets TcpListener]
   ;; [System.Threading ThreadAbortException]
   ))

;; (def ^:private listener-thread (atom nil))

;; :started -> server started
;; :stopped -> server stopped by the user
;; :waiting -> waiting for client
;; :serving -> client connected and serving REPL
;; :dormant -> server waiting to start
(def ^:private state (atom :dormant))
(def ^:private server (atom nil))
(def ^:private client (atom nil))
(def ^:private queue (atom (clojure.lang.PersistentQueue/EMPTY)))

(defn- transition! [new-state]
  (log "State: " @state " -> " new-state)
  (reset! state new-state))

(defn is-running? [] (not (= @state :dormant)))

(defn- log-client [client msg]
  (letfn [(endpoint [c]
            (->> c .get_Client .get_RemoteEndPoint))]
    (let [addr (.get_Address (endpoint client))
          port (.get_Port (endpoint client))]
      (log (format "TCP client '%s:%s' -> " addr port) msg))))

(defn- handle-connect [result]
  (let [listener (.get_AsyncState result)
        new-client (.EndAcceptTcpClient listener result)]
    (log-client new-client "connected")
    (reset! client new-client)
    (transition! :serving)))

(defn- listener
  "Server listener thread."
  [port]
  (letfn [(start-server [port]
            (transition! :started)
            (reset! server (doto (TcpListener.
                                  (IPAddress/Parse "127.0.0.1") port)
                             .Start)))
          (accept-client [server]
            (transition! :waiting)
            (.BeginAcceptTcpClient server handle-connect server))
          (read-messages [client]
            (let [stream (.GetStream client)]
              (swap! queue conj (n/receive stream))))
          (shutdown []
            (when @server (.Stop @server))
            (when @client (.Close @client))
            (transition! :dormant))]
    (while true
      (try
        (case @state
          :dormant (start-server port)
          :started (accept-client @server)
          :waiting nil ; do nothing, just spin
          :serving (read-messages @client))
        (catch Exception e
          (log "Error caught on listener: " (str e))
          (shutdown))))))

;; (defn- abort
;;   "Kill the listener thread."
;;   []
;;   (reset! state :exiting)
;;   (reset! listener-thread nil))

(defn start
  "Start the TCP REPL server."
  [^long port]
  (when (not (is-running?))
    (log "Starting TCP REPL server ...")
    (u/named-thread
      "REPL Server" (partial listener port))
    ;; (reset! listener-thread
    ;;  (u/named-thread
    ;;   "REPL Server" (partial listener port)))
    ))

(defn stop
  "Stop the TCP REPL server."
  []
  (log "Stopping TCP REPL server ...")
  (transition! state :shutdown))

(defn repl-command [code]
  (try
    (repl-eval-string code)
    (catch EndOfStreamException e "")
    (catch Exception e (str e))))

(defn update
  "Evaluate the queue of pending messages from the client."
  []
  ;; (loop [code (pop @queue)]
  ;;   (try
  ;;     (let [result (repl-command code)
  ;;           stream (.GetStream @client)]
  ;;       (n/send stream result))
  ;;     (catch Exception e
  ;;       (log (str "Send error: " e)))))
  ;; (reset! queue (clojure.lang.PersistentQueue/EMPTY))
  nil)
