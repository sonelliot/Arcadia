(ns arcadia.repl.server
  (:require [arcadia.config :refer [configuration]]
            [arcadia.log :refer :all]
            [arcadia.util :as u])
  (:import
   ;; [System.Net.Sockets TcpListener]
   ;; [System.Threading ThreadAbortException]
   ))

(def ^:private listener-thread (atom nil))

(defn is-running? [] (not (nil? @listener-thread)))

(defn- log-client [client msg]
  (letfn [(endpoint [c]
            (->> c .get_Client .get_RemoteEndPoint))]
    (let [addr (.get_Address (endpoint client))
          port (.get_Port (endpoint client))]
      (log (format "TCP client '%s:%s' -> " addr port) msg))))

(defn- listener
  "Server listener thread."
  [port]
  (try
   (let [address (IPAddress/Parse "127.0.0.1")
         server (TcpListener. address port)]
     (log "Listener started on localhost:" port)
     (.Start server)
     (while true
       (let [client (.AcceptTcpClient server)
             stream (.GetStream client)
             buffer (byte-array 1024)]
         (log-client client "connected")
         ;; Read until the stream is closed (i.e. zero bytes received).
         (while (> (.Read stream buffer 0 (.Length buffer)) 0)
           (log (u/bytes->utf8 buffer)))
         (.Close client))))
   (catch ThreadAbortException e
     (log "Asked to abort listener thread"))
   (catch Exception e
     (log "Error caught on listener: " (str e)))))

(defn- abort
  "Kill the listener thread."
  []
  (doto @listener-thread .Abort .Join)
  (reset! listener-thread nil))

(defn start
  "Start the TCP REPL server."
  [^long port]
  (when (not (is-running?))
    (log "Starting TCP REPL server ...")
    (reset! listener-thread
     (u/named-thread
      "REPL Server" (partial listener port)))))

(defn stop
  "Stop the TCP REPL server."
  [] (log "Stopping TCP REPL server ...") (abort))

(defn update
  "Evaluate the queue of pending messages from the client."
  [] nil)
