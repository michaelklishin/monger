(ns monger.test.multi.inserting-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject DBRef]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.core        :as mg]
            [monger.util        :as mu]
            [monger.multi.collection  :as mc]
            [monger.test.helper :as helper])
  (:use clojure.test
        monger.operators
        monger.conversion
        monger.test.fixtures))

(helper/connect!)

(defn drop-altdb
  [f]
  (mg/drop-db "altdb")
  (f))

(use-fixtures :each drop-altdb)

;;
;; insert
;;

(deftest insert-a-basic-document-without-id-and-with-default-write-concern
  (let [db         (mg/get-db "altdb")
        collection "people"
        doc        {:name "Joe" :age 30}]
    (is (monger.result/ok? (mc/insert db "people" doc)))
    (is (= 1 (mc/count db collection)))))

(deftest insert-a-basic-document-with-explicitly-passed-database-without-id-and-with-default-write-concern
  (let [db         (mg/get-db "altdb")
        collection "people"
        doc        {:name "Joe" :age 30}]
    (dotimes [n 5]
      (is (monger.result/ok? (mc/insert db "people" doc WriteConcern/SAFE))))
    (is (= 5 (mc/count db collection)))))

(deftest insert-a-basic-document-without-id-and-with-explicit-write-concern
  (let [db         (mg/get-db "altdb")
        collection "people"
        doc        {:name "Joe" :age 30}]
    (is (monger.result/ok? (mc/insert db "people" doc WriteConcern/SAFE)))
    (is (= 1 (mc/count db collection)))))

(deftest insert-a-basic-db-object-without-id-and-with-default-write-concern
  (let [db         (mg/get-db "altdb")
        collection "people"
        doc        (to-db-object {:name "Joe" :age 30})]
    (is (nil? (.get ^DBObject doc "_id")))
    (mc/insert db "people" doc)
    (is (not (nil? (monger.util/get-id doc))))))

(deftest insert-a-map-with-id-and-with-default-write-concern
  (let [db         (mg/get-db "altdb")
        collection "people"
        id         (ObjectId.)
        doc        {:name "Joe" :age 30 "_id" id}
        result     (mc/insert db "people" doc)]
    (is (= id (monger.util/get-id doc)))))

(deftest insert-a-document-with-clojure-ratio-in-it
  (let [db         (mg/get-db "altdb")
        collection "widgets"
        id         (ObjectId.)
        doc        {:ratio 11/2 "_id" id}
        result     (mc/insert db "widgets" doc)]
    (is (= 5.5 (:ratio (mc/find-map-by-id db collection id))))))

(deftest insert-a-document-with-clojure-keyword-in-it
  (let [db         (mg/get-db "altdb")
        collection "widgets"
        id         (ObjectId.)
        doc        {:keyword :kwd "_id" id}
        result     (mc/insert db "widgets" doc)]
    (is (= (name :kwd) (:keyword (mc/find-map-by-id db collection id))))))

(deftest insert-a-document-with-clojure-keyword-in-a-set-in-it
  (let [db         (mg/get-db "altdb")
        collection "widgets"
        id         (ObjectId.)
        doc        {:keyword1 {:keyword2 #{:kw1 :kw2}} "_id" id}
        result     (mc/insert db "widgets" doc)]
    (is (= (sort ["kw1" "kw2"])
           (sort (get-in (mc/find-map-by-id db collection id) [:keyword1 :keyword2]))))))
