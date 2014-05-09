(ns monger.test.regular-finders-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger core util]
            [monger.collection :as mgcol]
            [monger.result     :as mgres]
            [monger.conversion :as mgcnv]
            [monger.test.helper :as helper]
            [clojure.test :refer :all]
            [monger.operators :refer :all]
            [monger.test.fixtures :refer :all]))

(helper/connect!)

(use-fixtures :each purge-people purge-docs purge-things purge-libraries
  purge-finder-docs)


;;
;; find-one
;;

(deftest find-one-full-document-when-collection-is-empty
  (let [collection "regular_finders_docs"]
    (is (nil? (mgcol/find-one collection {})))))

(deftest find-one-full-document-as-map-when-collection-is-empty
  (let [collection "regular_finders_docs"]
    (mgcol/remove collection)
    (is (nil? (mgcol/find-one-as-map collection {})))))


(deftest find-one-full-document-when-collection-has-matches
  (let [collection "regular_finders_docs"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }
        _          (mgcol/insert collection doc)
        found-one (mgcol/find-one collection { :language "Clojure" })]
    (is found-one)
    (is (= (:_id doc) (monger.util/get-id found-one)))
    (is (= (mgcnv/from-db-object found-one true) doc))
    (is (= (mgcnv/to-db-object doc) found-one))))

(deftest find-one-full-document-as-map-when-collection-has-matches
  (let [collection "regular_finders_docs"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
    (mgcol/insert collection doc)
    (is (= doc (mgcol/find-one-as-map collection { :language "Clojure" })))))



(deftest find-one-partial-document-when-collection-has-matches
  (let [collection "regular_finders_docs"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }
        _          (mgcol/insert collection doc)
        loaded     (mgcol/find-one collection { :language "Clojure" } [:language])]
    (is (nil? (.get ^DBObject loaded "data-store")))
    (is (= doc-id (monger.util/get-id loaded)))
    (is (= "Clojure" (.get ^DBObject loaded "language")))))


(deftest find-one-partial-document-using-field-negation-when-collection-has-matches
  (let [collection       "regular_finders_docs"
        doc-id           (monger.util/random-uuid)
        doc              { :data-store "MongoDB", :language "Clojure", :_id doc-id }
        _                (mgcol/insert collection doc)
        ^DBObject loaded (mgcol/find-one collection { :language "Clojure" } {:data-store 0 :_id 0})]
    (is (nil? (.get loaded "data-store")))
    (is (nil? (.get loaded "_id")))
    (is (nil? (monger.util/get-id loaded)))
    (is (= "Clojure" (.get loaded "language")))))


(deftest find-one-partial-document-as-map-when-collection-has-matches
  (let [collection "regular_finders_docs"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
    (mgcol/insert collection doc)
    (is (= { :data-store "MongoDB", :_id doc-id } (mgcol/find-one-as-map collection { :language "Clojure" } [:data-store])))))


(deftest find-one-partial-document-as-map-when-collection-has-matches-with-keywordize
  (let [collection "regular_finders_docs"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }
        fields     [:data-store]
        _id        (mgcol/insert collection doc)
        loaded     (mgcol/find-one-as-map collection { :language "Clojure" } fields true)
        ]
    (is (= { :data-store "MongoDB", :_id doc-id } loaded ))))


(deftest find-one-partial-document-as-map-when-collection-has-matches-with-keywordize-false
  (let [collection "regular_finders_docs"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }
        fields     [:data-store]
        _id        (mgcol/insert collection doc)
        loaded     (mgcol/find-one-as-map collection { :language "Clojure" } fields false)
        ]
    (is (= { "_id" doc-id, "data-store" "MongoDB" } loaded ))))

;;
;; find-by-id
;;

(deftest find-full-document-by-string-id-when-that-document-does-not-exist
  (let [collection "libraries"
        doc-id     (monger.util/random-uuid)]
    (is (nil? (mgcol/find-by-id collection doc-id)))))

(deftest find-full-document-by-string-id-when-id-is-nil
  (let [collection "libraries"
        doc-id     nil]
    (is (thrown? IllegalArgumentException (mgcol/find-by-id collection doc-id)))))

(deftest find-full-document-by-object-id-when-that-document-does-not-exist
  (let [collection "libraries"
        doc-id     (ObjectId.)]
    (is (nil? (mgcol/find-by-id collection doc-id)))))

(deftest find-full-document-by-id-as-map-when-that-document-does-not-exist
  (let [collection "libraries"
        doc-id     (monger.util/random-uuid)]
    (is (nil? (mgcol/find-map-by-id collection doc-id)))))

(deftest find-full-document-by-id-as-map-when-id-is-nil
  (let [collection "libraries"
        doc-id     nil]
    (is (thrown? IllegalArgumentException
                 (mgcol/find-map-by-id collection doc-id)))))


