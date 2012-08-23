;; Copyright (c) 2011-2012 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:doc "An optional convenience namespaces for applications that heavily use dates and would prefer use JodaTime types
            transparently when storing and loading them from MongoDB and serializing to JSON and/or with Clojure reader.

            Enables automatic conversion of JodaTime date/time/instant instances to JDK dates (java.util.Date) when documents
            are serialized and the other way around when documents are loaded. Extends clojure.data.json/Write-JSON protocol for
            JodaTime types.

            To use it, make sure you add dependencies on clj-time (or JodaTime) and clojure.data.json."} monger.joda-time
  (:import [org.joda.time DateTime DateTimeZone ReadableInstant]
           [org.joda.time.format ISODateTimeFormat])
  (:use [monger.conversion]))

;;
;; API
;;

(extend-protocol ConvertToDBObject
  org.joda.time.base.AbstractInstant
  (to-db-object [^AbstractInstant input]
    (to-db-object (.toDate input))))

(extend-protocol ConvertFromDBObject
  java.util.Date
  (from-db-object [^java.util.Date input keywordize]
    (org.joda.time.DateTime. input)))



;;
;; Reader extensions
;;

(defmethod print-dup java.util.Date
  [^java.util.Date d ^java.io.Writer out]
  (.write out
          (str "#="
               `(java.util.Date. ~(.getYear d)
                                 ~(.getMonth d)
                                 ~(.getDate d)
                                 ~(.getHours d)
                                 ~(.getMinutes d)
                                 ~(.getSeconds d)))))


(defmethod print-dup org.joda.time.base.AbstractInstant
  [^org.joda.time.base.AbstractInstant d out]
  (print-dup (.toDate d) out))


;;
;; JSON serialization
;;

(try
  ;; try to load clojure.data.json. If available, load CLJW Support
  ;; extensions.
  (require 'clojure.data.json)
  (require 'clojurewerkz.support.json)
  (catch Throwable _
    false))