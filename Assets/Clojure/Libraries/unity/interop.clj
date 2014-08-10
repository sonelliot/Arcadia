(ns unity.interop
  (:import [UnityEngine GameObject ])
  (:require [clojure.test :as test]))

;;; namespace for essential unity interop conveniences.

;; not sure the following is essential :\
;; (defmacro with-unchecked-math [& xs]
;;   `(binding [*unchecked-math* true]
;;      ~@xs))

(defn get-component
  {:inline (fn [obj t]
             (with-meta `(.GetComponent ~obj (~'type-args ~t))
               {:tag t}))
   :inline-arities #{2}}
  [^GameObject obj, ^Type t]
  (.GetComponent obj t))

;;; ======================================================
;;; Tests 
;;; ======================================================

(defmacro with-fresh-object [obj-var & body]
  `(let [~obj-var (GameObject. (name (gensym "arbitrary-object")))
         ret#     (do ~@body)]
     (UnityEngine.Object/Destroy ~obj-var)
     ret#))

(test/deftest get-component-test
  (test/is
    (with-fresh-object obj
      (instance?
        UnityEngine.Transform
        (get-component obj UnityEngine.Transform)))))
