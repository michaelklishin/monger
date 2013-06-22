(set! *warn-on-reflection* true)

(ns monger.test.multi.atomic-modifiers-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject]
            [org.bson.types ObjectId]
            [java.util Date])
  (:require [monger.core        :as mg]
            [monger core util]
            [monger.multi.collection :as mgcol]
            [monger.result     :as mgres]
            [monger.test.helper :as helper])
  (:use [clojure.test]
        [monger.operators]
        [monger.test.fixtures]))

(helper/connect!)

(defn drop-altdb
  [f]
  (mg/drop-db "altdb")
  (f))

(use-fixtures :each drop-altdb)

;;
;; $inc
;;

(deftest increment-a-single-existing-field-using-$inc-modifier
  (let [db (mg/get-db "altdb")
        coll "scores"
        oid  (ObjectId.)]
    (mgcol/insert db coll { :_id oid :username "l33r0y" :score 100 })
    (mgcol/update db coll { :_id oid } { $inc { :score 20 } })
    (is (= 120 (:score (mgcol/find-map-by-id db coll oid))))))

(deftest set-a-single-non-existing-field-using-$inc-modifier
  (let [db (mg/get-db "altdb")
        coll "scores"
        oid  (ObjectId.)]
    (mgcol/insert db coll { :_id oid :username "l33r0y" })
    (mgcol/update db coll { :_id oid } { $inc { :score 30 } })
    (is (= 30 (:score (mgcol/find-map-by-id db coll oid))))))


(deftest increment-multiple-existing-fields-using-$inc-modifier
  (let [db (mg/get-db "altdb")
        coll "scores"
        oid  (ObjectId.)]
    (mgcol/insert db coll { :_id oid :username "l33r0y" :score 100 :bonus 0 })
    (mgcol/update db coll { :_id oid } {$inc { :score 20 :bonus 10 } })
    (is (= { :_id oid :score 120 :bonus 10 :username "l33r0y" } (mgcol/find-map-by-id db coll oid)))))


(deftest increment-and-set-multiple-existing-fields-using-$inc-modifier
  (let [db (mg/get-db "altdb")
        coll "scores"
        oid  (ObjectId.)]
    (mgcol/insert db coll { :_id oid :username "l33r0y" :score 100 })
    (mgcol/update db coll { :_id oid } { $inc { :score 20 :bonus 10 } })
    (is (= { :_id oid :score 120 :bonus 10 :username "l33r0y" } (mgcol/find-map-by-id db coll oid)))))



;;
;; $set
;;

(deftest update-a-single-existing-field-using-$set-modifier
  (let [db (mg/get-db "altdb")
        coll "things"
        oid  (ObjectId.)]
    (mgcol/insert db coll { :_id oid :weight 10.0 })
    (mgcol/update db coll { :_id oid } { $set { :weight 20.5 } })
    (is (= 20.5 (:weight (mgcol/find-map-by-id db coll oid [:weight]))))))

(deftest set-a-single-non-existing-field-using-$set-modifier
  (let [db (mg/get-db "altdb")
        coll "things"
        oid  (ObjectId.)]
    (mgcol/insert db coll { :_id oid :weight 10.0 })
    (mgcol/update db coll { :_id oid } { $set { :height 17.2 } })
    (is (= 17.2 (:height (mgcol/find-map-by-id db coll oid [:height]))))))

(deftest update-multiple-existing-fields-using-$set-modifier
  (let [db (mg/get-db "altdb")
        coll "things"
        oid  (ObjectId.)]
    (mgcol/insert db coll { :_id oid :weight 10.0 :height 15.2 })
    (mgcol/update db coll { :_id oid } { $set { :weight 20.5 :height 25.6 } })
    (is (= { :_id oid :weight 20.5 :height 25.6 } (mgcol/find-map-by-id db coll oid [:weight :height])))))


