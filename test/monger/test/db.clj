(ns monger.test.db
  (:require [monger core db]
            [monger.test.helper :as helper]
            [monger.collection :as mgcol])
  (:import [com.mongodb Mongo DB]
           [java.util Set])
  (:use [clojure.test]))

(helper/connect!)



(deftest test-add-user
  (let [username "clojurewerkz/monger!"
        pwd      (.toCharArray "monger!")
        db-name  "monger-test4"]
    ;; use a secondary database here. MK.
    (monger.core/with-db (monger.core/get-db db-name)      
      (monger.db/add-user username pwd)
      (is (monger.core/authenticate db-name username pwd)))))


(deftest test-drop-database
  ;; drop a secondary database here. MK.
  (monger.core/with-db (monger.core/get-db "monger-test3")
    (let [collection "test"
          _          (mgcol/insert collection { :name "Clojure" })
          check      (mgcol/count collection)
          _          (monger.db/drop-db)]
      (is (= 1 check))
      (is (not (mgcol/exists? collection)))
      (is (= 0 (mgcol/count collection))))))


(deftest test-get-collection-names
  (mgcol/insert "test-1" { :name "Clojure" })
  (mgcol/insert "test-2" { :name "Clojure" })
  (let [^Set collections (monger.db/get-collection-names)]
    (is (.contains collections "test-1"))
    (is (.contains collections "test-2"))))
