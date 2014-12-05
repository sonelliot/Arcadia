(ns arcadia.repl.server
  (:require [arcadia.config :refer [configuration]]
            [arcadia.log :refer :all])
  (:import
   [System.Net.Sockets TcpListener]))

(def ^:private running (atom false))

(defn is-running? [] @running)

(defn start [^long port]
  "Start the TCP REPL server."
  (log "Starting TCP REPL server ...")
  (reset! running true))

(defn stop []
  "Stop the TCP REPL server."
  (log "Stopping TCP REPL server ...")
  (reset! running false))

(defn update []
  "Evaluate the queue of pending messages from the client."
  nil)
