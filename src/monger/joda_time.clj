(ns monger.joda-time
  (:import (org.joda.time DateTime DateTimeZone))
  (:use [monger.conversion]))

;;
;; API
;;

(extend-protocol ConvertToDBObject
  org.joda.time.DateTime
  (to-db-object [^org.joda.time.DateTime input]
    (to-db-object (.toDate input))))
