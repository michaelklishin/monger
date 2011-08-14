;; Copyright (c) 2011 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

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

