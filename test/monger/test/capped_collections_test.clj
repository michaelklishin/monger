(set! *warn-on-reflection* true)

(ns monger.test.capped-collections-test
  (:require [monger core util]
            [monger.collection  :as mc]
            [monger.result      :as mres]
            [monger.test.helper :as helper]
            [clojure.test :refer :all]
            [monger.operators :refer :all]
            [monger.test.fixtures :refer :all]))

(helper/connect!)

(defn- megabytes
  [^long n]
  (* n 1024 1024))


;;
;; Tests
;;

(deftest test-inserting-into-capped-collection
  (let [n     1000
        cname "cached"
        _     (mc/drop cname)
        coll  (mc/create cname {:capped true :size (-> 16 megabytes) :max n})]
    (is (= cname (.getName coll)))
    (mc/insert-batch cname (for [i (range 0 (+ n 100))] {:i i}))
    (is (= n (mc/count cname)))
    ;; older elements get replaced by newer ones
    (is (not (mc/any? cname {:i 1})))
    (is (not (mc/any? cname {:i 5})))
    (is (not (mc/any? cname {:i 9})))
    (is (mc/any? cname {:i (+ n 80)}))))
