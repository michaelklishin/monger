(ns monger.multi.collection
  "Includes versions of key monger.collection functions that always take a database
   as explicit argument instead of relying on monger.core/*mongodb-database*.

   Use these functions when you need to work with multiple databases or manage database
   and connection lifecycle explicitly."
  (:refer-clojure :exclude [find remove count])
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
;; monger.collection/find
;;

(defn ^DBCursor find
  "Like monger.collection/find but always takes a database as explicit argument"
  ([^DB db ^String collection ^Map ref]
     (.find (.getCollection db (name collection))
            (to-db-object ref)))
  ([^DB db ^String collection ^Map ref fields]
     (.find (.getCollection db (name collection))
            (to-db-object ref)
            (as-field-selector fields))))

(defn find-maps
  "Like monger.collection/find-maps but always takes a database as explicit argument"
  ([^DB db ^String collection ^Map ref]
     (with-open [dbc (find db collection ref)]
       (map (fn [x] (from-db-object x true)) dbc)))
  ([^DB db ^String collection ^Map ref fields]
     (with-open [dbc (find db collection ref fields)]
       (map (fn [x] (from-db-object x true)) dbc))))

;;
;; monger.collection/find-one
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
;; monger.collection/find-by-id
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
     (from-db-object ^DBObject (find-one-as-map db collection {:_id id} fields) true)))

;;
;; monger.collection/count
;;

(defn count
  "Like monger.collection/count but always takes a database as explicit argument"
  (^long [^DB db ^String collection]
         (.count (.getCollection db (name collection)) (to-db-object {})))
  (^long [^DB db ^String collection ^Map conditions]
         (.count (.getCollection db (name collection)) (to-db-object conditions))))
