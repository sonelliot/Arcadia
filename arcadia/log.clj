(ns arcadia.log
  (:require [clojure.string :as s]
            [arcadia.config :refer [configuration]])
  (:import [UnityEngine Debug]
           [System.IO File]))

(def ^:private logfile "Assets/Arcadia/arcadia.log")

(defn- timestamp
  "Current time as a string."
  [] (str "[ " (str DateTime/Now) " ]"))

(defn- form
  "Form the log file message."
  [msg] (str (timestamp) " " msg Environment/NewLine))

(defn log
  "Log a message to the logfile."
  [& msgs]
  (let [msg (s/join msgs)]
    (File/AppendAllText logfile (form msg))
    (when (@configuration :verbose)
      (Debug/Log msg))))
