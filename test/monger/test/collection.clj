(ns monger.test.collection
  (:require [monger core collection])
  (:use [clojure.test]))

(deftest get-collection-size
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")]
    (is 0 (monger.collection/count db "things"))))

