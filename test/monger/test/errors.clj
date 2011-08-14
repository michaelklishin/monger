(ns monger.test.errors
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
    (is (not (monger.errors/ok? result-that-is-not-ok-1)))
    (is (not (monger.errors/ok? result-that-is-not-ok-2)))
    (is (monger.errors/ok? result-that-is-ok-1))
    (is (monger.errors/ok? result-that-is-ok-2))
    (is (monger.errors/ok? result-that-is-ok-3))))

