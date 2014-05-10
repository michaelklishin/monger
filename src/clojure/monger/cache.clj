;; Copyright (c) 2011-2014 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.cache
  "clojure.core.cache implementation(s) on top of MongoDB.
      
   Related documentation guide: http://clojuremongodb.info/articles/integration.html"
  (:require [monger.collection  :as mc :refer [find-one find-by-id find-map-by-id]]
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

;;
;; API
;;

(defrecord BasicMongerCache [db collection])

(extend-protocol cache/CacheProtocol
  BasicMongerCache
  (lookup [c k]
    (let [m (mc/find-map-by-id (:db c) (:collection c) k)]
      (:value m)))
  (has? [c k]
    (not (nil? (mc/find-by-id  (:db c) (:collection c) k))))
  (hit [this k]
    this)
  (miss [c k v]
    (mc/insert (:db c) (:collection c) {:_id k :value v})
    c)
  (evict [c k]
    (mc/remove-by-id (:db c) (:collection c) k)
    c)
  (seed [c m]
    (mc/insert-batch (:db c) (:collection c) (map (fn [[k v]]
                                                    {:_id k :value v}) m))
    c))


(defn basic-monger-cache-factory
  ([^DB db]
     (BasicMongerCache. db default-cache-collection))
  ([^DB db collection]
     (BasicMongerCache. db collection))
  ([^DB db collection base]
     (cache/seed (BasicMongerCache. db collection) base)))
