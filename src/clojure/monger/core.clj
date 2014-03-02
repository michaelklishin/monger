;; Copyright (c) 2011-2014 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.core
  "Thin idiomatic wrapper around MongoDB Java client. monger.core includes
   fundamental functions that perform database/replica set connection, set default write concern, default database, performing commands
   and so on. Most of the functionality is in other monger.* namespaces, in particular monger.collection, monger.query and monger.gridfs

   Related documentation guides:

   * http://clojuremongodb.info/articles/connecting.html
   * http://clojuremongodb.info/articles/commands.html
   * http://clojuremongodb.info/articles/gridfs.html"
  (:refer-clojure :exclude [count])
  (:require [monger.conversion :refer :all]
            [monger.result :refer [ok?]])
  (:import [com.mongodb MongoClient MongoClientURI DB WriteConcern DBObject DBCursor Bytes MongoClientOptions MongoClientOptions$Builder ServerAddress MapReduceOutput MongoException]
           [com.mongodb.gridfs GridFS]
           [java.util Map ArrayList]))

;;
;; Defaults
;;

(def ^:dynamic ^String *mongodb-host* "127.0.0.1")
(def ^:dynamic ^long   *mongodb-port* 27017)

(declare ^:dynamic ^MongoClient  *mongodb-connection*)
(declare ^:dynamic ^DB           *mongodb-database*)
(def     ^:dynamic ^WriteConcern *mongodb-write-concern* WriteConcern/ACKNOWLEDGED)

(declare ^:dynamic ^GridFS       *mongodb-gridfs*)


;;
;; API
;;

