(ns monger.test.command-test
  (:require [monger core command]
            [monger.test.helper :as helper]
            [monger.collection :as mgcol])
  (:import (com.mongodb Mongo DB CommandResult))
  (:use clojure.test))

(helper/connect!)


(deftest test-db-stats
  (let [stats (monger.command/db-stats)]
    (is (monger.result/ok? stats))
    (is (= "monger-test" (get stats "db")))))

(deftest test-collection-stats
  (let [collection "stat_test"
        _          (mgcol/insert collection { :name "Clojure" })
        check      (mgcol/count collection)
        stats      (monger.command/collection-stats collection)]  
    (is (monger.result/ok? stats))
    (is (= "monger-test.stat_test" (get stats "ns")))
    (is (= check (get stats "count")))))


(deftest test-reindex-collection
  (let [_      (mgcol/insert "test" { :name "Clojure" })
        result (monger.command/reindex-collection "test")]
    (is (monger.result/ok? result))
    (is (get result "indexes"))))


(deftest test-server-status
  (let [status (monger.command/server-status)]
    (is (monger.result/ok? status))
    (is (not-empty status))
    (is (get status "serverUsed"))))


(deftest test-top
  (let [result (monger.command/top)]
    (is (monger.result/ok? result))
    (is (not-empty result))
    (is (get result "serverUsed"))))
