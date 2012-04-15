(set! *warn-on-reflection* true)

(ns monger.test.inserting-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger core util]
            [monger.collection :as mgcol]
            [monger.result     :as mgres]
            [monger.conversion :as mgcnv]
            [monger.test.helper :as helper])
  (:use clojure.test
        monger.operators
        monger.test.fixtures))

(helper/connect!)

(use-fixtures :each purge-people purge-docs purge-things purge-libraries)


;;
;; insert
;;

(deftest insert-a-basic-document-without-id-and-with-default-write-concern
  (let [collection "people"
        doc        { :name "Joe", :age 30 }]
    (is (monger.result/ok? (mgcol/insert "people" doc)))
    (is (= 1 (mgcol/count collection)))))

(deftest insert-a-basic-document-with-explicitly-passed-database-without-id-and-with-default-write-concern
  (let [collection "people"
        doc        { :name "Joe", :age 30 }]
    (dotimes [n 5]
      (is (monger.result/ok? (mgcol/insert monger.core/*mongodb-database* "people" doc WriteConcern/SAFE))))
    (is (= 5 (mgcol/count collection)))))

(deftest insert-a-basic-document-without-id-and-with-explicit-write-concern
  (let [collection "people"
        doc        { :name "Joe", :age 30 }]
    (is (monger.result/ok? (mgcol/insert "people" doc WriteConcern/SAFE)))
    (is (= 1 (mgcol/count collection)))))

(deftest insert-a-basic-db-object-without-id-and-with-default-write-concern
  (let [collection "people"
        doc        (mgcnv/to-db-object { :name "Joe", :age 30 })]
    (is (nil? (.get ^DBObject doc "_id")))
    (mgcol/insert "people" doc)
    (is (not (nil? (monger.util/get-id doc))))))

(deftest insert-a-map-with-id-and-with-default-write-concern
  (let [collection "people"
        id         (ObjectId.)
        doc        { :name "Joe", :age 30 "_id" id }
        result     (mgcol/insert "people" doc)]
    (is (= id (monger.util/get-id doc)))))

(deftest insert-a-document-with-clojure-ratio-in-it
  (let [collection "widgets"
        id         (ObjectId.)
        doc        { :ratio 11/2 "_id" id }
        result     (mgcol/insert "widgets" doc)]
    (is (= 5.5 (:ratio (mgcol/find-map-by-id collection id))))))

(deftest insert-a-document-with-clojure-keyword-in-it
  (let [collection "widgets"
        id         (ObjectId.)
        doc        { :keyword :kwd "_id" id }
        result     (mgcol/insert "widgets" doc)]
    (is (= (name :kwd) (:keyword (mgcol/find-map-by-id collection id)))))) ;


(defrecord Metrics
    [rps eps])

(deftest ^:focus insert-a-document-with-clojure-record-in-it
  (let [collection "widgets"
        id         (ObjectId.)
        doc        { :record (Metrics. 10 20) "_id" id }
        result     (mgcol/insert "widgets" doc)]
    (is (= {:rps 10 :eps 20} (:record (mgcol/find-map-by-id collection id))))))



;;
;; insert-batch
;;

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-default-write-concern
  (let [collection "people"
        docs       [{ :name "Joe", :age 30 }, { :name "Paul", :age 27 }]]
    (is (monger.result/ok? (mgcol/insert-batch "people" docs)))
    (is (= 2 (mgcol/count collection)))))

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-explicit-write-concern
  (let [collection "people"
        docs       [{ :name "Joe", :age 30 }, { :name "Paul", :age 27 }]]
    (is (monger.result/ok? (mgcol/insert-batch "people" docs WriteConcern/NORMAL)))
    (is (= 2 (mgcol/count collection)))))

(deftest insert-a-batch-of-basic-documents-with-explicit-database-without-ids-and-with-explicit-write-concern
  (let [collection "people"
        docs       [{ :name "Joe", :age 30 }, { :name "Paul", :age 27 }]]
    (dotimes [n 44]
      (is (monger.result/ok? (mgcol/insert-batch monger.core/*mongodb-database* "people" docs WriteConcern/NORMAL))))
    (is (= 88 (mgcol/count collection)))))
