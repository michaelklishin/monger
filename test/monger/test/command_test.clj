(ns monger.test.command-test
  (:require [monger.command     :as mcom]
            [monger.test.helper :as helper]
            [monger.collection  :as mc])
  (:use clojure.test
        monger.result))

(helper/connect!)


(deftest test-db-stats
  (let [stats (mcom/db-stats)]
    (is (ok? stats))
    (is (= "monger-test" (get stats "db")))))

(deftest test-collection-stats
  (let [collection "stat_test"
        _          (mc/insert collection {:name "Clojure"})
        check      (mc/count collection)
        stats      (mcom/collection-stats collection)]  
    (is (ok? stats))
    (is (= "monger-test.stat_test" (get stats "ns")))
    (is (= check (get stats "count")))))

(deftest test-reindex-collection
  (let [_      (mc/insert "test" {:name "Clojure"})
        result (mcom/reindex-collection "test")]
    (is (ok? result))
    (is (get result "indexes"))))

(deftest test-server-status
  (let [status (mcom/server-status)]
    (is (ok? status))
    (is (not-empty status))
    (is (get status "serverUsed"))))

(deftest test-top
  (let [result (mcom/top)]
    (is (ok? result))
    (is (not-empty result))
    (is (get result "serverUsed"))))
