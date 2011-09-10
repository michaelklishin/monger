;; Copyright (c) 2011 Michael S. Klishin
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
  (:use [monger.convertion])
  (:import (com.mongodb Mongo DB WriteConcern DBObject DBCursor)
           (java.util Map)))

;;
;; Defaults
;;

(def ^:dynamic ^String *mongodb-host* "localhost")
(def ^:dynamic ^long   *mongodb-port* 27017)

(def ^:dynamic ^Mongo        *mongodb-connection*)
(def ^:dynamic ^DB           *mongodb-database*)
(def ^:dynamic ^WriteConcern *mongodb-write-concern* WriteConcern/NORMAL)


;;
;; API
;;

(defn ^Mongo connect
  "Connects to MongoDB. When used without arguments, connects to *mongodb-host* and
   *mongodb-test*.


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



(defprotocol Countable
  (count [this] "Returns size of the object"))

(extend-protocol Countable
  DBCursor
  (count [^com.mongodb.DBCursor this]
    (.count this)))


(defn command
  [^Map cmd]
  (.command ^DB *mongodb-database* ^DBObject (to-db-object cmd)))