(deftest update-and-set-multiple-fields-using-$set-modifier
  (let [db (mg/get-db "altdb")
        coll "things"
        oid  (ObjectId.)]
    (mgcol/insert db coll { :_id oid :weight 10.0 })
    (mgcol/update db coll { :_id oid } {$set { :weight 20.5 :height 25.6 } })
    (is (= { :_id oid :weight 20.5 :height 25.6 } (mgcol/find-map-by-id db coll oid [:weight :height])))))


;;
;; $unset
;;

(deftest unset-a-single-existing-field-using-$unset-modifier
  (let [db (mg/get-db "altdb")
        coll "docs"
        oid  (ObjectId.)]
    (mgcol/insert db coll { :_id oid :title "Document 1" :published true })
    (mgcol/update db coll { :_id oid } { $unset { :published 1 } })
    (is (= { :_id oid :title "Document 1" } (mgcol/find-map-by-id db coll oid)))))


(deftest unset-multiple-existing-fields-using-$unset-modifier
  (let [db (mg/get-db "altdb")
        coll "docs"
        oid  (ObjectId.)]
    (mgcol/insert db coll { :_id oid :title "Document 1" :published true :featured true })
    (mgcol/update db coll { :_id oid } { $unset { :published 1 :featured true } })
    (is (= { :_id oid :title "Document 1" } (mgcol/find-map-by-id db coll oid)))))


(deftest unsetting-an-unexisting-field-using-$unset-modifier-is-not-considered-an-issue
  (let [db (mg/get-db "altdb")
        coll "docs"
        oid  (ObjectId.)]
    (mgcol/insert db coll { :_id oid :title "Document 1" :published true })
    (is (mgres/ok? (mgcol/update db coll { :_id oid } { $unset { :published 1 :featured true } })))
    (is (= { :_id oid :title "Document 1" } (mgcol/find-map-by-id db coll oid)))))

;;
;; $setOnInsert
;;

(deftest setOnInsert-in-upsert-for-non-existing-document
  (let [db (mg/get-db "altdb")
        coll "docs"
        now  456
        oid  (ObjectId.)]
    (mgcol/find-and-modify db coll {:_id oid} {$set {:lastseen now} $setOnInsert {:firstseen now}} :upsert true)
    (is (= { :_id oid :lastseen now :firstseen now} (mgcol/find-map-by-id db coll oid)))))

(deftest setOnInsert-in-upsert-for-existing-document
  (let [db (mg/get-db "altdb")
        coll   "docs"
        before 123
        now    456
        oid    (ObjectId.)]
    (mgcol/insert db coll { :_id oid :firstseen before :lastseen before})
    (mgcol/find-and-modify db coll {:_id oid} {$set {:lastseen now} $setOnInsert {:firstseen now}} :upsert true)
    (is (= { :_id oid :lastseen now :firstseen before} (mgcol/find-map-by-id db coll oid)))))

;;
;; $push
;;

(deftest initialize-an-array-using-$push-modifier
  (let [db (mg/get-db "altdb")
        coll  "docs"
        oid   (ObjectId.)
        title "$push modifier appends value to field"]
    (mgcol/insert db coll { :_id oid :title title })
    (mgcol/update db coll { :_id oid } { $push { :tags "modifiers" } })
    (is (= { :_id oid :title title :tags ["modifiers"] } (mgcol/find-map-by-id db coll oid)))))

(deftest add-value-to-an-existing-array-using-$push-modifier
  (let [db (mg/get-db "altdb")
        coll  "docs"
        oid   (ObjectId.)
        title "$push modifier appends value to field"]
    (mgcol/insert db coll { :_id oid :title title :tags ["mongodb"] })
    (mgcol/update db coll { :_id oid } { $push { :tags "modifiers" } })
    (is (= { :_id oid :title title :tags ["mongodb" "modifiers"] } (mgcol/find-map-by-id db coll oid)))))


