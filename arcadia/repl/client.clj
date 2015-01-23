(ns arcadia.repl.client
  (:gen-class :main true)
  (:require
   [arcadia.network :as n]
   [arcadia.util :as u]
   [clojure.string :as s])
  (:import
   [System.IO StreamReader]
   [System.Net.Sockets TcpClient]
   [System.Text StringBuilder]))

(defn send-and-receive [stream code]
  (n/send stream code)
  (print (n/receive stream)))

(defn balanced? [code]
  (s/blank?
   (-> code
       (s/replace #"[^\(\)\[\]\{\}]" "")
       (s/replace #"(\(\)|\[\]|\{\})" ""))))

(defn read-input! [proc]
  (loop [line "" code ""]
    (if (and (not (s/blank? code)) (balanced? code))
      (do (proc code) (recur (read-line) ""))
      (recur (read-line) (str code line)))))

(defn -main [& [p]]
  (let [port (or (and p (int p)) 11211)
        client (TcpClient. "localhost" port)
        stream (.GetStream client)]
    (read-input! (partial send-and-receive stream))))
