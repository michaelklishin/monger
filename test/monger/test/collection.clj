(set! *warn-on-reflection* true)

(ns monger.test.collection
  (:import  [com.mongodb WriteResult WriteConcern])
  (:require [monger core collection errors] [clojure stacktrace])
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

