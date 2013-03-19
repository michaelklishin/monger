(ns monger.search
  "Full text search queries support (MongoDB 2.4+)"
  (:require [monger.command :as cmd]
            [monger.conversion :as cnv])
  (:import [com.mongodb CommandResult BasicDBList DBObject]))

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
  [^String collection query]
  (cmd/search collection query))

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
