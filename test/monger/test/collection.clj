(set! *warn-on-reflection* true)

(ns monger.test.collection
  (:import  [com.mongodb WriteResult WriteConcern DBCursor])
  (:require [monger core collection errors util] [clojure stacktrace])
  (:use [clojure.test]))


;;
;; insert
;;

(deftest insert-a-basic-document-without-id-and-with-default-write-concern
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")
        collection "people"
        doc        { :name "Joe", :age 30 }]
    (monger.collection/remove db collection)
    (is (monger.errors/ok? (monger.collection/insert db "people" doc)))
    (is (= 1 (monger.collection/count db collection)))))


(deftest insert-a-basic-document-without-id-and-with-explicit-write-concern
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")
        collection "people"
        doc        { :name "Joe", :age 30 }]
    (monger.collection/remove db collection)
    (is (monger.errors/ok? (monger.collection/insert db "people" doc WriteConcern/SAFE)))
    (is (= 1 (monger.collection/count db collection)))))



;;
;; insert
;;

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-default-write-concern
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")
        collection "people"
        docs       [{ :name "Joe", :age 30 }, { :name "Paul", :age 27 }]]
    (monger.collection/remove db collection)
    (is (monger.errors/ok? (monger.collection/insert-batch db "people" docs)))
    (is (= 2 (monger.collection/count db collection)))))

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-explicit-write-concern
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")
        collection "people"
        docs       [{ :name "Joe", :age 30 }, { :name "Paul", :age 27 }]]
    (monger.collection/remove db collection)
    (is (monger.errors/ok? (monger.collection/insert-batch db "people" docs WriteConcern/NORMAL)))
    (is (= 2 (monger.collection/count db collection)))))




;;
;; count
;;

(deftest get-collection-size
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")]
    (is 0 (monger.collection/count db "things"))))



;;
;; find
;;

(deftest find-full-document-when-collection-is-empty
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")
        collection "docs"]
    (monger.collection/remove db collection)
    (def cursor (monger.collection/find db collection))
    (is (instance? DBCursor cursor))))



;;
;; find-by-id
;;

(deftest find-full-document-by-id-when-document-does-not-exist
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")
        collection "libraries"
        doc-id     (monger.util/random-str 140 16)]
    (monger.collection/remove db collection)
    (is (nil? (monger.collection/find-by-id db collection doc-id)))))


(deftest find-full-document-by-id-when-document-exists
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")
        collection "libraries"
        doc-id     (monger.util/random-str 140 16)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
    (monger.collection/remove db collection)
    (monger.collection/insert db collection doc)
    (is (= (doc (monger.collection/find-by-id db collection doc-id))))))


    

