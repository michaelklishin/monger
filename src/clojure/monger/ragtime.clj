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

(ns monger.ragtime
  "Ragtime integration"
  (:refer-clojure :exclude [find sort])
  (:require [ragtime.protocols :as proto]
            [monger.core       :as mg]
            [monger.collection :as mc]
            [monger.query :refer [with-collection find sort]])
  (:import java.util.Date
           [com.mongodb DB WriteConcern]))


(def ^{:const true}
  migrations-collection "meta.migrations")


(extend-type com.mongodb.DB
  proto/DataStore
  (add-migration-id [db id]
    (mc/insert db migrations-collection {:_id id :created_at (Date.)} WriteConcern/FSYNC_SAFE))
  (remove-migration-id [db id]
    (mc/remove-by-id db migrations-collection id))
  (applied-migration-ids [db]
    (let [xs (with-collection db migrations-collection
               (find {})
               (sort {:created_at 1}))]
      (vec (map :_id xs)))))


(defn flush-migrations!
  "REMOVES all the information about previously performed migrations"
  [^DB db]
  (mc/remove db migrations-collection))
