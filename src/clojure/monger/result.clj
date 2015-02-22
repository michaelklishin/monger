;; Copyright (c) 2011-2014 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.result
  "Provides functions that determine if a query (or other database operation)
   was successful or not.

   Related documentation guides:

   * http://clojuremongodb.info/articles/inserting.html
   * http://clojuremongodb.info/articles/updating.html
   * http://clojuremongodb.info/articles/commands.html
   * http://clojuremongodb.info/articles/mapreduce.html"
  (:import [com.mongodb WriteResult])
  (:require monger.conversion))

;;
;; API
;;

(defprotocol WriteResultPredicates
  (acknowledged?     [input] "Returns true if write result is a success")
  (updated-existing? [input] "Returns true if write result has updated an existing document"))

(extend-protocol WriteResultPredicates
  WriteResult
  (acknowledged?
    [^WriteResult result]
    (.wasAcknowledged result))
  (updated-existing?
    [^WriteResult result]
    (.isUpdateOfExisting result)))
