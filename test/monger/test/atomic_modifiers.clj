(set! *warn-on-reflection* true)

(ns monger.test.atomic-modifiers
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject CommandResult$CommandFailure]
            [org.bson.types ObjectId])
  (:require [monger core util]
            [monger.collection :as mgcol]
            [monger.result     :as mgres])
  (:use [clojure.test]
        [monger.test.fixtures]))


(defn purge-scores-collection
  [f]
  (purge-collection "scores" f))


(use-fixtures :each purge-docs-collection purge-things-collection purge-scores-collection)

(monger.core/set-default-write-concern! WriteConcern/SAFE)


;;
;; $inc
;;

(deftest increment-a-single-existing-field-using-$inc-modifier
  (let [coll "scores"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :username "l33r0y" :score 100 })
    (mgcol/update coll { :_id oid } { "$inc" { :score 20 } })
    (is (= 120 (:score (mgcol/find-map-by-id coll oid))))))


(deftest set-a-single-non-existing-field-using-$inc-modifier
  (let [coll "scores"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :username "l33r0y" })
    (mgcol/update coll { :_id oid } { "$inc" { :score 30 } })
    (is (= 30 (:score (mgcol/find-map-by-id coll oid))))))


(deftest increment-multiple-existing-fields-using-$inc-modifier
  (let [coll "scores"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :username "l33r0y" :score 100 :bonus 0 })
    (mgcol/update coll { :_id oid } { "$inc" { :score 20 :bonus 10 } })
    (is (= { :_id oid :score 120 :bonus 10 :username "l33r0y" } (mgcol/find-map-by-id coll oid)))))


(deftest increment-and-set-multiple-existing-fields-using-$inc-modifier
  (let [coll "scores"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :username "l33r0y" :score 100 })
    (mgcol/update coll { :_id oid } { "$inc" { :score 20 :bonus 10 } })
    (is (= { :_id oid :score 120 :bonus 10 :username "l33r0y" } (mgcol/find-map-by-id coll oid)))))



;;
;; $set
;;

(deftest update-a-single-existing-field-using-$set-modifier
  (let [coll "things"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :weight 10.0 })
    (mgcol/update coll { :_id oid } { "$set" { :weight 20.5 } })
    (is (= 20.5 (:weight (mgcol/find-map-by-id coll oid [:weight]))))))

(deftest set-a-single-non-existing-field-using-$set-modifier
  (let [coll "things"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :weight 10.0 })
    (mgcol/update coll { :_id oid } { "$set" { :height 17.2 } })
    (is (= 17.2 (:height (mgcol/find-map-by-id coll oid [:height]))))))

(deftest update-multiple-existing-fields-using-$set-modifier
  (let [coll "things"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :weight 10.0 :height 15.2 })
    (mgcol/update coll { :_id oid } { "$set" { :weight 20.5 :height 25.6 } })
    (is (= { :_id oid :weight 20.5 :height 25.6 } (mgcol/find-map-by-id coll oid [:weight])))))


(deftest update-and-set-multiple-fields-using-$set-modifier
  (let [coll "things"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :weight 10.0 })
    (mgcol/update coll { :_id oid } { "$set" { :weight 20.5 :height 25.6 } })
    (is (= { :_id oid :weight 20.5 :height 25.6 } (mgcol/find-map-by-id coll oid [:weight])))))


;;
;; $unset
;;

(deftest unset-a-single-existing-field-using-$unset-modifier
  (let [coll "docs"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :title "Document 1" :published true })
    (mgcol/update coll { :_id oid } { "$unset" { :published 1 } })
    (is (= { :_id oid :title "Document 1" } (mgcol/find-map-by-id coll oid)))))


(deftest unset-multiple-existing-fields-using-$unset-modifier
  (let [coll "docs"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :title "Document 1" :published true :featured true })
    (mgcol/update coll { :_id oid } { "$unset" { :published 1 :featured true } })
    (is (= { :_id oid :title "Document 1" } (mgcol/find-map-by-id coll oid)))))


(deftest unsetting-an-unexisting-field-using-$unset-modifier-is-not-considered-an-issue
  (let [coll "docs"
        oid  (ObjectId.)]
    (mgcol/insert coll { :_id oid :title "Document 1" :published true })
    (is (mgres/ok? (mgcol/update coll { :_id oid } { "$unset" { :published 1 :featured true } })))
    (is (= { :_id oid :title "Document 1" } (mgcol/find-map-by-id coll oid)))))
