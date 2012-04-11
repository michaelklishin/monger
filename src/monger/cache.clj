(ns ^{:doc "clojure.core.cache implementation(s) on top of MongoDB."
      :author "Michael S. Klishin"}
  monger.cache
  (:require [monger.collection  :as mc]
            [clojure.core.cache :as cache])
  (:use monger.conversion)
  (:import [clojure.core.cache CacheProtocol]))

;;
;; Implementation
;;

(def ^{:const true}
  default-cache-collection "cache_entries")

;;
;; API
;;

(defrecord BasicMongerCache [collection])

(extend-protocol cache/CacheProtocol
  BasicMongerCache
  (lookup [c k]
    (:value (mc/find-map-by-id (:collection c) k)))
  #_ (lookup [c k not-found]
    (if-let [doc (mc/find-map-by-id (:collection c) k)]
      (:value doc)
      not-found))
  (has? [c k]
    (mc/any? (get c :collection) {:_id k}))
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
