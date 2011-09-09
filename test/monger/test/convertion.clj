(ns monger.test.convertion
  (:require [monger core collection convertion])
  (:import (com.mongodb DBObject BasicDBObject BasicDBList) (java.util List ArrayList))
  (:use [clojure.test]))


;;
;; Clojure to DBObject
;;

(deftest convert-nil-to-dbobject
  (let [input  nil
        output (monger.convertion/to-db-object input)]
    (is (nil? output))))

(deftest convert-integer-to-dbobject
  (let [input  1
        output (monger.convertion/to-db-object input)]
    (is (= input output))))

(deftest convert-float-to-dbobject
  (let [input  11.12
        output (monger.convertion/to-db-object input)]
    (is (= input output))))

(deftest convert-string-to-dbobject
  (let [input  "MongoDB"
        output (monger.convertion/to-db-object input)]
    (is (= input output))))


(deftest convert-map-to-dbobject
  (let [input  { :int 1, :string "Mongo", :float 22.23 }
        output #^DBObject (monger.convertion/to-db-object input)]
    (is (= 1 (.get output "int")))
    (is (= "Mongo" (.get output "string")))
    (is (= 22.23 (.get output "float")))))


(deftest convert-nested-map-to-dbobject
  (let [input  { :int 1, :string "Mongo", :float 22.23, :map { :int 10, :string "Clojure", :float 11.9, :list '(1 "a" :b), :map { :key "value" } } }
        output #^DBObject (monger.convertion/to-db-object input)
        inner  #^DBObject (.get output "map")]
    (is (= 10           (.get inner "int")))
    (is (= "Clojure"    (.get inner "string")))
    (is (= 11.9         (.get inner "float")))
    (is (= '(1 "a" "b") (.get inner "list")))
    (is (= { "key" "value" } (.get inner "map")))))

;; for cases when you want to pass in a DBObject, for example,
;; to obtain _id that was generated. MK.
(deftest convert-dbobject-to-dbobject
  (let [input  (BasicDBObject.)
        output (monger.convertion/to-db-object input)]
    (is (= input output))))




;;
;; DBObject to Clojure
;;

(deftest convert-nil-from-db-object
  (is (nil? (monger.convertion/from-db-object nil false)))
  (is (nil? (monger.convertion/from-db-object nil true))))

(deftest convert-integer-from-dbobject
  (is (= 2 (monger.convertion/from-db-object 2 false)))
  (is (= 2 (monger.convertion/from-db-object 2 true))))

(deftest convert-float-from-dbobject
  (is (= 3.3 (monger.convertion/from-db-object 3.3 false)))
  (is (= 3.3 (monger.convertion/from-db-object 3.3 true))))

(deftest convert-flat-db-object-to-map-without-keywordizing
  (let [name   "Michael"
        age    26
        input (doto (BasicDBObject.)
                (.put "name" name)
                (.put "age"  age))
        output (monger.convertion/from-db-object input false)]
    (is (= (output { "name" name, "age" age })))
    (is (= (output "name") name))
    (is (nil? (output :name)))
    (is (= (output "age") age))
    (is (nil? (output "points")))
    ))

(deftest convert-flat-db-object-to-map-without-keywordizing
  (let [name   "Michael"
        age    26
        input (doto (BasicDBObject.)
                (.put "name" name)
                (.put "age"  age))
        output (monger.convertion/from-db-object input true)]
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
        output (monger.convertion/from-db-object input false)]
    (is (= (output "_id") did))
    (is (= (-> output (get "nested") (get "int")) 101))
    (is (= (-> output (get "nested") (get "list")) ["red" "green" "blue"]))
    (is (= (-> output (get "nested") (get "dblist")) [0 1]))))

