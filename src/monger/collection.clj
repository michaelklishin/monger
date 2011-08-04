(ns monger.collection
  (:import (com.mongodb Mongo DB))
  )

;;
;; API
;;

;; monger.collection/insert
;; monger.collection/find
;; monger.collection/group

;; monger.collection/count
(defn ^long count
  [^DB db, ^String collection]
  (.count (.getCollection db collection)))

;; monger.collection/update
;; monger.collection/update-multi
;; monger.collection/remove

;; monger.collection/ensure-index
;; monger.collection/drop-index