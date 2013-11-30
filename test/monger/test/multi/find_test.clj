(ns monger.test.multi.find-test
  (:import  [com.mongodb WriteResult WriteConcern DBCursor DBObject DBRef]
            org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.core        :as mg]
            [monger.util        :as mu]
            [monger.multi.collection  :as mgcol]
            [monger.test.helper :as helper]
            [monger.conversion :as mgcnv]
            [clojure.test :refer :all]
            [monger.operators :refer :all]
            [monger.test.fixtures :refer :all]
            [monger.conversion :refer :all]))

(helper/connect!)

(defn drop-altdb
  [f]
  (mg/drop-db "altdb")
  (f))

(use-fixtures :each drop-altdb)

;;
;; find
;;

(deftest find-full-document-when-collection-is-empty
  (let [db (mg/get-db "altdb")
        collection "docs"
        cursor     (mgcol/find db collection)]
    (is (empty? (iterator-seq cursor)))))

(deftest find-document-seq-when-collection-is-empty
  (let [db (mg/get-db "altdb")
        collection "docs"]
    (is (empty? (mgcol/find-seq db collection)))))

(deftest find-multiple-documents-when-collection-is-empty
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (is (empty? (mgcol/find db collection { :language "Scala" })))))

(deftest find-multiple-maps-when-collection-is-empty
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (is (empty? (mgcol/find-maps db collection { :language "Scala" })))))

(deftest find-multiple-documents-by-regex
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (mgcol/insert-batch db collection [{ :language "Clojure",    :name "monger" }
                                       { :language "Java",       :name "nhibernate" }
                                       { :language "JavaScript", :name "sprout-core" }])
    (is (= 2 (monger.core/count (mgcol/find db collection { :language #"Java*" }))))))

(deftest find-multiple-documents
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (mgcol/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                       { :language "Clojure", :name "langohr" }
                                       { :language "Clojure", :name "incanter" }
                                       { :language "Scala",   :name "akka" }])
    (is (= 1 (monger.core/count (mgcol/find db collection { :language "Scala"   }))))
    (is (= 3 (.count (mgcol/find db collection { :language "Clojure" }))))
    (is (empty? (mgcol/find db collection      { :language "Java"    })))))


(deftest find-document-specify-fields
  (let [db (mg/get-db "altdb")
        collection "libraries"
        _          (mgcol/insert db collection { :language "Clojure", :name "monger" })
        result     (mgcol/find db collection { :language "Clojure"} [:language])]
    (is (= (seq [:_id :language]) (keys (mgcnv/from-db-object (.next result) true))))))

(deftest find-and-iterate-over-multiple-documents-the-hard-way
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (mgcol/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                       { :language "Clojure", :name "langohr" }
                                       { :language "Clojure", :name "incanter" }
                                       { :language "Scala",   :name "akka" }])
    (doseq [doc (take 3 (map (fn [dbo]
                               (mgcnv/from-db-object dbo true))
                             (mgcol/find-seq db collection { :language "Clojure" })))]
      (is (= "Clojure" (:language doc))))))

(deftest find-and-iterate-over-multiple-documents
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (mgcol/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                       { :language "Clojure", :name "langohr" }
                                       { :language "Clojure", :name "incanter" }
                                       { :language "Scala",   :name "akka" }])
    (doseq [doc (take 3 (mgcol/find-maps db collection { :language "Clojure" }))]
      (is (= "Clojure" (:language doc))))))


(deftest find-multiple-maps
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (mgcol/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                       { :language "Clojure", :name "langohr" }
                                       { :language "Clojure", :name "incanter" }
                                       { :language "Scala",   :name "akka" }])
    (is (= 1 (count (mgcol/find-maps db collection { :language "Scala" }))))
    (is (= 3 (count (mgcol/find-maps db collection { :language "Clojure" }))))
    (is (empty? (mgcol/find-maps db collection      { :language "Java"    })))
    (is (empty? (mgcol/find-maps db collection { :language "Java" } [:language :name])))))



(deftest find-multiple-partial-documents
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (mgcol/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                       { :language "Clojure", :name "langohr" }
                                       { :language "Clojure", :name "incanter" }
                                       { :language "Scala",   :name "akka" }])
    (let [scala-libs   (mgcol/find db collection { :language "Scala" } [:name])
          clojure-libs (mgcol/find db collection { :language "Clojure"} [:language])]
      (is (= 1 (.count scala-libs)))
      (is (= 3 (.count clojure-libs)))
      (doseq [i clojure-libs]
        (let [doc (mgcnv/from-db-object i true)]
          (is (= (:language doc) "Clojure"))))
      (is (empty? (mgcol/find db collection { :language "Erlang" } [:name]))))))

(deftest finds-one-as-map
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (mgcol/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                       { :language "Clojure", :name "langohr" }])
    (let [res (mgcol/find-one-as-map db collection { :name "langohr" })]
      (is (map? res))
      (is (= "langohr" (:name res)))
      (is (= "Clojure" (:language res))))
    (is (= 2 (count (mgcol/find-one-as-map db collection { :name "langohr" } [:name]))))
    (is (= "langohr" (get (mgcol/find-one-as-map db collection { :name "langohr" } [:name] false) "name")))))

(deftest find-and-modify
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (mgcol/insert-batch db collection [{ :language "Clojure", :name "monger" }
                                       { :language "Clojure", :name "langohr" }])))

