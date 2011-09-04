(ns monger.json
  (:import (org.bson.types ObjectId))
  (:require [clojure.data.json :as json]))

;;
;; API
;;

(extend-protocol json/Write-JSON
  ObjectId
  (write-json [^ObjectId object out escape-unicode?]
    (json/write-json (.toString object) out escape-unicode?)))