;; this is a common mistake, I leave it here to demonstrate it. You almost never
;; actually want to do this! What you really want is to use $pushAll instead of $push. MK.
(deftest add-array-value-to-an-existing-array-using-$push-modifier
  (let [db (mg/get-db "altdb")
        coll  "docs"
        oid   (ObjectId.)
        title "$push modifier appends value to field"]
    (mgcol/insert db coll { :_id oid :title title :tags ["mongodb"] })
    (mgcol/update db coll { :_id oid } { $push { :tags ["modifiers" "operators"] } })
    (is (= { :_id oid :title title :tags ["mongodb" ["modifiers" "operators"]] } (mgcol/find-map-by-id db coll oid)))))



(deftest double-add-value-to-an-existing-array-using-$push-modifier
  (let [db (mg/get-db "altdb")
        coll  "docs"
        oid   (ObjectId.)
        title "$push modifier appends value to field"]
    (mgcol/insert db coll { :_id oid :title title :tags ["mongodb"] })
    (mgcol/update db coll { :_id oid } { $push { :tags "modifiers" } })
    (mgcol/update db coll { :_id oid } { $push { :tags "modifiers" } })
    (is (= { :_id oid :title title :tags ["mongodb" "modifiers" "modifiers"] } (mgcol/find-map-by-id db coll oid)))))

;;
;; $pushAll
;;

(deftest initialize-an-array-using-$pushAll-modifier
  (let [db (mg/get-db "altdb")
        coll  "docs"
        oid   (ObjectId.)
        title "$pushAll modifier appends multiple values to field"]
    (mgcol/insert db coll { :_id oid :title title })
    (mgcol/update db coll { :_id oid } { $pushAll { :tags ["mongodb" "docs"] } })
    (is (= { :_id oid :title title :tags ["mongodb" "docs"] } (mgcol/find-map-by-id db coll oid)))))

(deftest add-value-to-an-existing-array-using-$pushAll-modifier
  (let [db (mg/get-db "altdb")
        coll  "docs"
        oid   (ObjectId.)
        title "$pushAll modifier appends multiple values to field"]
    (mgcol/insert db coll { :_id oid :title title :tags ["mongodb"] })
    (mgcol/update db coll { :_id oid } { $pushAll { :tags ["modifiers" "docs"] } })
    (is (= { :_id oid :title title :tags ["mongodb" "modifiers" "docs"] } (mgcol/find-map-by-id db coll oid)))))


(deftest double-add-value-to-an-existing-array-using-$pushAll-modifier
  (let [db (mg/get-db "altdb")
        coll  "docs"
        oid   (ObjectId.)
        title "$pushAll modifier appends multiple values to field"]
    (mgcol/insert db coll { :_id oid :title title :tags ["mongodb" "docs"] })
    (mgcol/update db coll { :_id oid } { $pushAll { :tags ["modifiers" "docs"] } })
    (is (= { :_id oid :title title :tags ["mongodb" "docs" "modifiers" "docs"] } (mgcol/find-map-by-id db coll oid)))))


;;
;; $addToSet
;;

(deftest initialize-an-array-using-$addToSet-modifier
  (let [db (mg/get-db "altdb")
        coll  "docs"
        oid   (ObjectId.)
        title "$addToSet modifier appends value to field unless it is already there"]
    (mgcol/insert db coll { :_id oid :title title })
    (mgcol/update db coll { :_id oid } { $addToSet { :tags "modifiers" } })
    (is (= { :_id oid :title title :tags ["modifiers"] } (mgcol/find-map-by-id db coll oid)))))

(deftest add-value-to-an-existing-array-using-$addToSet-modifier
  (let [db (mg/get-db "altdb")
        coll  "docs"
        oid   (ObjectId.)
        title "$addToSet modifier appends value to field unless it is already there"]
    (mgcol/insert db coll { :_id oid :title title :tags ["mongodb"] })
    (mgcol/update db coll { :_id oid } { $addToSet { :tags "modifiers" } })
    (is (= { :_id oid :title title :tags ["mongodb" "modifiers"] } (mgcol/find-map-by-id db coll oid)))))


