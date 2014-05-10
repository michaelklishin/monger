(ns monger.test.map-reduce-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject MapReduceOutput MapReduceCommand MapReduceCommand$OutputType]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.collection       :as mc]
            [monger.core             :as mg]
            [monger.result           :as mgres]
            [clojurewerkz.support.js :as js]
            [clojure.test :refer :all]
            [monger.operators :refer :all]
            [monger.conversion :refer :all]))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (use-fixtures :each (fn [f]
                        (mc/remove db "widgets")
                        (f)
                        (mc/remove db "widgets")))

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
    (deftest test-basic-inline-map-reduce-example
      (mc/remove db collection)
      (is (mgres/ok? (mc/insert-batch db collection batch)))
      (let [output  (mc/map-reduce db collection mapper reducer nil MapReduceCommand$OutputType/INLINE {})
            results (from-db-object ^DBObject (.results ^MapReduceOutput output) true)]
        (mgres/ok? output)
        (is (= expected results))))

    (deftest test-basic-map-reduce-example-that-replaces-named-collection
      (mc/remove db collection)
      (is (mgres/ok? (mc/insert-batch db collection batch)))
      (let [output  (mc/map-reduce db collection mapper reducer "mr_outputs" {})
            results (from-db-object ^DBObject (.results ^MapReduceOutput output) true)]
        (mgres/ok? output)
        (is (= 3 (mg/count results)))
        (is (= expected
               (map #(from-db-object % true) (seq results))))
        (is (= expected
               (map #(from-db-object % true) (mc/find db "mr_outputs"))))
        (.drop ^MapReduceOutput output)))

    (deftest test-basic-map-reduce-example-that-merged-results-into-named-collection
      (mc/remove db collection)
      (is (mgres/ok? (mc/insert-batch db collection batch)))
      (mc/map-reduce db collection mapper reducer "merged_mr_outputs" MapReduceCommand$OutputType/MERGE {})
      (is (mgres/ok? (mc/insert db collection { :state "OR" :price 17.95 :quantity 4 })))
      (let [^MapReduceOutput output (mc/map-reduce db collection mapper reducer "merged_mr_outputs" MapReduceCommand$OutputType/MERGE {})]
        (mgres/ok? output)
        (is (= 4 (mg/count output)))
        (is (= ["CA" "IL" "NY" "OR"]
               (map :_id (mc/find-maps db "merged_mr_outputs"))))
        (.drop ^MapReduceOutput output)))))
