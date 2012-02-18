;; Copyright (c) 2011-2012 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

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
