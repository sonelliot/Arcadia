(ns unity.docs
  (:require 'clojure.string)
  (:import [UnityEngine GameObject Transform]))

(def docs-base "/Applications/Unity/Documentation/html/en/ScriptReference/")

(defn open-docs-page [page]
  (Process/Start (str docs-base page ".html"))
  nil)

(defn docs
  ([sym]
    (if (symbol? sym)
      (let [sym-str (str sym)
            sym-resolved (str (resolve sym))
            sym-name (clojure.string/replace sym-str "UnityEngine." "")
            sym-resolved-name (clojure.string/replace sym-resolved "UnityEngine." "")]
        (if
          ;; class documentation
          (isa? (type (resolve sym)) System.Type)
            (open-docs-page sym-resolved-name))

          ;; static method or field documentation
          ;; have to do both, we can't tell methods from fields
          (do
            (open-docs-page (clojure.string/replace sym-name "/" "-"))
            (open-docs-page (clojure.string/replace sym-name "/" "."))))
      (docs (symbol (str (type sym))))))

  ([inst sym] (docs (symbol (str (type inst) "/" (str sym))))))

(defmacro docs*
  ([sym]
    `sym))


;; (Vector3/Distance)
;;   (open-method-doc "Vector3" "Distance")
;;   (open-docs-page "Vector3.Distance")
;; (Vector3/zero)
;;   (open-field-doc "Vector3" "zero")
;;   (open-docs-page "Vector3-zero")
;; (.normalized vec)
;; (.. vec normalized)
;;   (open-field-doc "Vector3" "normalized")
;;   (open-field-page "Vector3-noralized")
;; (.SetActive go true)
;; (.. go (SetActive))
;;   (open-field-doc "Vector3" "normalized")
;;   (open-field-page "Vector3-noralized")
