(ns monger.test.json-test
  (:require [clojure.test :refer :all]
    [monger.json]
    [clojure.data.json :as json])
  (:import org.bson.types.ObjectId
           org.bson.types.BSONTimestamp))

(deftest convert-dbobject-to-json
  (let [input (ObjectId.)
        output (json/write-str input)]
    (is (= (str "\"" input "\"") output))))

(deftest convert-bson-timestamp-to-json
  (let [input (BSONTimestamp. 123 4)
        output (json/write-str input)]
    (is (= "{\"time\":123,\"inc\":4}" output))))
