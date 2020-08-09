(ns monger.test.core-test
  (:require [monger util result]
            [monger.core :as mg :refer [server-address mongo-options]]
            [monger.collection :as mc]
            [clojure.test :refer :all])
  (:import [com.mongodb MongoClient DB WriteConcern MongoClientOptions ServerAddress]))

(println (str "Using Clojure version " *clojure-version*))

(deftest connect-to-mongo-with-default-host-and-port
  (let [connection (mg/connect)]
    (is (instance? com.mongodb.MongoClient connection))))

(deftest connect-and-disconnect
  (let [conn (mg/connect)]
    (mg/disconnect conn)))

(deftest connect-to-mongo-with-default-host-and-explicit-port
  (let [connection (mg/connect {:port 27017})]
    (is (instance? com.mongodb.MongoClient connection))))


(deftest connect-to-mongo-with-default-port-and-explicit-host
  (let [connection (mg/connect {:host "127.0.0.1"})]
    (is (instance? com.mongodb.MongoClient connection))))

(deftest test-server-address
  (let [host              "127.0.0.1"
        port              7878
        ^ServerAddress sa (server-address host port)]
    (is (= host (.getHost sa)))
    (is (= port (.getPort sa)))))

(deftest use-existing-mongo-connection
  (let [^MongoClientOptions opts (mongo-options {:threads-allowed-to-block-for-connection-multiplier 300})
        connection               (MongoClient. "127.0.0.1" opts)
        db                       (mg/get-db connection "monger-test")]
    (mg/disconnect connection)))

(deftest connect-to-mongo-with-extra-options
  (let [^MongoClientOptions opts (mongo-options {:threads-allowed-to-block-for-connection-multiplier 300})
        ^ServerAddress sa        (server-address "127.0.0.1" 27017)
        conn                     (mg/connect sa opts)]
    (mg/disconnect conn)))


(deftest get-database
  (let [connection (mg/connect)
        db         (mg/get-db connection "monger-test")]
    (is (instance? com.mongodb.DB db))))


(deftest test-get-db-names
  (let [conn (mg/connect)
        dbs  (mg/get-db-names conn)]  
    (is (not (empty? dbs)))
    (is (dbs "monger-test"))))

(deftest monger-options-test
  (let [opts {:add-cluster-listener nil
              :add-cluster-listeners []
              :add-command-listener nil
              :add-command-listeners []
              :add-connection-pool-listener nil
              :add-connection-pool-listeners []
              :add-server-listener nil
              :add-server-listeners []
              :add-server-monitor-listener nil
              :add-server-monitor-listeners []
              :always-use-mbeans true
              :application-name "app"
              :codec-registry nil
              :compressor-list []
              :connect-timeout 1
              :connections-per-host 1
              :cursor-finalizer-enabled true
              :db-decoder-factory nil
              :db-encoder-factory nil
              :description "Description"
              :heartbeat-connect-timeout 1
              :heartbeat-frequency 1
              :heartbeat-socket-timeout 1
              :local-threshold 1
              :max-connection-idle-time 1
              :max-connection-life-time 1
              :max-wait-time 1
              :min-connections-per-host 1
              :min-heartbeat-frequency 1
              :read-concern nil
              :read-preference nil
              :required-replica-set-name "rs"
              :retry-writes true
              :server-selection-timeout 1
              :server-selector nil
              :socket-keep-alive true
              :socket-factory nil
              :socket-timeout 1
              :ssl-context nil
              :ssl-enabled true
              :ssl-invalid-host-name-allowed true
              :threads-allowed-to-block-for-connection-multiplier 1
              :write-concern com.mongodb.WriteConcern/JOURNAL_SAFE}]
    (is (instance? com.mongodb.MongoClientOptions$Builder (mg/mongo-options-builder opts)))))

(deftest connect-to-uri-without-db-name
  (let [uri "mongodb://localhost:27017"]
    (is (thrown? IllegalArgumentException (mg/connect-via-uri uri)))))
