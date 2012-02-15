(ns monger.test.db
  (:require [monger core db]
            [monger.test.helper :as helper]            
            [monger.collection :as mgcol])
  (:import (com.mongodb Mongo DB))
  (:use [clojure.test]))

(helper/connect!)


(deftest get-collection-names    
    (mgcol/insert "test-1" { :name "Clojure" })
    (mgcol/insert "test-2" { :name "Clojure" })
  (let [collections (monger.db/get-collection-names)]
    (is (.contains collections "test-1"))
    (is (.contains collections "test-2"))
  ))    


(deftest drop-database     
  (let [collection "test"
        _          (mgcol/insert collection { :name "Clojure" })
        check      (mgcol/count collection)
        _          (monger.db/drop-db)
  ]
    (is (= 1 check))
    (is (not (mgcol/exists? collection)))
    (is (= 0 (mgcol/count collection)))
  )
)    


