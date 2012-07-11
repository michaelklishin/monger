(set! *warn-on-reflection* true)

(ns monger.test.querying-test
  (:refer-clojure :exclude [select find sort])
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject CommandResult$CommandFailure ReadPreference]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger core util]
            [monger.collection  :as mgcol]
            [monger.result      :as mgres]
            [monger.test.helper :as helper])
  (:use clojure.test
        monger.test.fixtures
        [monger conversion query operators joda-time]
        [clj-time.core :only [date-time]]))

(helper/connect!)

(use-fixtures :each purge-docs purge-things purge-locations)


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

(deftest query-full-document-using-exact-matching-over-string-field
  (let [coll "docs"
        doc  { :title "monger" :language "Clojure" :_id (ObjectId.) }]
    (mgcol/insert coll doc)
    (is (= [doc] (mgcol/find-maps coll { :title "monger" })))
    (is (= doc (from-db-object (first (mgcol/find coll { :title "monger" })) true)))))


;; exact match over string field with limit

(deftest query-full-document-using-exact-matching-over-string-with-field-with-limit
  (let [coll "docs"
        doc1  { :title "monger"  :language "Clojure" :_id (ObjectId.) }
        doc2  { :title "langohr" :language "Clojure" :_id (ObjectId.) }
        doc3  { :title "netty"   :language "Java" :_id (ObjectId.) }
        _     (mgcol/insert-batch coll [doc1 doc2 doc3])
        result (with-collection coll
                 (find { :title "monger" })
                 (fields [:title, :language, :_id])
                 (skip 0)
                 (limit 1))]
    (is (= 1 (count result)))
    (is (= [doc1] result))))


(deftest query-full-document-using-exact-matching-over-string-field-with-limit-and-offset
  (let [coll "docs"
        doc1  { :title "lucene"    :language "Java" :_id (ObjectId.) }
        doc2  { :title "joda-time" :language "Java" :_id (ObjectId.) }
        doc3  { :title "netty"     :language "Java" :_id (ObjectId.) }
        _     (mgcol/insert-batch coll [doc1 doc2 doc3])
        result (with-collection coll
                 (find { :language "Java" })
                 (skip 1)
                 (limit 2)
                 (sort { :title 1 }))]
    (is (= 2 (count result)))
    (is (= [doc1 doc3] result))))


;; < ($lt), <= ($lte), > ($gt), >= ($gte)

(deftest query-using-dsl-and-$lt-operator-with-integers
  (let [coll "docs"
        doc1 { :language "Clojure" :_id (ObjectId.) :inception_year 2006 }
        doc2 { :language "Java"    :_id (ObjectId.) :inception_year 1992 }
        doc3 { :language "Scala"   :_id (ObjectId.) :inception_year 2003 }
        _      (mgcol/insert-batch coll [doc1 doc2])
        lt-result (with-collection "docs"
                    (find { :inception_year { $lt 2000 } })
                    (limit 2))]
    (is (= [doc2] (vec lt-result)))))


(deftest query-using-dsl-and-$lt-operator-with-dates
  (let [coll "docs"
        ;; these rely on monger.joda-time being loaded. MK.
        doc1 { :language "Clojure" :_id (ObjectId.) :inception_year (date-time 2006 1 1) }
        doc2 { :language "Java"    :_id (ObjectId.) :inception_year (date-time 1992 1 2) }
        doc3 { :language "Scala"   :_id (ObjectId.) :inception_year (date-time 2003 3 3) }
        _    (mgcol/insert-batch coll [doc1 doc2])
        lt-result (with-collection "docs"
                    (find { :inception_year { $lt (date-time 2000 1 2) } })
                    (limit 2))]
    (is (= (map :_id [doc2])
           (map :_id (vec lt-result))))))

