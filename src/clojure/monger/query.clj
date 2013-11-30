;; Copyright (c) 2011-2012 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.query
  "Provides an expressive Query DSL that is very close to that in the Mongo shell (within reason).
   This is the most flexible and recommended way to query with Monger. Queries can be composed, like in Korma.

   Related documentation guide: http://clojuremongodb.info/articles/querying.html"
  (:refer-clojure :exclude [select find sort])
  (:require [monger.core]
            [monger.internal pagination]
            [monger.cursor :as cursor :refer [add-options]]
            [monger.conversion :refer :all]
            [monger.operators :refer :all])
  (:import [com.mongodb DB DBCollection DBObject DBCursor ReadPreference]
           java.util.List))


;;
;; Implementation
;;

(def ^{:dynamic true} *query-collection*)

;;
;; Cursor/chain methods
;;
;; Monger query is an auxiliary construction that helps to create funciton chains through cursors.
;;   You can specify several chained actions that will be performed on the certain collection through
;;   query fields.
;;
;; Existing query fields:
;;
;; :fields - selects which fields are returned. The default is all fields. _id is included by default.
;; :sort - adds a sort to the query.
;; :fields - set of fields to retrieve during query execution
;; :skip - Skips the first N results.
;; :limit - Returns a maximum of N results.
;; :batch-size - limits the nubmer of elements returned in one batch.
;; :hint - force Mongo to use a specific index for a query in order to improve performance.
;; :snapshot - sses snapshot mode for the query. Snapshot mode assures no duplicates are returned, or objects missed
;;    which were present at both the start and end of the query's execution (if an object is new during the query, or
;;    deleted during the query, it may or may not be returned, even with snapshot mode). Note that short query responses
;;    (less than 1MB) are always effectively snapshotted. Currently, snapshot mode may not be used with sorting or explicit hints.
(defn empty-query
  ([]
     {
      :query             {}
      :sort              {}
      :fields            []
      :skip              0
      :limit             0
      :batch-size        256
      :hint              nil
      :snapshot          false
      :keywordize-fields true
      })
  ([^DBCollection coll]
     (merge (empty-query) { :collection coll })))

(defn exec
  [{ :keys [^DBCollection collection query fields skip limit sort batch-size hint snapshot read-preference keywordize-fields options] :or { limit 0 batch-size 256 skip 0 } }]
  (with-open [cursor (doto (.find collection (to-db-object query) (as-field-selector fields))
                       (.limit limit)
                       (.skip  skip)
                       (.sort  (to-db-object sort))
                       (.batchSize batch-size)
                       (.hint (to-db-object hint)))]
    (when snapshot
      (.snapshot cursor))
    (when read-preference
      (.setReadPreference cursor read-preference))
    (when options
      (add-options cursor options))
    (map (fn [x] (from-db-object x keywordize-fields))
         cursor)))

;;
;; API
;;

(defn find
  [m query]
  (merge m { :query query }))

(defn fields
  [m flds]
  (merge m { :fields flds }))

(defn sort
  [m srt]
  (merge m { :sort srt }))

(defn skip
  [m ^long n]
  (merge m { :skip n }))

(defn limit
  [m ^long n]
  (merge m { :limit n }))

(defn batch-size
  [m ^long n]
  (merge m { :batch-size n }))

(defn hint
  [m h]
  (merge m { :hint h }))

(defn snapshot
  [m]
  (merge m { :snapshot true }))

(defn read-preference
  [m ^ReadPreference rp]
  (merge m { :read-preference rp }))

(defn options
  [m opts]
  (merge m { :options opts }))

(defn keywordize-fields
  [m bool]
  (merge m { :keywordize-fields bool }))

(defn paginate
  [m & { :keys [page per-page] :or { page 1 per-page 10 } }]
  (merge m { :limit per-page :skip (monger.internal.pagination/offset-for page per-page) }))

(defmacro with-collection
  [^String coll & body]
  `(binding [*query-collection* (if (string? ~coll)
                                  (.getCollection ^DB monger.core/*mongodb-database* ~coll)
                                  ~coll)]
     (let [query# (-> (empty-query *query-collection*) ~@body)]
       (exec query#))))

(defmacro partial-query
  [& body]
  `(-> {} ~@body))
