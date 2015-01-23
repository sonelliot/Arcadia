(ns arcadia.network
  (:require
   [arcadia.util :as u]
   [clojure.string :as s])
  (:import
   [clojure.lang PersistentQueue]
   [System.Net.Sockets NetworkStream]))

(defn message
  "Convert bytes in the network stream into UTF8 messages that are cons'd into 
  a lazy sequence. Use `string/join' on sequence for complete message."
  [stream msg]
  (cons
   msg (lazy-seq (when (.get_DataAvailable stream)
                   (let [buffer (byte-array 1024)
                         bytes (.Read stream buffer 0 (.Length buffer))]
                     (u/utf8-string buffer))))))

(defn send [stream code]
  (let [bytes (u/utf8-bytes code)]
    (.Write stream bytes 0 (.Length bytes))))

(defn receive [stream]
  (s/join (message stream "")))

(defn try-receive [stream messages]
  (try
    (conj messages (receive stream))
    (catch Exception e (println (format "error: %s" (str e))))
    (finally messages)))

(defn try-send [stream responses]
  (try
    (do
      (doseq [r responses] (send stream r))
      PersistentQueue/EMPTY)
    (catch Exception e (println (format "error: %s" (str e))))
    (finally responses)))
