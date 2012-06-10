(ns monger.test.indexing-test
  (:import  org.bson.types.ObjectId
            java.util.Date)
  (:require [monger core util]
            [monger.collection :as mc]
            [monger.test.helper :as helper])
  (:use clojure.test
        [monger operators conversion]
        monger.test.fixtures))

(helper/connect!)


;;
;; indexes
;;

(deftest ^{:indexing true} test-creating-and-dropping-indexes
  (let [collection "libraries"]
    (mc/drop-indexes collection)
    (mc/create-index collection { "language" 1 })
    (is (= "language_1"
           (:name (second (mc/indexes-on collection)))))
    (mc/drop-index collection "language_1")
    (mc/create-index collection ["language"])
    (mc/drop-index collection "language_1")
    (is (nil? (second (mc/indexes-on collection))))
    (mc/ensure-index collection { "language" 1 } {:unique true})
    (is (= "language_1"
           (:name (second (mc/indexes-on collection)))))
    (mc/ensure-index collection { "language" 1 })
    (mc/ensure-index collection { "language" 1 } { :unique true })
    (mc/drop-indexes collection)))
