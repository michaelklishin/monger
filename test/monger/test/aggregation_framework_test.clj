(ns monger.test.aggregation-framework-test
  (:require monger.core [monger.collection :as mc]
            [monger.test.helper :as helper])
  (:use clojure.test
        monger.operators
        monger.test.fixtures))


(helper/connect!)

(use-fixtures :each purge-docs)

(deftest ^{:edge-features true} test-basic-single-stage-$project-aggregation
  (let [collection "docs"
        batch      [{ :state "CA" :quantity 1 :price 199.00 }
                    { :state "NY" :quantity 2 :price 199.00 }
                    { :state "NY" :quantity 1 :price 299.00 }
                    { :state "IL" :quantity 2 :price 11.50  }
                    { :state "CA" :quantity 2 :price 2.95   }
                    { :state "IL" :quantity 3 :price 5.50   }]
        expected    #{{:quantity 1 :state "CA"}
                      {:quantity 2 :state "NY"}
                      {:quantity 1 :state "NY"}
                      {:quantity 2 :state "IL"}
                      {:quantity 2 :state "CA"}
                      {:quantity 3 :state "IL"}}]
    (mc/insert-batch collection batch)
    (is (= 6 (mc/count collection)))
    (let [result (set (map #(select-keys % [:state :quantity])
                           (mc/aggregate "docs" [{$project {:state 1 :quantity 1}}])))]
      (is (= expected result)))))


(deftest ^{:edge-features true} test-basic-projection-with-multiplication
  (let [collection "docs"
        batch      [{ :state "CA" :quantity 1 :price 199.00 }
                    { :state "NY" :quantity 2 :price 199.00 }
                    { :state "NY" :quantity 1 :price 299.00 }
                    { :state "IL" :quantity 2 :price 11.50  }
                    { :state "CA" :quantity 2 :price 2.95   }
                    { :state "IL" :quantity 3 :price 5.50   }]
        expected    #{{:_id "NY" :subtotal 398.0}
                      {:_id "NY" :subtotal 299.0}
                      {:_id "IL" :subtotal 23.0}
                      {:_id "CA" :subtotal 5.9}
                      {:_id "IL" :subtotal 16.5}
                      {:_id "CA" :subtotal 199.0}}]
    (mc/insert-batch collection batch)
    (let [result (set (mc/aggregate "docs" [{$project {:subtotal {$multiply ["$quantity", "$price"]}
                                                       :_id     "$state"}}]))]
      (is (= expected result)))))


(deftest ^{:edge-features true} test-basic-total-aggregation
  (let [collection "docs"
        batch      [{ :state "CA" :quantity 1 :price 199.00 }
                    { :state "NY" :quantity 2 :price 199.00 }
                    { :state "NY" :quantity 1 :price 299.00 }
                    { :state "IL" :quantity 2 :price 11.50  }
                    { :state "CA" :quantity 2 :price 2.95   }
                    { :state "IL" :quantity 3 :price 5.50   }]
        expected    #{{:_id "CA" :total 204.9} {:_id "IL" :total 39.5} {:_id "NY" :total 697.0}}]
    (mc/insert-batch collection batch)
    (let [result (set (mc/aggregate "docs" [{$project {:subtotal {$multiply ["$quantity", "$price"]}
                                                       :_id      1
                                                       :state   1}}
                                            {$group   {:_id   "$state"
                                                       :total {$sum "$subtotal"}}}]))]
      (is (= expected result)))))
