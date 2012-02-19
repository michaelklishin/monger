(ns monger.test.core
  (:require [monger core collection util result]
            [monger.test.helper :as helper]            
            [monger.collection :as mgcol])
  (:import (com.mongodb Mongo DB WriteConcern))
  (:use [clojure.test]))

(helper/connect!)

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


(deftest test-get-db-names
  (let [dbs (monger.core/get-db-names)]  
    (is (not (empty? dbs)))
    (is (some #{"monger-test"} dbs))))

;; (deftest get-database-with-valid-credentials
;;   (let [connection (monger.core/connect)
;;         db         (monger.core/get-db connection "monger-test" "monger" "test_password")]
;;     (is (instance? com.mongodb.DB db))))

(deftest issuing-a-command
  "Some commands require administrative priviledges or complex data / checks or heavily depend on DB version. They will be ommited here."
  (let [collection "things"]
    (doseq [c [{ :profile 1 }
               { :listCommands 1 }
               { :dbStats 1 }
               { :collstats "things" :scale (* 1024 1024) }
               { :getLastError 1 }]]
      (is (monger.result/ok? (monger.core/command c))))))

(deftest get-last-error
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")]
    (is (monger.result/ok? (monger.core/get-last-error)))
    (is (monger.result/ok? (monger.core/get-last-error db)))
    (is (monger.result/ok? (monger.core/get-last-error db WriteConcern/NORMAL)))
    (is (monger.result/ok? (monger.core/get-last-error db 1 100 true)))))