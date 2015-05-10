(ns monger.test.command-test
  (:require [monger.core        :as mg]
            [monger.command     :as mcom]
            [monger.collection  :as mc]
            [clojure.test :refer :all]
            [monger.result :refer [acknowledged?]]
            [monger.conversion :refer [from-db-object]]))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (deftest ^{:command true} test-reindex-collection
    (let [_      (mc/insert db "test" {:name "Clojure"})
          result (mcom/reindex-collection db "test")]
      (is (acknowledged? result))))

  (deftest ^{:command true} test-server-status
    (let [status (mcom/server-status db)]
      (is (acknowledged? status))
      (is (not-empty status))))

  (deftest ^{:command true} test-top
    (let [result (mcom/top conn)]
      (is (acknowledged? result))
      (is (not-empty result))))

  (deftest ^{:command true} test-running-is-master-as-an-arbitrary-command
    (let [raw    (mg/command db {:isMaster 1})
          result (from-db-object raw true)]
      (is (acknowledged? raw)))))
