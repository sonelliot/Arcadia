(ns arcadia.util
  (:import
   [System.Threading Thread ThreadStart]))

(defn bytes->utf8 [bytes]
  (.GetString Encoding/UTF8 bytes 0 (.Length bytes)))

(defn utf8->bytes [str]
  (.GetBytes Encoding/UTF8 str))

(defn named-thread [name proc]
  "Spawn a named thread with thunk procedure."
  (doto (Thread. (gen-delegate ThreadStart [] (proc)))
    (.set_Name name)
    (.Start)))
