;; requires profile dev2 for clj-time
(set! *warn-on-reflection* true)

(ns monger.test.clj-time-local-date-test
  (:import java.util.Date)
  (:require [monger core joda-time]
            [clojure stacktrace]
            [monger.collection :as mgcol]
            [clj-time.core :as dt]
            [clj-time.coerce :as dt-coerce]
            [monger.test.helper :as helper])
  (:use [clojure.test]
        [monger.operators]
        [monger.test.fixtures]))

(monger.core/connect!)
(monger.core/set-db! (monger.core/get-db "monger-test"))

(use-fixtures :each purge-people purge-docs purge-things purge-libraries)

(deftest find-with-local-date
  (let [collection "libraries"]
    (mgcol/insert-batch collection [{:language "Clojure" :name "aleph" :created_at (dt/now)}
                                    {:language "R"
                                     :name "bayes" 
                                     :created_at (dt-coerce/to-date-time (dt/plus (dt/today) (dt/days 1)))}])
    (are [n the-criteria] 
         (= n (.count (mgcol/find collection the-criteria)))
         2 {:created_at {$gt (dt/today)}}
         1 {:created_at {"$gte" (dt/today) "$lt" (dt/plus (dt/today) (dt/days 1))}}
         1 {:created_at {"$gte" (dt/plus (dt/today) (dt/days 1))}})))

