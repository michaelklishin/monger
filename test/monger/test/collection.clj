(set! *warn-on-reflection* true)

(ns monger.test.collection
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject CommandResult$CommandFailure MapReduceOutput MapReduceCommand MapReduceCommand$OutputType]
            [org.bson.types ObjectId]
            [java.util Date])
  (:require [monger core util]
            [clojure stacktrace]
            [monger.collection :as mgcol]
            [monger.result     :as mgres]
            [monger.conversion :as mgcnv]
            [monger.js         :as js]
            [monger.test.helper :as helper])
  (:use [clojure.test]
        [monger.operators]
        [monger.test.fixtures]))

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


;;
;; indexes
;;

(deftest index-operations
  (let [collection "libraries"]
    (mgcol/drop-indexes collection)
    (is (= "_id_"
           (:name (first (mgcol/indexes-on collection)))))
    (is (nil? (second (mgcol/indexes-on collection))))
    (mgcol/create-index collection { "language" 1 })
    (is (= "language_1"
           (:name (second (mgcol/indexes-on collection)))))
    (mgcol/drop-index collection "language_1")
    (is (nil? (second (mgcol/indexes-on collection))))
    (mgcol/ensure-index collection { "language" 1 })
    (is (= "language_1"
           (:name (second (mgcol/indexes-on collection)))))
    (mgcol/ensure-index collection { "language" 1 })))


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
;; Map/Reduce
;;

(let [collection "widgets"
      mapper     (js/load-resource "resources/mongo/js/mapfun1.js")
      reducer    "function(key, values) {
                    var result = 0;
                    values.forEach(function(v) { result += v });

                    return result;
                   }"
      batch      [{ :state "CA" :quantity 1 :price 199.00 }
                  { :state "NY" :quantity 2 :price 199.00 }
                  { :state "NY" :quantity 1 :price 299.00 }
                  { :state "IL" :quantity 2 :price 11.50  }
                  { :state "CA" :quantity 2 :price 2.95   }
                  { :state "IL" :quantity 3 :price 5.50   }]
      expected    [{:_id "CA", :value 204.9} {:_id "IL", :value 39.5} {:_id "NY", :value 697.0}]]
  (deftest basic-inline-map-reduce-example
    (mgcol/remove monger.core/*mongodb-database* collection {})
    (is (mgres/ok? (mgcol/insert-batch collection batch)))
    (let [output  (mgcol/map-reduce collection mapper reducer nil MapReduceCommand$OutputType/INLINE {})
          results (mgcnv/from-db-object ^DBObject (.results ^MapReduceOutput output) true)]
      (mgres/ok? output)
      (is (= expected results))))

  (deftest basic-map-reduce-example-that-replaces-named-collection
    (mgcol/remove monger.core/*mongodb-database* collection {})
    (is (mgres/ok? (mgcol/insert-batch collection batch)))
    (let [output  (mgcol/map-reduce collection mapper reducer "mr_outputs" {})
          results (mgcnv/from-db-object ^DBObject (.results ^MapReduceOutput output) true)]
      (mgres/ok? output)
      (is (= 3 (monger.core/count results)))
      (is (= expected
             (map #(mgcnv/from-db-object % true) (seq results))))
      (is (= expected
             (map #(mgcnv/from-db-object % true) (mgcol/find "mr_outputs"))))
      (.drop ^MapReduceOutput output)))

  (deftest basic-map-reduce-example-that-merged-results-into-named-collection
    (mgcol/remove monger.core/*mongodb-database* collection {})
    (is (mgres/ok? (mgcol/insert-batch collection batch)))
    (mgcol/map-reduce collection mapper reducer "merged_mr_outputs" MapReduceCommand$OutputType/MERGE {})
    (is (mgres/ok? (mgcol/insert       collection { :state "OR" :price 17.95 :quantity 4 })))
    (let [output  (mgcol/map-reduce collection mapper reducer "merged_mr_outputs" MapReduceCommand$OutputType/MERGE {})]
      (mgres/ok? output)
      (is (= 4 (monger.core/count (.results ^MapReduceOutput output))))
      (is (= ["CA" "IL" "NY" "OR"]
             (map :_id (mgcol/find-maps "merged_mr_outputs"))))
      (.drop ^MapReduceOutput output))))


;;
;; distinct
;;

(deftest distinct-values
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


;;
;; any?, empty?
;;

(deftest any-on-empty-collection
  (let [collection "things"]
    (is (not (mgcol/any? collection)))))

(deftest any-on-non-empty-collection
  (let [collection "things"
        _           (mgcol/insert collection { :language "Clojure", :name "langohr" })]
    (is (mgcol/any? "things"))
    (is (mgcol/any? monger.core/*mongodb-database* "things" {:language "Clojure"}))))

(deftest empty-on-empty-collection
  (let [collection "things"]
    (is (mgcol/empty? collection))
    (is (mgcol/empty? monger.core/*mongodb-database* collection))))

(deftest empty-on-non-empty-collection
  (let [collection "things"
        _           (mgcol/insert collection { :language "Clojure", :name "langohr" })]
    (is (not (mgcol/empty? "things")))))