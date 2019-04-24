(ns monger.test.collection-test
  (:import  org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.core        :as mg]
            [monger.collection  :as mc]
            [clojure.test :refer :all]
            [monger.operators :refer :all]))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]

  (defn purge-collections
    [f]
    (mc/remove db "people")
    (mc/remove db "docs")
    (mc/remove db "things")
    (mc/remove db "libraries")
    (f)
    (mc/remove db "people")
    (mc/remove db "docs")
    (mc/remove db "things")
    (mc/remove db "libraries"))

  (use-fixtures :each purge-collections)

  ;;
  ;; count, remove
  ;;

  (deftest get-collection-size
    (let [collection "things"]
      (is (= 0 (mc/count db collection)))
      (mc/insert-batch db collection [{:language "Clojure" :name "langohr"}
                                      {:language "Clojure" :name "monger"}
                                      {:language "Clojure" :name "incanter"}
                                      {:language "Scala"   :name "akka"}])
      (is (= 4 (mc/count db collection)))
      (is (mc/any? db collection))
      (is (= 3 (mc/count db collection {:language "Clojure"})))
      (is (mc/any? db collection {:language "Clojure"}))
      (is (= 1 (mc/count db collection {:language "Scala"  })))
      (is (mc/any? db collection {:language "Scala"}))
      (is (= 0 (mc/count db collection {:language "Python" })))
      (is (not (mc/any? db collection {:language "Python"})))))


  (deftest remove-all-documents-from-collection
    (let [collection "libraries"]
      (mc/insert-batch db collection [{:language "Clojure" :name "monger"}
                                      {:language "Clojure" :name "langohr"}
                                      {:language "Clojure" :name "incanter"}
                                      {:language "Scala"   :name "akka"}])
      (is (= 4 (mc/count db collection)))
      (mc/remove db collection)
      (is (= 0 (mc/count db collection)))))


  (deftest remove-some-documents-from-collection
    (let [collection "libraries"]
      (mc/insert-batch db collection [{:language "Clojure" :name "monger"}
                                      {:language "Clojure" :name "langohr"}
                                      {:language "Clojure" :name "incanter"}
                                      {:language "Scala"   :name "akka"}])
      (is (= 4 (mc/count db collection)))
      (mc/remove db collection {:language "Clojure"})
      (is (= 1 (mc/count db collection)))))

  (deftest remove-a-single-document-from-collection
    (let [collection "libraries"
          oid        (ObjectId.)]
      (mc/insert-batch db collection [{:language "Clojure" :name "monger" :_id oid}])
      (mc/remove-by-id db collection oid)
      (is (= 0 (mc/count db collection)))
      (is (nil? (mc/find-by-id db collection oid)))))


  ;;
  ;; exists?, drop, create
  ;;

  (deftest checking-for-collection-existence-when-it-does-not-exist
    (let [collection "widgets"]
      (mc/drop db collection)
      (is (false? (mc/exists? db collection)))))

  (deftest checking-for-collection-existence-when-it-does-exist
    (let [collection "widgets"]
      (mc/drop db collection)
      (mc/insert-batch db collection [{:name "widget1"}
                                      {:name "widget2"}])
      (is (mc/exists? db collection))
      (mc/drop db collection)
      (is (false? (mc/exists? db collection)))
      (mc/create db "widgets" {:capped true :size 100000 :max 10})
      (is (mc/exists? db collection))
      (mc/rename db collection "gadgets")
      (is (not (mc/exists? db collection)))
      (is (mc/exists? db "gadgets"))
      (mc/drop db "gadgets")))

  ;;
  ;; any?, empty?
  ;;

  (deftest test-any-on-empty-collection
    (let [collection "things"]
      (is (not (mc/any? db collection)))))

  (deftest test-any-on-non-empty-collection
    (let [collection "things"
          _           (mc/insert db collection {:language "Clojure" :name "langohr"})]
      (is (mc/any? db "things" {:language "Clojure"}))))

  (deftest test-empty-on-empty-collection
    (let [collection "things"]
      (is (mc/empty? db collection))))

  (deftest test-empty-on-non-empty-collection
    (let [collection "things"
          _           (mc/insert db collection {:language "Clojure" :name "langohr"})]
      (is (not (mc/empty? db "things")))))


  ;;
  ;; distinct
  ;;

  (deftest test-distinct-values
    (let [collection "widgets"
          batch      [{:state "CA" :quantity 1 :price 199.00}
                      {:state "NY" :quantity 2 :price 199.00}
                      {:state "NY" :quantity 1 :price 299.00}
                      {:state "IL" :quantity 2 :price 11.50 }
                      {:state "CA" :quantity 2 :price 2.95  }
                      {:state "IL" :quantity 3 :price 5.50  }]]
      (mc/insert-batch db collection batch)
      (is (= ["CA" "IL" "NY"] (sort (mc/distinct db collection :state))))
      (is (= ["CA" "IL" "NY"] (sort (mc/distinct db collection :state {}))))
      (is (= ["CA" "NY"] (sort (mc/distinct db collection :state {:price {$gt 100.00}}))))))

  ;;
  ;; update
  ;;

  (let [coll "things"
        batch [{:_id 1 :type "rock" :size "small"}
               {:_id 2 :type "bed" :size "bed-sized"}
               {:_id 3 :type "bottle" :size "1.5 liters"}]]

    (deftest test-update
      (mc/insert-batch db coll batch)
      (is (= "small" (:size (mc/find-one-as-map db coll {:type "rock"}))))
      (mc/update db coll {:type "rock"} {"$set" {:size "huge"}})
      (is (= "huge" (:size (mc/find-one-as-map db coll {:type "rock"})))))

    (deftest test-upsert
      (is (mc/empty? db coll))
      (mc/upsert db coll {:_id 4} {"$set" {:size "tiny"}})
      (is (not (mc/empty? db coll)))
      (mc/upsert db coll {:_id 4} {"$set" {:size "big"}})
      (is (= [{:_id 4 :size "big"}] (mc/find-maps db coll {:_id 4}))))

    (deftest test-update-by-id
      (mc/insert-batch db coll batch)
      (is (= "bed" (:type (mc/find-one-as-map db coll {:_id 2}))))
      (mc/update-by-id db coll 2 {"$set" {:type "living room"}})
      (is (= "living room" (:type (mc/find-one-as-map db coll {:_id 2})))))

    (deftest test-update-by-ids
      (mc/insert-batch db coll batch)
      (is (= "bed" (:type (mc/find-one-as-map db coll {:_id 2}))))
      (is (= "bottle" (:type (mc/find-one-as-map db coll {:_id 3}))))
      (mc/update-by-ids db coll [2 3] {"$set" {:type "dog"}})
      (is (= "dog" (:type (mc/find-one-as-map db coll {:_id 2}))))
      (is (= "dog" (:type (mc/find-one-as-map db coll {:_id 3}))))))

  ;;
  ;; miscellenous
  ;;

  (deftest test-system-collection-predicate
    (are [name] (is (mc/system-collection? name))
         "system.indexes"
         "system"
         ;; we treat default GridFS collections as system ones,
         ;; possibly this is a bad idea, time will tell. MK.
         "fs.chunks"
         "fs.files")
    (are [name] (is (not (mc/system-collection? name)))
         "events"
         "accounts"
         "megacorp_account"
         "myapp_development")))
