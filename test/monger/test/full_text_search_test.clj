(ns monger.test.full-text-search-test
  (:require [monger.core        :as mg]
            [monger.collection  :as mc]
            [monger.command     :as cmd]
            [monger.operators :refer :all]
            [clojure.test :refer [deftest is use-fixtures]]
            [monger.result :refer [acknowledged?]])
  (:import com.mongodb.BasicDBObjectBuilder))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")
      coll "search-docs"]

  (defn purge-collections
    [f]
    (mc/purge-many db [coll])
    (f)
    (mc/purge-many db [coll]))

  (use-fixtures :each purge-collections)

  (deftest ^{:search true} test-basic-full-text-search-query
    (mc/ensure-index db coll (array-map :subject "text" :content "text"))
    (mc/insert db coll {:subject "hello there" :content "this should be searchable"})
    (mc/insert db coll {:subject "untitled" :content "this is just noize"})
    (let [xs (mc/find-maps db coll {$text {$search "hello"}})]
      (is (= 1 (count xs)))
      (is (= "hello there" (-> xs first :subject))))))
