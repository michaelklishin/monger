;; This source code is dual-licensed under the Apache License, version
;; 2.0, and the Eclipse Public License, version 1.0.
;;
;; The APL v2.0:
;;
;; ----------------------------------------------------------------------------------
;; Copyright (c) 2011-2018 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
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
;; Copyright (c) 2011-2018 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team.
;; All rights reserved.
;;
;; This program and the accompanying materials are made available under the terms of
;; the Eclipse Public License Version 1.0,
;; which accompanies this distribution and is available at
;; http://www.eclipse.org/legal/epl-v10.html.
;; ----------------------------------------------------------------------------------

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