(deftest double-add-value-to-an-existing-array-using-$addToSet-modifier
  (let [db (mg/get-db "altdb")
        coll  "docs"
        oid   (ObjectId.)
        title "$addToSet modifier appends value to field unless it is already there"]
    (mgcol/insert db coll { :_id oid :title title :tags ["mongodb"] })
    (mgcol/update db coll { :_id oid } { $addToSet { :tags "modifiers" } })
    (mgcol/update db coll { :_id oid } { $addToSet { :tags "modifiers" } })
    (is (= { :_id oid :title title :tags ["mongodb" "modifiers"] } (mgcol/find-map-by-id db coll oid)))))


;;
;; $pop
;;

(deftest pop-last-value-in-the-array-using-$pop-modifier
  (let [db (mg/get-db "altdb")
        coll   "docs"
        oid    (ObjectId.)
        title "$pop modifier removes last or first value in the array"]
    (mgcol/insert db coll { :_id oid :title title :tags ["products" "apple" "reviews"] })
    (mgcol/update db coll { :_id oid } { $pop { :tags 1 } })
    (is (= { :_id oid :title title :tags ["products" "apple"] } (mgcol/find-map-by-id db coll oid)))))

(deftest unshift-first-value-in-the-array-using-$pop-modifier
  (let [db (mg/get-db "altdb")
        coll   "docs"
        oid    (ObjectId.)
        title "$pop modifier removes last or first value in the array"]
    (mgcol/insert db coll { :_id oid :title title :tags ["products" "apple" "reviews"] })
    (mgcol/update db coll { :_id oid } { $pop { :tags -1 } })
    (is (= { :_id oid :title title :tags ["apple" "reviews"] } (mgcol/find-map-by-id db coll oid)))))

(deftest pop-last-values-from-multiple-arrays-using-$pop-modifier
  (let [db (mg/get-db "altdb")
        coll   "docs"
        oid    (ObjectId.)
        title "$pop modifier removes last or first value in the array"]
    (mgcol/insert db coll { :_id oid :title title :tags ["products" "apple" "reviews"] :categories ["apple" "reviews" "drafts"] })
    (mgcol/update db coll { :_id oid } { $pop { :tags 1 :categories 1 } })
    (is (= { :_id oid :title title :tags ["products" "apple"] :categories ["apple" "reviews"] } (mgcol/find-map-by-id db coll oid)))))


;;
;; $pull
;;

(deftest remove-all-value-entries-from-array-using-$pull-modifier
  (let [db (mg/get-db "altdb")
        coll   "docs"
        oid    (ObjectId.)
        title "$pull modifier removes all value entries in the array"]
    (mgcol/insert db coll { :_id oid :title title :measurements [1.0 1.2 1.2 1.2 1.1 1.1 1.2 1.3 1.0] })
    (mgcol/update db coll { :_id oid } { $pull { :measurements 1.2 } })
    (is (= { :_id oid :title title :measurements [1.0 1.1 1.1 1.3 1.0] } (mgcol/find-map-by-id db coll oid)))))

(deftest remove-all-value-entries-from-array-using-$pull-modifier-based-on-a-condition
  (let [db (mg/get-db "altdb")
        coll   "docs"
        oid    (ObjectId.)
        title "$pull modifier removes all value entries in the array"]
    (mgcol/insert db coll { :_id oid :title title :measurements [1.0 1.2 1.2 1.2 1.1 1.1 1.2 1.3 1.0] })
    (mgcol/update db coll { :_id oid } { $pull { :measurements { $gte 1.2 } } })
    (is (= { :_id oid :title title :measurements [1.0 1.1 1.1 1.0] } (mgcol/find-map-by-id db coll oid)))))
