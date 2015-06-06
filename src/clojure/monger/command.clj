;; This source code is dual-licensed under the Apache License, version
;; 2.0, and the Eclipse Public License, version 1.0.
;;
;; The APL v2.0:
;;
;; ----------------------------------------------------------------------------------
;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;; Copyright (c) 2012 Toby Hede
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
;; Copyright (c) 2012 Toby Hede
;; All rights reserved.
;;
;; This program and the accompanying materials are made available under the terms of
;; the Eclipse Public License Version 1.0,
;; which accompanies this distribution and is available at
;; http://www.eclipse.org/legal/epl-v10.html.
;; ----------------------------------------------------------------------------------

(ns monger.command
  "Provides convenience functions for performing most commonly used MongoDB commands.
   For a lower-level API that gives maximum flexibility, see `monger.core/command`. To use
   MongoDB 2.2 Aggregation Framework, see `monger.collection/aggregate`.

   Related documentation guides:

   * http://clojuremongodb.info/articles/commands.html
   * http://clojuremongodb.info/articles/aggregation.html
   * http://clojuremongodb.info/articles/mapreduce.html"
  (:require monger.core
            [monger.conversion :refer :all])
  (:import [com.mongodb MongoClient DB DBObject]))


;;
;; API
;;

(defn admin-command
  "Executes a command on the admin database"
  [^MongoClient conn m]
  (monger.core/command (monger.core/admin-db conn) m))

(defn raw-admin-command
  "Executes a command on the admin database"
  [^MongoClient conn ^DBObject cmd]
  (monger.core/raw-command (monger.core/admin-db conn) cmd))

(defn collection-stats
  [^DB database collection]
  (monger.core/command database {:collstats collection}))

(defn db-stats
  [^DB database]
  (monger.core/command database {:dbStats 1}))


(defn reindex-collection
  "Forces an existing collection to be reindexed using the reindexCollection command"
  [^DB database ^String collection]
  (monger.core/command database {:reIndex collection}))

(defn rename-collection
  "Changes the name of an existing collection using the renameCollection command"
  [^DB db ^String from ^String to]
  (monger.core/command db (sorted-map :renameCollection from :to to)))

(defn convert-to-capped
  "Converts an existing, non-capped collection to a capped collection using the convertToCapped command"
  [^DB db ^String collection ^long size]
  (monger.core/command db (sorted-map :convertToCapped collection :size size)))

(defn empty-capped
  "Removes all documents from a capped collection using the emptycapped command"
  [^DB db ^String collection]
  (monger.core/command db {:emptycapped collection}))


(defn compact
  "Rewrites and defragments a single collection using the compact command. This also forces all indexes on the collection to be rebuilt"
  [^DB db ^String collection]
  (monger.core/command db {:compact collection}))


(defn server-status
  [^DB db]
  (monger.core/command db {:serverStatus 1}))


(defn top
  [^MongoClient conn]
  (monger.core/command (monger.core/admin-db conn) {:top 1}))
