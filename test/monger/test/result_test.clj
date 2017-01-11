(ns monger.test.result-test
  (:import [com.mongodb BasicDBObject WriteResult WriteConcern] java.util.Date)
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.result :as mgres]
            monger.util
            [clojure.test :refer :all]))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (deftest test-updated-existing?-with-write-result
    (mc/remove db "libraries")
    (let [collection "libraries"
          doc-id       (monger.util/random-uuid)
          date         (Date.)
          doc          { :created-at date :data-store "MongoDB" :language "Clojure" :_id doc-id }
          modified-doc { :created-at date :data-store "MongoDB" :language "Erlang"  :_id doc-id }]
      (let [result (mc/update db collection { :language "Clojure" } doc {:upsert true})]
        (is (not (mgres/updated-existing? result)))
        (is (= 1 (mgres/affected-count result))))
      (is (mgres/updated-existing? (mc/update db collection { :language "Clojure" } doc {:upsert true})))
      (is (mgres/updated-existing? (mc/update db collection { :language "Clojure" } modified-doc {:multi false :upsert true})))
      (is (= 1 (mgres/affected-count (mc/remove db collection { :_id doc-id }))))
      (mc/remove db collection)
      (mg/disconnect conn))))
