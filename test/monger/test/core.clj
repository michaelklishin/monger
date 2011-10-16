(ns monger.test.core
  (:require [monger core collection util])
  (:import (com.mongodb Mongo DB))
  (:use [clojure.test]))


(monger.core/connect!)
(monger.core/set-db! (monger.core/get-db "monger-test"))


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

(deftest issuing-a-command
  "Some commands require administrative priviledges or complex data / checks or heavily depend on DB version. They will be ommited here."
  (let [collection "things"]
    (are [a b] (= a (.ok (monger.core/command b)))
         true { :profile 1 }
         true { :listCommands 1 }
         true { :dbStats 1 }
         true { :collstats "things" :scale (* 1024 1024) }
         true { :getLastError 1 }
         )))
