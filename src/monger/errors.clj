(ns monger.errors
  (:import (com.mongodb DBObject WriteResult)
           (clojure.lang IPersistentMap))
  (:require [monger convertion]))


;;
;; API
;;

(defprotocol MongoCommandResult
  (ok? [input] "Returns true if command result is a success"))

(extend-protocol MongoCommandResult
  DBObject
  (ok?
    [^DBObject result]
    (.contains [true "true" 1 1.0] (.get result "ok")))

  WriteResult
  (ok?
    [^WriteResult result]
    (ok? (.getLastError result))))

