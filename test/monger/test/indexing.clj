(ns monger.test.map-reduce
  (:import  org.bson.types.ObjectId
            java.util.Date)
  (:require [monger core util]
            [monger.collection :as mgcol]
            [monger.result     :as mgres]
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
    (mgcol/drop-indexes collection)
    (is (= "_id_"
           (:name (first (mgcol/indexes-on collection)))))
    (is (nil? (second (mgcol/indexes-on collection))))
    (mgcol/create-index collection { "language" 1 })
    (is (= "language_1"
           (:name (second (mgcol/indexes-on collection)))))
    (mgcol/drop-index collection "language_1")
    (is (nil? (second (mgcol/indexes-on collection))))
    (mgcol/ensure-index collection { "language" 1 } {:unique true})
    (is (= "language_1"
           (:name (second (mgcol/indexes-on collection)))))
    (mgcol/ensure-index collection { "language" 1 })
    (mgcol/ensure-index collection { "language" 1 } { :unique true })
    (mgcol/drop-indexes collection)))
