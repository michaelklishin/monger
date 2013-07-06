(ns monger.multi.collection
  "Includes versions of key monger.collection functions that always take a database
   as explicit argument instead of relying on monger.core/*mongodb-database*.

   Use these functions when you need to work with multiple databases or manage database
   and connection lifecycle explicitly."
  (:refer-clojure :exclude [find remove count empty? distinct drop])
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

(defn ^WriteResult insert
  "Like monger.collection/insert but always takes a database as explicit argument"
  ([^DB db ^String collection document]
     (.insert (.getCollection db (name collection))
              (to-db-object document)
              monger.core/*mongodb-write-concern*))
  ([^DB db ^String collection document ^WriteConcern concern]
     (.insert (.getCollection db (name collection))
              (to-db-object document)
              concern)))


(defn ^clojure.lang.IPersistentMap insert-and-return
  "Like monger.collection/insert-and-return but always takes a database as explicit argument"
  ([^DB db ^String collection document]
     (let [doc (merge {:_id (ObjectId.)} document)]
       (insert db collection doc monger.core/*mongodb-write-concern*)
       doc))
  ([^DB db ^String collection document ^WriteConcern concern]
     ;; MongoDB Java driver will generate the _id and set it but it tries to mutate the inserted DBObject
     ;; and it does not work very well in our case, because that DBObject is short lived and produced
     ;; from the Clojure map we are passing in. Plus, this approach is very awkward with immutable data
     ;; structures being the default. MK.
     (let [doc (merge {:_id (ObjectId.)} document)]
       (insert db collection doc concern)
       doc)))


(defn ^WriteResult insert-batch
  "Like monger.collection/insert-batch but always takes a database as explicit argument"
  ([^DB db ^String collection ^List documents]
     (.insert (.getCollection db (name collection))
              ^List (to-db-object documents)
              monger.core/*mongodb-write-concern*))
  ([^DB db ^String collection ^List documents ^WriteConcern concern]
     (.insert (.getCollection db (name collection))
              ^List (to-db-object documents)
              concern)))


;;
;; monger.multi.collection/find
;;

(defn ^DBCursor find
  "Like monger.collection/find but always takes a database as explicit argument"
  ([^DB db ^String collection]
     (.find (.getCollection db (name collection))))
  ([^DB db ^String collection ^Map ref]
     (.find (.getCollection db (name collection))
            (to-db-object ref)))
  ([^DB db ^String collection ^Map ref fields]
     (.find (.getCollection db (name collection))
            (to-db-object ref)
            (as-field-selector fields))))

(defn find-maps
  "Like monger.collection/find-maps but always takes a database as explicit argument"
  ([^DB db ^String collection]
     (with-open [dbc (find db collection)]
       (map (fn [x] (from-db-object x true)) dbc)))
  ([^DB db ^String collection ^Map ref]
     (with-open [dbc (find db collection ref)]
       (map (fn [x] (from-db-object x true)) dbc)))
  ([^DB db ^String collection ^Map ref fields]
     (with-open [dbc (find db collection ref fields)]
       (map (fn [x] (from-db-object x true)) dbc))))

(defn find-seq
  "Like monger.collection/find-seq but always takes a database as explicit argument"
  ([^DB db ^String collection]
     (with-open [dbc (find db collection)]
       (seq dbc)))
  ([^DB db ^String collection ^Map ref]
     (with-open [dbc (find db collection ref)]
       (seq dbc)))
  ([^DB db ^String collection ^Map ref fields]
     (with-open [dbc (find db collection ref fields)]
       (seq dbc))))

;;
;; monger.multi.collection/find-one
;;

(defn ^DBObject find-one
  "Like monger.collection/find-one but always takes a database as explicit argument"
  ([^DB db ^String collection ^Map ref]
     (.findOne (.getCollection db (name collection))
               (to-db-object ref)))
  ([^DB db ^String collection ^Map ref fields]
     (.findOne (.getCollection db (name collection))
               (to-db-object ref)
               ^DBObject (as-field-selector fields))))

(defn ^IPersistentMap find-one-as-map
  "Like monger.collection/find-one-as-map but always takes a database as explicit argument"
  ([^DB db ^String collection ^Map ref]
     (from-db-object ^DBObject (find-one db collection ref) true))
  ([^DB db ^String collection ^Map ref fields]
     (from-db-object ^DBObject (find-one db collection ref fields) true))
  ([^DB db ^String collection ^Map ref fields keywordize]
     (from-db-object ^DBObject (find-one db collection ref fields) keywordize)))

;;
;; monger.multi.collection/find-and-modify
;;

(defn ^IPersistentMap find-and-modify
  "Like monger.collection/find-and-modify but always takes a database as explicit argument"
  ([^DB db ^String collection ^Map conditions ^Map document & {:keys [fields sort remove return-new upsert keywordize] :or
                                                               {fields nil sort nil remove false return-new false upsert false keywordize true}}]
     (let [coll (.getCollection db (name collection))
           maybe-fields (when fields (as-field-selector fields))
           maybe-sort (when sort (to-db-object sort))]
       (from-db-object
        ^DBObject (.findAndModify ^DBCollection coll (to-db-object conditions) maybe-fields maybe-sort remove
                                  (to-db-object document) return-new upsert) keywordize))))

;;
;; monger.multi.collection/find-by-id
;;

(defn ^DBObject find-by-id
  "Like monger.collection/find-by-id but always takes a database as explicit argument"
  ([^DB db ^String collection id]
     (check-not-nil! id "id must not be nil")
     (find-one db collection {:_id id}))
  ([^DB db ^String collection id fields]
     (check-not-nil! id "id must not be nil")
     (find-one db collection {:_id id} fields)))

(defn ^IPersistentMap find-map-by-id
  "Like monger.collection/find-map-by-id but always takes a database as explicit argument"
  ([^DB db ^String collection id]
     (check-not-nil! id "id must not be nil")
     (from-db-object ^DBObject (find-one-as-map db collection {:_id id}) true))
  ([^DB db ^String collection id fields]
     (check-not-nil! id "id must not be nil")
     (from-db-object ^DBObject (find-one-as-map db collection {:_id id} fields) true))
  ([^DB db ^String collection id fields keywordize]
     (check-not-nil! id "id must not be nil")
     (from-db-object ^DBObject (find-one-as-map db collection {:_id id} fields) keywordize)))

;;
;; monger.multi.collection/count
;;

(defn count
  "Like monger.collection/count but always takes a database as explicit argument"
  (^long [^DB db ^String collection]
         (.count (.getCollection db (name collection)) (to-db-object {})))
  (^long [^DB db ^String collection ^Map conditions]
         (.count (.getCollection db (name collection)) (to-db-object conditions))))

(defn any?
  "Like monger.collection/any? but always takes a database as explicit argument"
  ([^DB db ^String collection]
     (> (count db collection) 0))
  ([^DB db ^String collection ^Map conditions]
     (> (count db collection conditions) 0)))

(defn empty?
  "Like monger.collection/empty? but always takes a database as explicit argument"
  ([^DB db ^String collection]
     (= (count db collection {}) 0)))

(defn ^WriteResult update
  "Like monger.collection/update but always takes a database as explicit argument"
  ([^DB db ^String collection ^Map conditions ^Map document & {:keys [upsert multi write-concern] :or {upsert false
                                                                                                       multi false
                                                                                                       write-concern monger.core/*mongodb-write-concern*}}]
     (.update (.getCollection db (name collection))
              (to-db-object conditions)
              (to-db-object document)
              upsert
              multi
              write-concern)))

(defn ^WriteResult upsert
  "Like monger.collection/upsert but always takes a database as explicit argument"
  [^DB db ^String collection ^Map conditions ^Map document & {:keys [multi write-concern] :or {multi false
                                                                                               write-concern monger.core/*mongodb-write-concern*}}]
  (update db collection conditions document :multi multi :write-concern write-concern :upsert true))

(defn ^WriteResult update-by-id
  "Like monger.collection/update-by-id but always takes a database as explicit argument"
  [^DB db ^String collection id ^Map document & {:keys [upsert write-concern] :or {upsert false
                                                                                   write-concern monger.core/*mongodb-write-concern*}}]
  (check-not-nil! id "id must not be nil")
  (.update (.getCollection db (name collection))
           (to-db-object {:_id id})
           (to-db-object document)
           upsert
           false
           write-concern))

(defn ^WriteResult save
  "Like monger.collection/save but always takes a database as explicit argument"
  ([^DB db ^String collection ^Map document]
     (.save (.getCollection db (name collection))
            (to-db-object document)
            monger.core/*mongodb-write-concern*))
  ([^DB db ^String collection ^Map document ^WriteConcern write-concern]
     (.save (.getCollection db (name collection))
            (to-db-object document)
            write-concern)))

(defn ^clojure.lang.IPersistentMap save-and-return
  "Like monger.collection/save-and-return but always takes a database as explicit argument"
  ([^DB db ^String collection ^Map document]
     (save-and-return ^DB db collection document ^WriteConcern monger.core/*mongodb-write-concern*))
  ([^DB db ^String collection ^Map document ^WriteConcern write-concern]
     ;; see the comment in insert-and-return. Here we additionally need to make sure to not scrap the :_id key if
     ;; it is already present. MK.
     (let [doc (merge {:_id (ObjectId.)} document)]
       (save db collection doc write-concern)
       doc)))

(defn ^WriteResult remove
  "Like monger.collection/remove but always takes a database as explicit argument"
  ([^DB db ^String collection]
     (.remove (.getCollection db (name collection)) (to-db-object {})))
  ([^DB db ^String collection ^Map conditions]
     (.remove (.getCollection db (name collection)) (to-db-object conditions))))

(defn ^WriteResult remove-by-id
  "Like monger.collection/remove-by-id but always takes a database as explicit argument"
  ([^DB db ^String collection id]
     (check-not-nil! id "id must not be nil")
     (let [coll (.getCollection db (name collection))]
       (.remove coll (to-db-object {:_id id})))))

;;
;; monger.multi.collection/create-index
;;

(defn create-index
  "Like monger.collection/create-index but always takes a database as explicit argument"
  ([^DB db ^String collection ^Map keys]
     (.createIndex (.getCollection db (name collection)) (as-field-selector keys)))
  ([^DB db ^String collection ^Map keys ^Map options]
     (.createIndex (.getCollection db (name collection))
                   (as-field-selector keys)
                   (to-db-object options))))

;;
;; monger.multi.collection/ensure-index
;;

(defn ensure-index
  "Like monger.collection/ensure-index but always takes a database as explicit argument"
  ([^DB db ^String collection ^Map keys]
     (.ensureIndex (.getCollection db (name collection)) (as-field-selector keys)))
  ([^DB db ^String collection ^Map keys ^Map options]
     (.ensureIndex (.getCollection db (name collection))
                   (as-field-selector keys)
                   (to-db-object options)))
  ([^DB db ^String collection ^Map keys ^String name ^Boolean unique?]
     (.ensureIndex (.getCollection db (name collection))
                   (as-field-selector keys)
                   name
                   unique?)))

;;
;; monger.multi.collection/indexes-on
;;

(defn indexes-on
  "Like monger.collection/indexes-on but always takes a database as explicit argument"
  [^DB db ^String collection]
  (from-db-object (.getIndexInfo (.getCollection db (name collection))) true))


;;
;; monger.multi.collection/drop-index
;;

(defn drop-index
  "Like monger.collection/drop-index but always takes a database as explicit argument"
  ([^DB db ^String collection ^String idx-name]
     (.dropIndex (.getCollection db (name collection)) idx-name)))

(defn drop-indexes
  "Like monger.collection/drop-indexes but always takes a database as explicit argument"
  ([^DB db ^String collection]
     (.dropIndexes (.getCollection db (name collection)))))


;;
;; monger.multi.collection/exists?, /create, /drop, /rename
;;


(defn exists?
  "Like monger.collection/exists? but always takes a database as explicit argument"
  ([^DB db ^String collection]
     (.collectionExists db collection)))

(defn create
  "Like monger.collection/create but always takes a database as explicit argument"
  ([^DB db ^String collection ^Map options]
     (.createCollection db collection (to-db-object options))))

(defn drop
  "Like monger.collection/drop but always takes a database as explicit argument"
  ([^DB db ^String collection]
     (.drop (.getCollection db (name collection)))))

(defn rename
  "Like monger.collection/rename but always takes a database as explicit argument"
  ([^DB db ^String from, ^String to]
     (.rename (.getCollection db from) to))
  ([^DB db ^String from ^String to ^Boolean drop-target]
     (.rename (.getCollection db from) to drop-target)))

;;
;; Map/Reduce
;;

(defn map-reduce
  "Like monger.collection/map-reduce but always takes a database as explicit argument"
  ([^DB db ^String collection ^String js-mapper ^String js-reducer ^String output ^Map query]
     (let [coll (.getCollection db (name collection))]
       (.mapReduce coll js-mapper js-reducer output (to-db-object query))))
  ([^DB db ^String collection ^String js-mapper ^String js-reducer ^String output ^MapReduceCommand$OutputType output-type ^Map query]
     (let [coll (.getCollection db (name collection))]
       (.mapReduce coll js-mapper js-reducer output output-type (to-db-object query)))))


;;
;; monger.multi.collection/distinct
;;

(defn distinct
  "Like monger.collection/distinct but always takes a database as explicit argument"
  ([^DB db ^String collection ^String key]
     (.distinct (.getCollection db (name collection)) ^String (to-db-object key)))
  ([^DB db ^String collection ^String key ^Map query]
     (.distinct (.getCollection db (name collection)) ^String (to-db-object key) (to-db-object query))))
