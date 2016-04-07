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

(ns ^{:doc "Provides various utility functions, primarily for working with document ids."} monger.util
  (:import java.security.SecureRandom
           java.math.BigInteger
           org.bson.types.ObjectId
           com.mongodb.DBObject
           clojure.lang.IPersistentMap
           java.util.Map))

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
  "Returns a new BSON object id, or converts str to BSON object id"
  ([]
     (ObjectId.))
  ([^String s]
     (ObjectId. s)))

(defprotocol GetDocumentId
  (get-id  [input] "Returns document id"))

(extend-protocol GetDocumentId
  DBObject
  (get-id
   [^DBObject object]
    (.get object "_id"))

  IPersistentMap
  (get-id
    [^IPersistentMap object]
    (or (:_id object) (object "_id"))))

(defn into-array-list
  "Coerce a j.u.Collection into a j.u.ArrayList."
  ^java.util.ArrayList [^java.util.Collection coll]
  (java.util.ArrayList. coll))
