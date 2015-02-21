(ns monger.test.inserting-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject DBRef]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.core        :as mg]
            [monger.util        :as mu]
            [monger.collection  :as mc]
            [clojure.test :refer :all]
            [monger.operators :refer :all]
            [monger.conversion :refer :all]))

(defrecord Metrics
    [rps eps])

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (defn purge-collections
    [f]
    (mc/remove db "people")
    (mc/remove db "docs")
    (mc/remove db "things")
    (mc/remove db "widgets")
    (f)
    (mc/remove db "people")
    (mc/remove db "docs")
    (mc/remove db "things")
    (mc/remove db "widgets"))

  (use-fixtures :each purge-collections)


  ;;
  ;; insert
  ;;

  (deftest insert-a-basic-document-without-id-and-with-default-write-concern
    (let [collection "people"
          doc        {:name "Joe" :age 30}]
      (is (monger.result/ok? (mc/insert db collection doc)))
      (is (= 1 (mc/count db collection)))))

  (deftest insert-a-basic-document-with-explicitly-passed-database-without-id-and-with-default-write-concern
    (let [collection "people"
          doc        {:name "Joe" :age 30}]
      (dotimes [n 5]
        (is (monger.result/ok? (mc/insert db collection doc WriteConcern/SAFE))))
      (is (= 5 (mc/count db collection)))))

  (deftest insert-a-basic-document-without-id-and-with-explicit-write-concern
    (let [collection "people"
          doc        {:name "Joe" :age 30}]
      (is (monger.result/ok? (mc/insert db collection doc WriteConcern/SAFE)))
      (is (= 1 (mc/count db collection)))))

  (deftest insert-a-basic-db-object-without-id-and-with-default-write-concern
    (let [collection "people"
          doc        (to-db-object {:name "Joe" :age 30})]
      (is (nil? (.get ^DBObject doc "_id")))
      (mc/insert db collection doc)
      (is (not (nil? (monger.util/get-id doc))))))

  (deftest insert-a-map-with-id-and-with-default-write-concern
    (let [collection "people"
          id         (ObjectId.)
          doc        {:name "Joe" :age 30 "_id" id}
          result     (mc/insert db collection doc)]
      (is (= id (monger.util/get-id doc)))))

  (deftest insert-a-document-with-clojure-ratio-in-it
    (let [collection "widgets"
          id         (ObjectId.)
          doc        {:ratio 11/2 "_id" id}
          result     (mc/insert db collection doc)]
      (is (= 5.5 (:ratio (mc/find-map-by-id db collection id))))))

  (deftest insert-a-document-with-clojure-keyword-in-it
    (let [collection "widgets"
          id         (ObjectId.)
          doc        {:keyword :kwd "_id" id}
          result     (mc/insert db collection doc)]
      (is (= (name :kwd) (:keyword (mc/find-map-by-id db collection id))))))

  (deftest insert-a-document-with-clojure-keyword-in-a-set-in-it
    (let [collection "widgets"
          id         (ObjectId.)
          doc        {:keyword1 {:keyword2 #{:kw1 :kw2}} "_id" id}
          result     (mc/insert db collection doc)]
      (is (= (sort ["kw1" "kw2"])
             (sort (get-in (mc/find-map-by-id db collection id) [:keyword1 :keyword2]))))))

  (deftest insert-a-document-with-clojure-record-in-it
    (let [collection "widgets"
          id         (ObjectId.)
          doc        {:record (Metrics. 10 20) "_id" id}
          result     (mc/insert db collection doc)]
      (is (= {:rps 10 :eps 20} (:record (mc/find-map-by-id db collection id))))))

  (deftest test-insert-a-document-with-dbref
    (mc/remove db "widgets")
    (mc/remove db "owners")
    (let [coll1 "widgets"
          coll2 "owners"
          oid   (ObjectId.)
          joe   (mc/insert db coll2 {:name "Joe" :_id oid})
          dbref (DBRef. coll2 oid)]
      (mc/insert db coll1 {:type "pentagon" :owner dbref})
      (let [fetched (mc/find-one-as-map db coll1 {:type "pentagon"})
            fo      (:owner fetched)]
        (is (= {:_id oid :name "Joe"} (from-db-object @fo true))))))


  ;;
  ;; insert-and-return
  ;;

  (deftest  insert-and-return-a-basic-document-without-id-and-with-default-write-concern
    (let [collection "people"
          doc        {:name "Joe" :age 30}
          result     (mc/insert-and-return db collection doc)]
      (is (= (:name doc)
             (:name result)))
      (is (= (:age doc)
             (:age result)))
      (is (:_id result))
      (is (= 1 (mc/count db collection)))))

  (deftest  insert-and-return-a-basic-document-without-id-but-with-a-write-concern
    (let [collection "people"
          doc        {:name "Joe" :age 30 :ratio 3/4}
          result     (mc/insert-and-return db collection doc WriteConcern/FSYNC_SAFE)]
      (is (= (:name doc)
             (:name result)))
      (is (= (:age doc)
             (:age result)))
      (is (= (:ratio doc)
             (:ratio result)))    
      (is (:_id result))
      (is (= 1 (mc/count db collection)))))

  (deftest  insert-and-return-with-a-provided-id
    (let [collection "people"
          oid        (ObjectId.)
          doc        {:name "Joe" :age 30 :_id oid}
          result     (mc/insert-and-return db collection doc)]
      (is (= (:_id result) (:_id doc) oid))
      (is (= 1 (mc/count db collection)))))


  ;;
  ;; insert-batch
  ;;

  (deftest insert-a-batch-of-basic-documents-without-ids-and-with-default-write-concern
    (let [collection "people"
          docs       [{:name "Joe" :age 30} {:name "Paul" :age 27}]]
      (is (monger.result/ok? (mc/insert-batch db collection docs)))
      (is (= 2 (mc/count db collection)))))

  (deftest insert-a-batch-of-basic-documents-without-ids-and-with-explicit-write-concern
    (let [collection "people"
          docs       [{:name "Joe" :age 30} {:name "Paul" :age 27}]]
      (is (monger.result/ok? (mc/insert-batch db collection docs WriteConcern/NORMAL)))
      (is (= 2 (mc/count db collection)))))

  (deftest insert-a-batch-of-basic-documents-with-explicit-database-without-ids-and-with-explicit-write-concern
    (let [collection "people"
          docs       [{:name "Joe" :age 30} {:name "Paul" :age 27}]]
      (dotimes [n 44]
        (is (monger.result/ok? (mc/insert-batch db collection docs WriteConcern/NORMAL))))
      (is (= 88 (mc/count db collection)))))

  (deftest insert-a-batch-of-basic-documents-from-a-lazy-sequence
    (let [collection "people"
          numbers    (range 0 1000)]
      (is (monger.result/ok? (mc/insert-batch db collection (map (fn [^long l]
                                                                   {:n l})
                                                                 numbers))))
      (is (= (count numbers) (mc/count db collection))))))