(deftest find-full-document-by-string-id-when-document-does-exist
  (let [collection "libraries"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
    (mgcol/insert collection doc)
    (is (= (doc (mgcol/find-by-id collection doc-id))))))

(deftest find-full-document-by-object-id-when-document-does-exist
  (let [collection "libraries"
        doc-id     (ObjectId.)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
    (mgcol/insert collection doc)
    (is (= (doc (mgcol/find-by-id collection doc-id))))))

(deftest find-full-document-map-by-string-id-when-document-does-exist
  (let [collection "libraries"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
    (mgcol/insert collection doc)
    (is (= (doc (mgcol/find-map-by-id collection doc-id))))))

(deftest find-full-document-map-by-object-id-when-document-does-exist
  (let [collection "libraries"
        doc-id     (ObjectId.)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
    (mgcol/insert collection doc)
    (is (= (doc (mgcol/find-map-by-id collection doc-id))))))

(deftest find-partial-document-by-id-when-document-does-exist
  (let [collection "libraries"
        doc-id     (monger.util/random-uuid)
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
    (mgcol/insert collection doc)
    (is (= ({ :language "Clojure" } (mgcol/find-by-id collection doc-id [ :language ]))))))


(deftest find-partial-document-as-map-by-id-when-document-does-exist
  (let [collection "libraries"
        doc-id     (monger.util/random-uuid)
        fields     [:data-store]                
        doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }
        _          (mgcol/insert collection doc)        
        loaded     (mgcol/find-map-by-id collection doc-id [ :language ])]      
    (is (= { :language "Clojure", :_id doc-id } loaded ))
  )
)


;;
;; find
;;

(deftest find-full-document-when-collection-is-empty
  (let [collection "regular_finders_docs"
        cursor     (mgcol/find collection)]
    (is (empty? (iterator-seq cursor)))))

(deftest find-document-seq-when-collection-is-empty
  (let [collection "regular_finders_docs"]
    (is (empty? (mgcol/find-seq collection)))))

(deftest find-multiple-documents-when-collection-is-empty
  (let [collection "libraries"]
    (is (empty? (mgcol/find collection { :language "Scala" })))))

(deftest find-multiple-maps-when-collection-is-empty
  (let [collection "libraries"]
    (is (empty? (mgcol/find-maps collection { :language "Scala" })))))

(deftest find-multiple-documents-by-regex
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{ :language "Clojure",    :name "monger" }
                                    { :language "Java",       :name "nhibernate" }
                                    { :language "JavaScript", :name "sprout-core" }])
    (is (= 2 (monger.core/count (mgcol/find collection { :language #"Java*" }))))))

(deftest find-multiple-documents
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{ :language "Clojure", :name "monger" }
                                    { :language "Clojure", :name "langohr" }
                                    { :language "Clojure", :name "incanter" }
                                    { :language "Scala",   :name "akka" }])
    (is (= 1 (monger.core/count (mgcol/find collection { :language "Scala"   }))))
    (is (= 3 (.count (mgcol/find collection { :language "Clojure" }))))
    (is (empty? (mgcol/find collection      { :language "Java"    })))))


(deftest find-document-specify-fields
  (let [collection "libraries"
        _          (mgcol/insert collection { :language "Clojure", :name "monger" })
        result     (mgcol/find collection { :language "Clojure"} [:language])]
    (is (= (seq [:_id :language]) (keys (mgcnv/from-db-object (.next result) true))))))

(deftest find-and-iterate-over-multiple-documents-the-hard-way
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{ :language "Clojure", :name "monger" }
                                    { :language "Clojure", :name "langohr" }
                                    { :language "Clojure", :name "incanter" }
                                    { :language "Scala",   :name "akka" }])
    (doseq [doc (take 3 (map (fn [dbo]
                               (mgcnv/from-db-object dbo true))
                             (mgcol/find-seq collection { :language "Clojure" })))]
      (is (= "Clojure" (:language doc))))))

(deftest find-and-iterate-over-multiple-documents
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{ :language "Clojure", :name "monger" }
                                    { :language "Clojure", :name "langohr" }
                                    { :language "Clojure", :name "incanter" }
                                    { :language "Scala",   :name "akka" }])
    (doseq [doc (take 3 (mgcol/find-maps collection { :language "Clojure" }))]
      (is (= "Clojure" (:language doc))))))


(deftest find-multiple-maps
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{ :language "Clojure", :name "monger" }
                                    { :language "Clojure", :name "langohr" }
                                    { :language "Clojure", :name "incanter" }
                                    { :language "Scala",   :name "akka" }])
    (is (= 1 (clojure.core/count (mgcol/find-maps collection { :language "Scala" }))))
    (is (= 3 (.count (mgcol/find-maps collection { :language "Clojure" }))))
    (is (empty? (mgcol/find-maps collection      { :language "Java"    })))
    (is (empty? (mgcol/find-maps monger.core/*mongodb-database* collection { :language "Java" } [:language :name])))))



(deftest find-multiple-partial-documents
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{ :language "Clojure", :name "monger" }
                                    { :language "Clojure", :name "langohr" }
                                    { :language "Clojure", :name "incanter" }
                                    { :language "Scala",   :name "akka" }])
    (let [scala-libs   (mgcol/find collection { :language "Scala" } [:name])
          clojure-libs (mgcol/find collection { :language "Clojure"} [:language])]
      (is (= 1 (.count scala-libs)))
      (is (= 3 (.count clojure-libs)))
      (doseq [i clojure-libs]
        (let [doc (mgcnv/from-db-object i true)]
          (is (= (:language doc) "Clojure"))))
      (is (empty? (mgcol/find collection { :language "Erlang" } [:name]))))))
