(ns monger.test.updating-test
  (:import  [com.mongodb WriteResult WriteConcern DBObject]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.util :as mu]
            [monger.result     :as mr]
            [clojure.test :refer :all]
            [monger.operators :refer :all]
            [monger.conversion :refer [to-db-object]]))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (defn purge-collections
    [f]
    (mc/remove db "people")
    (mc/remove db "docs")
    (mc/remove db "things")
    (mc/remove db "libraries")
    (f)
    (mc/remove db "people")
    (mc/remove db "docs")
    (mc/remove db "things")
    (mc/remove db "libraries"))

  (use-fixtures :each purge-collections)

  (deftest ^{:updating true} update-document-by-id-without-upsert
    (let [collection "libraries"
          doc-id       (mu/random-uuid)
          date         (Date.)
          doc          { :created-at date, :data-store "MongoDB", :language "Clojure", :_id doc-id }
          modified-doc { :created-at date, :data-store "MongoDB", :language "Erlang",  :_id doc-id }]
      (mc/insert db collection doc)
      (is (= (to-db-object doc) (mc/find-by-id db collection doc-id)))
      (mc/update db collection { :_id doc-id } { $set { :language "Erlang" } })
      (is (= (to-db-object modified-doc) (mc/find-by-id db collection doc-id)))))

  (deftest ^{:updating true} update-document-by-id-without-upsert-using-update-by-id
    (let [collection "libraries"
          doc-id       (mu/random-uuid)
          date         (Date.)
          doc          { :created-at date, :data-store "MongoDB", :language "Clojure", :_id doc-id }
          modified-doc { :created-at date, :data-store "MongoDB", :language "Erlang",  :_id doc-id }]
      (mc/insert db collection doc)
      (is (= (to-db-object doc) (mc/find-by-id db collection doc-id)))
      (mc/update-by-id db collection doc-id { $set { :language "Erlang" } })
      (is (= (to-db-object modified-doc) (mc/find-by-id db collection doc-id)))))

  (deftest ^{:updating true} update-nested-document-fields-without-upsert-using-update-by-id
    (let [collection "libraries"
          doc-id       (ObjectId.)
          date         (Date.)
          doc          { :created-at date :data-store "MongoDB" :language { :primary "Clojure" } :_id doc-id }
          modified-doc { :created-at date :data-store "MongoDB" :language { :primary "Erlang"  } :_id doc-id }]
      (mc/insert db collection doc)
      (is (= (to-db-object doc) (mc/find-by-id db collection doc-id)))
      (mc/update-by-id db collection doc-id { $set { "language.primary" "Erlang" }})
      (is (= (to-db-object modified-doc) (mc/find-by-id db collection doc-id)))))


  (deftest ^{:updating true} update-multiple-documents
    (let [collection "libraries"]
      (mc/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                      { :language "Clojure", :name "langohr" }
                                      { :language "Clojure", :name "incanter" }
                                      { :language "Scala",   :name "akka" }])
      (is (= 3 (mc/count db collection { :language "Clojure" })))
      (is (= 1 (mc/count db collection { :language "Scala"   })))
      (is (= 0 (mc/count db collection { :language "Python"  })))
      (mc/update db collection { :language "Clojure" } { $set { :language "Python" } } {:multi true})
      (is (= 0 (mc/count db collection { :language "Clojure" })))
      (is (= 1 (mc/count db collection { :language "Scala"   })))
      (is (= 3 (mc/count db collection { :language "Python"  })))))


  (deftest ^{:updating true} save-a-new-document
    (let [collection "people"
          document       {:name "Joe" :age 30}]
      (is (mc/save db "people" document))
      (is (= 1 (mc/count db collection)))))

  (deftest ^{:updating true} save-and-return-a-new-document
    (let [collection "people"
          document       {:name "Joe" :age 30}
          returned   (mc/save-and-return db "people" document)]
      (is (:_id returned))
      (is (= document (dissoc returned :_id)))
      (is (= 1 (mc/count db collection)))))


  (deftest ^{:updating true} save-a-new-basic-db-object
    (let [collection "people"
          doc        (to-db-object {:name "Joe" :age 30})]
      (is (nil? (mu/get-id doc)))
      (mc/save db "people" doc WriteConcern/SAFE)
      (is (not (nil? (mu/get-id doc))))))



  (deftest ^{:updating true} update-an-existing-document-using-save
    (let [collection "people"
          doc-id            "people-1"
          document          { :_id doc-id, :name "Joe",   :age 30 }]
      (is (mc/insert db collection document))
      (is (= 1 (mc/count db collection)))
      (mc/save db collection { :_id doc-id, :name "Alan", :age 40 })
      (is (= 1 (mc/count db collection { :name "Alan", :age 40 })))))

  (deftest ^{:updating true} update-an-existing-document-using-save-and-return
    (let [collection "people"
          document   (mc/insert-and-return db collection {:name "Joe" :age 30})
          doc-id     (:_id document)
          updated    (mc/save-and-return db collection {:_id doc-id :name "Alan" :age 40})]
      (is (= {:_id doc-id :name "Alan" :age 40} updated))
      (is (= 1 (mc/count db collection)))
      (is (= 1 (mc/count db collection {:name "Alan" :age 40})))))


  (deftest ^{:updating true} set-an-attribute-on-existing-document-using-update
    (let [collection "people"
          doc-id            (mu/object-id)
          document          { :_id doc-id, :name "Joe",   :age 30 }]
      (is (mc/insert db collection document))
      (is (= 1 (mc/count db collection)))
      (is (= 0 (mc/count db collection { :has_kids true })))
      (mc/update db collection { :_id doc-id } { $set { :has_kids true } })
      (is (= 1 (mc/count db collection { :has_kids true })))))


  (deftest ^{:updating true}  increment-multiple-fields-using-exists-operator-and-update
    (let [collection "matches"
          doc-id     (mu/object-id)
          document   { :_id doc-id :abc 0 :def 10 }]
      (mc/remove db collection)
      (is (mc/insert db collection document))
      (is (= 1 (mc/count db collection {:abc {$exists true} :def {$exists true}})))
      (mc/update db collection {:abc {$exists true} :def {$exists true}} {$inc {:abc 1 :def 0}})
      (is (= 1 (mc/count db collection { :abc 1 })))))



  (deftest ^{:updating true} upsert-a-document-using-update
    (let [collection "libraries"
          doc-id       (mu/random-uuid)
          date         (Date.)
          doc          { :created-at date, :data-store "MongoDB", :language "Clojure", :_id doc-id }
          modified-doc { :created-at date, :data-store "MongoDB", :language "Erlang",  :_id doc-id }]
      (is (not (mr/updated-existing? (mc/update db collection { :language "Clojure" } doc {:upsert true}))))
      (is (= 1 (mc/count db collection)))
      (is (mr/updated-existing? (mc/update db collection { :language "Clojure" } modified-doc {:multi false :upsert true})))
      (is (= 1 (mc/count db collection)))
      (is (= (to-db-object modified-doc) (mc/find-by-id db collection doc-id)))
      (mc/remove db collection)))

  (deftest ^{:updating true} upsert-a-document-using-upsert
    (let [collection "libraries"
          doc-id       (mu/random-uuid)
          date         (Date.)
          doc          {:created-at date :data-store "MongoDB" :language "Clojure" :_id doc-id}
          modified-doc {:created-at date :data-store "MongoDB" :language "Erlang"  :_id doc-id}]
      (mc/remove db collection)
      (is (not (mr/updated-existing? (mc/upsert db collection {:language "Clojure"} doc))))
      (is (= 1 (mc/count db collection)))
      (is (mr/updated-existing? (mc/upsert db collection {:language "Clojure"} modified-doc {:multi false})))
      (is (= 1 (mc/count db collection)))
      (is (= (to-db-object modified-doc) (mc/find-by-id db collection doc-id)))
      (mc/remove db collection))))
