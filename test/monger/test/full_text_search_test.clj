(ns monger.test.full-text-search-test
  (:require [monger.core        :as mg]
            [monger.collection  :as mc]
            [monger.search      :as ms]
            [monger.command     :as cmd]
            [clojure.test :refer [deftest is use-fixtures]]
            [monger.result :refer [acknowledged?]])
  (:import com.mongodb.BasicDBObjectBuilder))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")
      coll "search-docs"]

  (defn enable-search
    [f]
    ;; {:textSearchEnabled true :setParameter 1}
    (let [bldr (doto (BasicDBObjectBuilder.)
                 (.append "setParameter" 1)
                 (.append "textSearchEnabled" true))
          cmd  (.get bldr)]
      (is (acknowledged? (cmd/raw-admin-command conn cmd))))
    (f))

  (defn purge-collections
    [f]
    (mc/purge-many db [coll])
    (f)
    (mc/purge-many db [coll]))

  (use-fixtures :each purge-collections)
  (use-fixtures :once enable-search)

  (deftest ^{:search true} test-basic-full-text-search-query
    (mc/ensure-index db coll (array-map :subject "text" :content "text"))
    (mc/insert db coll {:subject "hello there" :content "this should be searchable"})
    (mc/insert db coll {:subject "untitled" :content "this is just noize"})
    (let [res (ms/search db coll "hello")
          xs  (ms/results-from res)]
      (is (= "hello there" (-> xs first :obj :subject)))
      (is (= 1.0 (-> xs first :score))))))
