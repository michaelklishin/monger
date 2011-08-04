(ns monger.core
  (:import (com.mongodb Mongo DB))
  )

;;
;; Defaults
;;

(def ^:dynamic *default-host* "localhost")
(def ^:dynamic *default-port* 27017)


;;
;; Protocols
;;




;;
;; API
;;

(defn ^Mongo connect
  "Connects to MongoDB"
  ([]
     (Mongo.))
  ([{ :keys [host port] :or { host *default-host*, port *default-port* }}]
     (Mongo. host port)))

(defn ^DB get-db
  "Get database reference by name"
  [^Mongo connection, ^String name]
  (.getDB connection name))

