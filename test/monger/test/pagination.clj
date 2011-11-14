(ns monger.test.pagination
  (:use [clojure.test]
        [monger.pagination]))

(deftest test-pagination-offset
  (are [a b] (= a b)
       0 (offset-for 1 20)
       0 (offset-for 1 30)
       10 (offset-for 2 10)
       13 (offset-for 2 13)
       20 (offset-for 3 10)
       22 (offset-for 3 11)
       21 (offset-for 4 7)
       39 (offset-for 4 13)))
