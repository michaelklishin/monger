(ns monger.test.util
  (:import (com.mongodb DBObject))
  (:require [monger util conversion])
  (:use [clojure.test]))


(deftest get-object-id
  (let [id     ^ObjectId (monger.util/object-id)
        input  ^DBObject (monger.conversion/to-db-object { :_id id })
        output (monger.util/get-id input)]
    (is (not (nil? output)))))


