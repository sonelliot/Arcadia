(ns arcadia.util
  (:import
   [System.Text Encoding]
   [System.Threading Thread ThreadStart]))

(defn utf8-string [bytes]
  (.GetString Encoding/UTF8 bytes))

(defn utf8-bytes [str]
  (.GetBytes Encoding/UTF8 str))

(defn make-thread [proc]
  (Thread. (gen-delegate ThreadStart [] (proc))))

(defn do-thread! [proc]
  (let [t (make-thread proc)] (.Start t) t))

(defn sleep! [ms] (Thread/Sleep ms))

(defn named-thread [name proc]
  "Spawn a named thread with thunk procedure."
  (doto (Thread. (gen-delegate ThreadStart [] (proc)))
    (.set_Name name)
    (.Start)))
