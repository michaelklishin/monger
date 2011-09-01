(set! *warn-on-reflection* true)

(ns monger.test.collection
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject] [java.util Date])
  (:require [monger core collection result util] [clojure stacktrace])
  (:use [clojure.test]))

(monger.util/with-ns 'monger.core
  (defonce ^:dynamic *mongodb-connection* (monger.core/connect))
  (defonce ^:dynamic *mongodb-database*   (monger.core/get-db "monger-test")))



;;
;; insert
;;

(deftest insert-a-basic-document-without-id-and-with-default-write-concern
  (let [collection "people"
        doc        { :name "Joe", :age 30 }]
    (monger.collection/remove collection)
    (is (monger.result/ok? (monger.collection/insert "people" doc)))
    (is (= 1 (monger.collection/count collection)))))


(deftest insert-a-basic-document-without-id-and-with-explicit-write-concern
  (let [collection "people"
        doc        { :name "Joe", :age 30 }]
    (monger.collection/remove collection)
    (is (monger.result/ok? (monger.collection/insert "people" doc WriteConcern/SAFE)))
    (is (= 1 (monger.collection/count collection)))))



;;
;; insert-batch
;;

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-default-write-concern
  (let [collection "people"
        docs       [{ :name "Joe", :age 30 }, { :name "Paul", :age 27 }]]
    (monger.collection/remove collection)
    (is (monger.result/ok? (monger.collection/insert-batch "people" docs)))
    (is (= 2 (monger.collection/count collection)))))

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-explicit-write-concern
  (let [collection "people"
        docs       [{ :name "Joe", :age 30 }, { :name "Paul", :age 27 }]]
    (monger.collection/remove collection)
    (is (monger.result/ok? (monger.collection/insert-batch "people" docs WriteConcern/NORMAL)))
    (is (= 2 (monger.collection/count collection)))))




;;
;; count, remove
;;

(deftest get-collection-size
  (let [collection "things"]
    (monger.collection/remove collection)
    (is (= 0 (monger.collection/count collection)))
    (monger.collection/insert-batch collection [{ :language "Clojure", :name "langohr" },
                                                { :language "Clojure", :name "monger" },
                                                { :language "Clojure", :name "incanter" },
                                                { :language "Scala",   :name "akka" }] )
    (is (= 4 (monger.collection/count collection)))
    (is (= 3 (monger.collection/count collection { :language "Clojure" })))
    (is (= 1 (monger.collection/count collection { :language "Scala"   })))
    (is (= 0 (monger.collection/count collection { :language "Python"  })))))


(deftest remove-all-documents-from-collection
  (let [collection "libraries"]
    (monger.collection/remove collection)
    (monger.collection/insert collection { :language "Clojure", :name "monger" })
    (monger.collection/insert collection { :language "Clojure", :name "langohr" })
    (monger.collection/insert collection { :language "Clojure", :name "incanter" })
    (monger.collection/insert collection { :language "Scala",   :name "akka" })
    (is (= 4 (monger.collection/count collection)))
    (monger.collection/remove collection)
    (is (= 0 (monger.collection/count collection)))))


(deftest remove-some-documents-from-collection
  (let [collection "libraries"]
    (monger.collection/remove collection)
    (monger.collection/insert collection { :language "Clojure", :name "monger" })
    (monger.collection/insert collection { :language "Clojure", :name "langohr" })
    (monger.collection/insert collection { :language "Clojure", :name "incanter" })
    (monger.collection/insert collection { :language "Scala",   :name "akka" })
    (is (= 4 (monger.collection/count collection)))
    (monger.collection/remove collection { :language "Clojure" })
    (is (= 1 (monger.collection/count collection)))))



;;
;; find
;;

(deftest find-full-document-when-collection-is-empty
  (let [collection "docs"]
    (monger.collection/remove collection)
    (def cursor (monger.collection/find collection))
    (is (instance? DBCursor cursor))))


;;
;; find-one
;;

(deftest find-one-full-document-when-collection-is-empty
  (let [collection "docs"]
    (monger.collection/remove collection)
    (def cursor (monger.collection/find-one collection {}))
    (is (instance? DBCursor cursor))
    (is (empty? cursor))))

