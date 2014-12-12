(ns arcadia.network
  (:require
   [arcadia.util :as u]
   [clojure.string :as s])
  (:import
   [System.Net.Sockets NetworkStream]))

(defn message
  "Convert bytes in the network stream into UTF8 messages that are cons'd into 
  a lazy sequence. Use `string/join' on sequence for complete message."
  [stream msg]
  (cons
   msg (lazy-seq (when (.get_DataAvailable stream)
                   (let [buffer (byte-array 1024)
                         bytes (.Read stream buffer 0 (.Length bytes))]
                     (u/bytes->utf8 bytes))))))

(defn send [stream code]
  (let [bytes (u/utf8->bytes code)]
    (.Write stream bytes 0 (.Length bytes))))

(defn receive [stream]
  (s/join (message stream "")))
