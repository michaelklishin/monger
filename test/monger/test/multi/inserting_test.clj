(ns monger.test.multi.inserting-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject DBRef]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.core        :as mg]
            [monger.util        :as mu]
            [monger.multi.collection  :as mc]
            [monger.test.helper :as helper]
            [clojure.test :refer :all]
            [monger.operators :refer :all]
            [monger.conversion :refer :all]
            [monger.test.fixtures :refer :all]))

(helper/connect!)

(def db (mg/get-db "altdb"))

(defn purge-altdb
  [f]
  (mc/remove db "people")
  (mc/remove db "widgets")
  (f))

(use-fixtures :each purge-altdb)

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

(defrecord Metrics
    [rps eps])

(deftest insert-a-document-with-clojure-record-in-it
  (let [db         (mg/get-db "altdb")
        collection "widgets"
        id         (ObjectId.)
        doc        {:record (Metrics. 10 20) "_id" id}
        result     (mc/insert db "widgets" doc)]
    (is (= {:rps 10 :eps 20} (:record (mc/find-map-by-id db collection id))))))

(deftest test-insert-a-document-with-dbref
  (let [db    (mg/get-db "altdb")]
    (mc/remove db "widgets")
    (mc/remove db "owners")
    (let [coll1 "widgets"
          coll2 "owners"
          oid   (ObjectId.)
          joe   (mc/insert db "owners" {:name "Joe" :_id oid})
          dbref (DBRef. (mg/current-db) coll2 oid)]
      (mc/insert db coll1 {:type "pentagon" :owner dbref})
      (let [fetched (mc/find-one-as-map db coll1 {:type "pentagon"})
            fo      (:owner fetched)]
        (is (= {:_id oid :name "Joe"} (from-db-object @fo true)))))))


;;
;; insert-and-return
;;

(deftest  insert-and-return-a-basic-document-without-id-and-with-default-write-concern
  (let [db         (mg/get-db "altdb")
        collection "people"
        doc        {:name "Joe" :age 30}
        result     (mc/insert-and-return db :people doc)]
    (is (= (:name doc)
           (:name result)))
    (is (= (:age doc)
           (:age result)))
    (is (:_id result))
    (is (= 1 (mc/count db collection)))))

(deftest  insert-and-return-a-basic-document-without-id-but-with-a-write-concern
  (let [db         (mg/get-db "altdb")
        collection "people"
        doc        {:name "Joe" :age 30 :ratio 3/4}
        result     (mc/insert-and-return db "people" doc WriteConcern/FSYNC_SAFE)]
    (is (= (:name doc)
           (:name result)))
    (is (= (:age doc)
           (:age result)))
    (is (= (:ratio doc)
           (:ratio result)))    
    (is (:_id result))
    (is (= 1 (mc/count db collection)))))

(deftest  insert-and-return-with-a-provided-id
  (let [db         (mg/get-db "altdb")
        collection "people"
        oid        (ObjectId.)
        doc        {:name "Joe" :age 30 :_id oid}
        result     (mc/insert-and-return db :people doc)]
    (is (= (:_id result) (:_id doc) oid))
    (is (= 1 (mc/count db collection)))))


;;
;; insert-batch
;;

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-default-write-concern
  (let [db         (mg/get-db "altdb")
        collection "people"
        docs       [{:name "Joe" :age 30} {:name "Paul" :age 27}]]
    (is (monger.result/ok? (mc/insert-batch db "people" docs)))
    (is (= 2 (mc/count db collection)))))

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-explicit-write-concern
  (let [db         (mg/get-db "altdb")
        collection "people"
        docs       [{:name "Joe" :age 30} {:name "Paul" :age 27}]]
    (is (monger.result/ok? (mc/insert-batch db "people" docs WriteConcern/NORMAL)))
    (is (= 2 (mc/count db collection)))))

(deftest insert-a-batch-of-basic-documents-with-explicit-database-without-ids-and-with-explicit-write-concern
  (let [db         (mg/get-db "altdb")
        collection "people"
        docs       [{:name "Joe" :age 30} {:name "Paul" :age 27}]]
    (dotimes [n 44]
      (is (monger.result/ok? (mc/insert-batch db "people" docs WriteConcern/NORMAL))))
    (is (= 88 (mc/count db collection)))))

(deftest insert-a-batch-of-basic-documents-from-a-lazy-sequence
  (let [db         (mg/get-db "altdb")
        collection "people"
        numbers    (range 0 1000)]
    (is (monger.result/ok? (mc/insert-batch db "people" (map (fn [^long l]
                                                               {:n l})
                                                             numbers))))
    (is (= (count numbers) (mc/count db collection)))))
