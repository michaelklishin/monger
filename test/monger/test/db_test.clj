(ns monger.test.db-test
  (:require [monger core db]
            [monger.test.helper :as helper]
            [monger.collection :as mc]
            [clojure.test :refer :all])
  (:import [com.mongodb Mongo DB]
           java.util.Set))

(helper/connect!)



;; do not run this test for CI, it complicates matters by messing up
;; authentication for some other tests :( MK.
(when-not (System/getenv "CI")
  (deftest test-drop-database
    ;; drop a secondary database here. MK.
    (monger.core/with-db (monger.core/get-db "monger-test3")
      (let [collection "test"
            _          (mc/insert collection {:name "Clojure"})
            check      (mc/count collection)
            _          (monger.db/drop-db)]
        (is (= 1 check))
        (is (not (mc/exists? collection)))
        (is (= 0 (mc/count collection))))))

  (deftest test-use-database
    (monger.core/use-db! "monger-test5")
    (is (= "monger-test5" (.getName (monger.core/current-db))))
    (let [collection "test"
            _          (mc/insert collection {:name "Clojure"})
            check      (mc/count collection)
            _          (monger.db/drop-db)]
        (is (= 1 check))
        (is (not (mc/exists? collection)))
        (is (= 0 (mc/count collection))))))


(deftest test-get-collection-names
  (mc/insert "test-1" {:name "Clojure"})
  (mc/insert "test-2" {:name "Clojure"})
  (let [^Set collections (monger.db/get-collection-names)]
    (is (.contains collections "test-1"))
    (is (.contains collections "test-2"))))
