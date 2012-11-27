(ns monger.test.core-test
  (:require [monger core collection util result]
            [monger.test.helper :as helper]            
            [monger.collection :as mc])
  (:import [com.mongodb MongoClient DB WriteConcern MongoClientOptions ServerAddress])
  (:use clojure.test
        [monger.core :only [server-address mongo-options]]))

(println (str "Using Clojure version " *clojure-version*))
(helper/connect!)

(deftest connect-to-mongo-with-default-host-and-port
  (let [connection (monger.core/connect)]
    (is (instance? com.mongodb.MongoClient connection))))

(deftest connect-and-disconnect
  (monger.core/connect!)
  (monger.core/disconnect!)
  (monger.core/connect!))

(deftest connect-to-mongo-with-default-host-and-explicit-port
  (let [connection (monger.core/connect { :port 27017 })]
    (is (instance? com.mongodb.MongoClient connection))))


(deftest connect-to-mongo-with-default-port-and-explicit-host
  (let [connection (monger.core/connect { :host "127.0.0.1" })]
    (is (instance? com.mongodb.MongoClient connection))))

(deftest test-server-address
  (let [host              "127.0.0.1"
        port              7878
        ^ServerAddress sa (server-address host port)]
    (is (= host (.getHost sa)))
    (is (= port (.getPort sa)))))

(deftest use-existing-mongo-connection
  (let [^MongoClientOptions opts (mongo-options :threads-allowed-to-block-for-connection-multiplier 300)
        connection               (MongoClient. "127.0.0.1" opts)]
    (monger.core/set-connection! connection)
    (is (= monger.core/*mongodb-connection* connection))))

(deftest connect-to-mongo-with-extra-options
  (let [^MongoClientOptions opts (mongo-options :threads-allowed-to-block-for-connection-multiplier 300)
        ^ServerAddress sa        (server-address "127.0.0.1" 27017)]
    (monger.core/connect! sa opts)))


(deftest get-database
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")]
    (is (instance? com.mongodb.DB db))))


(deftest test-get-db-names
  (let [dbs (monger.core/get-db-names)]  
    (is (not (empty? dbs)))
    (is (dbs "monger-test"))))

(deftest get-last-error
  (let [connection (monger.core/connect)
        db         (monger.core/get-db connection "monger-test")]
    (is (monger.result/ok? (monger.core/get-last-error)))
    (is (monger.result/ok? (monger.core/get-last-error db)))
    (is (monger.result/ok? (monger.core/get-last-error db WriteConcern/NORMAL)))
    (is (monger.result/ok? (monger.core/get-last-error db 1 100 true)))))
