(ns monger.test.result
  (:import (com.mongodb BasicDBObject WriteResult WriteConcern))
  (:require [monger core collection convertion])
  (:use [clojure.test]))


;;
;; MongoCommandResult
;;


(deftest test-ok?
  (let [result-that-is-not-ok-1 (doto (BasicDBObject.) (.put "ok" 0))
        result-that-is-not-ok-2 (doto (BasicDBObject.) (.put "ok" "false"))
        result-that-is-ok-1     (doto (BasicDBObject.) (.put "ok" 1))
        result-that-is-ok-2     (doto (BasicDBObject.) (.put "ok" "true"))
        result-that-is-ok-3     (doto (BasicDBObject.) (.put "ok" 1.0))]
    (is (not (monger.result/ok? result-that-is-not-ok-1)))
    (is (not (monger.result/ok? result-that-is-not-ok-2)))
    (is (monger.result/ok? result-that-is-ok-1))
    (is (monger.result/ok? result-that-is-ok-2))
    (is (monger.result/ok? result-that-is-ok-3))))


(deftest test-has-error?
  (let [result-that-has-no-error1 (doto (BasicDBObject.) (.put "ok" 0))
        result-that-has-no-error2 (doto (BasicDBObject.) (.put "err" ""))
        result-that-has-error1    (doto (BasicDBObject.) (.put "err" (BasicDBObject.)))]
        (is (not (monger.result/has-error? result-that-has-no-error1)))
        (is (not (monger.result/has-error? result-that-has-no-error2)))
        (is (monger.result/has-error?      result-that-has-error1))))
