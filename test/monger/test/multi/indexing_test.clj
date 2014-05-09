(ns monger.test.multi.indexing-test
  (:import  org.bson.types.ObjectId
            java.util.Date)
  (:require [monger.core        :as mg]
            [monger.multi.collection :as mc]
            [monger.test.helper :as helper]
            monger.joda-time
            [clojure.test :refer :all]
            [monger.test.fixtures :refer :all]
            [clj-time.core :refer [now secs ago from-now]]))

(helper/connect!)

(def db (mg/get-db "altdb"))

(defn purge-altdb
  [f]
  (mc/remove db "libraries")
  (mc/remove db "recent_events")
  (f))

(use-fixtures :each purge-altdb)

(deftest ^{:indexing true} test-creating-and-dropping-indexes
  (let [db (mg/get-db "altdb")
        collection "libraries"]
    (mc/drop-indexes db collection)
    (mc/create-index db collection { "language" 1 })
    (is (= "language_1"
           (:name (second (mc/indexes-on db collection)))))
    (mc/drop-index db collection "language_1")
    (mc/create-index db collection ["language"])
    (mc/drop-index db collection "language_1")
    (is (nil? (second (mc/indexes-on db collection))))
    (mc/ensure-index db collection (array-map "language" 1) {:unique true})
    (is (= "language_1"
           (:name (second (mc/indexes-on db collection)))))
    (mc/ensure-index db collection (array-map "language" 1))
    (mc/ensure-index db collection (array-map "language" 1) { :unique true })
    (mc/drop-indexes db collection)))

(deftest ^{:indexing true :edge-features true :time-consuming true} test-ttl-collections
  (let [db (mg/get-db "altdb")
        coll  "recent_events"
        ttl   30
        sleep 120]
    (mc/remove db coll)
    (mc/ensure-index db coll (array-map :created-at 1) {:expireAfterSeconds ttl})
    (dotimes [i 100]
      (mc/insert db coll {:type "signup" :created-at (-> i secs ago) :i i}))
    (dotimes [i 100]
      (mc/insert db coll {:type "signup" :created-at (-> i secs from-now) :i i}))
    (is (= 200 (mc/count db coll {:type "signup"})))
    ;; sleep for 65 seconds. MongoDB 2.1.2 seems to run TTLMonitor once per minute, according to
    ;; the log. MK.
    (println (format "Now sleeping for %d seconds to test TTL collections!" sleep))
    (Thread/sleep (* sleep 1000))
    (println (format "Documents in the TTL collection: %d" (mc/count db coll {:type "signup"})))
    (is (< (mc/count db coll {:type "signup"}) 100))
    (mc/remove db coll)))
