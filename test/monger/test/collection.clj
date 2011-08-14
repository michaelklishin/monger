(set! *warn-on-reflection* true)

(ns monger.test.collection
  (:import  [com.mongodb WriteResult WriteConcern DBCursor])
  (:require [monger core collection errors util] [clojure stacktrace])
  (:use [clojure.test] [monger.core]))

(monger.util/with-ns 'monger.core
  (defonce ^:dynamic *mongodb-connection* (monger.core/connect))
  (defonce ^:dynamic *mongodb-database*   (monger.core/get-db "monger-test")))



;;
;; insert
;;

(deftest insert-a-basic-document-without-id-and-with-default-write-concern
  (let [collection "people"
        doc        { :name "Joe", :age 30 }]
    (monger.collection/remove collection)
    (is (monger.errors/ok? (monger.collection/insert "people" doc)))
    (is (= 1 (monger.collection/count collection)))))


(deftest insert-a-basic-document-without-id-and-with-explicit-write-concern
  (let [collection "people"
        doc        { :name "Joe", :age 30 }]
    (monger.collection/remove collection)
    (is (monger.errors/ok? (monger.collection/insert "people" doc WriteConcern/SAFE)))
    (is (= 1 (monger.collection/count collection)))))



;;
;; insert
;;

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-default-write-concern
  (let [collection "people"
        docs       [{ :name "Joe", :age 30 }, { :name "Paul", :age 27 }]]
    (monger.collection/remove collection)
    (is (monger.errors/ok? (monger.collection/insert-batch "people" docs)))
    (is (= 2 (monger.collection/count collection)))))

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-explicit-write-concern
  (let [collection "people"
        docs       [{ :name "Joe", :age 30 }, { :name "Paul", :age 27 }]]
    (monger.collection/remove collection)
    (is (monger.errors/ok? (monger.collection/insert-batch "people" docs WriteConcern/NORMAL)))
    (is (= 2 (monger.collection/count collection)))))




;;
;; count
;;

(deftest get-collection-size
  (is 0 (monger.collection/count "things")))



;;
;; find
;;

(deftest find-full-document-when-collection-is-empty
  (let [collection "docs"]
    (monger.collection/remove collection)
    (def cursor (monger.collection/find collection))
    (is (instance? DBCursor cursor))))



;;
;; find-by-id
;;

(deftest find-full-document-by-id-when-document-does-not-exist
  (let [collection "libraries"
        doc-id     (monger.util/random-str 140 16)]
    (monger.collection/remove collection)
    (is (nil? (monger.collection/find-by-id collection doc-id)))))


(deftest find-full-document-by-id-when-document-exists
  (let [collection "libraries"
        doc-id     (monger.util/random-str 140 16)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
    (monger.collection/remove collection)
    (monger.collection/insert collection doc)
    (is (= (doc (monger.collection/find-by-id collection doc-id))))))

;; (deftest find-partial-document-by-id-when-document-exists
;;   (let [collection "libraries"
;;         doc-id     (monger.util/random-str 140 16)
;;         doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
;;     (monger.collection/remove collection)
;;     (monger.collection/insert collection doc)
;;     (is (= (doc (monger.collection/find-by-id collection doc-id []))))))
