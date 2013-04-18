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
  "Provides key functionality for interaction with MongoDB: inserting, querying, updating and deleting documents, performing Aggregation Framework
   queries, creating and dropping indexes, creating collections and more.

   For more advanced read queries, see monger.query.

   Related documentation guides:

   * http://clojuremongodb.info/articles/getting_started.html
   * http://clojuremongodb.info/articles/inserting.html
   * http://clojuremongodb.info/articles/querying.html
   * http://clojuremongodb.info/articles/updating.html
   * http://clojuremongodb.info/articles/deleting.html
   * http://clojuremongodb.info/articles/aggregation.html"
  (:refer-clojure :exclude [find remove count drop distinct empty?])
  (:import [com.mongodb Mongo DB DBCollection WriteResult DBObject WriteConcern DBCursor MapReduceCommand MapReduceCommand$OutputType]
           [java.util List Map]
           [clojure.lang IPersistentMap ISeq]
           org.bson.types.ObjectId)
  (:require monger.core
            monger.result)
  (:use     monger.conversion
            monger.constraints))



;;
;; API
;;

;;
;; monger.collection/insert
;;

(defn ^WriteResult insert
  "Saves @document@ to @collection@ and returns write result monger.result/ok? and similar functions operate on. You can optionally specify WriteConcern.

   In case you need the exact inserted document returned, with the :_id key generated, use monger.collection/insert-and-return
   instead.

   EXAMPLES:

       ;; returns write result
       (monger.collection/insert \"people\" {:name \"Joe\", :age 30})

       (monger.collection/insert \"people\" {:name \"Joe\", :age 30, WriteConcern/SAFE})
  "
  ([^String collection document]
     (.insert (.getCollection monger.core/*mongodb-database* (name collection))
              (to-db-object document)
              ^WriteConcern monger.core/*mongodb-write-concern*))
  ([^String collection document ^WriteConcern concern]
     (.insert (.getCollection monger.core/*mongodb-database* (name collection))
              (to-db-object document)
              concern))
  ([^DB db ^String collection document ^WriteConcern concern]
     (.insert (.getCollection db (name collection))
              (to-db-object document)
              concern)))


(defn ^clojure.lang.IPersistentMap insert-and-return
  "Like monger.collection/insert but returns the inserted document as a persistent Clojure map.

  If the :_id key wasn't set on the document, it will be generated and merged into the returned map.

   EXAMPLES:

       ;; returns the entire document with :_id generated
       (monger.collection/insert-and-return \"people\" {:name \"Joe\", :age 30})

       (monger.collection/insert-and-return \"people\" {:name \"Joe\", :age 30, WriteConcern/SAFE})
  "
  ([^String collection document]
     (insert-and-return ^DB monger.core/*mongodb-database* collection document ^WriteConcern monger.core/*mongodb-write-concern*))
  ([^String collection document ^WriteConcern concern]
     (insert-and-return ^DB monger.core/*mongodb-database* collection document concern))
  ([^DB db ^String collection document ^WriteConcern concern]
     ;; MongoDB Java driver will generate the _id and set it but it tries to mutate the inserted DBObject
     ;; and it does not work very well in our case, because that DBObject is short lived and produced
     ;; from the Clojure map we are passing in. Plus, this approach is very awkward with immutable data
     ;; structures being the default. MK.
     (let [doc (merge {:_id (ObjectId.)} document)]
       (insert db collection doc concern)
       doc)))


(defn ^WriteResult insert-batch
  "Saves @documents@ do @collection@. You can optionally specify WriteConcern as a third argument.

  EXAMPLES:

      (monger.collection/insert-batch \"people\" [{:name \"Joe\", :age 30}, {:name \"Paul\", :age 27}])

      (monger.collection/insert-batch \"people\" [{:name \"Joe\", :age 30}, {:name \"Paul\", :age 27}] WriteConcern/NORMAL)

  "
  ([^String collection ^List documents]
     (.insert (.getCollection monger.core/*mongodb-database* (name collection))
              ^List (to-db-object documents)
              ^WriteConcern monger.core/*mongodb-write-concern*))
  ([^String collection ^List documents ^WriteConcern concern]
     (.insert (.getCollection monger.core/*mongodb-database* (name collection))
              ^List (to-db-object documents)
              concern))
  ([^DB db ^String collection ^List documents ^WriteConcern concern]
     (.insert (.getCollection db (name collection))
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
      (mgcol/find \"people\" {:company \"Comp Corp\"})

      ;; return all objects matching query, taking only specified fields
      (mgcol/find \"people\" {:company \"Comp Corp\"} [:first_name :last_name])
  "
  ([^String collection]
     (.find (.getCollection monger.core/*mongodb-database* (name collection))))
  ([^String collection ^Map ref]
     (.find (.getCollection monger.core/*mongodb-database* (name collection))
            (to-db-object ref)))
  ([^String collection ^Map ref fields]
     (.find (.getCollection monger.core/*mongodb-database* (name collection))
            (to-db-object ref)
            (as-field-selector fields)))
  ([^DB db ^String collection ^Map ref fields]
     (.find (.getCollection db (name collection))
            (to-db-object ref)
            (as-field-selector fields))))

(defn find-maps
  "Queries for objects in this collection.
   This function returns clojure Seq of Maps.
   If you want to work directly with DBObject, use find.
  "
  ([^String collection]
    (with-open [dbc (find collection)]
     (map (fn [x] (from-db-object x true)) dbc)))
  ([^String collection ^Map ref]
     (with-open [dbc (find collection ref)]
       (map (fn [x] (from-db-object x true)) dbc)))
  ([^String collection ^Map ref fields]
     (with-open [dbc (find collection ref fields)]
       (map (fn [x] (from-db-object x true)) dbc)))
  ([^DB db ^String collection ^Map ref fields]
     (with-open [dbc (find db collection ref fields)]
       (map (fn [x] (from-db-object x true)) dbc))))

(defn find-seq
  "Queries for objects in this collection, returns ISeq of DBObjects."
  ([^String collection]
     (with-open [dbc (find collection)]
      (seq dbc)))
  ([^String collection ^Map ref]
     (with-open [dbc (find collection ref)]
      (seq dbc)))
  ([^String collection ^Map ref fields]
     (with-open [dbc (find collection ref fields)]
      (seq dbc)))
  ([^DB db ^String collection ^Map ref fields]
     (with-open [dbc (find db collection ref fields)]
      (seq dbc))))

;;
;; monger.collection/find-one
;;

(defn ^DBObject find-one
  "Returns a single DBObject from this collection matching the query.

   EXAMPLES:

      (mgcol/find-one collection {:language \"Clojure\"})

      ;; Return only :language field.
      ;; Note that _id field is always returned.
      (mgcol/find-one collection {:language \"Clojure\"} [:language])

  "
  ([^String collection ^Map ref]
     (.findOne (.getCollection monger.core/*mongodb-database* (name collection))
               (to-db-object ref)))
  ([^String collection ^Map ref fields]
     (.findOne (.getCollection monger.core/*mongodb-database* (name collection))
               (to-db-object ref)
               ^DBObject (as-field-selector fields)))
  ([^DB db ^String collection ^Map ref fields]
     (.findOne (.getCollection db (name collection))
               (to-db-object ref)
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
      (mgcol/find-and-modify collection {:language \"Python\"} {:language \"Clojure\"})

      ;; If multiple documents match, choose the first one in the specified order
      (mgcol/find-and-modify collection {:language \"Python\"} {:language \"Clojure\"} :sort {:language -1})

      ;; Remove the object before returning
      (mgcol/find-and-modify collection {:language \"Python\"} {} :remove true)

      ;; Return the modified object instead of the old one
      (mgcol/find-and-modify collection {:language \"Python\"} {:language \"Clojure\"} :return-new true)

      ;; Retrieve a subset of fields
      (mgcol/find-and-modify collection {:language \"Python\"} {:language \"Clojure\"} :fields [ :language ])

      ;; Create the object if it doesn't exist
      (mgcol/find-and-modify collection {:language \"Factor\"} {:language \"Clojure\"} :upsert true)

  "
  ([^String collection ^Map conditions ^Map document & {:keys [fields sort remove return-new upsert keywordize] :or
                                                        {fields nil sort nil remove false return-new false upsert false keywordize true}}]
     (let [coll (.getCollection monger.core/*mongodb-database* (name collection))
           maybe-fields (when fields (as-field-selector fields))
           maybe-sort (when sort (to-db-object sort))]
       (from-db-object
        ^DBObject (.findAndModify ^DBCollection coll (to-db-object conditions) maybe-fields maybe-sort remove
                                  (to-db-object document) return-new upsert) keywordize))))

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
     (find-one collection {:_id id}))
  ([^String collection id fields]
     (check-not-nil! id "id must not be nil")
     (find-one collection {:_id id} fields))
  ([^DB db ^String collection id fields]
     (check-not-nil! id "id must not be nil")
     (find-one db collection {:_id id} fields)))

(defn ^IPersistentMap find-map-by-id
  "Returns a single object, converted to map with matching _id field."
  ([^String collection id]
     (check-not-nil! id "id must not be nil")
     (from-db-object ^DBObject (find-one-as-map collection {:_id id}) true))
  ([^String collection id fields]
     (check-not-nil! id "id must not be nil")
     (from-db-object ^DBObject (find-one-as-map collection {:_id id} fields) true))
  ([^String collection id fields keywordize]
     (check-not-nil! id "id must not be nil")
     (from-db-object ^DBObject (find-one-as-map collection {:_id id} fields) keywordize)))


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

      (monger.collection/count collection {:first_name \"Paul\"})"
  (^long [^String collection]
         (.count (.getCollection monger.core/*mongodb-database* (name collection))))
  (^long [^String collection ^Map conditions]
         (.count (.getCollection monger.core/*mongodb-database* (name collection)) (to-db-object conditions)))
  (^long [^DB db ^String collection ^Map conditions]
         (.count (.getCollection db (name collection)) (to-db-object conditions))))

(defn any?
  "Whether the collection has any items at all, or items matching query.

   EXAMPLES:

    ;; whether the collection has any items
    (mgcol/any? collection)

    (mgcol/any? collection {:language \"Clojure\"}))
 "
  ([^String collection]
     (> (count collection) 0))
  ([^String collection ^Map conditions]
     (> (count collection conditions) 0))
  ([^DB db ^String collection ^Map conditions]
     (> (count db collection conditions) 0)))


(defn empty?
  "Whether the collection is empty.

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

      (monger.collection/update \"people\" {:first_name \"Raul\"} {\"$set\" {:first_name \"Paul\"}})

  You can use all the Mongodb Modifier Operations ($inc, $set, $unset, $push, $pushAll, $addToSet, $pop, $pull
  $pullAll, $rename, $bit) here, as well

  EXAMPLES

    (monger.collection/update \"people\" {:first_name \"Paul\"} {\"$set\" {:index 1}})
    (monger.collection/update \"people\" {:first_name \"Paul\"} {\"$inc\" {:index 5}})

    (monger.collection/update \"people\" {:first_name \"Paul\"} {\"$unset\" {:years_on_stage 1}})

  It also takes modifiers, such as :upsert and :multi.

  EXAMPLES

    ;; add :band field to all the records found in \"people\" collection, otherwise only the first matched record
    ;; will be updated
    (monger.collection/update \"people\" {} {\"$set\" {:band \"The Beatles\"}} :multi true)

    ;; inserts the record if it did not exist in the collection
    (monger.collection/update \"people\" {:first_name \"Yoko\"} {:first_name \"Yoko\" :last_name \"Ono\"} :upsert true)

  By default :upsert and :multi are false."
  ([^String collection ^Map conditions ^Map document & {:keys [upsert multi write-concern] :or {upsert false
                                                                                                multi false
                                                                                                write-concern monger.core/*mongodb-write-concern*}}]
     (.update (.getCollection monger.core/*mongodb-database* (name collection))
              (to-db-object conditions)
              (to-db-object document)
              upsert
              multi
              write-concern)))

(defn ^WriteResult upsert
  "Performs an upsert.

   This is a convenience function that delegates to monger.collection/update and
   sets :upsert to true.

   See monger.collection/update documentation"
  [^String collection ^Map conditions ^Map document & {:keys [multi write-concern] :or {multi false
                                                                                        write-concern monger.core/*mongodb-write-concern*}}]
  (update collection conditions document :multi multi :write-concern write-concern :upsert true))

(defn ^WriteResult update-by-id
  "Update a document with given id"
  [^String collection id ^Map document & {:keys [upsert write-concern] :or {upsert false
                                                                            write-concern monger.core/*mongodb-write-concern*}}]
  (check-not-nil! id "id must not be nil")
  (.update (.getCollection monger.core/*mongodb-database* (name collection))
           (to-db-object {:_id id})
           (to-db-object document)
           upsert
           false
           write-concern))


;; monger.collection/save

(defn ^WriteResult save
  "Saves an object to the given collection (does insert or update based on the object _id).

   If the object is not present in the database, insert operation will be performed.
   If the object is already in the database, it will be updated.

   This function returns write result. If you want to get the exact persisted document back,
   use `save-and-return`.

   EXAMPLES

       (monger.collection/save \"people\" {:first_name \"Ian\" :last_name \"Gillan\"})
   "
  ([^String collection ^Map document]
     (.save (.getCollection monger.core/*mongodb-database* (name collection))
            (to-db-object document)
            monger.core/*mongodb-write-concern*))
  ([^String collection ^Map document ^WriteConcern write-concern]
     (.save (.getCollection monger.core/*mongodb-database* (name collection))
            (to-db-object document)
            write-concern))
  ([^DB db ^String collection ^Map document ^WriteConcern write-concern]
     (.save (.getCollection db (name collection))
            (to-db-object document)
            write-concern)))

(defn ^clojure.lang.IPersistentMap save-and-return
  "Saves an object to the given collection (does insert or update based on the object _id).

   If the object is not present in the database, insert operation will be performed.
   If the object is already in the database, it will be updated.

   This function returns the exact persisted document back, including the `:_id` key in
   case of an insert.

   If you want to get write result back, use `save`.

   EXAMPLES

       (monger.collection/save-and-return \"people\" {:first_name \"Ian\" :last_name \"Gillan\"})
   "
  ([^String collection ^Map document]
     (save-and-return ^DB monger.core/*mongodb-database* collection document ^WriteConcern monger.core/*mongodb-write-concern*))
  ([^String collection ^Map document ^WriteConcern write-concern]
     (save-and-return ^DB monger.core/*mongodb-database* collection document write-concern))
  ([^DB db ^String collection ^Map document ^WriteConcern write-concern]
     ;; see the comment in insert-and-return. Here we additionally need to make sure to not scrap the :_id key if
     ;; it is already present. MK.
     (let [doc (merge {:_id (ObjectId.)} document)]
       (save db collection doc write-concern)
       doc)))


;; monger.collection/remove

(defn ^WriteResult remove
  "Removes objects from the database.

  EXAMPLES

      (monger.collection/remove collection) ;; Removes all documents from DB

      (monger.collection/remove collection {:language \"Clojure\"}) ;; Removes documents based on given query

  "
  ([^String collection]
     (.remove (.getCollection monger.core/*mongodb-database* (name collection)) (to-db-object {})))
  ([^String collection ^Map conditions]
     (.remove (.getCollection monger.core/*mongodb-database* (name collection)) (to-db-object conditions)))
  ([^DB db ^String collection ^Map conditions]
     (.remove (.getCollection db (name collection)) (to-db-object conditions))))


(defn ^WriteResult remove-by-id
  "Removes a single document with given id"
  ([^String collection id]
     (remove-by-id monger.core/*mongodb-database* collection id))
  ([^DB db ^String collection id]
     (check-not-nil! id "id must not be nil")
     (let [coll (.getCollection db (name collection))]
       (.remove coll (to-db-object {:_id id})))))


;;
;; monger.collection/create-index
;;

(defn create-index
  "Forces creation of index on a set of fields, if one does not already exists.

  EXAMPLES

      ;; Will create an index on the \"language\" field
      (monger.collection/create-index collection {\"language\" 1})
      (monger.collection/create-index collection {\"language\" 1} {:unique true :name \"unique_language\"})

  "
  ([^String collection ^Map keys]
     (.createIndex (.getCollection monger.core/*mongodb-database* (name collection)) (as-field-selector keys)))
  ([^String collection ^Map keys options]
     (.createIndex (.getCollection monger.core/*mongodb-database* (name collection))
                   (as-field-selector keys)
                   (to-db-object options)))
  ([^DB db ^String collection ^Map keys ^Map options]
     (.createIndex (.getCollection db (name collection))
                   (as-field-selector keys)
                   (to-db-object options))))


;;
;; monger.collection/ensure-index
;;

(defn ensure-index
  "Creates an index on a set of fields, if one does not already exist.
   This operation is inexpensive in the case when an index already exists.

   Options are:

   :unique (boolean) to create a unique index
   :name (string) to specify a custom index name and not rely on the generated one

   EXAMPLES

     ;; create a regular index
     ;; clojure.core/array-map produces an ordered map
     (monger.collection/ensure-index \"documents\" (array-map \"language\" 1))
     ;; create a unique index
     (monger.collection/ensure-index \"pages\"     (array-map :url 1) {:unique true})
  "
  ([^String collection ^Map keys]
     (.ensureIndex (.getCollection monger.core/*mongodb-database* (name collection)) (as-field-selector keys)))
  ([^String collection ^Map keys ^Map options]
     (.ensureIndex (.getCollection monger.core/*mongodb-database* (name collection))
                   (as-field-selector keys)
                   (to-db-object options)))
  ([^String collection ^Map keys ^String name ^Boolean unique?]
     (.ensureIndex (.getCollection monger.core/*mongodb-database* (name collection))
                   (as-field-selector keys)
                   name
                   unique?)))


;;
;; monger.collection/indexes-on
;;

(defn indexes-on
  "Return a list of the indexes for this collection.

   EXAMPLES

     (monger.collection/indexes-on collection)

  "
  [^String collection]
  (from-db-object (.getIndexInfo (.getCollection monger.core/*mongodb-database* (name collection))) true))


;;
;; monger.collection/drop-index
;;

(defn drop-index
  "Drops an index from this collection."
  ([^String collection ^String idx-name]
     (.dropIndex (.getCollection monger.core/*mongodb-database* (name collection)) idx-name))
  ([^DB db ^String collection ^String idx-name]
     (.dropIndex (.getCollection db (name collection)) idx-name)))

(defn drop-indexes
  "Drops all indixes from this collection."
  ([^String collection]
     (.dropIndexes (.getCollection monger.core/*mongodb-database* (name collection))))
  ([^DB db ^String collection]
     (.dropIndexes (.getCollection db (name collection)))))


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
  "Creates a collection with a given name and options.

   Options are:

   :capped (pass true to create a capped collection)
   :max (number of documents)
   :size (max allowed size of the collection, in bytes)

   EXAMPLE:

     ;; create a capped collection
     (monger.collection/create \"coll\" {:capped true :size 100000 :max 10})
  "
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
     (.drop (.getCollection monger.core/*mongodb-database* (name collection))))
  ([^DB db ^String collection]
     (.drop (.getCollection db (name collection)))))

(defn rename
  "Renames collection.

   EXAMPLE:

      (monger.collection/rename \"old_name\" \"new_name\")
   "
  ([^String from, ^String to]
     (.rename (.getCollection monger.core/*mongodb-database* from) to))
  ([^String from ^String to ^Boolean drop-target]
     (.rename (.getCollection monger.core/*mongodb-database* from) to drop-target))
  ([^DB db ^String from ^String to ^Boolean drop-target]
     (.rename (.getCollection db from) to drop-target)))

;;
;; Map/Reduce
;;

(defn map-reduce
  "Performs a map reduce operation"
  ([^String collection ^String js-mapper ^String js-reducer ^String output ^Map query]
     (let [coll (.getCollection monger.core/*mongodb-database* (name collection))]
       (.mapReduce coll js-mapper js-reducer output (to-db-object query))))
  ([^String collection ^String js-mapper ^String js-reducer ^String output ^MapReduceCommand$OutputType output-type ^Map query]
     (let [coll (.getCollection monger.core/*mongodb-database* (name collection))]
       (.mapReduce coll js-mapper js-reducer output output-type (to-db-object query)))))


;;
;; monger.collection/distinct
;;

(defn distinct
  "Finds distinct values for a key"
  ([^String collection ^String key]
     (.distinct (.getCollection monger.core/*mongodb-database* (name collection)) ^String (to-db-object key)))
  ([^String collection ^String key ^Map query]
     (.distinct (.getCollection monger.core/*mongodb-database* (name collection)) ^String (to-db-object key) (to-db-object query)))
  ([^DB db ^String collection ^String key ^Map query]
     (.distinct (.getCollection db (name collection)) ^String (to-db-object key) (to-db-object query))))


;;
;; Aggregation
;;

(defn aggregate
  "Performs aggregation query. MongoDB 2.1/2.2+ only.

  See http://docs.mongodb.org/manual/applications/aggregation/ to learn more."
  [^String collection stages]
  (let [res (monger.core/command {:aggregate collection :pipeline stages})]
    ;; this is what DBCollection#distinct does. Turning a blind eye!
    (.throwOnError res)
    (map #(from-db-object % true) (.get res "result"))))


;;
;; Misc
;;

(def ^{:const true}
  system-collection-pattern #"^(system|fs)")

(defn system-collection?
  "Evaluates to true if the given collection name refers to a system collection. System collections
   are prefixed with system. or fs. (default GridFS collection prefix)"
  [^String collection]
  (re-find system-collection-pattern collection))
