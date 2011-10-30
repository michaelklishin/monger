(set! *warn-on-reflection* true)

(ns monger.test.atomic-modifiers
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject CommandResult$CommandFailure]
            [org.bson.types ObjectId])
  (:require [monger core util]
            [monger.collection :as mgcol]
            [monger.result     :as mgres])
  (:use [clojure.test]
        [monger.test.fixtures]))


(use-fixtures :each purge-docs-collection purge-things-collection)

(monger.core/set-default-write-concern! WriteConcern/SAFE)



;;
;; $set
;;

(deftest update-a-single-existing-field-using-$set-operator
  (let [coll "things"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :weight 10.0 })
    (mgcol/update coll { :_id oid } { "$set" { :weight 20.5 } })
    (is (= 20.5 (:weight (mgcol/find-map-by-id coll oid [:weight]))))))

(deftest set-a-single-non-existing-field-using-$set-operator
  (let [coll "things"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :weight 10.0 })
    (mgcol/update coll { :_id oid } { "$set" { :height 17.2 } })
    (is (= 17.2 (:height (mgcol/find-map-by-id coll oid [:height]))))))

(deftest update-multiple-existing-fields-using-$set-operator
  (let [coll "things"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :weight 10.0 :height 15.2 })
    (mgcol/update coll { :_id oid } { "$set" { :weight 20.5 :height 25.6 } })
    (is (= { :_id oid :weight 20.5 :height 25.6 } (mgcol/find-map-by-id coll oid [:weight])))))


(deftest update-and-set-multiple-fields-using-$set-operator
  (let [coll "things"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :weight 10.0 })
    (mgcol/update coll { :_id oid } { "$set" { :weight 20.5 :height 25.6 } })
    (is (= { :_id oid :weight 20.5 :height 25.6 } (mgcol/find-map-by-id coll oid [:weight])))))
