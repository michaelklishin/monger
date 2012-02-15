;; Copyright (c) 2011 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.db
  (:refer-clojure :exclude [find remove count drop distinct empty?])
  (:import [com.mongodb Mongo DB DBCollection])
  (:require [monger core]))


(defn drop-db
  "Drops the specified database."
  ([] 
     (.dropDatabase ^DB monger.core/*mongodb-database*))
  ([^DB database]
     (.dropDatabase ^DB database)))


(defn get-collection-names
  "Returns a set containing the names of all collections in this database."
  ([] 
     (into #{} (.getCollectionNames ^DB monger.core/*mongodb-database*)))
  ([^DB database]
     (into #{} (.getCollectionNames ^DB database)))
)




