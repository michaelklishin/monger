(ns monger.test.lib-integration-test
  (:use clojure.test
        monger.json
        monger.joda-time
        monger.conversion)
  (:import [org.joda.time DateTime DateMidnight]
           org.bson.types.ObjectId
           com.mongodb.DBObject)
  (:require [clojure.data.json :as json]
            [clj-time.core     :as t]))


(deftest serialization-of-joda-datetime-to-json-with-clojure-data-json
  (is (= "\"2011-10-13T23:55:00.000Z\"" (json/json-str (t/date-time 2011 10 13 23 55 0)))))

(deftest serialization-of-object-id-to-json-with-clojure-data-json
  (is (= "\"4ec2d1a6b55634a935ea4ac8\"" (json/json-str (ObjectId. "4ec2d1a6b55634a935ea4ac8")))))


(deftest conversion-of-joda-datetime-to-db-object
  (let [d (to-db-object (t/date-time 2011 10 13 23 55 0))]
    (is (instance? java.util.Date d))
    (is (= 1318550100000 (.getTime ^java.util.Date d)))))


(deftest conversion-of-joda-datemidnight-to-db-object
  (let [d (to-db-object (DateMidnight. (t/date-time 2011 10 13)))]
    (is (instance? java.util.Date d))
    (is (= 1318464000000 (.getTime ^java.util.Date d)))))


(deftest conversion-of-java-util-date-to-joda-datetime
  (let [input  (.toDate ^DateTime (t/date-time 2011 10 13 23 55 0))
        output (from-db-object input false)]
    (is (instance? org.joda.time.DateTime output))
    (is (= input (.toDate ^DateTime output)))))


(deftest test-reader-extensions
  (let [^DateTime d (t/date-time 2011 10 13 23 55 0)]
    (binding [*print-dup* true]
      (pr-str d))))
