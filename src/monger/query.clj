(ns monger.query
  (:refer-clojure :exclude [select find sort])
  (:require [monger.core]
            [monger.internal pagination])
  (:import [com.mongodb DB DBCollection DBObject DBCursor]
           [java.util List])
  (:use [monger conversion operators]))


;;
;; Implementation
;;

(def ^{ :dynamic true } *query-collection*)

(defn empty-query
  [^DBCollection coll]
  {
   :collection      coll
   :query           {}
   :sort            {}
   :fields          []
   :skip            0
   :limit           0
   :batch-size      256
   :hint            nil
   :snapshot        false
   })

(defn- fields-to-db-object
  [^List fields]
  (to-db-object (zipmap fields (repeat 1))))

(defn exec
  [{ :keys [collection query fields skip limit sort batch-size hint snapshot] :or { limit 0 batch-size 256 skip 0 } }]
  (let [cursor (doto ^DBCursor (.find ^DBCollection collection (to-db-object query) (fields-to-db-object fields))
                     (.limit limit)
                     (.skip  skip)
                     (.sort  (to-db-object sort))
                     (.batchSize batch-size)
                     (.hint ^DBObject (to-db-object hint))
                     )]
    (if snapshot
      (.snapshot cursor))
    (map (fn [x] (from-db-object x true))
         (seq cursor))))

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
