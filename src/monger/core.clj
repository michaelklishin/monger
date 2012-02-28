;; Copyright (c) 2011-2012 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:author "Michael S. Klishin"
      :doc "Thin idiomatic wrapper around MongoDB Java client. monger.core includes
       fundamental functions that work with connections & databases. Most of functionality
       is in other monger.* namespaces, in particular monger.collection."}
  monger.core
  (:refer-clojure :exclude [count])
  (:use [monger.conversion])
  (:import [com.mongodb Mongo DB WriteConcern DBObject DBCursor CommandResult Bytes MongoOptions ServerAddress]
           [com.mongodb.gridfs GridFS]
           [java.util Map]))

;;
;; Defaults
;;

(def ^:dynamic ^String *mongodb-host* "localhost")
(def ^:dynamic ^long   *mongodb-port* 27017)

(declare ^:dynamic ^Mongo        *mongodb-connection*)
(declare ^:dynamic ^DB           *mongodb-database*)
(def     ^:dynamic ^WriteConcern *mongodb-write-concern* WriteConcern/SAFE)

(declare ^:dynamic ^GridFS       *mongodb-gridfs*)

;;
;; API
;;

(defn ^Mongo connect
  "Connects to MongoDB. When used without arguments, connects to

   Arguments:
     :host (*mongodb-host* by default)
     :port (*mongodb-port* by default)

   EXAMPLES

       (monger.core/connect)
       (monger.core/connect { :host \"db3.intranet.local\", :port 27787 })
   "
  ([]
     (Mongo.))
  ([^ServerAddress server-address ^MongoOptions options]
     (Mongo. server-address options))
  ([{ :keys [host port] :or { host *mongodb-host*, port *mongodb-port* }}]
     (Mongo. ^String host ^Long port)))



(defn ^DB get-db-names
  "Gets a list of all database names present on the server"
  ([]
     (get-db-names *mongodb-connection*))
  ([^Mongo connection]
     (set (.getDatabaseNames connection))))


(defn ^DB get-db
  "Get database reference by name.

   EXAMPLES

       (monger.core/get-db \"myapp_production\")
       (monger.core/get-db connection \"myapp_production\")"
  ([^String name]
     (.getDB *mongodb-connection* name))
  ([^Mongo connection, ^String name]
     (.getDB connection name)))


(defn authenticate
  ([^String db ^String username ^chars password]
     (authenticate *mongodb-connection* db username password))
  ([^Mongo connection ^String db ^String username ^chars password]
     (.authenticate (.getDB connection db) username password)))



(defmacro with-connection
  [conn & body]
  `(binding [*mongodb-connection* ~conn]
     (do ~@body)))


(defmacro with-db
  [db & body]
  `(binding [*mongodb-database* ~db]
     (do ~@body)))

(defmacro with-gridfs
  [fs & body]
  `(binding [*mongodb-gridfs* ~fs]
     (do ~@body)))


(defn server-address
  ([^String hostname]
     (ServerAddress. hostname))
  ([^String hostname ^long port]
     (ServerAddress. hostname port)))


(defn mongo-options
  [& { :keys [connections-per-host threads-allowed-to-block-for-connection-multiplier
              max-wait-time connect-timeout socket-timeout socket-keep-alive auto-connect-retry max-auto-connect-retry-time
              safe w w-timeout fsync j] }]
  (let [mo (MongoOptions.)]
    (when connections-per-host
      (set! (. mo connectionsPerHost) connections-per-host))
    (when threads-allowed-to-block-for-connection-multiplier
      (set! (. mo threadsAllowedToBlockForConnectionMultiplier) threads-allowed-to-block-for-connection-multiplier))
    (when max-wait-time
      (set! (. mo maxWaitTime) max-wait-time))
    (when connect-timeout
      (set! (. mo connectTimeout) connect-timeout))
    (when socket-timeout
      (set! (. mo socketTimeout) socket-timeout))
    (when socket-keep-alive
      (set! (. mo socketKeepAlive) socket-keep-alive))
    (when auto-connect-retry
      (set! (. mo autoConnectRetry) auto-connect-retry))
    (when max-auto-connect-retry-time
      (set! (. mo maxAutoConnectRetryTime) max-auto-connect-retry-time))
    (when safe
      (set! (. mo safe) safe))
    (when w
      (set! (. mo w) w))
    (when w-timeout
      (set! (. mo wtimeout) w-timeout))
    (when j
      (set! (. mo j) j))
    (when fsync
      (set! (. mo fsync) fsync))
    mo))


(defn set-connection!
  "Sets given MongoDB connection as default by altering *mongodb-connection* var"
  ^Mongo [^Mongo conn]
  (alter-var-root (var *mongodb-connection*) (constantly conn)))

(defn connect!
  "Connect to MongoDB, store connection in the *mongodb-connection* var"
  ^Mongo [& args]
  (let [c (apply connect args)]
    (set-connection! c)))



(defn set-db!
  "Sets *mongodb-database* var to given db, updates *mongodb-gridfs* var state. Recommended to be used for
  applications that only use one database."
  [db]
  (alter-var-root (var *mongodb-database*) (constantly db))
  (alter-var-root (var *mongodb-gridfs*)   (constantly (GridFS. db))))


(defn set-default-write-concern!
  [wc]
  "Set *mongodb-write-concert* var to :wc

  Unlike the official Java driver, Monger uses WriteConcern/SAFE by default. We think defaults should be safe first
  and WebScale fast second."
  (def ^:dynamic *mongodb-write-concern* wc))

(defn ^CommandResult command
  "Runs a database command (please check MongoDB documentation for the complete list of commands). Some common commands
  are:

   { :buildinfo 1 } returns version number and build information about the current MongoDB server, should be executed via admin DB.

   { :collstats collection-name [ :scale scale ] } returns stats about given collection.

   { :dbStats 1 } returns the stats of current database

   { :dropDatabase 1 }  deletes the current database

   { :findAndModify find-and-modify-config } runs find, modify and return for the given query.
       Takes :query, :sory, :remove, :update, :new, :fields and :upsert arguments.
       Please refer MongoDB documentation for details. http://www.mongodb.org/display/DOCS/findAndModify+Command

   { :fsync config } performs a full fsync, that flushes all pending writes to database, provides an optional write lock that will make
      backups easier.
      Please refer MongoDB documentation for details :http://www.mongodb.org/display/DOCS/fsync+Command

   { :getLastError 1 } returns the status of the last operation on current connection.

   { :group group-config } performs grouping aggregation, docs and support for grouping are TBD in Monger.

   { :listCommands 1 }  displays the list of available commands.

   { :profile new-profile-level } sets the database profiler to profile level N.

   { :reIndex coll } performs re-index on a given collection.

   { :renameCollection old-name :to new-name } renames collection from old-name to new-name

   { :repairDatabase 1 } repair and compact the current database (may be very time-consuming, depending on DB size)

   Replica set commands
     { :isMaster 1 } checks if this server is a master server.
     { :replSetGetStatus 1 } get the status of a replica set.
     { :replSetInitiate replica-config } initiate a replica set with given config.
     { :replSetReconfig replica-config } set a given config for replica set.
     { :replSetStepDown seconds } manually tell a member to step down as primary. It will become primary again after specified amount of seconds.
     { :replSetFreeze seconds } freeze state of member, call with 0 to unfreeze.
     { :resync 1 } start a full resync of a replica slave
        For more information, please refer Mongodb Replica Set Command guide: http://www.mongodb.org/display/DOCS/Replica+Set+Commands

   { :serverStatus 1 } gets administrative statistics about the server.

   { :shutdown 1 } shuts the MongoDB server down.

   { :top 1 } get a breakdown of usage by collection.

   { :validate namespace-name } validate the namespace (collection or index). May be very time-consuming, depending on DB size.

   For :distinct, :count, :drop, :dropIndexes, :mapReduce we suggest to use monger/collection #distinct, #count,  #drop, #dropIndexes, :mapReduce respectively.
  "
  ([^Map cmd]
     (.command ^DB *mongodb-database* ^DBObject (to-db-object cmd)))
  ([^DB database ^Map cmd]
     (.command ^DB database ^DBObject (to-db-object cmd))))

(defprotocol Countable
  (count [this] "Returns size of the object"))

(extend-protocol Countable
  DBCursor
  (count [^DBCursor this]
    (.count this)))

(defn ^DBObject get-last-error
  "Returns the the error (if there is one) from the previous operation on this connection.

   The result of this command looks like:

      #<CommandResult { \"serverUsed\" : \"127.0.0.1:27017\" , \"n\" : 0 , \"connectionId\" : 66 , \"err\" :  null  , \"ok\" : 1.0}>\"

   The value for err will be null if no error occurred, or a description otherwise.

   Important note: when calling this method directly, it is undefined which connection \"getLastError\" is called on.
   You may need to explicitly use a \"consistent Request\", see requestStart() For most purposes it is better not to call this method directly but instead use WriteConcern."
  ([]
     (.getLastError ^DB *mongodb-database*))
  ([^DB database]
     (.getLastError ^DB database))
  ([^DB database ^Integer w ^Integer wtimeout ^Boolean fsync]
     (.getLastError ^DB database w wtimeout fsync))
  ([^DB database ^WriteConcern write-concern]
     (.getLastError ^DB database write-concern)))
