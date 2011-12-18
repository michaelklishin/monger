(set! *warn-on-reflection* true)

(ns monger.test.collection
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject CommandResult$CommandFailure MapReduceOutput MapReduceCommand MapReduceCommand$OutputType]
            [org.bson.types ObjectId]
            [java.util Date])
  (:require [monger core util]
            [clojure stacktrace]
            [monger.collection :as mgcol]
            [monger.result     :as mgres]
            [monger.conversion :as mgcnv]
            [monger.js         :as js]
            [monger.test.helper :as helper])
  (:use [clojure.test]
        [monger.operators]
        [monger.test.fixtures]))

(monger.core/connect!)
(monger.core/set-db! (monger.core/get-db "monger-test"))

(use-fixtures :each purge-people purge-docs purge-things purge-libraries)

;;
;; $gt, $gte, $lt, lte
;;

(deftest find-with-conditional-operators-comparison
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{ :language "Clojure", :name "monger"   :users 1}
                                    { :language "Clojure", :name "langohr"  :users 5 }
                                    { :language "Clojure", :name "incanter" :users 15 }
                                    { :language "Scala",   :name "akka"     :users 150}])
    (are [a b] (= a (.count (mgcol/find collection b)))
         2 { :users { $gt 10 }}
         3 { :users { $gte 5 }}
         2 { :users { $lt 10 }}
         2 { :users { $lte 5 }}
         1 { :users { $gt 10 $lt 150 }})))

;;
;; $ne
;;

(deftest find-with-and-or-operators
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{ :language "Ruby",    :name "mongoid"  :users 1}
                                    { :language "Clojure", :name "langohr"  :users 5 }
                                    { :language "Clojure", :name "incanter" :users 15 }
                                    { :language "Scala",   :name "akka"     :users 150}])
    (is (= 2 (.count (mgcol/find collection {$ne { :language "Clojure" }}))))))


;;
;; $and, $or, $nor
;;

(deftest find-with-and-or-operators
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{ :language "Ruby",    :name "mongoid"  :users 1}
                                    { :language "Clojure", :name "langohr"  :users 5 }
                                    { :language "Clojure", :name "incanter" :users 15 }
                                    { :language "Scala",   :name "akka"     :users 150}])
    (is (= 1 (.count (mgcol/find collection {$and [{ :language "Clojure" }
                                                   { :users { $gt 10 } }]}))))
    (is (= 3 (.count (mgcol/find collection {$or [{ :language "Clojure" }
                                                  {:users { $gt 10 } } ]}))))
    (is (= 1 (.count (mgcol/find collection {$nor [{ :language "Clojure" }
                                                  {:users { $gt 10 } } ]}))))))

;;
;; $all, $in
;;

(deftest find-on-embedded-arrays
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{ :language "Clojure", :tags [ "functional" ] }
                                    { :language "Scala",   :tags [ "functional" "object-oriented" ] }
                                    { :language "Ruby",    :tags [ "object-oriented" "dynamic" ] }])

    (is (= "Scala" (:language (first (mgcol/find-maps collection { :tags { $all [ "functional" "object-oriented" ] } } )))))
    (is (= 3 (.count (mgcol/find-maps collection { :tags { $in [ "functional" "object-oriented" ] } } ))))))


(deftest find-with-conditional-operators-on-embedded-documents
  (let [collection "people"]
    (mgcol/insert-batch collection [{ :name "Bob", :comments [ { :text "Nice!" :rating 1 }
                                                               { :text "Love it" :rating 4 }
                                                               { :text "What?":rating -5 } ] }
                                    { :name "Alice", :comments [ { :text "Yeah" :rating 2 }
                                                                 { :text "Doh" :rating 1 }
                                                                 { :text "Agreed" :rating 3 }
                                                                 ] } ])
    (are [a b] (= a (.count (mgcol/find collection b)))
         1 { :comments { $elemMatch { :text "Nice!" :rating { $gte 1 } } } }
         2 { "comments.rating" 1 }
         1 { "comments.rating" { $gt 3 } })))