(ns monger.test.full-text-search-test
  (:require [monger.core        :as mg]
            [monger.collection  :as mc]
            [monger.search      :as ms]
            [monger.command     :as cmd]
            [monger.test.helper :as helper]
            [clojure.test :refer [deftest is use-fixtures]]
            [monger.test.fixtures :refer :all]
            [monger.result :refer [ok?]])
  (:import com.mongodb.BasicDBObjectBuilder))

(helper/connect!)

(defn enable-search
  [f]
  ;; {:textSearchEnabled true :setParameter 1}
  (let [bldr (doto (BasicDBObjectBuilder.)
               (.append "setParameter" 1)
               (.append "textSearchEnabled" true))
        cmd  (.get bldr)]
    (is (ok? (cmd/raw-admin-command cmd))))
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
