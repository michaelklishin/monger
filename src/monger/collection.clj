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

(ns monger.collection
  (:import (com.mongodb Mongo DB WriteResult DBObject WriteConcern DBCursor) (java.util List Map))
  (:require [monger core convertion errors]))

;;
;; API
;;

;; monger.collection/insert

(defn ^WriteResult insert
  ([^DB db, ^String collection, ^DBObject doc]
     (.insert (.getCollection db collection) (monger.convertion/to-db-object doc) WriteConcern/NORMAL))
  ([^DB db, ^String collection, ^DBObject doc, ^WriteConcern concern]
     (.insert (.getCollection db collection) (monger.convertion/to-db-object doc) concern)))


(defn ^WriteResult insert-batch
  ([^DB db, ^String collection, ^List docs]
     (.insert (.getCollection db collection) (monger.convertion/to-db-object docs) WriteConcern/NORMAL))
  ([^DB db, ^String collection, ^List docs, ^WriteConcern concern]
     (.insert (.getCollection db collection) (monger.convertion/to-db-object docs) concern)))

;; monger.collection/find

(defn ^DBCursor find
  ([^DB db, ^String collection]
     (.find (.getCollection db collection)))
  ([^DB db, ^String collection, ^Map ref]
     (.find (.getCollection db collection) (monger.convertion/to-db-object ref)))
  )


(defn ^DBObject find-by-id
  ([^DB db, ^String collection, ^String id]
     (.findOne (.getCollection db collection) (monger.convertion/to-db-object { :_id id })))
  )



;; monger.collection/group

;; monger.collection/count
(defn ^long count
  [^DB db, ^String collection]
  (.count (.getCollection db collection)))

;; monger.collection/update
;; monger.collection/update-multi
;; monger.collection/remove

(defn ^WriteResult remove
([^DB db, ^String collection]
     (.remove (.getCollection db collection) (monger.convertion/to-db-object {})))
  ([^DB db, ^String collection, ^DBObject conditions]
     (.remove (.getCollection db collection) (monger.convertion/to-db-object conditions)))
  )

;; monger.collection/ensure-index
;; monger.collection/drop-index