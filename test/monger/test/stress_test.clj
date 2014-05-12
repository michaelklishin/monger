(ns monger.test.stress-test
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.conversion :refer [to-db-object]]
            [clojure.test :refer :all])
  (:import [com.mongodb WriteConcern]
           java.util.Date))


(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (defn purge-collection
    [coll f]
    (mc/remove db coll)
    (f)
    (mc/remove db coll))

  (defn purge-things-collection
    [f]
    (purge-collection "things" f))

  (use-fixtures :each purge-things-collection)

  (monger.core/set-default-write-concern! WriteConcern/NORMAL)

  (deftest ^{:performance true} insert-large-batches-of-documents-without-object-ids
    (doseq [n [10 100 1000 10000 20000]]
      (let [collection "things"
            docs       (map (fn [i]
                              (to-db-object { :title "Untitled" :created-at (Date.) :number i }))
                            (take n (iterate inc 1)))]
        (mc/remove db collection)
        (println "Inserting " n " documents...")
        (time (mc/insert-batch db collection docs))
        (is (= n (mc/count db collection)))))))
