(ns monger.js
  (:require [clojure.java.io :as io]))

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
           (slurp (io/resource (normalize-resource path)))))
