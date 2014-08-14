(ns unity.docs
  (:import [UnityEngine GameObject Transform]))

(def docs-base "/Applications/Unity/Documentation/html/en/ScriptReference/")

(defn open-docs-page [page]
  (Process/Start (str docs-base page ".html"))
  nil)

(defn open-class-doc [cls]
  (open-docs-page cls))

(defn open-field-doc [cls field]
  (open-docs-page (str cls "-" field)))

(defn open-method-doc [cls meth]
  (open-docs-page (str cls "." meth)))

(defmacro docs [d]
  (cond
    (instance? clojure.lang.Symbol d)
      (let [dval (resolve d)]
        (cond
          (instance? System.Type dval)
            `(open-docs-page ~(.Name d))

          :else
            `(open-docs-page
              ~(clojure.string/replace (str dval) "/" "."))))))