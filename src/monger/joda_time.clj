(ns monger.joda-time
  (:import (org.joda.time DateTime DateTimeZone))
  (:use [monger.conversion])
  (:require [clojure.data.json :as json]))

;;
;; API
;;

(extend-protocol ConvertToDBObject
  org.joda.time.DateTime
  (to-db-object [^org.joda.time.DateTime input]
    (to-db-object (.toDate input))))

(extend-protocol ConvertFromDBObject
  java.util.Date
  (from-db-object [^java.util.Date input keywordize]
    (org.joda.time.DateTime. input)))


(extend-protocol json/Write-JSON
  org.joda.time.DateTime
  (write-json [^org.joda.time.DateTime object out escape-unicode?]
    ;; TODO: use .printTo(Writer) here instead of ignoring
    ;;       out parameter. MK.
    (.print (org.joda.time.format.ISODateTimeFormat/dateTime) object)))
