(set! *warn-on-reflection* true)

(ns monger.test.updating
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject]
            [org.bson.types ObjectId]
            [java.util Date])
  (:require [monger core util]
            [monger.collection :as mgcol]
            [monger.result     :as mgres]
            [monger.conversion :as mgcnv]
            [monger.test.helper :as helper])
  (:use [clojure.test]
        [monger.operators]
        [monger.test.fixtures]))

(helper/connect!)

(use-fixtures :each purge-people purge-docs purge-things purge-libraries)


;;
;; update, save
;;

(deftest update-document-by-id-without-upsert
  (let [collection "libraries"
        doc-id       (monger.util/random-uuid)
        date         (Date.)
        doc          { :created-at date, :data-store "MongoDB", :language "Clojure", :_id doc-id }
        modified-doc { :created-at date, :data-store "MongoDB", :language "Erlang",  :_id doc-id }]
    (mgcol/insert collection doc)
    (is (= (doc (mgcol/find-by-id collection doc-id))))
    (mgcol/update collection { :_id doc-id } { :language "Erlang" })
    (is (= (modified-doc (mgcol/find-by-id collection doc-id))))))

(deftest update-document-by-id-without-upsert-using-update-by-id
  (let [collection "libraries"
        doc-id       (monger.util/random-uuid)
        date         (Date.)
        doc          { :created-at date, :data-store "MongoDB", :language "Clojure", :_id doc-id }
        modified-doc { :created-at date, :data-store "MongoDB", :language "Erlang",  :_id doc-id }]
    (mgcol/insert collection doc)
    (is (= (doc (mgcol/find-by-id collection doc-id))))
    (mgcol/update-by-id collection doc-id { :language "Erlang" })
    (is (= (modified-doc (mgcol/find-by-id collection doc-id))))))


(deftest update-multiple-documents
  (let [collection "libraries"]
    (mgcol/insert collection { :language "Clojure", :name "monger" })
    (mgcol/insert collection { :language "Clojure", :name "langohr" })
    (mgcol/insert collection { :language "Clojure", :name "incanter" })
    (mgcol/insert collection { :language "Scala",   :name "akka" })
    (is (= 3 (mgcol/count collection { :language "Clojure" })))
    (is (= 1 (mgcol/count collection { :language "Scala"   })))
    (is (= 0 (mgcol/count collection { :language "Python"  })))
    (mgcol/update collection { :language "Clojure" } { $set { :language "Python" } } :multi true)
    (is (= 0 (mgcol/count collection { :language "Clojure" })))
    (is (= 1 (mgcol/count collection { :language "Scala"   })))
    (is (= 3 (mgcol/count collection { :language "Python"  })))))


(deftest save-a-new-document
  (let [collection "people"
        document       { :name "Joe", :age 30 }]
    (is (monger.result/ok? (mgcol/save "people" document)))
    (is (= 1 (mgcol/count collection)))))


(deftest save-a-new-basic-db-object
  (let [collection "people"
        doc        (mgcnv/to-db-object { :name "Joe", :age 30 })]
    (is (nil? (monger.util/get-id doc)))
    (mgcol/save monger.core/*mongodb-database* "people" doc WriteConcern/SAFE)
    (is (not (nil? (monger.util/get-id doc))))))



(deftest update-an-existing-document-using-save
  (let [collection "people"
        doc-id            "people-1"
        document          { :_id doc-id, :name "Joe",   :age 30 }]
    (is (monger.result/ok? (mgcol/insert "people" document)))
    (is (= 1 (mgcol/count collection)))
    (mgcol/save collection { :_id doc-id, :name "Alan", :age 40 })
    (is (= 1 (mgcol/count collection { :name "Alan", :age 40 })))))


(deftest set-an-attribute-on-existing-document-using-update
  (let [collection "people"
        doc-id            (monger.util/object-id)
        document          { :_id doc-id, :name "Joe",   :age 30 }]
    (is (monger.result/ok? (mgcol/insert "people" document)))
    (is (= 1 (mgcol/count collection)))
    (is (= 0 (mgcol/count collection { :has_kids true })))
    (mgcol/update collection { :_id doc-id } { $set { :has_kids true } })
    (is (= 1 (mgcol/count collection { :has_kids true })))))



(deftest upsert-a-document
  (let [collection "libraries"
        doc-id       (monger.util/random-uuid)
        date         (Date.)
        doc          { :created-at date, :data-store "MongoDB", :language "Clojure", :_id doc-id }
        modified-doc { :created-at date, :data-store "MongoDB", :language "Erlang",  :_id doc-id }]
    (is (not (monger.result/updated-existing? (mgcol/update collection { :language "Clojure" } doc :upsert true))))
    (is (= 1 (mgcol/count collection)))
    (is (monger.result/updated-existing? (mgcol/update collection { :language "Clojure" } modified-doc :multi false :upsert true)))
    (is (= 1 (mgcol/count collection)))
    (is (= (modified-doc (mgcol/find-by-id collection doc-id))))
    (mgcol/remove collection)))
