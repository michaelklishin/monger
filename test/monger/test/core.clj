(ns monger.test.core
  (:require [monger.core])
  (:import (com.mongodb Mongo DB))
  (:use [clojure.test]))

(deftest connect-to-mongo-with-default-host-and-port
  (let [connection (monger.core/connect)]
    (is (instance? com.mongodb.Mongo connection))))


(deftest connect-to-mongo-with-default-host-and-explicit-port
  (let [connection (monger.core/connect { :port 27017 })]
    (is (instance? com.mongodb.Mongo connection))))


(deftest connect-to-mongo-with-default-port-and-explicit-host
  (let [connection (monger.core/connect { :host "127.0.0.1" })]
    (is (instance? com.mongodb.Mongo connection))))


(deftest get-database
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")]
    (is (instance? com.mongodb.DB db))))

;; (deftest get-database-with-valid-credentials
;;   (let [connection (monger.core/connect)
;;         db         (monger.core/get-db connection "monger-test" "monger" "test_password")]
;;     (is (instance? com.mongodb.DB db))))