(deftest find-one-full-document-when-collection-has-matches
  (let [collection "docs"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
    (monger.collection/remove collection)
    (monger.collection/insert collection doc)
    (def cursor (monger.collection/find-one collection { :language "Clojure" }))
    (is (= (:_id doc) (.get (.next #^DBCursor cursor) "_id")))))


(deftest find-one-full-document-when-collection-has-matches
  (let [collection "docs"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }
        fields     [:language]]
    (monger.collection/remove collection)
    (monger.collection/insert collection doc)
    (def cursor (monger.collection/find-one collection { :language "Clojure" } fields))
    (def #^DBObject loaded (.next #^DBCursor cursor))
    (is (nil? (.get #^DBObject loaded "data-stire")))
    (is (= doc-id (.get #^DBObject loaded "_id")))
    (is (= "Clojure" (.get #^DBObject loaded "language")))))



;;
;; find-by-id
;;

(deftest find-full-document-by-id-when-document-does-not-exist
  (let [collection "libraries"
        doc-id     (monger.util/random-uuid)]
    (monger.collection/remove collection)
    (is (nil? (monger.collection/find-by-id collection doc-id)))))


(deftest find-full-document-by-id-when-document-exists
  (let [collection "libraries"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
    (monger.collection/remove collection)
    (monger.collection/insert collection doc)
    (is (= (doc (monger.collection/find-by-id collection doc-id))))))

(deftest find-partial-document-by-id-when-document-exists
  (let [collection "libraries"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
    (monger.collection/remove collection)
    (monger.collection/insert collection doc)
    (is (= ({ :language "Clojure" } (monger.collection/find-by-id collection doc-id [ :language ]))))))


(deftest find-multiple-documents-when-collection-is-empty
  (let [collection "libraries"]
    (monger.collection/remove collection)
    (is (empty? (monger.collection/find collection { :language "Scala" })))))


(deftest find-multiple-documents
  (let [collection "libraries"]
    (monger.collection/remove collection)
    (monger.collection/insert-batch collection [{ :language "Clojure", :name "monger" }
                                                { :language "Clojure", :name "langohr" },
                                                { :language "Clojure", :name "incanter" },
                                                { :language "Scala",   :name "akka" }])
    (is (= 1 (monger.core/count (monger.collection/find collection { :language "Scala"   }))))
    (is (= 3 (.count (monger.collection/find collection { :language "Clojure" }))))
    (is (empty? (monger.collection/find collection      { :language "Java"    })))))


(deftest find-multiple-partial-documents
  (let [collection "libraries"]
    (monger.collection/remove collection)
        (monger.collection/insert-batch collection [{ :language "Clojure", :name "monger" }
                                                { :language "Clojure", :name "langohr" },
                                                { :language "Clojure", :name "incanter" },
                                                { :language "Scala",   :name "akka" }])
    (let [scala-libs   (monger.collection/find collection { :language "Scala" } [:name])
          clojure-libs (monger.collection/find collection { :language "Clojure"} [:language])]
      (is (= 1 (.count scala-libs)))
      (is (= 3 (.count clojure-libs)))
      (doseq [i clojure-libs]
        (let [doc (monger.convertion/from-db-object i true)]
          (is (= (:language doc) "Clojure"))))
      (is (empty? (monger.collection/find collection { :language "Erlang" } [:name]))))))


;;
;; update
;;

(deftest update-document-by-id-without-upsert
  (let [collection "libraries"
        doc-id       (monger.util/random-uuid)
        date         (Date.)
        doc          { :created-at date, :data-store "MongoDB", :language "Clojure", :_id doc-id }
        modified-doc { :created-at date, :data-store "MongoDB", :language "Erlang",  :_id doc-id }]
    (monger.collection/remove collection)
    (monger.collection/insert collection doc)
    (is (= (doc (monger.collection/find-by-id collection doc-id))))
    (monger.collection/update collection { :_id doc-id } { :language "Erlang" })
    (is (= (modified-doc (monger.collection/find-by-id collection doc-id))))))


(deftest update-multiple-documents
  (let [collection "libraries"]
    (monger.collection/remove collection)
    (monger.collection/insert collection { :language "Clojure", :name "monger" })
    (monger.collection/insert collection { :language "Clojure", :name "langohr" })
    (monger.collection/insert collection { :language "Clojure", :name "incanter" })
    (monger.collection/insert collection { :language "Scala",   :name "akka" })
    (is (= 3 (monger.collection/count collection { :language "Clojure" })))
    (is (= 1 (monger.collection/count collection { :language "Scala"   })))
    (is (= 0 (monger.collection/count collection { :language "Python"  })))
    (monger.collection/update collection { :language "Clojure" } { "$set" { :language "Python" } } :multi true)
    (is (= 0 (monger.collection/count collection { :language "Clojure" })))
    (is (= 1 (monger.collection/count collection { :language "Scala"   })))
    (is (= 3 (monger.collection/count collection { :language "Python"  })))))


(deftest save-a-new-document
  (let [collection "people"
        document       { :name "Joe", :age 30 }]
    (monger.collection/remove collection)
    (is (monger.result/ok? (monger.collection/save "people" document)))
    (is (= 1 (monger.collection/count collection)))))


(deftest update-an-existing-document-using-save
  (let [collection "people"
        doc-id            "people-1"
        document          { :_id doc-id, :name "Joe",   :age 30 }]
    (monger.collection/remove collection)
    (is (monger.result/ok? (monger.collection/insert "people" document)))
    (is (= 1 (monger.collection/count collection)))
    (monger.collection/save collection { :_id doc-id, :name "Alan", :age 40 })
    (is (= 1 (monger.collection/count collection { :name "Alan", :age 40 })))))


(deftest upsert-a-document
  (let [collection "libraries"
        doc-id       (monger.util/random-uuid)
        date         (Date.)
        doc          { :created-at date, :data-store "MongoDB", :language "Clojure", :_id doc-id }
        modified-doc { :created-at date, :data-store "MongoDB", :language "Erlang",  :_id doc-id }]
    (monger.collection/remove collection)
    (is (not (monger.result/updated-existing? (monger.collection/update collection { :language "Clojure" } doc :upsert true))))
    (is (= 1 (monger.collection/count collection)))
    (is (monger.result/updated-existing? (monger.collection/update collection { :language "Clojure" } modified-doc :multi false :upsert true)))
    (is (= 1 (monger.collection/count collection)))
    (is (= (modified-doc (monger.collection/find-by-id collection doc-id))))))
