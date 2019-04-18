(ns monger.test.atomic-modifiers-test
  (:import  [com.mongodb WriteResult WriteConcern DBObject]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.result :refer [acknowledged?]]
            [clojure.test :refer :all]
            [monger.operators :refer :all]))


(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]

  (defn purge-collections
    [f]
    (mc/remove db "docs")
    (mc/remove db "things")
    (mc/remove db "scores")
    (f)
    (mc/remove db "docs")
    (mc/remove db "things")
    (mc/remove db "scores"))

  (use-fixtures :each purge-collections)

  ;;
  ;; $inc
  ;;

  (deftest increment-a-single-existing-field-using-$inc-modifier
    (let [coll "scores"
          oid  (ObjectId.)]
      (mc/insert db coll {:_id oid :username "l33r0y" :score 100})
      (mc/update db coll {:_id oid} {$inc {:score 20}})
      (is (= 120 (:score (mc/find-map-by-id db coll oid))))))

  (deftest set-a-single-non-existing-field-using-$inc-modifier
    (let [coll "scores"
          oid  (ObjectId.)]
      (mc/insert db coll {:_id oid :username "l33r0y"})
      (mc/update db coll {:_id oid} {$inc {:score 30}})
      (is (= 30 (:score (mc/find-map-by-id db coll oid))))))


  (deftest increment-multiple-existing-fields-using-$inc-modifier
    (let [coll "scores"
          oid  (ObjectId.)]
      (mc/insert db coll {:_id oid :username "l33r0y" :score 100 :bonus 0})
      (mc/update db coll {:_id oid} {$inc {:score 20 :bonus 10}})
      (is (= {:_id oid :score 120 :bonus 10 :username "l33r0y"}
             (mc/find-map-by-id db coll oid)))))


  (deftest increment-and-set-multiple-existing-fields-using-$inc-modifier
    (let [coll "scores"
          oid  (ObjectId.)]
      (mc/insert db coll {:_id oid :username "l33r0y" :score 100})
      (mc/update db coll {:_id oid} {$inc {:score 20 :bonus 10}})
      (is (= {:_id oid :score 120 :bonus 10 :username "l33r0y"}
             (mc/find-map-by-id db coll oid)))))



  ;;
  ;; $set
  ;;

  (deftest update-a-single-existing-field-using-$set-modifier
    (let [coll "things"
          oid  (ObjectId.)]
      (mc/insert db coll {:_id oid :weight 10.0})
      (mc/update db coll {:_id oid} {$set {:weight 20.5}})
      (is (= 20.5 (:weight (mc/find-map-by-id db coll oid [:weight]))))))

  (deftest set-a-single-non-existing-field-using-$set-modifier
    (let [coll "things"
          oid  (ObjectId.)]
      (mc/insert db coll {:_id oid :weight 10.0})
      (mc/update db coll {:_id oid} {$set {:height 17.2}})
      (is (= 17.2 (:height (mc/find-map-by-id db coll oid [:height]))))))

  (deftest update-multiple-existing-fields-using-$set-modifier
    (let [coll "things"
          oid  (ObjectId.)]
      (mc/insert db coll {:_id oid :weight 10.0 :height 15.2})
      (mc/update db coll {:_id oid} {$set {:weight 20.5 :height 25.6}})
      (is (= {:_id oid :weight 20.5 :height 25.6}
             (mc/find-map-by-id db coll oid [:weight :height])))))


  (deftest update-and-set-multiple-fields-using-$set-modifier
    (let [coll "things"
          oid  (ObjectId.)]
      (mc/insert db coll {:_id oid :weight 10.0})
      (mc/update db coll {:_id oid} {$set {:weight 20.5 :height 25.6}})
      (is (= {:_id oid :weight 20.5 :height 25.6}
             (mc/find-map-by-id db coll oid [:weight :height])))))


  ;;
  ;; $unset
  ;;

  (deftest unset-a-single-existing-field-using-$unset-modifier
    (let [coll "docs"
          oid  (ObjectId.)]
      (mc/insert db coll {:_id oid :title "Document 1" :published true})
      (mc/update db coll {:_id oid} {$unset {:published 1}})
      (is (= {:_id oid :title "Document 1"}
             (mc/find-map-by-id db coll oid)))))


  (deftest unset-multiple-existing-fields-using-$unset-modifier
    (let [coll "docs"
          oid  (ObjectId.)]
      (mc/insert db coll {:_id oid :title "Document 1" :published true :featured true})
      (mc/update db coll {:_id oid} {$unset {:published 1 :featured true}})
      (is (= {:_id oid :title "Document 1"}
             (mc/find-map-by-id db coll oid)))))


  (deftest unsetting-an-unexisting-field-using-$unset-modifier-is-not-considered-an-issue
    (let [coll "docs"
          oid  (ObjectId.)]
      (mc/insert db coll {:_id oid :title "Document 1" :published true})
      (is (acknowledged? (mc/update db coll {:_id oid} {$unset {:published 1 :featured true}})))
      (is (= {:_id oid :title "Document 1"}
             (mc/find-map-by-id db coll oid)))))

  ;;
  ;; $setOnInsert
  ;;

  (deftest setOnInsert-in-upsert-for-non-existing-document
    (let [coll "docs"
          now  456
          oid  (ObjectId.)]
      (mc/find-and-modify db coll {:_id oid} {$set {:lastseen now} $setOnInsert {:firstseen now}} {:upsert true})
      (is (= {:_id oid :lastseen now :firstseen now}
             (mc/find-map-by-id db coll oid)))))

  (deftest setOnInsert-in-upsert-for-existing-document
    (let [coll   "docs"
          before 123
          now    456
          oid    (ObjectId.)]
      (mc/insert db coll {:_id oid :firstseen before :lastseen before})
      (mc/find-and-modify db coll {:_id oid} {$set {:lastseen now} $setOnInsert {:firstseen now}} {:upsert true})
      (is (= {:_id oid :lastseen now :firstseen before}
             (mc/find-map-by-id db coll oid)))))

  ;;
  ;; $push
  ;;

  (deftest initialize-an-array-using-$push-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$push modifier appends value to field"]
      (mc/insert db coll {:_id oid :title title})
      (mc/update db coll {:_id oid} {$push {:tags "modifiers"}})
      (is (= {:_id oid :title title :tags ["modifiers"]}
             (mc/find-map-by-id db coll oid)))))

  (deftest add-value-to-an-existing-array-using-$push-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$push modifier appends value to field"]
      (mc/insert db coll {:_id oid :title title :tags ["mongodb"]})
      (mc/update db coll {:_id oid} {$push {:tags "modifiers"}})
      (is (= {:_id oid :title title :tags ["mongodb" "modifiers"]}
             (mc/find-map-by-id db coll oid)))))


  ;; this is a common mistake, I leave it here to demonstrate it. You almost never
  ;; actually want to do this! What you really want is to use $push with $each instead of $push. MK.
  (deftest add-array-value-to-an-existing-array-using-$push-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$push modifier appends value to field"]
      (mc/insert db coll {:_id oid :title title :tags ["mongodb"]})
      (mc/update db coll {:_id oid} {$push {:tags ["modifiers" "operators"]}})
      (is (= {:_id oid :title title :tags ["mongodb" ["modifiers" "operators"]]}
             (mc/find-map-by-id db coll oid)))))



  (deftest double-add-value-to-an-existing-array-using-$push-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$push modifier appends value to field"]
      (mc/insert db coll {:_id oid :title title :tags ["mongodb"]})
      (mc/update db coll {:_id oid} {$push {:tags "modifiers"}})
      (mc/update db coll {:_id oid} {$push {:tags "modifiers"}})
      (is (= {:_id oid :title title :tags ["mongodb" "modifiers" "modifiers"]}
             (mc/find-map-by-id db coll oid)))))

  ;;
  ;; $push $each
  ;;

  (deftest initialize-an-array-using-$push-$each-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$push with $each modifier appends multiple values to field"]
      (mc/insert db coll {:_id oid :title title})
      (mc/update db coll {:_id oid} {$push {:tags {$each ["mongodb" "docs"]}}})
      (is (= {:_id oid :title title :tags ["mongodb" "docs"]}
             (mc/find-map-by-id db coll oid)))))

  (deftest add-values-to-an-existing-array-using-$push-$each-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$push with $each modifier appends multiple values to field"]
      (mc/insert db coll {:_id oid :title title :tags ["mongodb"]})
      (mc/update db coll {:_id oid} {$push {:tags {$each ["modifiers" "docs"]}}})
      (is (= {:_id oid :title title :tags ["mongodb" "modifiers" "docs"]}
             (mc/find-map-by-id db coll oid)))))

  (deftest double-add-value-to-an-existing-array-using-$push-$each-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$push with $each modifier appends multiple values to field"]
      (mc/insert db coll {:_id oid :title title :tags ["mongodb" "docs"]})
      (mc/update db coll {:_id oid} {$push {:tags {$each ["modifiers" "docs"]}}})
      (is (= {:_id oid :title title :tags ["mongodb" "docs" "modifiers" "docs"]}
             (mc/find-map-by-id db coll oid)))))

  ;;
  ;; $push + $each (formerly $pushAll)
  ;;

  (deftest initialize-an-array-using-$push-and-$each-modifiers
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$pushAll modifier appends multiple values to field"]
      (mc/insert db coll {:_id oid :title title})
      (mc/update db coll {:_id oid} {$push {:tags {$each ["mongodb" "docs"]}}})
      (is (= {:_id oid :title title :tags ["mongodb" "docs"]}
             (mc/find-map-by-id db coll oid)))))

  (deftest add-value-to-an-existing-array-using-$push-and-$each-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$pushAll modifier appends multiple values to field"]
      (mc/insert db coll {:_id oid :title title :tags ["mongodb"]})
      (mc/update db coll {:_id oid} {$push {:tags {$each ["modifiers" "docs"]}}})
      (is (= {:_id oid :title title :tags ["mongodb" "modifiers" "docs"]}
             (mc/find-map-by-id db coll oid)))))


  (deftest double-add-value-to-an-existing-array-using-$push-and-$each-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$pushAll modifier appends multiple values to field"]
      (mc/insert db coll {:_id oid :title title :tags ["mongodb" "docs"]})
      (mc/update db coll {:_id oid} {$push {:tags {$each ["modifiers" "docs"]}}})
      (is (= {:_id oid :title title :tags ["mongodb" "docs" "modifiers" "docs"]}
             (mc/find-map-by-id db coll oid)))))


  ;;
  ;; $addToSet
  ;;

  (deftest initialize-an-array-using-$addToSet-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$addToSet modifier appends value to field unless it is already there"]
      (mc/insert db coll {:_id oid :title title})
      (mc/update db coll {:_id oid} {$addToSet {:tags "modifiers"}})
      (is (= {:_id oid :title title :tags ["modifiers"]}
             (mc/find-map-by-id db coll oid)))))

  (deftest add-value-to-an-existing-array-using-$addToSet-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$addToSet modifier appends value to field unless it is already there"]
      (mc/insert db coll {:_id oid :title title :tags ["mongodb"]})
      (mc/update db coll {:_id oid} {$addToSet {:tags "modifiers"}})
      (is (= {:_id oid :title title :tags ["mongodb" "modifiers"]}
             (mc/find-map-by-id db coll oid)))))


  (deftest double-add-value-to-an-existing-array-using-$addToSet-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$addToSet modifier appends value to field unless it is already there"]
      (mc/insert db coll {:_id oid :title title :tags ["mongodb"]})
      (mc/update db coll {:_id oid} {$addToSet {:tags "modifiers"}})
      (mc/update db coll {:_id oid} {$addToSet {:tags "modifiers"}})
      (is (= {:_id oid :title title :tags ["mongodb" "modifiers"]}
             (mc/find-map-by-id db coll oid)))))

  ;;
  ;; $addToSet $each
  ;;

  (deftest initialize-an-array-using-$addToSet-$each-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$addToSet with $each modifier appends multiple values to field unless they are already there"]
      (mc/insert db coll {:_id oid :title title})
      (mc/update db coll {:_id oid} {$addToSet {:tags {$each ["mongodb" "docs"]}}})
      (is (= {:_id oid :title title :tags ["mongodb" "docs"]}
             (mc/find-map-by-id db coll oid)))))

  (deftest add-values-to-an-existing-array-using-$addToSet-$each-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$addToSet with $each modifier appends multiple values to field unless they are already there"]
      (mc/insert db coll {:_id oid :title title :tags ["mongodb"]})
      (mc/update db coll {:_id oid} {$addToSet {:tags {$each ["modifiers" "docs"]}}})
      (is (= {:_id oid :title title :tags ["mongodb" "modifiers" "docs"]}
             (mc/find-map-by-id db coll oid)))))

  (deftest double-add-value-to-an-existing-array-using-$addToSet-$each-modifier
    (let [coll  "docs"
          oid   (ObjectId.)
          title "$addToSet with $each modifier appends multiple values to field unless they are already there"]
      (mc/insert db coll {:_id oid :title title :tags ["mongodb" "docs"]})
      (mc/update db coll {:_id oid} {$addToSet {:tags {$each ["modifiers" "docs" "operators"]}}})
      (is (= {:_id oid :title title :tags ["mongodb" "docs" "modifiers" "operators"]}
             (mc/find-map-by-id db coll oid)))))

  ;;
  ;; $pop
  ;;

  (deftest pop-last-value-in-the-array-using-$pop-modifier
    (let [coll   "docs"
          oid    (ObjectId.)
          title "$pop modifier removes last or first value in the array"]
      (mc/insert db coll {:_id oid :title title :tags ["products" "apple" "reviews"]})
      (mc/update db coll {:_id oid} {$pop {:tags 1}})
      (is (= {:_id oid :title title :tags ["products" "apple"]}
             (mc/find-map-by-id db coll oid)))))

  (deftest unshift-first-value-in-the-array-using-$pop-modifier
    (let [coll   "docs"
          oid    (ObjectId.)
          title "$pop modifier removes last or first value in the array"]
      (mc/insert db coll {:_id oid :title title :tags ["products" "apple" "reviews"]})
      (mc/update db coll {:_id oid} {$pop {:tags -1}})
      (is (= {:_id oid :title title :tags ["apple" "reviews"]}
             (mc/find-map-by-id db coll oid)))))

  (deftest pop-last-values-from-multiple-arrays-using-$pop-modifier
    (let [coll   "docs"
          oid    (ObjectId.)
          title "$pop modifier removes last or first value in the array"]
      (mc/insert db coll {:_id oid :title title :tags ["products" "apple" "reviews"] :categories ["apple" "reviews" "drafts"]})
      (mc/update db coll {:_id oid} {$pop {:tags 1 :categories 1}})
      (is (= {:_id oid :title title :tags ["products" "apple"] :categories ["apple" "reviews"]}
             (mc/find-map-by-id db coll oid)))))


  ;;
  ;; $pull
  ;;

  (deftest remove-all-value-entries-from-array-using-$pull-modifier
    (let [coll   "docs"
          oid    (ObjectId.)
          title "$pull modifier removes all value entries in the array"]
      (mc/insert db coll {:_id oid :title title :measurements [1.0 1.2 1.2 1.2 1.1 1.1 1.2 1.3 1.0]})
      (mc/update db coll {:_id oid} {$pull {:measurements 1.2}})
      (is (= {:_id oid :title title :measurements [1.0 1.1 1.1 1.3 1.0]}
             (mc/find-map-by-id db coll oid)))))

  (deftest remove-all-value-entries-from-array-using-$pull-modifier-based-on-a-condition
    (let [coll   "docs"
          oid    (ObjectId.)
          title "$pull modifier removes all value entries in the array"]
      (mc/insert db coll {:_id oid :title title :measurements [1.0 1.2 1.2 1.2 1.1 1.1 1.2 1.3 1.0]})
      (mc/update db coll {:_id oid} {$pull {:measurements {$gte 1.2}}})
      (is (= {:_id oid :title title :measurements [1.0 1.1 1.1 1.0]}
             (mc/find-map-by-id db coll oid)))))
  ;;
  ;; $pullAll
  ;;

  (deftest remove-all-value-entries-from-array-using-$pullAll-modifier
    (let [coll   "docs"
          oid    (ObjectId.)
          title "$pullAll modifier removes entries of multiple values in the array"]
      (mc/insert db coll {:_id oid :title title :measurements [1.0 1.2 1.2 1.2 1.1 1.1 1.2 1.3 1.0]})
      (mc/update db coll {:_id oid} {$pullAll {:measurements [1.0 1.1 1.2]}})
      (is (= {:_id oid :title title :measurements [1.3]}
             (mc/find-map-by-id db coll oid)))))


  ;;
  ;; $rename
  ;;

  (deftest rename-a-single-field-using-$rename-modifier
    (let [coll   "docs"
          oid    (ObjectId.)
          title "$rename renames fields"
          v     [1.0 1.2 1.2 1.2 1.1 1.1 1.2 1.3 1.0]]
      (mc/insert db coll {:_id oid :title title :measurements v})
      (mc/update db coll {:_id oid} {$rename {:measurements "results"}})
      (is (= {:_id oid :title title :results v}
             (mc/find-map-by-id db coll oid)))))


  ;;
  ;; find-and-modify
  ;;

  (deftest find-and-modify-a-single-document
    (let [coll "docs"
          oid (ObjectId.)
          doc {:_id oid :name "Sophie Bangs" :level 42}
          conditions {:name "Sophie Bangs"}
          update {$inc {:level 1}}]
      (mc/insert db coll doc)
      (let [res (mc/find-and-modify db coll conditions update {:return-new true})]
        (is (= (select-keys res [:name :level]) {:name "Sophie Bangs" :level 43})))))


  (deftest find-and-modify-remove-a-document
    (let [coll "docs"
          oid (ObjectId.)
          doc {:_id oid :name "Sophie Bangs" :level 42}
          conditions {:name "Sophie Bangs"}]
      (mc/insert db coll doc)
      (let [res (mc/find-and-modify db coll conditions {} {:remove true})]
        (is (= (select-keys res [:name :level]) {:name "Sophie Bangs" :level 42}))
        (is (empty? (mc/find-maps db coll conditions))))))


  (deftest find-and-modify-upsert-a-document
    (testing "case 1"
      (let [coll "docs"
            oid (ObjectId.)
            doc {:_id oid :name "Sophie Bangs" :level 42}]
        (let [res (mc/find-and-modify db coll doc doc {:upsert true})]
          (is (empty? res))
          (is (select-keys (mc/find-map-by-id db coll oid) [:name :level]) (dissoc doc :_id)))))
    (testing "case 2"
      (let [coll  "docs"
            query {:name "Sophie Bangs"}
            doc   (merge query {:level 42})]
        (let [res (mc/find-and-modify db coll query doc {:upsert true :return-new true})]
          (is (:_id res))
          (is (select-keys (mc/find-map-by-id db coll (:_id res)) [:name :level]) doc)))))


  (deftest find-and-modify-after-sort
    (let [coll "docs"
          oid (ObjectId.)
          oid2 (ObjectId.)
          doc {:name "Sophie Bangs"}
          doc1 (assoc doc :_id oid :level 42)
          doc2 (assoc doc :_id oid2 :level 0)]
      (mc/insert-batch db coll [doc1 doc2])
      (let [res (mc/find-and-modify db coll doc {$inc {:level 1}} {:sort {:level -1}})]
        (is (= (select-keys res [:name :level]) {:name "Sophie Bangs" :level 42}))))))
