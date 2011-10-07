;; Copyright (c) 2011 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.collection
  (:refer-clojure :exclude [find remove count drop])
  (:import (com.mongodb Mongo DB DBCollection WriteResult DBObject WriteConcern DBCursor) (java.util List Map) (clojure.lang IPersistentMap ISeq))
  (:require [monger core result])
  (:use     [monger.conversion]))

;;
;; API
;;

;;
;; monger.collection/insert
;;

(defn ^WriteResult insert
  ([^String collection, ^DBObject document]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.insert ^DBCollection coll ^DBObject (to-db-object document) ^WriteConcern monger.core/*mongodb-write-concern*)))
  ([^String collection, ^DBObject document, ^WriteConcern concern]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.insert ^DBCollection coll ^DBObject (to-db-object document) ^WriteConcern concern))))


(defn ^WriteResult insert-batch
  ([^String collection, ^List documents]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.insert ^DBCollection coll ^List (to-db-object documents) ^WriteConcern WriteConcern/NORMAL)))
  ([^String collection, ^List documents, ^WriteConcern concern]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.insert ^DBCollection coll ^List (to-db-object documents) ^WriteConcern concern))))

;;
;; monger.collection/find
;;
(declare fields-to-db-object)

(defn ^DBCursor find
  ([^String collection]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.find coll)))
  ([^String collection, ^Map ref]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.find ^DBCollection coll ^DBObject (to-db-object ref))))
  ([^String collection, ^Map ref, ^List fields]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)
           map-of-fields (fields-to-db-object fields)]
       (.find ^DBCollection coll ^DBObject (to-db-object ref) ^DBObject (to-db-object map-of-fields)))))

(defn ^ISeq find-maps
  ([^String collection]
     (map (fn [x] (from-db-object x true)) (seq (find collection))))
  ([^String collection, ^Map ref]
     (map (fn [x] (from-db-object x true)) (seq (find collection ref))))
  ([^String collection, ^Map ref, ^List fields]
     (map (fn [x] (from-db-object x true)) (seq (find collection ref fields)))))

;;
;; monger.collection/find-one
;;

(defn ^DBObject find-one
  ([^String collection, ^Map ref]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.findOne ^DBCollection coll ^DBObject (to-db-object ref))))
  ([^String collection, ^Map ref, ^List fields]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)
           map-of-fields (fields-to-db-object fields)]
       (.findOne ^DBCollection coll ^DBObject (to-db-object ref) ^DBObject (to-db-object map-of-fields)))))

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
  ([^String collection, id]
     (find-one collection { :_id id }))
  ([^String collection, id, ^List fields]
     (find-one collection { :_id id } fields)))

(defn ^IPersistentMap find-map-by-id
  ([^String collection, id]
     (from-db-object ^DBObject (find-one-as-map collection { :_id id }) true))
  ([^String collection, id, keywordize]
     (from-db-object ^DBObject (find-one-as-map collection { :_id id }) keywordize))
  ([^String collection, id, ^List fields, keywordize]
     (from-db-object ^DBObject (find-one-as-map collection { :_id id } fields) keywordize)))


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
  "Performs an update operation.

  Please note that update is potentially destructive operation. It will update your document with the given set
  emptying the fields not mentioned in (^Map document). In order to only change certain fields, please use
  \"$set\", for example:

      (monger.collection/update \"people\" { :first_name \"Raul\" } { \"$set\" { :first_name \"Paul\" } })

  You can use all the Mongodb Modifier Operations ($inc, $set, $unset, $push, $pushAll, $addToSet, $pop, $pull
  $pullAll, $rename, $bit) here, as well. Few examples:

    (monger.collection/update \"people\" { :first_name \"Paul\" } { \"$set\" { :index 1 } })
    (monger.collection/update \"people\" { :first_name \"Paul\" } { \"$inc\" { :index 5 } })

    (monger.collection/update \"people\" { :first_name \"Paul\" } { \"$unset\" { :years_on_stage 1} })

  It also takes modifiers, such as :upsert and :multi.

    ;; add :band field to all the records found in \"people\" collection, otherwise only the first matched record
    ;; will be updated
    (monger.collection/update \"people\" { } { \"$set\" { :band \"The Beatles\" }} :multi true)

    ;; inserts the record if it did not exist in the collection
    (monger.collection/update \"people\" { :first_name \"Yoko\" } { :first_name \"Yoko\" :last_name \"Ono\" } :upsert true)

  By default :upsert and :multi are false."
  [^String collection, ^Map conditions, ^Map document, & { :keys [upsert multi write-concern] :or { upsert false, multi false, write-concern monger.core/*mongodb-write-concern* } }]
  (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
    (.update coll (to-db-object conditions) (to-db-object document) upsert multi write-concern)))


;; monger.collection/save

(defn ^WriteResult save
  "Saves an object to the given collection (does insert or update based on the object _id).

   If the object is not present in the database, insert operation will be performed:

       (monger.collection/save \"people\" { :first_name \"Ian\" :last_name \"Gillan\" })

   If the object is already in the database, it will be updated."
  ([^String collection, ^Map document]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.save coll (to-db-object document) monger.core/*mongodb-write-concern*)))
  ([^String collection, ^Map document, ^WriteConcern write-concern]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.save coll document write-concern))))


;; monger.collection/remove

(defn ^WriteResult remove
  ([^String collection]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.remove coll (to-db-object {}))))
  ([^String collection, ^Map conditions]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.remove coll (to-db-object conditions)))))



;;
;; monger.collection/create-index
;;

(defn create-index
  [^String collection, ^Map keys]
  (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
    (.createIndex coll (to-db-object keys))))


;;
;; monger.collection/ensure-index
;;

(defn ensure-index
  ([^String collection, ^Map keys]
     (let [coll ^DBCollection (.getCollection monger.core/*mongodb-database* collection)]
       (.ensureIndex ^DBCollection coll ^DBObject (to-db-object keys))))
  ([^String collection, ^Map keys, ^String name]
     (let [coll ^DBCollection (.getCollection monger.core/*mongodb-database* collection)]
       (.ensureIndex coll ^DBObject (to-db-object keys) ^String name))))


;;
;; monger.collection/indexes-on
;;

(defn indexes-on
  [^String collection]
  (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
    (from-db-object (.getIndexInfo coll) true)))


;;
;; monger.collection/drop-index
;;

(defn drop-index
  [^String collection, ^String name]
  (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
    (.dropIndex coll name)))

(defn drop-indexes
  [^String collection]
  (.dropIndexes ^DBCollection (.getCollection monger.core/*mongodb-database* collection)))


;;
;; monger.collection/exists?, /create, /drop, /rename
;;


(defn exists?
  [^String collection]
  (.collectionExists monger.core/*mongodb-database* collection))

(defn create
  [^String collection, ^Map options]
  (.createCollection monger.core/*mongodb-database* collection (to-db-object options)))

(defn drop
  [^String collection]
  (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
    (.drop coll)))

(defn rename
  ([^String from, ^String to]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* from)]
       (.rename coll to)))
  ([^String from, ^String to, ^Boolean drop-target]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* from)]
       (.rename coll to drop-target))))

;;
;; Implementation
;;

(defn- fields-to-db-object
  [^List fields]
  (zipmap fields (repeat 1)))