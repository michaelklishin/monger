(ns monger.test.command-test
  (:require [monger.core        :as mg]
            [monger.command     :as mcom]
            [monger.test.helper :as helper]
            [monger.collection  :as mc])
  (:use clojure.test
        [monger.result :only [ok?]]
        [monger.conversion :only [from-db-object]]))

(helper/connect!)


(deftest ^{:command true}  test-db-stats
  (let [stats (mcom/db-stats)]
    (is (ok? stats))
    (is (= "monger-test" (get stats "db")))))

(deftest ^{:command true} test-collection-stats
  (let [collection "stat_test"
        _          (mc/insert collection {:name "Clojure"})
        check      (mc/count collection)
        stats      (mcom/collection-stats collection)]  
    (is (ok? stats))
    (is (= "monger-test.stat_test" (get stats "ns")))
    (is (= check (get stats "count")))))

(deftest ^{:command true} test-reindex-collection
  (let [_      (mc/insert "test" {:name "Clojure"})
        result (mcom/reindex-collection "test")]
    (is (ok? result))
    (is (get result "indexes"))))

(deftest ^{:command true} test-server-status
  (let [status (mcom/server-status)]
    (is (ok? status))
    (is (not-empty status))
    (is (get status "serverUsed"))))

(deftest ^{:command true} test-top
  (let [result (mcom/top)]
    (is (ok? result))
    (is (not-empty result))
    (is (get result "serverUsed"))))

(deftest ^{:command true} test-running-is-master-as-an-arbitrary-command
  (let [raw    (mg/command {:isMaster 1})
        result (from-db-object raw true)]
    (is (ok? result))
    (is (ok? raw))
    (is (:ismaster result))))
