(ns monger.test.query-operators-test
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.js         :as js]
            [clojure.test :refer :all]
            [clojure.set :refer [difference]]
            [monger.operators :refer :all])
  (:import [com.mongodb QueryOperators]))

;; (use-fixtures :each purge-people purge-docs purge-things purge-libraries)

(deftest every-query-operator-is-defined
  (let [driver-query-operators (->> (.getDeclaredFields QueryOperators) (map #(.get % nil)) set)
        monger-query-operators (->> (ns-publics 'monger.operators) (map (comp name first)) set)
        ; $within is deprecated and replaced by $geoWithin since v2.4.
        ; $uniqueDocs is deprecated since v2.6.
        deprecated-query-operators #{"$within" "$uniqueDocs"}
        ; Query modifier operators that are deprecated in the mongo shell since v3.2
        deprecated-meta-operators #{"$comment" "$explain" "$hint" "$maxScan"
                                    "$maxTimeMS" "$max" "$min" "$orderby"
                                    "$returnKey" "$showDiskLoc" "$snapshot" "$query"}
        undefined-non-deprecated-operators (difference driver-query-operators
                                                       deprecated-query-operators
                                                       deprecated-meta-operators
                                                       monger-query-operators)]
    (is (= #{} undefined-non-deprecated-operators))))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (defn purge-collections
    [f]
    (mc/remove db "people")
    (mc/remove db "libraries")
    (f)
    (mc/remove db "people")
    (mc/remove db "libraries"))

  (use-fixtures :each purge-collections)

  ;;
  ;; $gt, $gte, $lt, lte
  ;;

  (deftest find-with-conditional-operators-comparison
    (let [collection "libraries"]
      (mc/insert-batch db collection [{:language "Clojure" :name "monger"   :users 1}
                                      {:language "Clojure" :name "langohr"  :users 5}
                                      {:language "Clojure" :name "incanter" :users 15}
                                      {:language "Scala"   :name "akka"     :users 150}])
      (are [a b] (= a (.count (mc/find db collection b)))
           2 {:users {$gt 10}}
           3 {:users {$gte 5}}
           2 {:users {$lt 10}}
           2 {:users {$lte 5}}
           1 {:users {$gt 10 $lt 150}})))

  ;;
  ;; $ne
  ;;

  (deftest find-with-and-or-operators
    (let [collection "libraries"]
      (mc/insert-batch db collection [{:language "Ruby"    :name "mongoid"  :users 1}
                                      {:language "Clojure" :name "langohr"  :users 5}
                                      {:language "Clojure" :name "incanter" :users 15}
                                      {:language "Scala"   :name "akka"     :users 150}])
      (is (= 2 (.count (mc/find db collection {$ne {:language "Clojure"}}))))))


  ;;
  ;; $and, $or, $nor
  ;;

  (deftest find-with-and-or-operators
    (let [collection "libraries"]
      (mc/insert-batch db collection [{:language "Ruby"    :name "mongoid"  :users 1}
                                      {:language "Clojure" :name "langohr"  :users 5}
                                      {:language "Clojure" :name "incanter" :users 15}
                                      {:language "Scala"   :name "akka"     :users 150}])
      (is (= 1 (.count (mc/find db collection {$and [{:language "Clojure"}
                                                     {:users {$gt 10}}]}))))
      (is (= 3 (.count (mc/find db collection {$or [{:language "Clojure"}
                                                    {:users {$gt 10}} ]}))))
      (is (= 1 (.count (mc/find db collection {$nor [{:language "Clojure"}
                                                     {:users {$gt 10}} ]}))))))

  ;;
  ;; $all, $in, $nin
  ;;

  (deftest find-on-embedded-arrays
    (let [collection "libraries"]
      (mc/insert-batch db collection [{:language "Clojure" :tags [ "functional" ]}
                                      {:language "Scala"   :tags [ "functional" "object-oriented" ]}
                                      {:language "Ruby"    :tags [ "object-oriented" "dynamic" ]}])

      (is (= "Scala" (:language (first (mc/find-maps db collection {:tags {$all [ "functional" "object-oriented" ]}} )))))
      (is (= 3 (.count (mc/find-maps db collection {:tags {$in [ "functional" "object-oriented" ]}} ))))
      (is (= 2 (.count (mc/find-maps db collection {:language {$in [ "Scala" "Ruby" ]}} ))))
      (is (= 1 (.count (mc/find-maps db collection {:tags {$nin [ "dynamic" "object-oriented" ]}} ))))
      (is (= 3 (.count (mc/find-maps db collection {:language {$nin [ "C#" ]}} ))))))


  (deftest find-with-conditional-operators-on-embedded-documents
    (let [collection "people"]
      (mc/insert-batch db collection [{:name "Bob" :comments [{:text "Nice!" :rating 1}
                                                              {:text "Love it" :rating 4}
                                                              {:text "What?":rating -5} ]}
                                      {:name "Alice" :comments [{:text "Yeah" :rating 2}
                                                                {:text "Doh" :rating 1}
                                                                {:text "Agreed" :rating 3}]}])
      (are [a b] (= a (.count (mc/find db collection b)))
           1 {:comments {$elemMatch {:text "Nice!" :rating {$gte 1}}}}
           2 {"comments.rating" 1}
           1 {"comments.rating" {$gt 3}})))

  (deftest  find-with-regex-operator
    (let [collection "libraries"]
      (mc/insert-batch db collection [{:language "Ruby"    :name "Mongoid"  :users 1}
                                      {:language "Clojure" :name "Langohr"  :users 5}
                                      {:language "Clojure" :name "Incanter" :users 15}
                                      {:language "Scala"   :name "Akka"     :users 150}])
      (are [query results] (is (= results (.count (mc/find db collection query))))
           {:language {$regex "Clo.*"}} 2
           {:language {$regex "clo.*" $options "i"}} 2
           {:name     {$regex "aK.*" $options "i"}} 1
           {:language {$regex ".*by"}} 1
           {:language {$regex ".*ala.*"}} 1))))
