(set! *warn-on-reflection* true)

(ns monger.test.multi.collection-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject MapReduceOutput MapReduceCommand MapReduceCommand$OutputType]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.core        :as mg]
            [monger.multi.collection  :as mc]
            [monger.result      :as mgres]
            [monger.test.helper :as helper])
  (:use clojure.test
        monger.operators
        monger.test.fixtures))

(helper/connect!)

(defn drop-altdb
  [f]
  (mg/drop-db "altdb")
  (f))

(use-fixtures :each drop-altdb)

(deftest get-collection-size
  (let [db (mg/get-db "altdb")
        collection "things"]
    (is (= 0 (mc/count db collection)))
    (mc/insert-batch db collection [{:language "Clojure" :name "langohr"}
                                    {:language "Clojure" :name "monger"}
                                    {:language "Clojure" :name "incanter"}
                                    {:language "Scala"   :name "akka"}])
    (is (= 4 (mc/count db collection)))
    (is (mc/any? db collection))
    (is (= 3 (mc/count db collection {:language "Clojure"})))
    (is (mc/any? db collection {:language "Clojure"}))
    (is (= 1 (mc/count db collection {:language "Scala"  })))
    (is (mc/any? db collection {:language "Scala"}))
    (is (= 0 (mc/count db collection {:language "Python" })))
    (is (not (mc/any? db collection {:language "Python"})))))

(deftest remove-all-documents-from-collection
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (mc/insert-batch db collection [{:language "Clojure" :name "monger"}
                                    {:language "Clojure" :name "langohr"}
                                    {:language "Clojure" :name "incanter"}
                                    {:language "Scala"   :name "akka"}])
    (is (= 4 (mc/count db collection)))
    (mc/remove db collection)
    (is (= 0 (mc/count db collection)))))

(deftest remove-some-documents-from-collection
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (mc/insert-batch db collection [{:language "Clojure" :name "monger"}
                                    {:language "Clojure" :name "langohr"}
                                    {:language "Clojure" :name "incanter"}
                                    {:language "Scala"   :name "akka"}])
    (is (= 4 (mc/count db collection)))
    (mc/remove db collection {:language "Clojure"})
    (is (= 1 (mc/count db collection)))))

(deftest remove-a-single-document-from-collection
  (let [db (mg/get-db "altdb")
        collection "libraries"
        oid        (ObjectId.)]
    (mc/insert-batch db collection [{:language "Clojure" :name "monger" :_id oid}])
    (mc/remove-by-id db collection oid)
    (is (= 0 (mc/count db collection)))
    (is (nil? (mc/find-by-id db collection oid)))))

(deftest checking-for-collection-existence-when-it-does-not-exist
  (let [db (mg/get-db "altdb")
        collection "widgets"]
    (mc/drop db collection)
    (is (false? (mc/exists? db collection)))))

(deftest checking-for-collection-existence-when-it-does-exist
  (let [db (mg/get-db "altdb")
        collection "widgets"]
    (mc/drop db collection)
    (mc/insert-batch db collection [{:name "widget1"}
                                    {:name "widget2"}])
    (is (mc/exists? db collection))
    (mc/drop db collection)
    (is (false? (mc/exists? db collection)))
    (mc/create db "widgets" {:capped true :size 100000 :max 10})
    (is (mc/exists? db collection))
    (mc/rename db collection "gadgets")
    (is (not (mc/exists? db collection)))
    (is (mc/exists? db "gadgets"))
    (mc/drop db "gadgets")))

(deftest test-any-on-empty-collection
  (let [db (mg/get-db "altdb")
        collection "things"]
    (is (not (mc/any? db collection)))))

(deftest test-any-on-non-empty-collection
  (let [db (mg/get-db "altdb")
        collection "things"
        _           (mc/insert db collection {:language "Clojure" :name "langohr"})]
    (is (mc/any? db "things"))
    (is (mc/any? db "things" {:language "Clojure"}))))

(deftest test-empty-on-empty-collection
  (let [db (mg/get-db "altdb")
        collection "things"]
    (is (mc/empty? db collection))
    (is (mc/empty? db collection))))

(deftest test-empty-on-non-empty-collection
  (let [db (mg/get-db "altdb")
        collection "things"
        _           (mc/insert db collection {:language "Clojure" :name "langohr"})]
    (is (not (mc/empty? db "things")))))

(deftest test-distinct-values
  (let [db (mg/get-db "altdb")
        collection "widgets"
        batch      [{:state "CA" :quantity 1 :price 199.00}
                    {:state "NY" :quantity 2 :price 199.00}
                    {:state "NY" :quantity 1 :price 299.00}
                    {:state "IL" :quantity 2 :price 11.50 }
                    {:state "CA" :quantity 2 :price 2.95  }
                    {:state "IL" :quantity 3 :price 5.50  }]]
    (mc/insert-batch db collection batch)
    (is (= ["CA" "IL" "NY"] (sort (mc/distinct db collection :state {}))))
    (is (= ["CA" "NY"] (sort (mc/distinct db collection :state {:price {$gt 100.00}}))))))


(run-tests)
