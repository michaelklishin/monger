(ns monger.js
  (:require [clojure.java.io]))

;;
;; Implementation
;;

(defn- normalize-resource
  [^String path]
  (if (.endsWith path ".js")
    path
    (str path ".js")))



;;
;; API
;;

(defn load-resource
  (^String [^String path]
  (with-open [rdr (clojure.java.io/reader (-> (Thread/currentThread)
                                              .getContextClassLoader
                                              (.getResourceAsStream (normalize-resource path))))]
    (reduce str "" (line-seq rdr)))))