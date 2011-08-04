(ns monger.collection
  (:import (com.mongodb Mongo DB))
  )

;;
;; API
;;

(defn ^long count
  [^DB db, ^String collection]
  (.count (.getCollection db collection)))
