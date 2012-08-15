(set! *warn-on-reflection* true)

(ns monger.test.inserting-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject DBRef]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.core        :as mg]
            [monger.util        :as mu]
            [monger.collection  :as mc]
            [monger.test.helper :as helper])
  (:use clojure.test
        monger.operators
        monger.conversion
        monger.test.fixtures))

(helper/connect!)

(use-fixtures :each purge-people purge-docs purge-things purge-libraries)


;;
;; insert
;;

(deftest insert-a-basic-document-without-id-and-with-default-write-concern
  (let [collection "people"
        doc        {:name "Joe" :age 30}]
    (is (monger.result/ok? (mc/insert "people" doc)))
    (is (= 1 (mc/count collection)))))

(deftest insert-a-basic-document-with-explicitly-passed-database-without-id-and-with-default-write-concern
  (let [collection "people"
        doc        {:name "Joe" :age 30}]
    (dotimes [n 5]
      (is (monger.result/ok? (mc/insert monger.core/*mongodb-database* "people" doc WriteConcern/SAFE))))
    (is (= 5 (mc/count collection)))))

(deftest insert-a-basic-document-without-id-and-with-explicit-write-concern
  (let [collection "people"
        doc        {:name "Joe" :age 30}]
    (is (monger.result/ok? (mc/insert "people" doc WriteConcern/SAFE)))
    (is (= 1 (mc/count collection)))))

(deftest insert-a-basic-db-object-without-id-and-with-default-write-concern
  (let [collection "people"
        doc        (to-db-object {:name "Joe" :age 30})]
    (is (nil? (.get ^DBObject doc "_id")))
    (mc/insert "people" doc)
    (is (not (nil? (monger.util/get-id doc))))))

(deftest insert-a-map-with-id-and-with-default-write-concern
  (let [collection "people"
        id         (ObjectId.)
        doc        {:name "Joe" :age 30 "_id" id}
        result     (mc/insert "people" doc)]
    (is (= id (monger.util/get-id doc)))))

(deftest insert-a-document-with-clojure-ratio-in-it
  (let [collection "widgets"
        id         (ObjectId.)
        doc        {:ratio 11/2 "_id" id}
        result     (mc/insert "widgets" doc)]
    (is (= 5.5 (:ratio (mc/find-map-by-id collection id))))))

(deftest insert-a-document-with-clojure-keyword-in-it
  (let [collection "widgets"
        id         (ObjectId.)
        doc        {:keyword :kwd "_id" id}
        result     (mc/insert "widgets" doc)]
    (is (= (name :kwd) (:keyword (mc/find-map-by-id collection id))))))

(deftest insert-a-document-with-clojure-keyword-in-a-set-in-it
  (let [collection "widgets"
        id         (ObjectId.)
        doc        {:keyword1 {:keyword2 #{:kw1 :kw2}} "_id" id}
        result     (mc/insert "widgets" doc)]
    (is (= (sort ["kw1" "kw2"])
           (sort (get-in (mc/find-map-by-id collection id) [:keyword1 :keyword2]))))))


(defrecord Metrics
    [rps eps])

(deftest insert-a-document-with-clojure-record-in-it
  (let [collection "widgets"
        id         (ObjectId.)
        doc        {:record (Metrics. 10 20) "_id" id}
        result     (mc/insert "widgets" doc)]
    (is (= {:rps 10 :eps 20} (:record (mc/find-map-by-id collection id))))))

(deftest test-insert-a-document-with-dbref
  (let [coll1 "widgets"
        coll2 "owners"
        oid   (ObjectId.)
        joe   (mc/insert "owners" {:name "Joe" :_id oid})
        dbref (DBRef. (mg/current-db) coll2 oid)]
    (mc/insert coll1 {:type "pentagon" :owner dbref})
    (let [fetched (mc/find-one-as-map coll1 {:type "pentagon"})
          fo      (:owner fetched)]
      (is (= {:_id oid :name "Joe"} (from-db-object @fo true))))))


;;
;; insert-and-return
;;

(deftest  insert-and-return-a-basic-document-without-id-and-with-default-write-concern
  (let [collection "people"
        doc        {:name "Joe" :age 30}
        result     (mc/insert-and-return :people doc)]
    (is (= (:name doc)
           (:name result)))
    (is (= (:age doc)
           (:age result)))
    (is (:_id result))
    (is (= 1 (mc/count collection)))))

(deftest  insert-and-return-a-basic-document-without-id-but-with-a-write-concern
  (let [collection "people"
        doc        {:name "Joe" :age 30 :ratio 3/4}
        result     (mc/insert-and-return "people" doc WriteConcern/FSYNC_SAFE)]
    (is (= (:name doc)
           (:name result)))
    (is (= (:age doc)
           (:age result)))
    (is (= (:ratio doc)
           (:ratio result)))    
    (is (:_id result))
    (is (= 1 (mc/count collection)))))

(deftest  insert-and-return-with-a-provided-id
  (let [collection "people"
        oid        (ObjectId.)
        doc        {:name "Joe" :age 30 :_id oid}
        result     (mc/insert-and-return :people doc)]
    (is (= (:_id result) (:_id doc) oid))
    (is (= 1 (mc/count collection)))))


;;
;; insert-batch
;;

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-default-write-concern
  (let [collection "people"
        docs       [{:name "Joe" :age 30} {:name "Paul" :age 27}]]
    (is (monger.result/ok? (mc/insert-batch "people" docs)))
    (is (= 2 (mc/count collection)))))

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-explicit-write-concern
  (let [collection "people"
        docs       [{:name "Joe" :age 30} {:name "Paul" :age 27}]]
    (is (monger.result/ok? (mc/insert-batch "people" docs WriteConcern/NORMAL)))
    (is (= 2 (mc/count collection)))))

(deftest insert-a-batch-of-basic-documents-with-explicit-database-without-ids-and-with-explicit-write-concern
  (let [collection "people"
        docs       [{:name "Joe" :age 30} {:name "Paul" :age 27}]]
    (dotimes [n 44]
      (is (monger.result/ok? (mc/insert-batch monger.core/*mongodb-database* "people" docs WriteConcern/NORMAL))))
    (is (= 88 (mc/count collection)))))
