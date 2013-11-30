(ns monger.test.util-test
  (:import com.mongodb.DBObject)
  (:require [monger util conversion]
            [clojure.test :refer :all]))


(deftest get-object-id
  (let [clj-map   { :_id (monger.util/object-id) }
        db-object ^DBObject (monger.conversion/to-db-object clj-map)
        _id       (:_id clj-map)]
    (is (= _id (monger.util/get-id clj-map)))
    (is (= _id (monger.util/get-id db-object)))))


