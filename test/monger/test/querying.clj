(set! *warn-on-reflection* true)

(ns monger.test.querying
  (:refer-clojure :exclude [select find])
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject CommandResult$CommandFailure]
            [org.bson.types ObjectId]
            [java.util Date])
  (:require [monger core util]
            [monger.collection :as mgcol]
            [monger.result     :as mgres])
  (:use [clojure.test]
        [monger.test.fixtures]
        ;; [monger.query]
        [monger.conversion]))


(defn purge-locations-collection
  [f]
  (purge-collection "locations" f))

(use-fixtures :each purge-docs-collection purge-things-collection purge-locations-collection)

(monger.core/set-default-write-concern! WriteConcern/SAFE)


;;
;; monger.collection/* finders ("low-level API")
;;

;; by ObjectId

(deftest query-full-document-by-object-id
  (let [coll "docs"
        oid  (ObjectId.)
        doc  { :_id oid :title "Introducing Monger" }]
    (mgcol/insert coll doc)
    (is (= doc (mgcol/find-map-by-id coll oid)))
    (is (= doc (mgcol/find-one-as-map coll { :_id oid })))))


;; exact match over string field

(deftest query-full-document-with-find-maps-using-exact-matching-over-string-field
  (let [coll "docs"
        doc  { :title "monger" :language "Clojure" :_id (ObjectId.) }]
    (mgcol/insert coll doc)
    (is (= [doc] (mgcol/find-maps coll { :title "monger" })))
    (is (= doc (from-db-object (first (mgcol/find coll { :title "monger" })) true)))))


;; exact match over string field with limit

(deftest query-full-document-with-find-maps-using-exact-matching-over-string-with-field-with-limit
  (let [coll "docs"
        doc1  { :title "monger"  :language "Clojure" :_id (ObjectId.) }
        doc2  { :title "langohr" :language "Clojure" :_id (ObjectId.) }
        doc3  { :title "netty"   :language "Java" :_id (ObjectId.) }
        _     (mgcol/insert-batch coll [doc1 doc2 doc3])
        result (mgcol/find-maps coll { :title "monger" } {} 0 1)]
    (is (= 1 (count result)))
    (is (= [doc1] result))))


(deftest query-full-document-with-find-maps-using-exact-matching-over-string-field-with-limit-and-offset
  (let [coll "docs"
        doc1  { :title "lucene"    :language "Java" :_id (ObjectId.) }
        doc2  { :title "joda-time" :language "Java" :_id (ObjectId.) }
        doc3  { :title "netty"     :language "Java" :_id (ObjectId.) }
        _     (mgcol/insert-batch coll [doc1 doc2 doc3])
        ;; this example is indeed ugly, we need to come up with a DSL similar to what Casbah has
        ;; to make this easy to read and write. MK.
        result (from-db-object (seq (.sort (mgcol/find coll { :language "Java" } {} 1 2)
                                           (to-db-object { :title 1 })))
                               true)]
    (is (= 2 (count result)))
    (is (= [doc1 doc3] result))))


;; < ($lt), <= ($lte), > ($gt), >= ($gte)

;; (deftest query-using-dsl-and-$lt-operator
;;   (let [coll "docs"
;;         doc1 { :language "Clojure" :_id (ObjectId.) :inception_year 2006 }
;;         doc2 { :language "Java"    :_id (ObjectId.) :inception_year 1992 }
;;         doc3 { :language "Scala"   :_id (ObjectId.) :inception_year 2003 }
;;         _      (mgcol/insert-batch coll [doc1 doc2])
;;         lt-result (in-collection "docs"
;;                                  (find { :inception_year { "$lt" 2000 } })]
;;         (is (= [doc2] lt-result))))


  (deftest query-with-find-maps-using-$lt-operator
    (let [coll "docs"
          doc1 { :language "Clojure" :_id (ObjectId.) :inception_year 2006 }
          doc2 { :language "Java"    :_id (ObjectId.) :inception_year 1992 }
          doc3 { :language "Scala"   :_id (ObjectId.) :inception_year 2003 }
          _      (mgcol/insert-batch coll [doc1 doc2])
          lt-result  (mgcol/find-maps coll { :inception_year { "$lt"  2000 } })
          lte-result (mgcol/find-maps coll { :inception_year { "$lte" 1992 } })
          gt-result  (mgcol/find-maps coll { :inception_year { "$gt"  2005 } })
          gte-result (mgcol/find-maps coll { :inception_year { "$gte" 2006 } })]
      (is (= [doc2] lt-result))
      (is (= [doc2] lte-result))
      (is (= [doc1] gt-result))
      (is (= [doc1] gte-result))))


  ;; $all

  (deftest query-with-find-maps-using-$all
    (let [coll "docs"
          doc1 { :_id (ObjectId.) :title "Clojure" :tags ["functional" "homoiconic" "syntax-oriented" "dsls" "concurrency features" "jvm"] }
          doc2 { :_id (ObjectId.) :title "Java"    :tags ["object-oriented" "jvm"] }
          doc3 { :_id (ObjectId.) :title "Scala"   :tags ["functional" "object-oriented" "dsls" "concurrency features" "jvm"] }
          -    (mgcol/insert-batch coll [doc1 doc2 doc3])
          result1 (mgcol/find-maps coll { :tags { "$all" ["functional" "jvm" "homoiconic"] } })
          result2 (mgcol/find-maps coll { :tags { "$all" ["functional" "native" "homoiconic"] } })
          result3 (mgcol/find-maps coll { :tags { "$all" ["functional" "jvm" "dsls"] } })]
      (is (= [doc1] result1))
      (is (empty? result2))
      (is (= 2 (count result3)))))


  ;; $exists

  (deftest query-with-find-one-as-map-using-$exists
    (let [coll "docs"
          doc1 { :_id (ObjectId.) :published-by "Jill The Blogger" :draft false :title "X announces another Y" }
          doc2 { :_id (ObjectId.) :draft true :title "Z announces a Y competitor" }
          _    (mgcol/insert-batch coll [doc1 doc2])
          result1 (mgcol/find-one-as-map coll { :published-by { "$exists" true } })
          result2 (mgcol/find-one-as-map coll { :published-by { "$exists" false } })]
      (is (= doc1 result1))
      (is (= doc2 result2))))

  ;; $mod

  (deftest query-with-find-one-as-map-using-$mod
    (let [coll "docs"
          doc1 { :_id (ObjectId.) :counter 25 }
          doc2 { :_id (ObjectId.) :counter 32 }
          doc3 { :_id (ObjectId.) :counter 63 }
          _    (mgcol/insert-batch coll [doc1 doc2 doc3])
          result1 (mgcol/find-one-as-map coll { :counter { "$mod" [10, 5] } })
          result2 (mgcol/find-one-as-map coll { :counter { "$mod" [10, 2] } })
          result3 (mgcol/find-one-as-map coll { :counter { "$mod" [11, 1] } })]
      (is (= doc1 result1))
      (is (= doc2 result2))
      (is (empty? result3))))


  ;; $ne

  (deftest query-with-find-one-as-map-using-$ne
    (let [coll "docs"
          doc1 { :_id (ObjectId.) :counter 25 }
          doc2 { :_id (ObjectId.) :counter 32 }
          _    (mgcol/insert-batch coll [doc1 doc2])
          result1 (mgcol/find-one-as-map coll { :counter { "$ne" 25 } })
          result2 (mgcol/find-one-as-map coll { :counter { "$ne" 32 } })]
      (is (= doc2 result1))
      (is (= doc1 result2))))
