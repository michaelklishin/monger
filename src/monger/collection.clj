;; Copyright (c) 2011-2012 Michael S. Klishin
;; Copyright (c) 2012 Toby Hede
;; Copyright (c) 2012 Baishampayan Ghose
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.collection
  (:refer-clojure :exclude [find remove count drop distinct empty?])
  (:import [com.mongodb Mongo DB DBCollection WriteResult DBObject WriteConcern DBCursor MapReduceCommand MapReduceCommand$OutputType]
           [java.util List Map]
           [clojure.lang IPersistentMap ISeq]
           [org.bson.types ObjectId])
  (:require [monger core result])
  (:use     [monger.conversion]))

;;
;; Implementation
;;

(definline check-not-nil!
  [ref ^String message]
  `(when (nil? ~ref)
     (throw (IllegalArgumentException. ~message))))


;;
;; API
;;

;;
;; monger.collection/insert
;;

(defn ^WriteResult insert
  "Saves @document@ to @collection@. You can optionally specify WriteConcern.

   EXAMPLES:

       (monger.collection/insert \"people\" { :name \"Joe\", :age 30 })

       (monger.collection/insert \"people\" { :name \"Joe\", :age 30, WriteConcern/SAFE })
  "
  ([^String collection document]
     (.insert ^DBCollection (.getCollection monger.core/*mongodb-database* collection)
                ^DBObject (to-db-object document)
                ^WriteConcern monger.core/*mongodb-write-concern*))
  ([^String collection document ^WriteConcern concern]
     (.insert ^DBCollection (.getCollection monger.core/*mongodb-database* collection)
                ^DBObject (to-db-object document)
                concern))
  ([^DB db ^String collection document ^WriteConcern concern]
     (.insert ^DBCollection (.getCollection db collection)
                ^DBObject (to-db-object document)
                concern)))


(defn ^WriteResult insert-batch
  "Saves @documents@ do @collection@. You can optionally specify WriteConcern as a third argument.

  EXAMPLES:

      (monger.collection/insert-batch \"people\" [{ :name \"Joe\", :age 30 }, { :name \"Paul\", :age 27 }])

      (monger.collection/insert-batch \"people\" [{ :name \"Joe\", :age 30 }, { :name \"Paul\", :age 27 }] WriteConcern/NORMAL)

  "
  ([^String collection ^List documents]
     (.insert ^DBCollection (.getCollection monger.core/*mongodb-database* collection)
              ^List (to-db-object documents)
              ^WriteConcern monger.core/*mongodb-write-concern*))
  ([^String collection ^List documents ^WriteConcern concern]
     (.insert ^DBCollection (.getCollection monger.core/*mongodb-database* collection)
              ^List (to-db-object documents)
              concern))
  ([^DB db ^String collection ^List documents ^WriteConcern concern]
     (.insert ^DBCollection (.getCollection db collection)
                ^List (to-db-object documents)
                concern)))

;;
;; monger.collection/find
;;

(defn ^DBCursor find
  "Queries for objects in this collection.
   This function returns DBCursor, which allows you to iterate over DBObjects.
   If you want to manipulate clojure sequences maps, please @find-maps@.

   EXAMPLES:
      ;; return all objects in this collection.
      (mgcol/find \"people\")

      ;; return all objects matching query
      (mgcol/find \"people\" { :company \"Comp Corp\"})

      ;; return all objects matching query, taking only specified fields
      (mgcol/find \"people\" { :company \"Comp Corp\"} [:first_name :last_name])
  "
  ([^String collection]
     (.find ^DBCollection (.getCollection monger.core/*mongodb-database* collection)))
  ([^String collection ^Map ref]
     (.find ^DBCollection (.getCollection monger.core/*mongodb-database* collection)
            ^DBObject (to-db-object ref)))
  ([^String collection ^Map ref fields]
     (.find ^DBCollection (.getCollection monger.core/*mongodb-database* collection)
            ^DBObject (to-db-object ref)
            ^DBObject (as-field-selector fields)))
  ([^DB db ^String collection ^Map ref fields]
     (.find ^DBCollection (.getCollection db collection)
            ^DBObject (to-db-object ref)
            ^DBObject (as-field-selector fields))))

(defn find-maps
  "Queries for objects in this collection.
   This function returns clojure Seq of Maps.
   If you want to work directly with DBObject, use find.
  "
  ([^String collection]
     (map (fn [x] (from-db-object x true)) (find collection)))
  ([^String collection ^Map ref]
     (map (fn [x] (from-db-object x true)) (find collection ref)))
  ([^String collection ^Map ref fields]
     (map (fn [x] (from-db-object x true)) (find collection ref fields)))
  ([^DB db ^String collection ^Map ref fields]
     (map (fn [x] (from-db-object x true)) (find db collection ref fields))))

(defn find-seq
  "Queries for objects in this collection, returns ISeq of DBObjects."
  ([^String collection]
     (seq (find collection)))
  ([^String collection ^Map ref]
     (seq (find collection ref)))
  ([^String collection ^Map ref fields]
     (seq (find collection ref fields)))
  ([^DB db ^String collection ^Map ref fields]
     (seq (find db collection ref fields))))

;;
;; monger.collection/find-one
;;

(defn ^DBObject find-one
  "Returns a single DBObject from this collection matching the query.

   EXAMPLES:

      (mgcol/find-one collection { :language \"Clojure\" })

      ;; Return only :language field.
      ;; Note that _id field is always returned.
      (mgcol/find-one collection { :language \"Clojure\" } [:language])

  "
  ([^String collection ^Map ref]
     (.findOne ^DBCollection (.getCollection monger.core/*mongodb-database* collection)
               ^DBObject (to-db-object ref)))
  ([^String collection ^Map ref fields]
     (.findOne ^DBCollection (.getCollection monger.core/*mongodb-database* collection)
               ^DBObject (to-db-object ref)
               ^DBObject (as-field-selector fields)))
  ([^DB db ^String collection ^Map ref fields]
     (.findOne ^DBCollection (.getCollection db collection)
               ^DBObject (to-db-object ref)
               ^DBObject (as-field-selector fields))))

(defn ^IPersistentMap find-one-as-map
  "Returns a single object converted to Map from this collection matching the query."
  ([^String collection ^Map ref]
     (from-db-object ^DBObject (find-one collection ref) true))
  ([^String collection ^Map ref fields]
     (from-db-object ^DBObject (find-one collection ref fields) true))
  ([^String collection ^Map ref fields keywordize]
     (from-db-object ^DBObject (find-one collection ref fields) keywordize)))


;;
;; monger.collection/find-and-modify
;;

(defn ^DBObject find-and-modify
  "Atomically modify a document (at most one) and return it.

   EXAMPLES:
      
      ;; Find and modify a document
      (mgcol/find-and-modify collection { :language \"Python\" } { :language \"Clojure\" })

      ;; If multiple documents match, choose the first one in the specified order
      (mgcol/find-and-modify collection { :language \"Python\" } { :language \"Clojure\" } :sort { :language -1 })

      ;; Remove the object before returning
      (mgcol/find-and-modify collection { :language \"Python\" } {} :remove true)

      ;; Return the modified object instead of the old one
      (mgcol/find-and-modify collection { :language \"Python\" } { :language \"Clojure\" } :return-new true)

      ;; Retrieve a subset of fields
      (mgcol/find-and-modify collection { :language \"Python\" } { :language \"Clojure\" } :fields [ :language ])

      ;; Create the object if it doesn't exist
      (mgcol/find-and-modify collection { :language \"Factor\" } { :language \"Clojure\" } :upsert true)

  "
  ([^String collection ^Map conditions ^Map document & { :keys [fields sort remove return-new upsert keywordize] :or
                                                       { fields nil sort nil remove false return-new false upsert false keywordize true }}]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)
           maybe-fields (when fields (as-field-selector fields))
           maybe-sort (when sort (to-db-object sort))]
       (from-db-object
        ^DBObject (.findAndModify ^DBCollection coll ^DBObject (to-db-object conditions) maybe-fields maybe-sort remove
                                  ^DBObject (to-db-object document) return-new upsert) keywordize))))

;;
;; monger.collection/find-by-id
;;

(defn ^DBObject find-by-id
  "Returns a single object with matching _id field.

   EXAMPLES:

      (mgcol/find-one-by-id collection (ObjectId. \"4ef45ab4744e9fd632640e2d\"))

      ;; Return only :language field.
      ;; Note that _id field is always returned.
      (mgcol/find-one-by-id collection (ObjectId. \"4ef45ab4744e9fd632640e2d\") [:language])
  "
  ([^String collection id]
     (check-not-nil! id "id must not be nil")
     (find-one collection { :_id id }))
  ([^String collection id fields]
     (check-not-nil! id "id must not be nil")
     (find-one collection { :_id id } fields))
  ([^DB db ^String collection id fields]
     (check-not-nil! id "id must not be nil")
     (find-one db collection { :_id id } fields)))

(defn ^IPersistentMap find-map-by-id
  "Returns a single object, converted to map with matching _id field."
  ([^String collection id]
     (check-not-nil! id "id must not be nil")
     (from-db-object ^DBObject (find-one-as-map collection { :_id id }) true))
  ([^String collection id fields]
     (check-not-nil! id "id must not be nil")
     (from-db-object ^DBObject (find-one-as-map collection { :_id id } fields) true))
  ([^String collection id fields keywordize]
     (check-not-nil! id "id must not be nil")
     (from-db-object ^DBObject (find-one-as-map collection { :_id id } fields) keywordize)))


;;
;; monger.collection/group
;;


;; TBD


;;
;; monger.collection/count
;;
(defn count
  "Returns the number of documents in this collection.

  Takes optional conditions as an argument.

      (monger.collection/count collection)

      (monger.collection/count collection { :first_name \"Paul\" })"
  (^long [^String collection]
         (.count ^DBCollection (.getCollection monger.core/*mongodb-database* collection)))
  (^long [^String collection ^Map conditions]
         (.count ^DBCollection (.getCollection monger.core/*mongodb-database* collection) (to-db-object conditions)))
  (^long [^DB db ^String collection ^Map conditions]
         (.count ^DBCollection (.getCollection db collection) (to-db-object conditions))))

(defn any?
  "Wether the collection has any items at all, or items matching query.

   EXAMPLES:

    ;; wether the collection has any items
    (mgcol/any? collection)

    (mgcol/any? collection { :language \"Clojure\" }))
 "
  ([^String collection]
     (> (count collection) 0))
  ([^String collection ^Map conditions]
     (> (count collection conditions) 0))
  ([^DB db ^String collection ^Map conditions]
     (> (count db collection conditions) 0)))


(defn empty?
  "Wether the collection is empty.

   EXAMPLES:
      (mgcol/empty? \"things\")
   "
  ([^String collection]
     (= (count collection) 0))
  ([^DB db ^String collection]
     (= (count db collection {}) 0)))

;; monger.collection/update

(defn ^WriteResult update
  "Performs an update operation.

  Please note that update is potentially destructive operation. It will update your document with the given set
  emptying the fields not mentioned in (^Map document). In order to only change certain fields, please use
  \"$set\".

  EXAMPLES

      (monger.collection/update \"people\" { :first_name \"Raul\" } { \"$set\" { :first_name \"Paul\" } })

  You can use all the Mongodb Modifier Operations ($inc, $set, $unset, $push, $pushAll, $addToSet, $pop, $pull
  $pullAll, $rename, $bit) here, as well

  EXAMPLES

    (monger.collection/update \"people\" { :first_name \"Paul\" } { \"$set\" { :index 1 } })
    (monger.collection/update \"people\" { :first_name \"Paul\" } { \"$inc\" { :index 5 } })

    (monger.collection/update \"people\" { :first_name \"Paul\" } { \"$unset\" { :years_on_stage 1} })

  It also takes modifiers, such as :upsert and :multi.

  EXAMPLES

    ;; add :band field to all the records found in \"people\" collection, otherwise only the first matched record
    ;; will be updated
    (monger.collection/update \"people\" { } { \"$set\" { :band \"The Beatles\" }} :multi true)

    ;; inserts the record if it did not exist in the collection
    (monger.collection/update \"people\" { :first_name \"Yoko\" } { :first_name \"Yoko\" :last_name \"Ono\" } :upsert true)

  By default :upsert and :multi are false."
  ([^String collection ^Map conditions ^Map document & { :keys [upsert multi write-concern] :or { upsert false
                                                                                                 multi false
                                                                                                 write-concern monger.core/*mongodb-write-concern* } }]
     (.update ^DBCollection (.getCollection monger.core/*mongodb-database* collection)
              (to-db-object conditions)
              (to-db-object document)
              upsert
              multi
              write-concern)))

(defn ^WriteResult update-by-id
  "Update a document with given id"
  [^String collection id ^Map document & { :keys [upsert write-concern] :or { upsert false
                                                                                       write-concern monger.core/*mongodb-write-concern* } }]
  (check-not-nil! id "id must not be nil")
  (.update ^DBCollection (.getCollection monger.core/*mongodb-database* collection)
           (to-db-object { :_id id })
           (to-db-object document)
           upsert
           false
           write-concern))


;; monger.collection/save

(defn ^WriteResult save
  "Saves an object to the given collection (does insert or update based on the object _id).

   If the object is not present in the database, insert operation will be performed.
   If the object is already in the database, it will be updated.

   EXAMPLES

       (monger.collection/save \"people\" { :first_name \"Ian\" :last_name \"Gillan\" })
   "
  ([^String collection ^Map document]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.save coll (to-db-object document) monger.core/*mongodb-write-concern*)))
  ([^String collection ^Map document ^WriteConcern write-concern]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.save coll document write-concern)))
  ([^DB db ^String collection ^Map document ^WriteConcern write-concern]
     (let [^DBCollection coll (.getCollection db collection)]
       (.save coll document write-concern))))


;; monger.collection/remove

(defn ^WriteResult remove
  "Removes objects from the database.

  EXAMPLES

      (monger.collection/remove collection) ;; Removes all documents from DB

      (monger.collection/remove collection { :language \"Clojure\" }) ;; Removes documents based on given query

  "
  ([^String collection]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.remove coll (to-db-object {}))))
  ([^String collection ^Map conditions]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.remove coll (to-db-object conditions))))
  ([^DB db ^String collection ^Map conditions]
     (let [^DBCollection coll (.getCollection db collection)]
       (.remove coll (to-db-object conditions)))))


(defn ^WriteResult remove-by-id
  "Removes a single document with given id"
  ([^String collection id]
     (remove-by-id monger.core/*mongodb-database* collection id))
  ([^DB db ^String collection id]
     (check-not-nil! id "id must not be nil")
     (let [^DBCollection coll (.getCollection db collection)]
       (.remove coll (to-db-object { :_id id })))))


;;
;; monger.collection/create-index
;;

(defn create-index
  "Forces creation of index on a set of fields, if one does not already exists.

  EXAMPLES

      ;; Will create an index on the \"language\" field
      (monger.collection/create-index collection { \"language\" 1 })
      (monger.collection/create-index collection { \"language\" 1 } { :unique true :name \"unique_language\" })

  "
  ([^String collection ^Map keys]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.createIndex coll (to-db-object keys))))
  ([^String collection ^Map keys options]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.createIndex coll (to-db-object keys) (to-db-object options))))
  ([^DB db ^String collection ^Map keys ^Map options]
     (let [^DBCollection coll (.getCollection db collection)]
       (.createIndex coll (to-db-object keys) (to-db-object options)))))


;;
;; monger.collection/ensure-index
;;

(defn ensure-index
  "Creates an index on a set of fields, if one does not already exist.
   This operation is optimized in the Java driver and is inexpensive in the case when an index already exists.

   EXAMPLES

     ;; create a regular index
     (monger.collection/ensure-index \"documents\" { \"language\" 1 })
     ;; create a unique index
     (monger.collection/ensure-index \"pages\"     { :url 1 } {:unique true})
  "
  ([^String collection, ^Map keys]
     (let [coll ^DBCollection (.getCollection monger.core/*mongodb-database* collection)]
       (.ensureIndex ^DBCollection coll ^DBObject (to-db-object keys))))
  ([^String collection, ^Map keys ^Map options]
     (let [coll ^DBCollection (.getCollection monger.core/*mongodb-database* collection)]
       (.ensureIndex ^DBCollection coll ^DBObject (to-db-object keys) (to-db-object options))))
  ([^String collection ^Map keys ^String name ^Boolean unique?]
     (let [coll ^DBCollection (.getCollection monger.core/*mongodb-database* collection)]
       (.ensureIndex coll ^DBObject (to-db-object keys) ^String name unique?))))


;;
;; monger.collection/indexes-on
;;

(defn indexes-on
  "Return a list of the indexes for this collection.

   EXAMPLES

     (monger.collection/indexes-on collection)

  "
  [^String collection]
  (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
    (from-db-object (.getIndexInfo coll) true)))


;;
;; monger.collection/drop-index
;;

(defn drop-index
  "Drops an index from this collection."
  ([^String collection ^String name]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.dropIndex coll name)))
  ([^DB db ^String collection ^String name]
     (let [^DBCollection coll (.getCollection db collection)]
       (.dropIndex coll name))))

(defn drop-indexes
  "Drops an indices from this collection."
  ([^String collection]
     (.dropIndexes ^DBCollection (.getCollection monger.core/*mongodb-database* collection)))
  ([^DB db ^String collection]
     (.dropIndexes ^DBCollection (.getCollection db collection))))


;;
;; monger.collection/exists?, /create, /drop, /rename
;;


(defn exists?
  "Checks weather collection with certain name exists.

   EXAMPLE:

      (monger.collection/exists? \"coll\")
  "
  ([^String collection]
     (.collectionExists monger.core/*mongodb-database* collection))
  ([^DB db ^String collection]
     (.collectionExists db collection)))

(defn create
  "Creates a collection with a given name and options."
  ([^String collection ^Map options]
     (.createCollection monger.core/*mongodb-database* collection (to-db-object options)))
  ([^DB db ^String collection ^Map options]
     (.createCollection db collection (to-db-object options))))

(defn drop
  "Deletes collection from database.

   EXAMPLE:

      (monger.collection/drop \"collection-to-drop\")
  "
  ([^String collection]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.drop coll)))
  ([^DB db ^String collection]
     (let [^DBCollection coll (.getCollection db collection)]
       (.drop coll))))

(defn rename
  "Renames collection.

   EXAMPLE:

      (monger.collection/rename \"old_name\" \"new_name\")
   "
  ([^String from, ^String to]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* from)]
       (.rename coll to)))
  ([^String from ^String to ^Boolean drop-target]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* from)]
       (.rename coll to drop-target)))
  ([^DB db ^String from ^String to ^Boolean drop-target]
     (let [^DBCollection coll (.getCollection db from)]
       (.rename coll to drop-target))))

;;
;; Map/Reduce
;;

(defn map-reduce
  "Performs a map reduce operation"
  ([^String collection, ^String js-mapper, ^String js-reducer, ^String output, ^Map query]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.mapReduce coll js-mapper js-reducer output (to-db-object query))))
  ([^String collection, ^String js-mapper, ^String js-reducer, ^String output, ^MapReduceCommand$OutputType output-type, ^Map query]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.mapReduce coll js-mapper js-reducer output output-type (to-db-object query)))))


;;
;; monger.collection/distinct
;;

(defn distinct
  "Finds distinct values for a key"
  ([^String collection ^String key]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.distinct coll ^String (to-db-object key))))
  ([^String collection ^String key ^Map query]
     (let [^DBCollection coll (.getCollection monger.core/*mongodb-database* collection)]
       (.distinct coll ^String (to-db-object key) ^DBObject (to-db-object query))))
  ([^DB db ^String collection ^String key ^Map query]
     (let [^DBCollection coll (.getCollection db collection)]
       (.distinct coll ^String (to-db-object key) ^DBObject (to-db-object query)))))


;;
;; create/capped collections
;;

(defn create
  "Creates a collection. Options are: :capped (pass true to create a capped collection), :max (number of documents)
   and :size (max allowed size of the collection, in bytes)."
  [^String name options]
  (.createCollection ^DB monger.core/*mongodb-database* name (to-db-object options)))

;;
;; Aggregation
;;

(defn aggregate
  "Performs aggregation query. MongoDB 2.1/2.2+ only.

  See http://docs.mongodb.org/manual/applications/aggregation/ to learn more."
  [^String coll stages]
  (let [res (monger.core/command {:aggregate coll :pipeline stages})]
    ;; this is what DBCollection#distinct does. Turning a blind eye!
    (.throwOnError res)
    (map #(from-db-object % true) (.get res "result"))))
