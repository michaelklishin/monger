;; Copyright (c) 2011-2012 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:doc "Provides functions that determine if a query (or other database operation)
            was successful or not.

            Related documentation guides:

            * http://clojuremongodb.info/articles/inserting.html
            * http://clojuremongodb.info/articles/updating.html
            * http://clojuremongodb.info/articles/commands.html
            * http://clojuremongodb.info/articles/mapreduce.html"}
  monger.result
  (:import [com.mongodb DBObject WriteResult MapReduceOutput]
           clojure.lang.IPersistentMap)
  (:require monger.conversion))


;;
;; Implementation
;;

(defn- okayish?
  [value]
  (contains? #{true "true" 1 1.0} value))


;;
;; API
;;

(defprotocol MongoCommandResult
  (ok?               [input] "Returns true if command result is a success")
  (has-error?        [input] "Returns true if command result indicates an error")
  (updated-existing? [input] "Returns true if command result has `updatedExisting` field set to true"))

(extend-protocol MongoCommandResult
  DBObject
  (ok?
    [^DBObject result]
    (okayish? (.get result "ok")))
  (has-error?
    [^DBObject result]
    ;; yes, this is exactly the logic MongoDB Java driver uses.
    (> (count (str (.get result "err"))) 0))
  (updated-existing?
    [^DBObject result]
    (let [v ^Boolean (.get result "updatedExisting")]
      (and v (Boolean/valueOf v))))


  WriteResult
  (ok?
    [^WriteResult result]
    (and (not (nil? result)) (ok? (.getLastError result))))
  (has-error?
    [^WriteResult result]
    (has-error? (.getLastError result)))
  (updated-existing?
    [^WriteResult result]
    (updated-existing? (.getLastError result)))

  MapReduceOutput
  (ok?
    [^MapReduceOutput result]
    (ok? ^DBObject (.getRaw result)))

  Boolean
  (ok?
    [^Boolean b]
    (= Boolean/TRUE b))
  
  IPersistentMap
  (ok?
    [^IPersistentMap m]
    (okayish? (or (get m :ok)
                  (get m "ok")))))
