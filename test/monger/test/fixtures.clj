(ns monger.test.fixtures
  (:require [monger.collection :as mgcol]))

;;
;; fixture functions
;;

(defn purge-collection
  [collection-name, f]
  (mgcol/remove collection-name)
  (f)
  (mgcol/remove collection-name))

(defn purge-people-collection
  [f]
  (purge-collection "people" f))

(defn purge-docs-collection
  [f]
  (purge-collection "docs" f))

(defn purge-things-collection
  [f]
  (purge-collection "things" f))

(defn purge-libraries-collection
  [f]
  (purge-collection "libraries" f))
