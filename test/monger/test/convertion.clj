(ns monger.test.convertion
  (:require [monger core collection convertion])
  (:use [clojure.test]))

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
        output (monger.convertion/to-db-object input)]
    (is (= 1 (.get output "int")))
    (is (= "Mongo" (.get output "string")))
    (is (= 22.23 (.get output "float")))))


(deftest convert-nested-map-to-dbobject
  (let [input  { :int 1, :string "Mongo", :float 22.23, :map { :int 10, :string "Clojure", :float 11.9, :list '(1 "a" :b) } }
        output (monger.convertion/to-db-object input)]
    (is (= 10 (.get (.get output "map") "int")))))
