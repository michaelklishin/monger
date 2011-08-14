;; Copyright (c) 2011 Michael S. Klishin
;;
;; Permission is hereby granted, free of charge, to any person obtaining a copy
;; of this software and associated documentation files (the "Software"), to deal
;; in the Software without restriction, including without limitation the rights
;; to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
;; copies of the Software, and to permit persons to whom the Software is
;; furnished to do so, subject to the following conditions:
;;
;; The above copyright notice and this permission notice shall be included in
;; all copies or substantial portions of the Software.
;;
;; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
;; IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
;; FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
;; AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
;; LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
;; OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
;; THE SOFTWARE.

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
;; Protocols
;;




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

