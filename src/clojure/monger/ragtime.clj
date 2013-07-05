(ns monger.ragtime
  (:refer-clojure :exclude [find sort])
  (:require [ragtime.core      :as ragtime]
            [monger.core       :as mg]
            [monger.collection :as mc])
  (:use [monger.query :only [with-collection find sort]])
  (:import java.util.Date
           [com.mongodb DB WriteConcern]))


(def ^{:const true}
  migrations-collection "meta.migrations")



(extend-type com.mongodb.DB
  ragtime/Migratable
  (add-migration-id [db id]
    (mc/insert db migrations-collection {:_id id :created_at (Date.)} WriteConcern/FSYNC_SAFE))
  (remove-migration-id [db id]
    (mc/remove-by-id db migrations-collection id))
  (applied-migration-ids [db]
    (mg/with-db db
      (let [xs (with-collection migrations-collection
                 (find {})
                 (sort {:created_at 1}))]
        (vec (map :_id xs))))))


(defn flush-migrations!
  "REMOVES all the information about previously performed migrations"
  [db]
  (mg/with-db db
    (mc/remove migrations-collection)))
