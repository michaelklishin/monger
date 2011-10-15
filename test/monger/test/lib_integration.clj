(ns monger.test.lib-integration
  (:use [clojure.test]
        [monger.json]
        [monger.joda-time]
        [monger.conversion])
  (:import [org.joda.time DateTime ReadableInstant]
           [org.joda.time.format ISODateTimeFormat]
           [java.io StringWriter PrintWriter])
  (:require [clojure.data.json :as json]
            [clj-time.core     :as t]))


(deftest serialization-of-joda-datetime-to-json
  (is (= "\"2011-10-13T23:55:00.000Z\"" (json/json-str (t/date-time 2011 10 13 23 55 0)))))


(deftest conversion-of-joda-datetime-to-db-object
  (let [d (to-db-object (t/date-time 2011 10 13 23 55 0))]
    (is (instance? java.util.Date d))
    (is (= 1318550100000 (.getTime ^java.util.Date d)))))


(deftest conversion-of-java-util-date-to-joda-datetime
  (let [input  (.toDate ^DateTime (t/date-time 2011 10 13 23 55 0))
        output (from-db-object input false)]
    (is (instance? org.joda.time.DateTime output))
    (is (= input (.toDate ^DateTime output)))))
