(ns monger.test.stress
  (:import [com.mongodb Mongo DB DBCollection WriteResult DBObject WriteConcern DBCursor]
           [java.util Date])
  (:require [monger core])
  (:use     [clojure.test]))


;;
;; Fixture functions
;;

(defn purge-collection
  [collection-name, f]
  (monger.collection/remove collection-name)
  (f)
  (monger.collection/remove collection-name))

(defn purge-things-collection
  [f]
  (purge-collection "things" f))

(use-fixtures :each purge-things-collection)



;;
;; Tests
;;

(monger.core/set-default-write-concern! WriteConcern/NORMAL)

(deftest insert-large-batches-of-documents-without-object-ids
  (doseq [n [1000 10000 100000]]
    (let [collection "things"
          docs       (map (fn [i]
                            (monger.conversion/to-db-object { :title "Untitled" :created-at (Date.) :number i }))
                          (take n (iterate inc 1)))]
      (monger.collection/remove collection)
      (println "Inserting " n " documents...")
      (time (monger.collection/insert-batch collection docs))
      (is (= n (monger.collection/count collection))))))