;;
;; $pullAll
;;

(deftest remove-all-value-entries-from-array-using-$pullAll-modifier
  (let [db (mg/get-db "altdb")
        coll   "docs"
        oid    (ObjectId.)
        title "$pullAll modifier removes entries of multiple values in the array"]
    (mgcol/insert db coll { :_id oid :title title :measurements [1.0 1.2 1.2 1.2 1.1 1.1 1.2 1.3 1.0] })
    (mgcol/update db coll { :_id oid } { $pullAll { :measurements [1.0 1.1 1.2] } })
    (is (= { :_id oid :title title :measurements [1.3] } (mgcol/find-map-by-id db coll oid)))))


;;
;; $rename
;;

(deftest rename-a-single-field-using-$rename-modifier
  (let [db (mg/get-db "altdb")
        coll   "docs"
        oid    (ObjectId.)
        title "$rename renames fields"
        v     [1.0 1.2 1.2 1.2 1.1 1.1 1.2 1.3 1.0]]
    (mgcol/insert db coll { :_id oid :title title :measurements v })
    (mgcol/update db coll { :_id oid } { $rename { :measurements "results" } })
    (is (= { :_id oid :title title :results v } (mgcol/find-map-by-id db coll oid)))))


;;
;; find-and-modify
;;

(deftest find-and-modify-a-single-document
  (let [db (mg/get-db "altdb")
        coll "docs"
        oid (ObjectId.)
        doc {:_id oid :name "Sophie Bangs" :level 42}
        conditions {:name "Sophie Bangs"}
        update {$inc {:level 1}}]
    (mgcol/insert db coll doc)
    (let [res (mgcol/find-and-modify db coll conditions update :return-new true)]
      (is (= (select-keys res [:name :level]) {:name "Sophie Bangs" :level 43})))))


(deftest find-and-modify-remove-a-document
  (let [db (mg/get-db "altdb")
        coll "docs"
        oid (ObjectId.)
        doc {:_id oid :name "Sophie Bangs" :level 42}
        conditions {:name "Sophie Bangs"}]
    (mgcol/insert db coll doc)
    (let [res (mgcol/find-and-modify db coll conditions {} :remove true)]
      (is (= (select-keys res [:name :level]) {:name "Sophie Bangs" :level 42}))
      (is (empty? (mgcol/find-maps db coll conditions))))))


(deftest find-and-modify-upsert-a-document
  (testing "case 1"
    (let [db (mg/get-db "altdb")
          coll "docs"
          oid (ObjectId.)
          doc {:_id oid :name "Sophie Bangs" :level 42}]
      (let [res (mgcol/find-and-modify db coll doc doc :upsert true)]
        (is (empty? res))
        (is (select-keys (mgcol/find-map-by-id db coll oid) [:name :level]) (dissoc doc :_id)))))
  (testing "case 2"
    (let [db (mg/get-db "altdb")
          coll  "docs"
          query {:name "Sophie Bangs"}
          doc   (merge query {:level 42})]
      (let [res (mgcol/find-and-modify db coll query doc :upsert true :return-new true)]
        (is (:_id res))
        (is (select-keys (mgcol/find-map-by-id db coll (:_id res)) [:name :level]) doc)))))


(deftest find-and-modify-after-sort
  (let [db (mg/get-db "altdb")
        coll "docs"
        oid (ObjectId.)
        oid2 (ObjectId.)
        doc {:name "Sophie Bangs"}
        doc1 (assoc doc :_id oid :level 42)
        doc2 (assoc doc :_id oid2 :level 0)]
    (mgcol/insert-batch db coll [doc1 doc2])
    (let [res (mgcol/find-and-modify db coll doc {$inc {:level 1}} :sort {:level -1})]
      (is (= (select-keys res [:name :level]) {:name "Sophie Bangs" :level 42})))))
