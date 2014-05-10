;; Copyright (c) 2011-2014 Michael S. Klishin
;; Copyright (c) 2012 Toby Hede
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

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
  (:import [com.mongodb DB DBObject]))


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
  ([^DB db ^String from ^String to]
     (monger.core/command db (sorted-map :renameCollection from :to to))))

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

(defn search
  [^DB db ^String collection query]
  (monger.core/command db {"text" collection "search" query}))