(deftest query-using-both-$lte-and-$gte-operators-with-dates
  (let [coll "docs"
        ;; these rely on monger.joda-time being loaded. MK.
        doc1 { :language "Clojure" :_id (ObjectId.) :inception_year (date-time 2006 1 1) }
        doc2 { :language "Java"    :_id (ObjectId.) :inception_year (date-time 1992 1 2) }
        doc3 { :language "Scala"   :_id (ObjectId.) :inception_year (date-time 2003 3 3) }
        _    (mgcol/insert-batch coll [doc1 doc2 doc3])
        lt-result (with-collection "docs"
                    (find { :inception_year { $gt (date-time 2000 1 2) $lte (date-time 2007 2 2) } })
                    (sort { :inception_year 1 }))]
    (is (= (map :_id [doc3 doc1])
           (map :_id (vec lt-result))))))


(deftest query-using-$gt-$lt-$gte-$lte-operators-as-strings
  (let [coll "docs"
        doc1 { :language "Clojure" :_id (ObjectId.) :inception_year 2006 }
        doc2 { :language "Java"    :_id (ObjectId.) :inception_year 1992 }
        doc3 { :language "Scala"   :_id (ObjectId.) :inception_year 2003 }
        _    (mgcol/insert-batch coll [doc1 doc2 doc3])]
    (are [doc, result]
         (= doc, result)
         (doc2 (with-collection coll
                 (find { :inception_year { "$lt"  2000 } })))
         (doc2 (with-collection coll
                 (find { :inception_year { "$lte" 1992 } })))
         (doc1 (with-collection coll
                 (find { :inception_year { "$gt"  2002 } })
                 (limit 1)
                 (sort { :inception_year -1 })))
         (doc1 (with-collection coll
                 (find { :inception_year { "$gte" 2006 } }))))))


(deftest query-using-$gt-$lt-$gte-$lte-operators-using-dsl-composition
  (let [coll "docs"
        doc1 { :language "Clojure" :_id (ObjectId.) :inception_year 2006 }
        doc2 { :language "Java"    :_id (ObjectId.) :inception_year 1992 }
        doc3 { :language "Scala"   :_id (ObjectId.) :inception_year 2003 }
        srt  (-> {}
                 (limit 1)
                 (sort { :inception_year -1 }))
        _    (mgcol/insert-batch coll [doc1 doc2 doc3])]
    (is (= [doc1] (with-collection coll
                    (find { :inception_year { "$gt"  2002 } })
                    (merge srt))))))


;; $all

(deftest query-with-using-$all
  (let [coll "docs"
        doc1 { :_id (ObjectId.) :title "Clojure" :tags ["functional" "homoiconic" "syntax-oriented" "dsls" "concurrency features" "jvm"] }
        doc2 { :_id (ObjectId.) :title "Java"    :tags ["object-oriented" "jvm"] }
        doc3 { :_id (ObjectId.) :title "Scala"   :tags ["functional" "object-oriented" "dsls" "concurrency features" "jvm"] }
        -    (mgcol/insert-batch coll [doc1 doc2 doc3])
        result1 (with-collection coll
                  (find { :tags { "$all" ["functional" "jvm" "homoiconic"] } }))
        result2 (with-collection coll
                  (find { :tags { "$all" ["functional" "native" "homoiconic"] } }))
        result3 (with-collection coll
                  (find { :tags { "$all" ["functional" "jvm" "dsls"] } })
                  (sort { :title 1 }))]
    (is (= [doc1] result1))
    (is (empty? result2))
    (is (= 2 (count result3)))
    (is (= doc1 (first result3)))))


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

;;
;; monger.query DSL features
;;

