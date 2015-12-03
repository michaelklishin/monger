;; This source code is dual-licensed under the Apache License, version
;; 2.0, and the Eclipse Public License, version 1.0.
;;
;; The APL v2.0:
;;
;; ----------------------------------------------------------------------------------
;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
;; ----------------------------------------------------------------------------------
;;
;; The EPL v1.0:
;;
;; ----------------------------------------------------------------------------------
;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team.
;; All rights reserved.
;;
;; This program and the accompanying materials are made available under the terms of
;; the Eclipse Public License Version 1.0,
;; which accompanies this distribution and is available at
;; http://www.eclipse.org/legal/epl-v10.html.
;; ----------------------------------------------------------------------------------

(ns monger.core
  "Thin idiomatic wrapper around MongoDB Java client. monger.core includes
   fundamental functions that perform database/replica set connection, set default write concern, default database, performing commands
   and so on. Most of the functionality is in other monger.* namespaces, in particular monger.collection, monger.query and monger.gridfs

   Related documentation guides:

   * http://clojuremongodb.info/articles/connecting.html
   * http://clojuremongodb.info/articles/commands.html
   * http://clojuremongodb.info/articles/gridfs.html"
  (:refer-clojure :exclude [count])
  (:require [monger.conversion :refer :all])
  (:import [com.mongodb MongoClient MongoClientURI MongoCredential DB WriteConcern DBObject DBCursor Bytes
            MongoClientOptions MongoClientOptions$Builder ServerAddress MapReduceOutput MongoException]
           [com.mongodb.gridfs GridFS]
           [java.util Map ArrayList]))

;;
;; Defaults
;;

(def ^:dynamic ^String *mongodb-host* "127.0.0.1")
(def ^:dynamic ^long   *mongodb-port* 27017)

(def ^:dynamic ^WriteConcern *mongodb-write-concern* WriteConcern/ACKNOWLEDGED)


;;
;; API
;;

(defn ^MongoClient connect
  "Connects to MongoDB. When used without arguments, connects to

   Arguments:
     :host (\"127.0.0.1\" by default)
     :port (27017 by default)"
  {:arglists '([]
                 [server-address options]
                   [[server-address & more] options]
                     [{:keys [host port uri] :or { host *mongodb-host* port *mongodb-port*}}])}
  ([]
     (MongoClient.))
  ([server-address ^MongoClientOptions options]
     (if (coll? server-address)
       ;; connect to a replica set
       (let [server-list ^ArrayList (ArrayList. ^java.util.Collection server-address)]
         (MongoClient. server-list options))
       ;; connect to a single instance
       (MongoClient. ^ServerAddress server-address options)))
  ([server-address ^MongoClientOptions options credentials]
     (let [creds (if (coll? credentials)
                   credentials
                   [credentials])]
       (if (coll? server-address)
         (let [server-list ^ArrayList (ArrayList. ^java.util.Collection server-address)]
           (MongoClient. server-list creds options))
         (MongoClient. ^ServerAddress server-address creds options))))
  ([{ :keys [host port uri] :or { host *mongodb-host* port *mongodb-port* }}]
    (if uri
       (MongoClient. (MongoClientURI. uri))
       (MongoClient. ^String host ^Long port))))

(defn ^MongoClient connect-with-credentials
  "Connect with provided credentials and default options"
  ([credentials]
     (connect-with-credentials *mongodb-host* *mongodb-port* credentials))
  ([^String hostname credentials]
     (connect-with-credentials hostname *mongodb-port* credentials))
  ([^String hostname port credentials]
     (MongoClient. [(ServerAddress. hostname port)]
                   (if (coll? credentials)
                     credentials
                     [credentials]))))

(defn get-db-names
  "Gets a list of all database names present on the server"
  [^MongoClient conn]
  (set (.getDatabaseNames conn)))


(defn ^DB get-db
  "Get database reference by name."
  [^MongoClient conn ^String name]
  (.getDB conn name))

(defn drop-db
  "Drops a database"
  [^MongoClient conn ^String db]
  (.dropDatabase conn db))

