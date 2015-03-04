(ns monger.test.json-cheshire-test
  (:require [clojure.test :refer :all]
    [monger.json]
    [cheshire.core :refer :all])
  (:import org.bson.types.ObjectId
           org.bson.types.BSONTimestamp))

(deftest convert-dbobject-to-json
  (let [input (ObjectId.)
        output (generate-string input)]
    (is (= (str "\"" input "\"") output))))

(deftest convert-bson-timestamp-to-json
  (let [input (BSONTimestamp. 123 4)
        output (generate-string input)]
    (is (= "{\"time\":123,\"inc\":4}" output))))
