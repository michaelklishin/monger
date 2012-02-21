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
       is in the monger.collection namespace."}
  monger.core
  (:refer-clojure :exclude [count])
  (:use [monger.conversion])
  (:import [com.mongodb Mongo DB WriteConcern DBObject DBCursor CommandResult]
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
  ([{ :keys [host port] :or { host *mongodb-host*, port *mongodb-port* }}]
     (Mongo. ^String host ^Long port)))


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


(defn connect!
  "Connect to MongoDB, store connection in the *mongodb-connection* var"
  ^Mongo [& args]
  (def ^:dynamic *mongodb-connection* (apply connect args)))


(defn set-db!
  "Sets *mongodb-database* var to given db, updates *mongodb-gridfs* var state. Recommended to be used for
  applications that only use one database."
  [db]
  (def ^:dynamic *mongodb-database* db)
  (def ^:dynamic *mongodb-gridfs* (GridFS. db)))


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
    (.command ^DB database ^DBObject (to-db-object cmd)))
  )

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
