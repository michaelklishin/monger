(ns monger.cache
  "clojure.core.cache implementation(s) on top of MongoDB.
      
   Related documentation guide: http://clojuremongodb.info/articles/integration.html"
  (:require [monger.collection  :as mc]
            [clojure.core.cache :as cache]
            [monger.conversion  :as cnv])
  (:import clojure.core.cache.CacheProtocol
           [com.mongodb DB DBObject WriteConcern]
           java.util.Map))

;;
;; Implementation
;;

(def ^{:const true}
  default-cache-collection "cache_entries")


(defn- ^DBObject find-one
  [^DB db ^String collection ^Map ref]
  (.findOne (.getCollection db (name collection))
            (cnv/to-db-object ref)))

(defn- find-by-id
  "A version of monger.collection/find-by-id that does not require the
   fields argument"
  [^DB db ^String collection id]
  (find-one db collection {:_id id}))

(defn- find-map-by-id
  "A version of monger.collection/find-by-map-id that accepts database
   as an argument"
  [^DB db ^String collection id]
  (cnv/from-db-object ^DBObject (find-one db collection {:_id id}) true))

;;
;; API
;;

(defrecord BasicMongerCache [collection])

(extend-protocol cache/CacheProtocol
  BasicMongerCache
  (lookup [c k]
    (let [m (mc/find-map-by-id (:collection c) k)]
      (:value m)))
  (has? [c k]
    (not (nil? (mc/find-by-id (get c :collection) k))))
  (hit [this k]
    this)
  (miss [c k v]
    (mc/insert (get c :collection) {:_id k :value v})
    c)
  (evict [c k]
    (mc/remove-by-id (get c :collection) k)
    c)
  (seed [c m]
    (mc/insert-batch (get c :collection) (map (fn [[k v]]
                                                {:_id k :value v}) m))
    c))


(defn basic-monger-cache-factory
  ([]
     (BasicMongerCache. default-cache-collection))
  ([collection]
     (BasicMongerCache. collection))
  ([collection base]
     (cache/seed (BasicMongerCache. collection) base)))


(defrecord DatabaseAwareMongerCache [db collection])

(extend-protocol cache/CacheProtocol
  DatabaseAwareMongerCache
  (lookup [c k]
    (let [m (find-map-by-id (:db c) (:collection c) k)]
      (:value m)))
  (has? [c k]
    (not (nil? (find-by-id (:db c) (:collection c) k))))
  (hit [this k]
    this)
  (miss [c k v]
    (mc/insert (:db c) (:collection c) {:_id k :value v} WriteConcern/SAFE)
    c)
  (evict [c k]
    (mc/remove-by-id (:db c) (:collection c) k)
    c)
  (seed [c m]
    (mc/insert-batch (:db c) (:collection c) (map (fn [[k v]]
                                                    {:_id k :value v}) m) WriteConcern/SAFE)
    c))


(defn db-aware-monger-cache-factory
  ([db]
     (DatabaseAwareMongerCache. db default-cache-collection))
  ([db collection]
     (DatabaseAwareMongerCache. db collection))
  ([db collection base]
     (cache/seed (DatabaseAwareMongerCache. db collection) base)))
