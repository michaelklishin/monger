(ns monger.test.full-text-search-test
  (:require [monger.core        :as mg]
            [monger.collection  :as mc]
            [monger.search      :as ms]
            [monger.command     :as cmd]
            [monger.test.helper :as helper]
            [clojure.test :refer [deftest is use-fixtures]]
            [monger.test.fixtures :refer :all]
            [monger.result :refer [ok?]]))

(helper/connect!)

(defn enable-search
  [f]
  (is (ok? (cmd/admin-command (array-map :textSearchEnabled true :setParameter "*"))))
  (f))

(use-fixtures :each purge-docs)
(use-fixtures :once enable-search)

(deftest ^{:search true} test-basic-full-text-search-query
  (let [coll "docs"]
    (mc/ensure-index coll (array-map :subject "text" :content "text"))
    (mc/insert coll {:subject "hello there" :content "this should be searchable"})
    (mc/insert coll {:subject "untitled" :content "this is just noize"})
    (let [res (ms/search coll "hello")
          xs  (ms/results-from res)]
      (is (= "hello there" (-> xs first :obj :subject)))
      (is (= 1.0 (-> xs first :score))))))
