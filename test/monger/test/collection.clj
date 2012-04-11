(set! *warn-on-reflection* true)

(ns monger.test.collection
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject CommandResult$CommandFailure MapReduceOutput MapReduceCommand MapReduceCommand$OutputType]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger core util]
            [monger.collection  :as mgcol]
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
    (is (= 0 (mgcol/count collection)))
    (mgcol/insert-batch collection [{ :language "Clojure", :name "langohr" }
                                    { :language "Clojure", :name "monger" }
                                    { :language "Clojure", :name "incanter" }
                                    { :language "Scala",   :name "akka" }] )
    (is (= 4 (mgcol/count collection)))
    (is (mgcol/any? collection))
    (is (= 3 (mgcol/count monger.core/*mongodb-database* collection { :language "Clojure" })))
    (is (mgcol/any? monger.core/*mongodb-database* collection { :language "Clojure" }))
    (is (= 1 (mgcol/count collection { :language "Scala"   })))
    (is (mgcol/any? collection { :language "Scala" }))
    (is (= 0 (mgcol/count monger.core/*mongodb-database* collection { :language "Python"  })))
    (is (not (mgcol/any? monger.core/*mongodb-database* collection { :language "Python" })))))


(deftest remove-all-documents-from-collection
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{ :language "Clojure", :name "monger" }
                                    { :language "Clojure", :name "langohr" }
                                    { :language "Clojure", :name "incanter" }
                                    { :language "Scala",   :name "akka" }])
    (is (= 4 (mgcol/count collection)))
    (mgcol/remove collection)
    (is (= 0 (mgcol/count collection)))))


(deftest remove-some-documents-from-collection
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{ :language "Clojure", :name "monger" }
                                    { :language "Clojure", :name "langohr" }
                                    { :language "Clojure", :name "incanter" }
                                    { :language "Scala",   :name "akka" }])
    (is (= 4 (mgcol/count collection)))
    (mgcol/remove collection { :language "Clojure" })
    (is (= 1 (mgcol/count collection)))))

(deftest remove-a-single-document-from-collection
  (let [collection "libraries"
        oid        (ObjectId.)]
    (mgcol/insert-batch collection [{ :language "Clojure" :name "monger" :_id oid }])
    (mgcol/remove-by-id collection oid)
    (is (= 0 (mgcol/count collection)))
    (is (nil? (mgcol/find-by-id collection oid)))))


;;
;; exists?, drop, create
;;

(deftest checking-for-collection-existence-when-it-does-not-exist
  (let [collection "widgets"]
    (mgcol/drop collection)
    (is (false? (mgcol/exists? collection)))))

(deftest checking-for-collection-existence-when-it-does-exist
  (let [collection "widgets"]
    (mgcol/drop collection)
    (mgcol/insert-batch collection [{ :name "widget1" }
                                    { :name "widget2" }])
    (is (mgcol/exists? collection))
    (mgcol/drop collection)
    (is (false? (mgcol/exists? collection)))
    (mgcol/create "widgets" { :capped true :size 100000 :max 10 })
    (is (mgcol/exists? collection))
    (mgcol/rename collection "gadgets")
    (is (not (mgcol/exists? collection)))
    (is (mgcol/exists? "gadgets"))
    (mgcol/drop "gadgets")))

;;
;; any?, empty?
;;

(deftest test-any-on-empty-collection
  (let [collection "things"]
    (is (not (mgcol/any? collection)))))

(deftest test-any-on-non-empty-collection
  (let [collection "things"
        _           (mgcol/insert collection { :language "Clojure", :name "langohr" })]
    (is (mgcol/any? "things"))
    (is (mgcol/any? monger.core/*mongodb-database* "things" {:language "Clojure"}))))

(deftest test-empty-on-empty-collection
  (let [collection "things"]
    (is (mgcol/empty? collection))
    (is (mgcol/empty? monger.core/*mongodb-database* collection))))

(deftest test-empty-on-non-empty-collection
  (let [collection "things"
        _           (mgcol/insert collection { :language "Clojure", :name "langohr" })]
    (is (not (mgcol/empty? "things")))))


;;
;; distinct
;;

(deftest test-distinct-values
  (let [collection "widgets"
        batch      [{ :state "CA" :quantity 1 :price 199.00 }
                    { :state "NY" :quantity 2 :price 199.00 }
                    { :state "NY" :quantity 1 :price 299.00 }
                    { :state "IL" :quantity 2 :price 11.50  }
                    { :state "CA" :quantity 2 :price 2.95   }
                    { :state "IL" :quantity 3 :price 5.50   }]]
    (mgcol/insert-batch collection batch)
    (is (= ["CA" "IL" "NY"] (sort (mgcol/distinct monger.core/*mongodb-database* collection :state {}))))
    (is (= ["CA" "NY"] (sort (mgcol/distinct collection :state { :price { $gt 100.00 } }))))))
