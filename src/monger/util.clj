;; Copyright (c) 2011 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.util
  (:import (java.security SecureRandom) (java.math BigInteger) (org.bson.types ObjectId) (com.mongodb DBObject)))

;;
;; API
;;

(defn ^String random-uuid
  "Generates a secure random UUID string"
  []
  (.toString (java.util.UUID/randomUUID)))

(defn ^String random-str
  "Generates a secure random string"
  [^long n, ^long num-base]
  (.toString (new BigInteger n (SecureRandom.)) num-base))

(defn ^ObjectId object-id
  "Returns a new BSON object id"
  []
  (ObjectId.))

(defmacro with-ns
  "Evaluates body in another namespace.  ns is either a namespace object or a symbol.
   This makes it possible to define functions in namespaces other than the current one."
  [ns & body]
  `(binding [*ns* (the-ns ~ns)]
     ~@(map (fn [form] `(eval '~form)) body)))

(defprotocol GetDBObjectId
  (get-id  [input] "Returns object id"))

(extend-protocol GetDBObjectId
  DBObject
  (get-id
   [^DBObject object]
   (.get ^DBObject object "_id")))