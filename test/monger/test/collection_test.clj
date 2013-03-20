(set! *warn-on-reflection* true)

(ns monger.test.collection-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject MapReduceOutput MapReduceCommand MapReduceCommand$OutputType]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.core        :as mg]
            [monger.collection  :as mc]
            [monger.result      :as mgres]
            [monger.test.helper :as helper])
  (:use clojure.test
        monger.operators
        monger.test.fixtures))

(helper/connect!)

(use-fixtures :each purge-people purge-docs purge-things purge-libraries)


;;
;; count, remove
;;

(deftest get-collection-size
  (let [collection "things"]
    (is (= 0 (mc/count collection)))
    (mc/insert-batch collection [{:language "Clojure" :name "langohr"}
                                 {:language "Clojure" :name "monger"}
                                 {:language "Clojure" :name "incanter"}
                                 {:language "Scala"   :name "akka"}])
    (is (= 4 (mc/count collection)))
    (is (mc/any? collection))
    (is (= 3 (mc/count mg/*mongodb-database* collection {:language "Clojure"})))
    (is (mc/any? mg/*mongodb-database* collection {:language "Clojure"}))
    (is (= 1 (mc/count collection {:language "Scala"  })))
    (is (mc/any? collection {:language "Scala"}))
    (is (= 0 (mc/count mg/*mongodb-database* collection {:language "Python" })))
    (is (not (mc/any? mg/*mongodb-database* collection {:language "Python"})))))


(deftest remove-all-documents-from-collection
  (let [collection "libraries"]
    (mc/insert-batch collection [{:language "Clojure" :name "monger"}
                                 {:language "Clojure" :name "langohr"}
                                 {:language "Clojure" :name "incanter"}
                                 {:language "Scala"   :name "akka"}])
    (is (= 4 (mc/count collection)))
    (mc/remove collection)
    (is (= 0 (mc/count collection)))))


(deftest remove-some-documents-from-collection
  (let [collection "libraries"]
    (mc/insert-batch collection [{:language "Clojure" :name "monger"}
                                 {:language "Clojure" :name "langohr"}
                                 {:language "Clojure" :name "incanter"}
                                 {:language "Scala"   :name "akka"}])
    (is (= 4 (mc/count collection)))
    (mc/remove collection {:language "Clojure"})
    (is (= 1 (mc/count collection)))))

(deftest remove-a-single-document-from-collection
  (let [collection "libraries"
        oid        (ObjectId.)]
    (mc/insert-batch collection [{:language "Clojure" :name "monger" :_id oid}])
    (mc/remove-by-id collection oid)
    (is (= 0 (mc/count collection)))
    (is (nil? (mc/find-by-id collection oid)))))


;;
;; exists?, drop, create
;;

(deftest checking-for-collection-existence-when-it-does-not-exist
  (let [collection "widgets"]
    (mc/drop collection)
    (is (false? (mc/exists? collection)))))

(deftest checking-for-collection-existence-when-it-does-exist
  (let [collection "widgets"]
    (mc/drop collection)
    (mc/insert-batch collection [{:name "widget1"}
                                 {:name "widget2"}])
    (is (mc/exists? collection))
    (mc/drop collection)
    (is (false? (mc/exists? collection)))
    (mc/create "widgets" {:capped true :size 100000 :max 10})
    (is (mc/exists? collection))
    (mc/rename collection "gadgets")
    (is (not (mc/exists? collection)))
    (is (mc/exists? "gadgets"))
    (mc/drop "gadgets")))

;;
;; any?, empty?
;;

(deftest test-any-on-empty-collection
  (let [collection "things"]
    (is (not (mc/any? collection)))))

(deftest test-any-on-non-empty-collection
  (let [collection "things"
        _           (mc/insert collection {:language "Clojure" :name "langohr"})]
    (is (mc/any? "things"))
    (is (mc/any? mg/*mongodb-database* "things" {:language "Clojure"}))))

(deftest test-empty-on-empty-collection
  (let [collection "things"]
    (is (mc/empty? collection))
    (is (mc/empty? mg/*mongodb-database* collection))))

(deftest test-empty-on-non-empty-collection
  (let [collection "things"
        _           (mc/insert collection {:language "Clojure" :name "langohr"})]
    (is (not (mc/empty? "things")))))


;;
;; distinct
;;

(deftest test-distinct-values
  (let [collection "widgets"
        batch      [{:state "CA" :quantity 1 :price 199.00}
                    {:state "NY" :quantity 2 :price 199.00}
                    {:state "NY" :quantity 1 :price 299.00}
                    {:state "IL" :quantity 2 :price 11.50 }
                    {:state "CA" :quantity 2 :price 2.95  }
                    {:state "IL" :quantity 3 :price 5.50  }]]
    (mc/insert-batch collection batch)
    (is (= ["CA" "IL" "NY"] (sort (mc/distinct mg/*mongodb-database* collection :state {}))))
    (is (= ["CA" "NY"] (sort (mc/distinct collection :state {:price {$gt 100.00}}))))))


;;
;; miscellenous
;;

(deftest test-system-collection-predicate
  (are [name] (is (mc/system-collection? name))
    "system.indexes"
    "system"
    ;; we treat default GridFS collections as system ones,
    ;; possibly this is a bad idea, time will tell. MK.
    "fs.chunks"
    "fs.files")
  (are [name] (is (not (mc/system-collection? name)))
    "events"
    "accounts"
    "megacorp_account"
    "myapp_development"))