;; pagination
(deftest query-using-pagination-dsl
  (let [coll "docs"
        doc1 { :_id (ObjectId.) :title "Clojure" :tags ["functional" "homoiconic" "syntax-oriented" "dsls" "concurrency features" "jvm"] }
        doc2 { :_id (ObjectId.) :title "Java"    :tags ["object-oriented" "jvm"] }
        doc3 { :_id (ObjectId.) :title "Scala"   :tags ["functional" "object-oriented" "dsls" "concurrency features" "jvm"] }
        doc4 { :_id (ObjectId.) :title "Ruby"    :tags ["dynamic" "object-oriented" "dsls" "jvm"] }
        doc5 { :_id (ObjectId.) :title "Groovy"  :tags ["dynamic" "object-oriented" "dsls" "jvm"] }
        doc6 { :_id (ObjectId.) :title "OCaml"   :tags ["functional" "static" "dsls"] }
        doc7 { :_id (ObjectId.) :title "Haskell" :tags ["functional" "static" "dsls" "concurrency features"] }
        -    (mgcol/insert-batch coll [doc1 doc2 doc3 doc4 doc5 doc6 doc7])
        result1 (with-collection coll
                  (find {})
                  (paginate :page 1 :per-page 3)
                  (sort { :title 1 })
                  (read-preference ReadPreference/PRIMARY)
                  (options com.mongodb.Bytes/QUERYOPTION_NOTIMEOUT))
        result2 (with-collection coll
                  (find {})
                  (paginate :page 2 :per-page 3)
                  (sort { :title 1 }))
        result3 (with-collection coll
                  (find {})
                  (paginate :page 3 :per-page 3)
                  (sort { :title 1 }))
        result4 (with-collection coll
                  (find {})
                  (paginate :page 10 :per-page 3)
                  (sort { :title 1 }))]
    (is (= [doc1 doc5 doc7] result1))
    (is (= [doc2 doc6 doc4] result2))
    (is (= [doc3] result3))
    (is (empty? result4))))


(deftest combined-querying-dsl-example1
  (let [coll "docs"
        ma-doc { :_id (ObjectId.) :name "Massachusetts" :iso "MA" :population 6547629  :joined_in 1788 :capital "Boston" }
        de-doc { :_id (ObjectId.) :name "Delaware"      :iso "DE" :population 897934   :joined_in 1787 :capital "Dover"  }
        ny-doc { :_id (ObjectId.) :name "New York"      :iso "NY" :population 19378102 :joined_in 1788 :capital "Albany" }
        ca-doc { :_id (ObjectId.) :name "California"    :iso "CA" :population 37253956 :joined_in 1850 :capital "Sacramento" }
        tx-doc { :_id (ObjectId.) :name "Texas"         :iso "TX" :population 25145561 :joined_in 1845 :capital "Austin" }
        top3               (partial-query (limit 3))
        by-population-desc (partial-query (sort { :population -1 }))
        _                  (mgcol/insert-batch coll [ma-doc de-doc ny-doc ca-doc tx-doc])
        result             (with-collection coll
                             (find {})
                             (merge top3)
                             (merge by-population-desc))]
    (is (= result [ca-doc tx-doc ny-doc]))))

(deftest combined-querying-dsl-example2
  (let [coll "docs"
        ma-doc { :_id (ObjectId.) :name "Massachusetts" :iso "MA" :population 6547629  :joined_in 1788 :capital "Boston" }
        de-doc { :_id (ObjectId.) :name "Delaware"      :iso "DE" :population 897934   :joined_in 1787 :capital "Dover"  }
        ny-doc { :_id (ObjectId.) :name "New York"      :iso "NY" :population 19378102 :joined_in 1788 :capital "Albany" }
        ca-doc { :_id (ObjectId.) :name "California"    :iso "CA" :population 37253956 :joined_in 1850 :capital "Sacramento" }
        tx-doc { :_id (ObjectId.) :name "Texas"         :iso "TX" :population 25145561 :joined_in 1845 :capital "Austin" }
        top3               (partial-query (limit 3))
        by-population-desc (partial-query (sort { :population -1 }))
        _                  (mgcol/insert-batch coll [ma-doc de-doc ny-doc ca-doc tx-doc])
        result             (with-collection coll
                             (find {})
                             (merge top3)
                             (merge by-population-desc)
                             (keywordize-fields false))]
    ;; documents have fields as strings,
    ;; not keywords
    (is (= (map #(% "name") result)
           (map #(% :name) [ca-doc tx-doc ny-doc])))))
