(set! *warn-on-reflection* true)

(ns monger.test.querying
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject CommandResult$CommandFailure]
            [org.bson.types ObjectId]
            [java.util Date])
  (:require [monger core util]
            [monger.collection :as mgcol]
            [monger.result     :as mgres])
  (:use [clojure.test]
        [monger.test.fixtures]
        [monger.conversion]))


(defn purge-locations-collection
  [f]
  (purge-collection "locations" f))

(use-fixtures :each purge-docs-collection purge-things-collection purge-locations-collection)

(monger.core/set-default-write-concern! WriteConcern/SAFE)


;;
;; by ObjectId
;;

(deftest query-full-document-by-object-id
  (let [coll "docs"
        oid  (ObjectId.)
        doc  { :_id oid :title "Introducing Monger" }]
    (mgcol/insert coll doc)
    (is (= doc (mgcol/find-map-by-id coll oid)))
    (is (= doc (mgcol/find-one-as-map coll { :_id oid })))))


;;
;; exact match over string field
;;

(deftest query-full-document-using-exact-matching-over-string-field
  (let [coll "docs"
        doc  { :title "monger" :language "Clojure" :_id (ObjectId.) }]
    (mgcol/insert coll doc)
    (is (= [doc] (mgcol/find-maps coll { :title "monger" })))
    (is (= doc (from-db-object (first (mgcol/find coll { :title "monger" })) true)))))


;;
;; exact match over string field with limit
;;

(deftest query-full-document-using-exact-matching-over-string-field-with-limit
  (let [coll "docs"
        doc1  { :title "monger"  :language "Clojure" :_id (ObjectId.) }
        doc2  { :title "langohr" :language "Clojure" :_id (ObjectId.) }
        doc3  { :title "netty"   :language "Java" :_id (ObjectId.) }
        _     (mgcol/insert-batch coll [doc1 doc2 doc3])
        result (mgcol/find-maps coll { :title "monger" } {} 0 1)]
    (is (= 1 (count result)))
    (is (= [doc1] result))))


(deftest query-full-document-using-exact-matching-over-string-field-with-limit-and-offset
  (let [coll "docs"
        doc1  { :title "lucene"    :language "Java" :_id (ObjectId.) }
        doc2  { :title "joda-time" :language "Java" :_id (ObjectId.) }
        doc3  { :title "netty"     :language "Java" :_id (ObjectId.) }
        _     (mgcol/insert-batch coll [doc1 doc2 doc3])
        ;; this example is indeed ugly, we need to come up with a DSL similar to what Casbah has
        ;; to make this easy to read and write. MK.
        result (from-db-object (seq (.sort (mgcol/find coll { :language "Java" } {} 1 2)
                                           (to-db-object { :title 1 })))
                               true)]
    (is (= 2 (count result)))
    (is (= [doc1 doc3] result))))