;; Copyright (c) 2011-2012 Michael S. Klishin
;; Copyright (c) 2012 Toby Hede
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.command
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
  ([collection]
     (reindex-collection monger.core/*mongodb-database* collection))
  ([^DB database collection]
     (monger.core/command database { :reIndex collection })))


(defn server-status
  ([]
     (server-status monger.core/*mongodb-database*))
  ([^DB database]
     (monger.core/command database {:serverStatus 1 })))


(defn top
  []
  (monger.core/command (monger.core/get-db "admin") {:top 1}))
