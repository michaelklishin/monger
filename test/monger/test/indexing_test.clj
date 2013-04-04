(ns monger.test.indexing-test
  (:import  org.bson.types.ObjectId
            java.util.Date)
  (:require [monger core util]
            [monger.collection :as mc]
            [monger.test.helper :as helper]
            monger.joda-time)
  (:use clojure.test
        monger.test.fixtures
        [clj-time.core :only [now secs ago from-now]]))

(helper/connect!)


;;
;; indexes
;;

(deftest ^{:indexing true} test-creating-and-dropping-indexes
  (let [collection "libraries"]
    (mc/drop-indexes collection)
    (mc/create-index collection { "language" 1 })
    (is (= "language_1"
           (:name (second (mc/indexes-on collection)))))
    (mc/drop-index collection "language_1")
    (mc/create-index collection ["language"])
    (mc/drop-index collection "language_1")
    (is (nil? (second (mc/indexes-on collection))))
    (mc/ensure-index collection (array-map "language" 1) {:unique true})
    (is (= "language_1"
           (:name (second (mc/indexes-on collection)))))
    (mc/ensure-index collection (array-map "language" 1))
    (mc/ensure-index collection (array-map "language" 1) { :unique true })
    (mc/drop-indexes collection)))

(deftest ^{:indexing true :edge-features true :time-consuming true} test-ttl-collections
  (let [coll  "recent_events"
        ttl   30
        sleep 120]
    (mc/remove coll)
    (mc/ensure-index coll (array-map :created-at 1) {:expireAfterSeconds ttl})
    (dotimes [i 100]
      (mc/insert coll {:type "signup" :created-at (-> i secs ago) :i i}))
    (dotimes [i 100]
      (mc/insert coll {:type "signup" :created-at (-> i secs from-now) :i i}))
    (is (= 200 (mc/count coll {:type "signup"})))
    ;; sleep for 65 seconds. MongoDB 2.1.2 seems to run TTLMonitor once per minute, according to
    ;; the log. MK.
    (println (format "Now sleeping for %d seconds to test TTL collections!" sleep))
    (Thread/sleep (* sleep 1000))
    (println (format "Documents in the TTL collection: %d" (mc/count coll {:type "signup"})))
    (is (< (mc/count coll {:type "signup"}) 100))
    (mc/remove coll)))
