(set! *warn-on-reflection* true)

(ns monger.test.collection
  (:import  [com.mongodb WriteResult WriteConcern DBCursor])
  (:require [monger core collection errors util] [clojure stacktrace])
  (:use [clojure.test] [monger.core]))

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
    (is (monger.errors/ok? (monger.collection/insert "people" doc)))
    (is (= 1 (monger.collection/count collection)))))


(deftest insert-a-basic-document-without-id-and-with-explicit-write-concern
  (let [collection "people"
        doc        { :name "Joe", :age 30 }]
    (monger.collection/remove collection)
    (is (monger.errors/ok? (monger.collection/insert "people" doc WriteConcern/SAFE)))
    (is (= 1 (monger.collection/count collection)))))



;;
;; insert
;;

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-default-write-concern
  (let [collection "people"
        docs       [{ :name "Joe", :age 30 }, { :name "Paul", :age 27 }]]
    (monger.collection/remove collection)
    (is (monger.errors/ok? (monger.collection/insert-batch "people" docs)))
    (is (= 2 (monger.collection/count collection)))))

(deftest insert-a-batch-of-basic-documents-without-ids-and-with-explicit-write-concern
  (let [collection "people"
        docs       [{ :name "Joe", :age 30 }, { :name "Paul", :age 27 }]]
    (monger.collection/remove collection)
    (is (monger.errors/ok? (monger.collection/insert-batch "people" docs WriteConcern/NORMAL)))
    (is (= 2 (monger.collection/count collection)))))




;;
;; count, remove
;;

(deftest get-collection-size
  (let [collection "things"]
    (monger.collection/remove collection)
    (is (= 0 (monger.collection/count collection)))
    (monger.collection/insert collection { :language "Clojure", :name "monger" })
    (monger.collection/insert collection { :language "Clojure", :name "langohr" })
    (monger.collection/insert collection { :language "Clojure", :name "incanter" })
    (monger.collection/insert collection { :language "Scala",   :name "akka" })
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
    (monger.collection/insert collection { :language "Clojure", :name "monger" })
    (monger.collection/insert collection { :language "Clojure", :name "langohr" })
    (monger.collection/insert collection { :language "Clojure", :name "incanter" })
    (monger.collection/insert collection { :language "Scala",   :name "akka" })
    (is (= 1 (monger.core/count (monger.collection/find collection { :language "Scala"   }))))
    (is (= 3 (.count (monger.collection/find collection { :language "Clojure" }))))
    (is (empty? (monger.collection/find collection      { :language "Java"    })))))


(deftest find-multiple-partial-documents
  (let [collection "libraries"]
    (monger.collection/remove collection)
    (monger.collection/insert collection { :language "Clojure", :name "monger" })
    (monger.collection/insert collection { :language "Clojure", :name "langohr" })
    (monger.collection/insert collection { :language "Clojure", :name "incanter" })
    (monger.collection/insert collection { :language "Scala",   :name "akka" })
    (let [scala-libs   (monger.collection/find collection { :language "Scala" } [:name])
          clojure-libs (monger.collection/find collection { :language "Clojure"} [:language])]
      (is (= 1 (.count scala-libs)))
      (is (= 3 (.count clojure-libs)))
      (doseq [i clojure-libs]
        (let [doc (monger.convertion/from-db-object i true)]
          (is (= (:language doc) "Clojure"))))
      (is (empty? (monger.collection/find collection { :language "Erlang" } [:name]))))))
