(ns monger.joda-time
  (:import [org.joda.time DateTime DateTimeZone ReadableInstant]
           [org.joda.time.format ISODateTimeFormat])
  (:use [monger.conversion])
  (:require [clojure.data.json :as json]))

;;
;; API
;;

(extend-protocol ConvertToDBObject
  org.joda.time.DateTime
  (to-db-object [^DateTime input]
    (to-db-object (.toDate input))))

(extend-protocol ConvertFromDBObject
  java.util.Date
  (from-db-object [^java.util.Date input keywordize]
    (org.joda.time.DateTime. input)))


(extend-protocol json/Write-JSON
  org.joda.time.DateTime
  (write-json [^DateTime object out escape-unicode?]
    (json/write-json (.print (ISODateTimeFormat/dateTime) ^ReadableInstant object) out escape-unicode?)))
