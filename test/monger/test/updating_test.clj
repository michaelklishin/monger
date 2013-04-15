(set! *warn-on-reflection* true)

(ns monger.test.updating-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger core util]
            [monger.collection :as mc]
            [monger.result     :as mr]
            [monger.test.helper :as helper])
  (:use clojure.test
        monger.operators
        monger.test.fixtures
        [monger.conversion :only [to-db-object]]))

(helper/connect!)

(use-fixtures :each purge-people purge-docs purge-things purge-libraries)


;;
;; update, save
;;

(deftest ^{:updating true} update-document-by-id-without-upsert
  (let [collection "libraries"
        doc-id       (monger.util/random-uuid)
        date         (Date.)
        doc          { :created-at date, :data-store "MongoDB", :language "Clojure", :_id doc-id }
        modified-doc { :created-at date, :data-store "MongoDB", :language "Erlang",  :_id doc-id }]
    (mc/insert collection doc)
    (is (= (doc (mc/find-by-id collection doc-id))))
    (mc/update collection { :_id doc-id } { :language "Erlang" })
    (is (= (modified-doc (mc/find-by-id collection doc-id))))))

(deftest ^{:updating true} update-document-by-id-without-upsert-using-update-by-id
  (let [collection "libraries"
        doc-id       (monger.util/random-uuid)
        date         (Date.)
        doc          { :created-at date, :data-store "MongoDB", :language "Clojure", :_id doc-id }
        modified-doc { :created-at date, :data-store "MongoDB", :language "Erlang",  :_id doc-id }]
    (mc/insert collection doc)
    (is (= (doc (mc/find-by-id collection doc-id))))
    (mc/update-by-id collection doc-id { :language "Erlang" })
    (is (= (modified-doc (mc/find-by-id collection doc-id))))))

(deftest ^{:updating true} update-nested-document-fields-without-upsert-using-update-by-id
  (let [collection "libraries"
        doc-id       (ObjectId.)
        date         (Date.)
        doc          { :created-at date :data-store "MongoDB" :language { :primary "Clojure" } :_id doc-id }
        modified-doc { :created-at date :data-store "MongoDB" :language { :primary "Erlang"  } :_id doc-id }]
    (mc/insert collection doc)
    (is (= (doc (mc/find-by-id collection doc-id))))
    (mc/update-by-id collection doc-id { $set { "language.primary" "Erlang" }})
    (is (= (modified-doc (mc/find-by-id collection doc-id))))))


(deftest ^{:updating true} update-multiple-documents
  (let [collection "libraries"]
    (mc/insert collection { :language "Clojure", :name "monger" })
    (mc/insert collection { :language "Clojure", :name "langohr" })
    (mc/insert collection { :language "Clojure", :name "incanter" })
    (mc/insert collection { :language "Scala",   :name "akka" })
    (is (= 3 (mc/count collection { :language "Clojure" })))
    (is (= 1 (mc/count collection { :language "Scala"   })))
    (is (= 0 (mc/count collection { :language "Python"  })))
    (mc/update collection { :language "Clojure" } { $set { :language "Python" } } :multi true)
    (is (= 0 (mc/count collection { :language "Clojure" })))
    (is (= 1 (mc/count collection { :language "Scala"   })))
    (is (= 3 (mc/count collection { :language "Python"  })))))


(deftest ^{:updating true} save-a-new-document
  (let [collection "people"
        document       {:name "Joe" :age 30}]
    (is (monger.result/ok? (mc/save "people" document)))
    (is (= 1 (mc/count collection)))))

(deftest ^{:updating true} save-and-return-a-new-document
  (let [collection "people"
        document       {:name "Joe" :age 30}
        returned   (mc/save-and-return "people" document)]
    (is (:_id returned))
    (is (= document (dissoc returned :_id)))
    (is (= 1 (mc/count collection)))))


