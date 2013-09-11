(ns monger.test.lib-integration-test
  (:use clojure.test
        monger.conversion)
  (:import [org.joda.time DateTime DateMidnight LocalDate]
           org.bson.types.ObjectId
           com.mongodb.DBObject)
  (:require monger.json
            monger.joda-time
            [clj-time.core   :as t]
            [cheshire.core   :as json]))


(deftest ^{:integration true} serialization-of-joda-datetime-to-json
  (let [dt (t/date-time 2011 10 13 23 55 0)]
    (is (= "\"2011-10-13T23:55:00.000Z\""
           (json/encode dt)))))

(deftest ^{:integration true} serialization-of-joda-date-to-json
  (let [d (.toDate (t/date-time 2011 10 13 23 55 0))]
    (is (= "\"2011-10-13T23:55:00Z\""
           (json/encode d)))))

(deftest ^{:integration true} conversion-of-joda-datetime-to-db-object
  (let [d (to-db-object (t/date-time 2011 10 13 23 55 0))]
    (is (instance? java.util.Date d))
    (is (= 1318550100000 (.getTime ^java.util.Date d)))))


(deftest ^{:integration true} conversion-of-joda-datemidnight-to-db-object
  (let [d (to-db-object (DateMidnight. (t/date-time 2011 10 13)))]
    (is (instance? java.util.Date d))
    (is (= 1318464000000 (.getTime ^java.util.Date d)))))

(deftest ^{:integration true} conversion-of-joda-localdate-to-db-object
  (let [d (to-db-object (LocalDate. 2011 10 13))]
    (is (instance? java.util.Date d))
    (is (= 1318456800000 (.getTime ^java.util.Date d)))))

(deftest ^{:integration true} conversion-of-java-util-date-to-joda-datetime
  (let [input  (.toDate ^DateTime (t/date-time 2011 10 13 23 55 0))
        output (from-db-object input false)]
    (is (instance? org.joda.time.DateTime output))
    (is (= input (.toDate ^DateTime output)))))

(deftest ^{:integration true} test-reader-extensions
  (let [^DateTime d (t/date-time 2011 10 13 23 55 0)]
    (binding [*print-dup* true]
      (pr-str d))))

(deftest ^{:integration true} test-reader-extensions-for-localdate
  (let [^DateTime d (t/today)]
    (binding [*print-dup* true]
      (pr-str d))))