(defn ^com.mongodb.MongoClient connect
  "Connects to MongoDB. When used without arguments, connects to

   Arguments:
     :host (*mongodb-host* by default)
     :port (*mongodb-port* by default)

   EXAMPLES

       (monger.core/connect)
       (monger.core/connect { :host \"db3.intranet.local\", :port 27787 })

       ;; Connecting to a replica set with a couple of seeds
       (let [^MongoClientOptions opts (mg/mongo-options :threads-allowed-to-block-for-connection-multiplier 300)
                           seeds [[\"192.168.1.1\" 27017] [\"192.168.1.2\" 27017] [\"192.168.1.1\" 27018]]
                           sas (map #(apply mg/server-address %) seeds)]
         (mg/connect! sas opts))
   "
  {:arglists '([]
                 [server-address options]
                   [[server-address & more] options]
                     [{ :keys [host port uri] :or { host *mongodb-host* port *mongodb-port* }}])}
  ([]
     (MongoClient.))
  ([server-address ^MongoClientOptions options]
     (if (coll? server-address)
       ;; connect to a replica set
       (let [server-list ^ArrayList (ArrayList. ^java.util.Collection server-address)]
         (MongoClient. server-list options))
       ;; connect to a single instance
       (MongoClient. ^ServerAddress server-address options)))
  ([{ :keys [host port uri] :or { host *mongodb-host* port *mongodb-port* }}]
     (MongoClient. ^String host ^Long port)))


(defn get-db-names
  "Gets a list of all database names present on the server"
  ([]
     (get-db-names *mongodb-connection*))
  ([^MongoClient connection]
     (set (.getDatabaseNames connection))))


(defn ^com.mongodb.DB get-db
  "Get database reference by name.

   EXAMPLES

       (monger.core/get-db \"myapp_production\")
       (monger.core/get-db connection \"myapp_production\")"
  ([]
     *mongodb-database*)
  ([^String name]
     (.getDB *mongodb-connection* name))
  ([^MongoClient connection ^String name]
     (.getDB connection name)))

(defn ^com.mongodb.DB current-db
  "Returns currently used database"
  []
  *mongodb-database*)

(defn drop-db
  "Drops a database"
  ([^String db]
     (.dropDatabase *mongodb-connection* db))
  ([^MongoClient conn ^String db]
     (.dropDatabase conn db)))


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
  ([^String hostname ^Long port]
     (ServerAddress. hostname port)))


(defn mongo-options
  [& { :keys [connections-per-host threads-allowed-to-block-for-connection-multiplier
              max-wait-time connect-timeout socket-timeout socket-keep-alive auto-connect-retry max-auto-connect-retry-time
              description write-concern cursor-finalizer-enabled] :or [auto-connect-retry true] }]
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
    (when auto-connect-retry
      (.autoConnectRetry mob auto-connect-retry))
    (when max-auto-connect-retry-time
      (.maxAutoConnectRetryTime mob max-auto-connect-retry-time))
    (when description
      (.description mob description))
    (when write-concern
      (.writeConcern mob write-concern))
    (when cursor-finalizer-enabled
      (.cursorFinalizerEnabled mob cursor-finalizer-enabled))
    (.build mob)))


(defn set-connection!
  "Sets given MongoDB connection as default by altering *mongodb-connection* var"
  ^MongoClient [^MongoClient conn]
  (alter-var-root (var *mongodb-connection*) (constantly conn)))

(defn connect!
  "Connect to MongoDB, store connection in the *mongodb-connection* var"
  ^MongoClient [& args]
  (let [c (apply connect args)]
    (set-connection! c)))

(defn disconnect!
  "Closes default connection to MongoDB"
  []
  (.close *mongodb-connection*))

(defn set-db!
  "Sets *mongodb-database* var to given db, updates *mongodb-gridfs* var state. Recommended to be used for
  applications that only use one database."
  [db]
  (alter-var-root (var *mongodb-database*) (constantly db))
  (alter-var-root (var *mongodb-gridfs*)   (constantly (GridFS. db))))


(def ^{:doc "Combines set-db! and get-db, so (use-db \"mydb\") is the same as (set-db! (get-db \"mydb\"))"}
  use-db! (comp set-db! get-db))

(def ^:const admin-db-name "admin")

(defn ^DB admin-db
  "Returns admin database"
  []
  (get-db admin-db-name))


(defn set-default-write-concern!
  [wc]
  "Set *mongodb-write-concert* var to :wc

  Unlike the official Java driver, Monger uses WriteConcern/SAFE by default. We think defaults should be safe first
  and WebScale fast second."
  (alter-var-root #'*mongodb-write-concern* (constantly wc)))


(defn authenticate
  ([^String username ^chars password]
     (authenticate *mongodb-connection* *mongodb-database* username password))
  ([^DB db ^String username ^chars password]
     (authenticate *mongodb-connection* db username password))
  ([^MongoClient connection ^DB db ^String username ^chars password]
     (try
       (.authenticate db username password)
       ;; MongoDB Java driver's exception hierarchy is a little crazy
       ;; and the exception we want is not a public class. MK.
       (catch Exception _
         false))))

(defn connect-via-uri
  "Connects to MongoDB using a URI, returns the connection and database as a map with :conn and :db.
   Commonly used for PaaS-based applications, for example, running on Heroku.
   If username and password are provided, performs authentication."
  [^String uri-string]
  (let [uri  (MongoClientURI. uri-string)
        conn (MongoClient. uri)
        db   (.getDB conn (.getDatabase uri))
        user (.getUsername uri)
        pwd  (.getPassword uri)]
    (when (and user pwd)
      (when-not (authenticate conn db user pwd)
        (throw (IllegalArgumentException. (format "Could not authenticate with MongoDB. Either database name or credentials are invalid. Database name: %s, username: %s" (.getName db) user)))))
    {:conn conn, :db db}))


(defn connect-via-uri!
  "Connects to MongoDB using a URI, sets up default connection and database. Commonly used for PaaS-based applications,
   for example, running on Heroku. If username and password are provided, performs authentication."
  [uri-string]
  (let [{:keys [conn db]} (connect-via-uri uri-string)]
    (set-connection! conn)
    (when db
      (set-db! db))
    conn))


(defn ^com.mongodb.CommandResult command
  "Runs a database command (please check MongoDB documentation for the complete list of commands).

   Ordering of keys in the command document may matter. Please use sorted maps instead of map literals, for example:
   (sorted-map geoNear \"bars\" :near 50 :test 430 :num 10)

   For commonly used commands (distinct, count, map/reduce, etc), use monger.command and monger.collection functions such as
   /distinct, /count,  /drop, /dropIndexes, and /mapReduce respectively."
  ([^Map cmd]
     (.command ^DB *mongodb-database* ^DBObject (to-db-object cmd)))
  ([^DB database ^Map cmd]
     (.command ^DB database ^DBObject (to-db-object cmd))))

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

(defn ^DBObject get-last-error
  "Returns the the error (if there is one) from the previous operation on this connection.

   The result of this command looks like:

      #<CommandResult { \"serverUsed\" : \"127.0.0.1:27017\" , \"n\" : 0 , \"connectionId\" : 66 , \"err\" :  null  , \"ok\" : 1.0}>\"

   The value for err will be null if no error occurred, or a description otherwise.

   Important note: when calling this method directly, it is undefined which connection \"getLastError\" is called on.
   You may need to explicitly use a \"consistent Request\", see requestStart() For most purposes it is better not to call this method directly but instead use WriteConcern."
  ([]
     (get-last-error *mongodb-database*))
  ([^DB database]
     (.getLastError ^DB database))
  ([^DB database ^Integer w ^Integer wtimeout ^Boolean fsync]
     (.getLastError ^DB database w wtimeout fsync))
  ([^DB database ^WriteConcern write-concern]
     (.getLastError ^DB database write-concern)))
