(ns monger.test.capped-collections-test
  (:require [monger.core :as mg]
            [monger.collection  :as mc]
            [clojure.test :refer :all]
            [monger.operators :refer :all]))

(defn- megabytes
  [^long n]
  (* n 1024 1024))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (deftest test-inserting-into-capped-collection
    (let [n     1000
          cname "cached"
          _     (mc/drop db cname)
          coll  (mc/create db cname {:capped true :size (-> 16 megabytes) :max n})]
      (is (= cname (.getName coll)))
      (mc/insert-batch db cname (for [i (range 0 (+ n 100))] {:i i}))
      (is (= n (mc/count db cname)))
      ;; older elements get replaced by newer ones
      (is (not (mc/any? db cname {:i 1})))
      (is (not (mc/any? db cname {:i 5})))
      (is (not (mc/any? db cname {:i 9})))
      (is (mc/any? db cname {:i (+ n 80)})))))
