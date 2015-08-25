(ns monger.test.regular-finders-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.util :as mu]
            [monger.result     :as mgres]
            [monger.conversion :as mgcnv]
            [clojure.test :refer :all]
            [monger.operators :refer :all]))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (use-fixtures :each (fn [f]
                        (mc/remove db "people")
                        (mc/remove db "docs")
                        (mc/remove db "regular_finders_docs")
                        (mc/remove db "things")
                        (mc/remove db "libraries")
                        (f)
                        (mc/remove db "people")
                        (mc/remove db "docs")
                        (mc/remove db "regular_finders_docs")
                        (mc/remove db "things")
                        (mc/remove db "libraries")))

  ;;
  ;; find-one
  ;;

  (deftest find-one-full-document-when-collection-is-empty
    (let [collection "regular_finders_docs"]
      (is (nil? (mc/find-one db collection {})))))

  (deftest find-one-full-document-as-map-when-collection-is-empty
    (let [collection "regular_finders_docs"]
      (mc/remove db collection)
      (is (nil? (mc/find-one-as-map db collection {})))))


  (deftest find-one-full-document-when-collection-has-matches
    (let [collection "regular_finders_docs"
          doc-id     (mu/random-uuid)
          doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }
          _          (mc/insert db collection doc)
          found-one (mc/find-one db collection { :language "Clojure" })]
      (is found-one)
      (is (= (:_id doc) (mu/get-id found-one)))
      (is (= (mgcnv/from-db-object found-one true) doc))
      (is (= (mgcnv/to-db-object doc) found-one))))

  (deftest find-one-full-document-as-map-when-collection-has-matches
    (let [collection "regular_finders_docs"
          doc-id     (mu/random-uuid)
          doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
      (mc/insert db collection doc)
      (is (= doc (mc/find-one-as-map db collection { :language "Clojure" })))))



  (deftest find-one-partial-document-when-collection-has-matches
    (let [collection "regular_finders_docs"
          doc-id     (mu/random-uuid)
          doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }
          _          (mc/insert db collection doc)
          loaded     (mc/find-one db collection { :language "Clojure" } [:language])]
      (is (nil? (.get ^DBObject loaded "data-store")))
      (is (= doc-id (mu/get-id loaded)))
      (is (= "Clojure" (.get ^DBObject loaded "language")))))


  (deftest find-one-partial-document-using-field-negation-when-collection-has-matches
    (let [collection       "regular_finders_docs"
          doc-id           (mu/random-uuid)
          doc              { :data-store "MongoDB", :language "Clojure", :_id doc-id }
          _                (mc/insert db collection doc)
          ^DBObject loaded (mc/find-one db collection { :language "Clojure" } {:data-store 0 :_id 0})]
      (is (nil? (.get loaded "data-store")))
      (is (nil? (.get loaded "_id")))
      (is (nil? (mu/get-id loaded)))
      (is (= "Clojure" (.get loaded "language")))))


  (deftest find-one-partial-document-as-map-when-collection-has-matches
    (let [collection "regular_finders_docs"
          doc-id     (mu/random-uuid)
          doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
      (mc/insert db collection doc)
      (is (= { :data-store "MongoDB", :_id doc-id }
             (mc/find-one-as-map db collection { :language "Clojure" } [:data-store])))))


  (deftest find-one-partial-document-as-map-when-collection-has-matches-with-keywordize
    (let [collection "regular_finders_docs"
          doc-id     (mu/random-uuid)
          doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }
          fields     [:data-store]
          _id        (mc/insert db collection doc)
          loaded     (mc/find-one-as-map db collection { :language "Clojure" } fields true)
          ]
      (is (= { :data-store "MongoDB", :_id doc-id } loaded ))))


  (deftest find-one-partial-document-as-map-when-collection-has-matches-with-keywordize-false
    (let [collection "regular_finders_docs"
          doc-id     (mu/random-uuid)
          doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }
          fields     [:data-store]
          _id        (mc/insert db collection doc)
          loaded     (mc/find-one-as-map db collection { :language "Clojure" } fields false)]
      (is (= { "_id" doc-id, "data-store" "MongoDB" } loaded ))))

  ;;
  ;; find-by-id
  ;;

  (deftest find-full-document-by-string-id-when-that-document-does-not-exist
    (let [collection "libraries"
          doc-id     (mu/random-uuid)]
      (is (nil? (mc/find-by-id db collection doc-id)))))

  (deftest find-full-document-by-string-id-when-id-is-nil
    (let [collection "libraries"
          doc-id     nil]
      (is (thrown? IllegalArgumentException (mc/find-by-id db collection doc-id)))))

  (deftest find-full-document-by-object-id-when-that-document-does-not-exist
    (let [collection "libraries"
          doc-id     (ObjectId.)]
      (is (nil? (mc/find-by-id db collection doc-id)))))

  (deftest find-full-document-by-id-as-map-when-that-document-does-not-exist
    (let [collection "libraries"
          doc-id     (mu/random-uuid)]
      (is (nil? (mc/find-map-by-id db collection doc-id)))))

  (deftest find-full-document-by-id-as-map-when-id-is-nil
    (let [collection "libraries"
          doc-id     nil]
      (is (thrown? IllegalArgumentException
                   (mc/find-map-by-id db collection doc-id)))))


  (deftest find-full-document-by-string-id-when-document-does-exist
    (let [collection "libraries"
          doc-id     (mu/random-uuid)
          doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
      (mc/insert db collection doc)
      (is (= (doc (mc/find-by-id db collection doc-id))))))

  (deftest find-full-document-by-object-id-when-document-does-exist
    (let [collection "libraries"
          doc-id     (ObjectId.)
          doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
      (mc/insert db collection doc)
      (is (= (doc (mc/find-by-id db collection doc-id))))))

  (deftest find-full-document-map-by-string-id-when-document-does-exist
    (let [collection "libraries"
          doc-id     (mu/random-uuid)
          doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
      (mc/insert db collection doc)
      (is (= (doc (mc/find-map-by-id db collection doc-id))))))

  (deftest find-full-document-map-by-object-id-when-document-does-exist
    (let [collection "libraries"
          doc-id     (ObjectId.)
          doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
      (mc/insert db collection doc)
      (is (= (doc (mc/find-map-by-id db collection doc-id))))))

  (deftest find-partial-document-by-id-when-document-does-exist
    (let [collection "libraries"
          doc-id     (mu/random-uuid)
          doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }]
      (mc/insert db collection doc)
      (is (= ({ :language "Clojure" }
              (mc/find-by-id db collection doc-id [ :language ]))))))


  (deftest find-partial-document-as-map-by-id-when-document-does-exist
    (let [collection "libraries"
          doc-id     (mu/random-uuid)
          fields     [:data-store]                
          doc        { :data-store "MongoDB", :language "Clojure", :_id doc-id }
          _          (mc/insert db collection doc)        
          loaded     (mc/find-map-by-id db collection doc-id [ :language ])]      
      (is (= { :language "Clojure", :_id doc-id } loaded ))))


  ;;
  ;; find
  ;;

  (deftest find-full-document-when-collection-is-empty
    (let [collection "regular_finders_docs"
          cursor     (mc/find db collection)]
      (is (empty? (iterator-seq cursor)))))

  (deftest find-document-seq-when-collection-is-empty
    (let [collection "regular_finders_docs"]
      (is (empty? (mc/find-seq db collection)))))

  (deftest find-multiple-documents-when-collection-is-empty
    (let [collection "libraries"]
      (is (empty? (mc/find db collection { :language "Scala" })))))

  (deftest find-multiple-maps-when-collection-is-empty
    (let [collection "libraries"]
      (is (empty? (mc/find-maps db collection { :language "Scala" })))))

  (deftest find-multiple-documents-by-regex
    (let [collection "libraries"]
      (mc/insert-batch db collection [{ :language "Clojure",    :name "monger" }
                                      { :language "Java",       :name "nhibernate" }
                                      { :language "JavaScript", :name "sprout-core" }])
      (is (= 2 (monger.core/count (mc/find db collection { :language #"Java*" }))))))

  (deftest find-multiple-documents
    (let [collection "libraries"]
      (mc/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                      { :language "Clojure", :name "langohr" }
                                      { :language "Clojure", :name "incanter" }
                                      { :language "Scala",   :name "akka" }])
      (is (= 1 (monger.core/count (mc/find db collection { :language "Scala"   }))))
      (is (= 3 (.count (mc/find db collection { :language "Clojure" }))))
      (is (empty? (mc/find db collection      { :language "Java"    })))))


  (deftest find-document-specify-fields
    (let [collection "libraries"
          _          (mc/insert db collection { :language "Clojure", :name "monger" })
          result     (mc/find db collection { :language "Clojure"} [:language])]
      (is (= (set [:_id :language]) (-> (mgcnv/from-db-object (.next result) true) keys set)))))

  (deftest find-and-iterate-over-multiple-documents-the-hard-way
    (let [collection "libraries"]
      (mc/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                      { :language "Clojure", :name "langohr" }
                                      { :language "Clojure", :name "incanter" }
                                      { :language "Scala",   :name "akka" }])
      (doseq [doc (take 3 (map (fn [dbo]
                                 (mgcnv/from-db-object dbo true))
                               (mc/find-seq db collection { :language "Clojure" })))]
        (is (= "Clojure" (:language doc))))))

  (deftest find-and-iterate-over-multiple-documents
    (let [collection "libraries"]
      (mc/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                      { :language "Clojure", :name "langohr" }
                                      { :language "Clojure", :name "incanter" }
                                      { :language "Scala",   :name "akka" }])
      (doseq [doc (take 3 (mc/find-maps db collection { :language "Clojure" }))]
        (is (= "Clojure" (:language doc))))))


  (deftest find-multiple-maps
    (let [collection "libraries"]
      (mc/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                      { :language "Clojure", :name "langohr" }
                                      { :language "Clojure", :name "incanter" }
                                      { :language "Scala",   :name "akka" }])
      (is (= 1 (clojure.core/count (mc/find-maps db collection { :language "Scala" }))))
      (is (= 3 (.count (mc/find-maps db collection { :language "Clojure" }))))
      (is (empty? (mc/find-maps db collection      { :language "Java"    })))
      (is (empty? (mc/find-maps db collection { :language "Java" } [:language :name])))))



  (deftest find-multiple-partial-documents
    (let [collection "libraries"]
      (mc/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                      { :language "Clojure", :name "langohr" }
                                      { :language "Clojure", :name "incanter" }
                                      { :language "Scala",   :name "akka" }])
      (let [scala-libs   (mc/find db collection { :language "Scala" } [:name])
            clojure-libs (mc/find db collection { :language "Clojure"} [:language])]
        (is (= 1 (.count scala-libs)))
        (is (= 3 (.count clojure-libs)))
        (doseq [i clojure-libs]
          (let [doc (mgcnv/from-db-object i true)]
            (is (= (:language doc) "Clojure"))))
        (is (empty? (mc/find db collection { :language "Erlang" } [:name]))))))

  (deftest find-maps-with-keywordize-false
    (let [collection "libraries"]
      (mc/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                      { :language "Clojure", :name "langohr" }])
      (let [results (mc/find-maps db collection {:name "langohr"} [] false)]
        (is (= 1 (.count results)))
        (is (= (get (first results) "language") "Clojure"))))))
