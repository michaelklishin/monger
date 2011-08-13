(ns monger.errors
  (:import (com.mongodb DBObject WriteResult CommandResult)
           (clojure.lang IPersistentMap))
  (:require [monger convertion]))


;;
;; API
;;

(defn ^boolean ok?
  [^DBObject result]
  (.contains [true "true" 1] (.get result "ok")))



