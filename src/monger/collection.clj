;; Copyright (c) 2011 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.collection
  (:import (com.mongodb Mongo DB DBCollection WriteResult DBObject WriteConcern DBCursor) (java.util List Map))
  (:require [monger core errors])
  (:use     [monger.convertion]))

;;
;; API
;;

;; monger.collection/insert

(defn ^WriteResult insert
  ([^String collection, ^DBObject doc]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.insert coll (to-db-object doc) monger.core/*mongodb-write-concern*)))
  ([^String collection, ^DBObject doc, ^WriteConcern concern]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.insert coll (to-db-object doc) concern))))


(defn ^WriteResult insert-batch
  ([^String collection, ^List docs]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.insert coll (to-db-object docs) WriteConcern/NORMAL)))
  ([^String collection, ^List docs, ^WriteConcern concern]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.insert coll (to-db-object docs) concern))))

;; monger.collection/find
(declare fields-to-db-object)

(defn ^DBCursor find
  ([^String collection]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.find coll)))
  ([^String collection, ^Map ref]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.find coll (to-db-object ref))))
  ([^String collection, ^Map ref, ^List fields]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)
           map-of-fields (fields-to-db-object fields)]
       (.find coll (to-db-object ref) (to-db-object map-of-fields))))
  )


(defn ^DBObject find-by-id
  ([^String collection, ^String id]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.findOne coll (to-db-object { :_id id }))))
  ([^String collection, ^String id, ^List fields]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)
           map-of-fields (fields-to-db-object fields)]
       (.findOne coll (to-db-object { :_id id }) (to-db-object map-of-fields))))
  )



;; monger.collection/group

;; monger.collection/count
(defn ^long count
  [^String collection]
  (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
    (.count coll)))

;; monger.collection/update
;; monger.collection/update-multi
;; monger.collection/remove

(defn ^WriteResult remove
  ([^String collection]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.remove coll (to-db-object {}))))
  ([^String collection, ^Map conditions]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.remove coll (to-db-object conditions))))
  )

;; monger.collection/ensure-index
;; monger.collection/drop-index



;;
;; Implementation
;;

(defn- fields-to-db-object
  [fields]
  (let [n             (clojure.core/count fields)
        ones          (replicate n 1)
        map-of-fields (zipmap fields ones)]
    map-of-fields))