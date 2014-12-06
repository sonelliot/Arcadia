(ns arcadia.repl.client
  (:gen-class :main true)
  (:require [arcadia.util :as u]
            [clojure.string :as s])
  (:import
   [System.IO StreamReader]
   [System.Net.Sockets TcpClient]
   [System.Text StringBuilder]))

;; (defn -main [& args] (println "Hello"))

(defn message [stream msg]
  (cons
   msg (lazy-seq (when (.get_DataAvailable stream)
                   (let [buffer (byte-array 1024)
                         bytes (.Read stream buffer 0 (.Length bytes))]
                     (u/bytes->utf8 bytes))))))

(defn send [stream code]
  (let [bytes (u/utf8->bytes code)]
    (.Write stream bytes 0 (.Length bytes))))

(defn receive [stream]
  (print (s/join (message stream ""))))

(defn send-and-receive [stream code]
  (send stream code) (receive stream))

(defn balanced? [code]
  (s/blank?
   (-> code
       (s/replace #"[^\(\)\[\]\{\}]" "")
       (s/replace #"(\(\)|\[\]|\{\})" ""))))

(defn read-input [f]
  (loop [line "" code ""]
    (if (and (not (s/blank? code)) (balanced? code))
      (do (f code) (recur (read-line) ""))
      (recur (read-line) (str code line)))))

(defn -main [& args]
  (let [client (TcpClient. "localhost" 11211)
        stream (.GetStream client)]
    (read-input (partial send-and-receive stream))))
