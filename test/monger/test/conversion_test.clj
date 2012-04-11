(ns monger.test.conversion-test
  (:require [monger core collection])
  (:import [com.mongodb DBObject BasicDBObject BasicDBList]
           [java.util Date Calendar List ArrayList]
           org.bson.types.ObjectId)
  (:use clojure.test monger.conversion))


;;
;; Clojure to DBObject
;;

(deftest convert-nil-to-dbobject
  (let [input  nil
        output (to-db-object input)]
    (is (nil? output))))

(deftest convert-integer-to-dbobject
  (let [input  1
        output (to-db-object input)]
    (is (= input output))))

(deftest convert-float-to-dbobject
  (let [input  11.12
        output (to-db-object input)]
    (is (= input output))))

(deftest convert-rationale-to-dbobject
  (let [input  11/2
        output (to-db-object input)]
    (is (= 5.5 output))))

(deftest convert-string-to-dbobject
  (let [input  "MongoDB"
        output (to-db-object input)]
    (is (= input output))))


(deftest convert-map-to-dbobject
  (let [input  { :int 1, :string "Mongo", :float 22.23 }
        output ^DBObject (to-db-object input)]
    (is (= 1 (.get output "int")))
    (is (= "Mongo" (.get output "string")))
    (is (= 22.23 (.get output "float")))))


(deftest convert-nested-map-to-dbobject
  (let [input  { :int 1, :string "Mongo", :float 22.23, :map { :int 10, :string "Clojure", :float 11.9, :list '(1 "a" :b), :map { :key "value" } } }
        output ^DBObject (to-db-object input)
        inner  ^DBObject (.get output "map")]
    (is (= 10           (.get inner "int")))
    (is (= "Clojure"    (.get inner "string")))
    (is (= 11.9         (.get inner "float")))
    (is (= '(1 "a" "b") (.get inner "list")))
    (is (= { "key" "value" } (.get inner "map")))))

;; for cases when you want to pass in a DBObject, for example,
;; to obtain _id that was generated. MK.
(deftest convert-dbobject-to-dbobject
  (let [input  (BasicDBObject.)
        output (to-db-object input)]
    (is (= input output))))

(deftest convert-java-date-to-dbobject
  (let [date   (Date.)
        input  { :int 1, :string "Mongo", :date date }
        output ^DBObject (to-db-object input)]
    (is (= date (.get output "date")))))

(deftest convert-java-calendar-instance-to-dbobject
  (let [date   (Calendar/getInstance)
        input  { :int 1, :string "Mongo", :date date }
        output ^DBObject (to-db-object input)]
    (is (= date (.get output "date")))))




;;
;; DBObject to Clojure
;;

(deftest convert-nil-from-db-object
  (is (nil? (from-db-object nil false)))
  (is (nil? (from-db-object nil true))))

(deftest convert-integer-from-dbobject
  (is (= 2 (from-db-object 2 false)))
  (is (= 2 (from-db-object 2 true))))

(deftest convert-float-from-dbobject
  (is (= 3.3 (from-db-object 3.3 false)))
  (is (= 3.3 (from-db-object 3.3 true))))

(deftest convert-flat-db-object-to-map-without-keywordizing
  (let [name   "Michael"
        age    26
        input (doto (BasicDBObject.)
                (.put "name" name)
                (.put "age"  age))
        output (from-db-object input false)]
    (is (= (output { "name" name, "age" age })))
    (is (= (output "name") name))
    (is (nil? (output :name)))
    (is (= (output "age") age))
    (is (nil? (output "points")))))

(deftest convert-flat-db-object-to-map-without-keywordizing
  (let [name   "Michael"
        age    26
        input (doto (BasicDBObject.)
                (.put "name" name)
                (.put "age"  age))
        output (from-db-object input true)]
    (is (= (output { :name name, :age age })))
    (is (= (output :name) name))
    (is (nil? (output "name")))
    (is (= (output :age) age))
    (is (nil? (output "points")))))


(deftest convert-flat-db-object-to-nested-map
  (let [did    "b38b357f5014a3250d813a16376ca2ff4837e8e1"
        nested (doto (BasicDBObject.)
                 (.put "int" 101)
                 (.put "dblist" (doto (BasicDBList.)
                                  (.put "0" 0)
                                  (.put "1" 1)))
                 (.put "list" (ArrayList. ["red" "green" "blue"])))
        input (doto (BasicDBObject.)
                (.put "_id" did)
                (.put "nested"  nested))
        output (from-db-object input false)]
    (is (= (output "_id") did))
    (is (= (-> output (get "nested") (get "int")) 101))
    (is (= (-> output (get "nested") (get "list")) ["red" "green" "blue"]))
    (is (= (-> output (get "nested") (get "dblist")) [0 1]))))



;;
;; ObjectId coercion
;;

(deftest test-conversion-to-object-id
  (let [output (ObjectId. "4efb39370364238a81020502")]
    (is (= output (to-object-id "4efb39370364238a81020502")))
    (is (= output (to-object-id output)))))


;;
;; Field selector coercion
;;

(deftest test-field-selector-coercion
  (are [i o] (is (= (from-db-object (as-field-selector i) true) o))
       [:a :b :c]          {:a 1 :b 1 :c 1}
       '(:a :b :c)         {:a 1 :b 1 :c 1}
       {:a 1 :b 1 :c 1}    {:a 1 :b 1 :c 1}
       {"a" 1 "b" 1 "c" 1} {:a 1 :b 1 :c 1}
       {:comments 0}       {:comments 0}))