(defn ^GridFS get-gridfs
  "Get GridFS for the given database."
  [^MongoClient conn ^String name]
  (GridFS. (.getDB conn name)))

(defn server-address
  ([^String hostname]
     (ServerAddress. hostname))
  ([^String hostname ^Long port]
     (ServerAddress. hostname port)))

(defn ^MongoClientOptions$Builder mongo-options-builder
  [{:keys [connections-per-host threads-allowed-to-block-for-connection-multiplier
           max-wait-time connect-timeout socket-timeout socket-keep-alive auto-connect-retry max-auto-connect-retry-time
           description write-concern cursor-finalizer-enabled read-preference
           required-replica-set-name ssl-invalid-hostname-allowed ssl-enabled]}]
  (let [mob (MongoClientOptions$Builder.)]
    (when connections-per-host
      (.connectionsPerHost mob connections-per-host))
    (when threads-allowed-to-block-for-connection-multiplier
      (.threadsAllowedToBlockForConnectionMultiplier mob threads-allowed-to-block-for-connection-multiplier))
    (when max-wait-time
      (.maxWaitTime mob max-wait-time))
    (when connect-timeout
      (.connectTimeout mob connect-timeout))
    (when socket-timeout
      (.socketTimeout mob socket-timeout))
    (when socket-keep-alive
      (.socketKeepAlive mob socket-keep-alive))
    (when read-preference
      (.readPreference mob read-preference))
    (when description
      (.description mob description))
    (when ssl-enabled
      (.sslEnabled mob ssl-enabled))
    (when ssl-invalid-hostname-allowed
      (.sslInvalidHostNameAllowed mob ssl-invalid-hostname-allowed))
    (when write-concern
      (.writeConcern mob write-concern))
    (when cursor-finalizer-enabled
      (.cursorFinalizerEnabled mob cursor-finalizer-enabled))
    (when required-replica-set-name
      (.requiredReplicaSetName mob required-replica-set-name))
    mob))

(defn ^MongoClientOptions mongo-options
  [opts]
  (let [mob (mongo-options-builder opts)]
    (.build mob)))

(defn disconnect
  "Closes default connection to MongoDB"
  [^MongoClient conn]
  (.close conn))

(def ^:const admin-db-name "admin")

(defn ^DB admin-db
  "Returns admin database"
  [^MongoClient conn]
  (get-db conn admin-db-name))


(defn set-default-write-concern!
  [wc]
  "Sets *mongodb-write-concert*"
  (alter-var-root #'*mongodb-write-concern* (constantly wc)))


(defn connect-via-uri
  "Connects to MongoDB using a URI, returns the connection and database as a map with :conn and :db.
   Commonly used for PaaS-based applications, for example, running on Heroku.
   If username and password are provided, performs authentication."
  [^String uri-string]
  (let [uri  (MongoClientURI. uri-string)
        conn (MongoClient. uri)
        db   (.getDB conn (.getDatabase uri))]
    {:conn conn :db db}))

(defn ^com.mongodb.CommandResult command
  "Runs a database command (please check MongoDB documentation for the complete list of commands).

   Ordering of keys in the command document may matter. Please use sorted maps instead of map literals, for example:
   (array-map :near 50 :test 430 :num 10)

   For commonly used commands (distinct, count, map/reduce, etc), use monger.command and monger.collection functions such as
   /distinct, /count,  /drop, /dropIndexes, and /mapReduce respectively."
  [^DB database ^Map cmd]
  (.command ^DB database ^DBObject (to-db-object cmd)))

(defn ^com.mongodb.CommandResult raw-command
  "Like monger.core/command but accepts DBObjects"
  [^DB database ^DBObject cmd]
  (.command database cmd))

(defprotocol Countable
  (count [this] "Returns size of the object"))

(extend-protocol Countable
  DBCursor
  (count [^DBCursor this]
    (.count this))

  MapReduceOutput
  (count [^MapReduceOutput this]
    ;; MongoDB Java driver could use a lot more specific type than Iterable but
    ;; it always uses DBCollection#find to popular result set. MK.
    (.count ^DBCursor (.results this))))
