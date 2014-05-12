;; Copyright (c) 2011-2014 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.search
  "Full text search queries support (MongoDB 2.4+)"
  (:require [monger.command :as cmd]
            [monger.conversion :as cnv])
  (:import [com.mongodb DB CommandResult BasicDBList DBObject]))

;;
;; Implementation
;;

(defn- convert-hit
  [^DBObject dbo keywordize-keys?]
  (cnv/from-db-object dbo keywordize-keys?))


;;
;; API
;;

(defn search
  "Performs a full text search query"
  [^DB db ^String collection query]
  (cmd/search db collection query))

(defn results-from
  "Returns a lazy sequence of results from a search query response, sorted by score.

   Each result is a Clojure map with two keys: :score and :obj."
  ([^CommandResult res]
     (results-from res true))
  ([^CommandResult res keywordize-keys?]
     (let [sorter (if keywordize-keys?
                    :score
                    (fn [m]
                      (get m "score")))]
       (sort-by sorter >
                (map #(convert-hit % keywordize-keys?) ^BasicDBList (.get res "results"))))))
