;; Copyright (c) 2011 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.core
  (:import (com.mongodb Mongo DB WriteConcern))
  )

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
  "Connects to MongoDB"
  ([]
     (Mongo.))
  ([{ :keys [host port] :or { host *mongodb-host*, port *mongodb-port* }}]
     (Mongo. host port)))

(defn ^DB get-db
  "Get database reference by name"
  ([^String name]
     (.getDB *mongodb-connection* name))
  ([^Mongo connection, ^String name]
     (.getDB connection name)))



(defprotocol Countable
  (count [this] "Returns size of the object"))

(extend-protocol Countable
  com.mongodb.DBCursor
  (count [^com.mongodb.DBCursor this]
    (.count this)))
