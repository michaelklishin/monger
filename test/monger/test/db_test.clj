(ns monger.test.db-test
  (:require [monger.db :as mdb]
            [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.test :refer :all])
  (:import [com.mongodb Mongo DB]
           java.util.Set))


;; do not run this test for CI, it complicates matters by messing up
;; authentication for some other tests :( MK.
(let [conn (mg/connect)]
  (when-not (System/getenv "CI")
    (deftest test-drop-database
      ;; drop a secondary database here. MK.
      (let [db         (mg/get-db conn "monger-test3")
            collection "test"
            _          (mc/insert db collection {:name "Clojure"})
            check      (mc/count db collection)
            _          (mdb/drop-db db)]
        (is (= 1 check))
        (is (not (mc/exists? db collection)))
        (is (= 0 (mc/count db collection))))))

  (deftest test-get-collection-names
    (let [db (mg/get-db conn "monger-test")]
      (mc/insert db "test-1" {:name "Clojure"})
      (mc/insert db "test-2" {:name "Clojure"})
      (let [^Set xs (mdb/get-collection-names db)]
        (is (.contains xs "test-1"))
        (is (.contains xs "test-2"))))))
