(ns arcadia.log
  (:require [clojure.clr.io :refer :all]
            ;; [clojure.string :refer [join]]
            [arcadia.config :refer [configuration]])
  (:import [UnityEngine Debug]
           [System.IO File]))

(def ^:private logfile "Assets/Arcadia/arcadia.log")

(defn- timestamp []
  "Current time as a string."
  (str "[ " (str DateTime/Now) " ]"))

(defn- form [msg]
  "Form the log file message."
  (str (timestamp) " " msg Environment/NewLine))

(defn log [msg]
  "Log a message to the logfile."
  (File/AppendAllText logfile (form msg))
  (when (@configuration :verbose)
    (Debug/Log msg)))
