;; Copyright (c) 2011-2012 Michael S. Klishin
;; Copyright (c) 2012 Toby Hede
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:doc "Provides convenience functions for performing most commonly used MongoDB commands.
            For a lower-level API that gives maximum flexibility, see `monger.core/command`. To use
            MongoDB 2.2 Aggregation Framework, see `monger.collection/aggregate`.

            Related documentation guides:

            * http://clojuremongodb.info/articles/commands.html
            * http://clojuremongodb.info/articles/aggregation.html
            * http://clojuremongodb.info/articles/mapreduce.html"}
  monger.command
  (:require monger.core)
  (:use monger.conversion)
  (:import com.mongodb.DB))


(defn collection-stats
  ([collection]
     (collection-stats monger.core/*mongodb-database* collection))
  ([^DB database collection]
     (monger.core/command database { :collstats collection })))

(defn db-stats
  ([]
     (db-stats monger.core/*mongodb-database*))
  ([^DB database]
     (monger.core/command database {:dbStats 1 })))


(defn reindex-collection
  "Forces an existing collection to be reindexed using the reindexCollection command"
  ([^String collection]
     (reindex-collection monger.core/*mongodb-database* collection))
  ([^DB database ^String collection]
     (monger.core/command database { :reIndex collection })))

(defn rename-collection
  "Changes the name of an existing collection using the renameCollection command"
  ([^String from ^String to]
     (reindex-collection monger.core/*mongodb-database* from to))
  ([^DB database ^String from ^String to]
     (monger.core/command database { :renameCollection from :to to })))

(defn convert-to-capped
  "Converts an existing, non-capped collection to a capped collection using the convertToCapped command"
  ([^String collection ^long size]
     (convert-to-capped monger.core/*mongodb-database* collection size))
  ([^DB database ^String collection ^long size]
     (monger.core/command database {:convertToCapped collection :size size})))

(defn empty-capped
  "Removes all documents from a capped collection using the emptycapped command"
  ([^String collection]
     (empty-capped monger.core/*mongodb-database* collection))
  ([^DB database ^String collection]
     (monger.core/command database {:emptycapped collection})))


(defn compact
  "Rewrites and defragments a single collection using the compact command. This also forces all indexes on the collection to be rebuilt"
  ([^String collection]
     (compact monger.core/*mongodb-database* collection))
  ([^DB database ^String collection]
     (monger.core/command database {:compact collection})))


(defn server-status
  ([]
     (server-status monger.core/*mongodb-database*))
  ([^DB database]
     (monger.core/command database {:serverStatus 1 })))


(defn top
  []
  (monger.core/command (monger.core/get-db "admin") {:top 1}))