(deftest ^{:updating true} save-a-new-basic-db-object
  (let [collection "people"
        doc        (to-db-object {:name "Joe" :age 30})]
    (is (nil? (monger.util/get-id doc)))
    (mc/save monger.core/*mongodb-database* "people" doc WriteConcern/SAFE)
    (is (not (nil? (monger.util/get-id doc))))))



(deftest ^{:updating true} update-an-existing-document-using-save
  (let [collection "people"
        doc-id            "people-1"
        document          { :_id doc-id, :name "Joe",   :age 30 }]
    (is (monger.result/ok? (mc/insert "people" document)))
    (is (= 1 (mc/count collection)))
    (mc/save collection { :_id doc-id, :name "Alan", :age 40 })
    (is (= 1 (mc/count collection { :name "Alan", :age 40 })))))

(deftest ^{:updating true} update-an-existing-document-using-save-and-return
  (let [collection "people"
        document   (mc/insert-and-return "people" {:name "Joe" :age 30})
        doc-id     (:_id document)
        updated    (mc/save-and-return collection {:_id doc-id :name "Alan" :age 40})]
    (is (= {:_id doc-id :name "Alan" :age 40} updated))
    (is (= 1 (mc/count collection)))
    (is (= 1 (mc/count collection {:name "Alan" :age 40})))))


(deftest ^{:updating true} set-an-attribute-on-existing-document-using-update
  (let [collection "people"
        doc-id            (monger.util/object-id)
        document          { :_id doc-id, :name "Joe",   :age 30 }]
    (is (monger.result/ok? (mc/insert "people" document)))
    (is (= 1 (mc/count collection)))
    (is (= 0 (mc/count collection { :has_kids true })))
    (mc/update collection { :_id doc-id } { $set { :has_kids true } })
    (is (= 1 (mc/count collection { :has_kids true })))))


(deftest ^{:updating true}  increment-multiple-fields-using-exists-operator-and-update
  (let [collection "matches"
        doc-id     (monger.util/object-id)
        document   { :_id doc-id :abc 0 :def 10 }]
    (mc/remove collection)
    (is (monger.result/ok? (mc/insert collection document)))
    (is (= 1 (mc/count collection {:abc {$exists true} :def {$exists true}})))
    (mc/update collection {:abc {$exists true} :def {$exists true}} {$inc {:abc 1 :def 0}})
    (is (= 1 (mc/count collection { :abc 1 })))))



(deftest ^{:updating true} upsert-a-document-using-update
  (let [collection "libraries"
        doc-id       (monger.util/random-uuid)
        date         (Date.)
        doc          { :created-at date, :data-store "MongoDB", :language "Clojure", :_id doc-id }
        modified-doc { :created-at date, :data-store "MongoDB", :language "Erlang",  :_id doc-id }]
    (is (not (monger.result/updated-existing? (mc/update collection { :language "Clojure" } doc :upsert true))))
    (is (= 1 (mc/count collection)))
    (is (monger.result/updated-existing? (mc/update collection { :language "Clojure" } modified-doc :multi false :upsert true)))
    (is (= 1 (mc/count collection)))
    (is (= (modified-doc (mc/find-by-id collection doc-id))))
    (mc/remove collection)))

(deftest ^{:updating true} upsert-a-document-using-upsert
  (let [collection "libraries"
        doc-id       (monger.util/random-uuid)
        date         (Date.)
        doc          {:created-at date :data-store "MongoDB" :language "Clojure" :_id doc-id}
        modified-doc {:created-at date :data-store "MongoDB" :language "Erlang"  :_id doc-id}]
    (is (not (monger.result/updated-existing? (mc/upsert collection {:language "Clojure"} doc))))
    (is (= 1 (mc/count collection)))
    (is (monger.result/updated-existing? (mc/upsert collection {:language "Clojure"} modified-doc :multi false)))
    (is (= 1 (mc/count collection)))
    (is (= (modified-doc (mc/find-by-id collection doc-id))))
    (mc/remove collection)))
