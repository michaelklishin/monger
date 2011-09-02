;; Copyright (c) 2011 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.collection
  (:import (com.mongodb Mongo DB DBCollection WriteResult DBObject WriteConcern DBCursor) (java.util List Map) (clojure.lang IPersistentMap))
  (:require [monger core result])
  (:use     [monger.convertion]))

;;
;; API
;;

;;
;; monger.collection/insert
;;

(defn ^WriteResult insert
  ([^String collection, ^DBObject document]
     (let [#^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.insert #^DBCollection coll #^DBObject (to-db-object document) #^WriteConcern monger.core/*mongodb-write-concern*)))
  ([^String collection, ^DBObject document, ^WriteConcern concern]
     (let [#^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.insert #^DBCollection coll #^DBObject (to-db-object document) #^WriteConcern concern))))


(defn ^WriteResult insert-batch
  ([^String collection, ^List documents]
     (let [#^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.insert #^DBCollection coll #^List (to-db-object documents) #^WriteConcern WriteConcern/NORMAL)))
  ([^String collection, ^List documents, ^WriteConcern concern]
     (let [#^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.insert #^DBCollection coll #^List (to-db-object documents) #^WriteConcern concern))))

;;
;; monger.collection/find
;;
(declare fields-to-db-object)

(defn ^DBCursor find
  ([^String collection]
     (let [#^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.find coll)))
  ([^String collection, ^Map ref]
     (let [#^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.find #^DBCollection coll #^DBObject (to-db-object ref))))
  ([^String collection, ^Map ref, ^List fields]
     (let [#^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)
           map-of-fields (fields-to-db-object fields)]
       (.find #^DBCollection coll #^DBObject (to-db-object ref) #^DBObject (to-db-object map-of-fields)))))


;;
;; monger.collection/find-one
;;

(defn ^DBObject find-one
  ([^String collection, ^Map ref]
     (let [#^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.findOne #^DBCollection coll #^DBObject (to-db-object ref))))
  ([^String collection, ^Map ref, ^List fields]
     (let [#^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)
           map-of-fields (fields-to-db-object fields)]
       (.findOne #^DBCollection coll #^DBObject (to-db-object ref) #^DBObject (to-db-object map-of-fields)))))

(defn ^IPersistentMap find-one-as-map
  ([^String collection, ^Map ref]
     (from-db-object ^DBObject (find-one collection ref) true))
  ([^String collection, ^Map ref, keywordize]
     (from-db-object ^DBObject (find-one collection ref) keywordize))
  ([^String collection, ^Map ref, ^List fields, keywordize]
     (from-db-object ^DBObject (find-one collection ref fields) keywordize)))



;;
;; monger.collection/find-by-id
;;

(defn ^DBObject find-by-id
  ([^String collection, ^String id]
     (let [#^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.findOne coll (to-db-object { :_id id }))))
  ([^String collection, ^String id, ^List fields]
     (let [#^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)
           map-of-fields (fields-to-db-object fields)]
       (.findOne #^DBCollection coll #^DBObject (to-db-object { :_id id }) #^DBObject (to-db-object map-of-fields)))))

(defn ^IPersistentMap find-map-by-id
  ([^String collection, ^String id]
     (from-db-object ^DBObject (find-by-id collection id) true))
  ([^String collection, ^String id, keywordize]
     (from-db-object ^DBObject (find-by-id collection id) keywordize))
  ([^String collection, ^String id, ^List fields, keywordize]
     (from-db-object ^DBObject (find-by-id collection id fields) keywordize)))


;;
;; monger.collection/group
;;

;; TBD


;;
;; monger.collection/count
;;
(defn ^long count
  ([^String collection]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.count coll)))
  ([^String collection, ^Map conditions]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.count coll (to-db-object conditions)))))


;; monger.collection/update

(defn ^WriteResult update
  [^String collection, ^Map conditions, ^Map document, & { :keys [upsert multi write-concern] :or { upsert false, multi false, write-concern monger.core/*mongodb-write-concern* } }]
  (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
    (.update coll (to-db-object conditions) (to-db-object document) upsert multi write-concern)))


;; monger.collection/save

(defn ^WriteResult save
  ([^String collection, ^Map document]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.save coll (to-db-object document) monger.core/*mongodb-write-concern*)))
  ([^String collection, ^Map document, ^WriteConcern write-concern]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.save coll document write-concern))))


;; monger.collection/update-multi
;; monger.collection/remove

(defn ^WriteResult remove
  ([^String collection]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.remove coll (to-db-object {}))))
  ([^String collection, ^Map conditions]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.remove coll (to-db-object conditions)))))

;; monger.collection/ensure-index
;; monger.collection/drop-index



;;
;; Implementation
;;

(defn- fields-to-db-object
  [^List fields]
  (zipmap fields (repeat 1)))