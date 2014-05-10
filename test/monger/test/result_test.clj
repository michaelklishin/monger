(ns monger.test.result-test
  (:import [com.mongodb BasicDBObject WriteResult WriteConcern] java.util.Date)
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            monger.result
            monger.util
            [clojure.test :refer :all]))

(deftest test-ok?
  (let [result-that-is-not-ok-1 (doto (BasicDBObject.) (.put "ok" 0))
        result-that-is-not-ok-2 (doto (BasicDBObject.) (.put "ok" "false"))
        result-that-is-ok-1     (doto (BasicDBObject.) (.put "ok" 1))
        result-that-is-ok-2     (doto (BasicDBObject.) (.put "ok" "true"))
        result-that-is-ok-3     (doto (BasicDBObject.) (.put "ok" 1.0))]
    (is (not (monger.result/ok? result-that-is-not-ok-1)))
    (is (not (monger.result/ok? result-that-is-not-ok-2)))
    (is (monger.result/ok? result-that-is-ok-1))
    (is (monger.result/ok? result-that-is-ok-2))
    (is (monger.result/ok? result-that-is-ok-3))))


(deftest test-has-error?
  (let [result-that-has-no-error1 (doto (BasicDBObject.) (.put "ok" 0))
        result-that-has-no-error2 (doto (BasicDBObject.) (.put "err" ""))
        result-that-has-error1    (doto (BasicDBObject.) (.put "err" (BasicDBObject.)))]
    (is (not (monger.result/has-error? result-that-has-no-error1)))
    (is (not (monger.result/has-error? result-that-has-no-error2)))
    (is (monger.result/has-error?      result-that-has-error1))))


(deftest test-updated-existing?-with-db-object
  (let [input1 (doto (BasicDBObject.) (.put "updatedExisting" true))
        input2 (doto (BasicDBObject.) (.put "updatedExisting" false))
        input3 (BasicDBObject.)]
    (is (monger.result/updated-existing?      input1))
    (is (not (monger.result/updated-existing? input2)))
    (is (not (monger.result/updated-existing? input3)))))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (deftest test-updated-existing?-with-write-result
    (mc/remove db "libraries")
    (let [collection "libraries"
          doc-id       (monger.util/random-uuid)
          date         (Date.)
          doc          { :created-at date, :data-store "MongoDB", :language "Clojure", :_id doc-id }
          modified-doc { :created-at date, :data-store "MongoDB", :language "Erlang",  :_id doc-id }]
      (is (not (monger.result/updated-existing? (mc/update db collection { :language "Clojure" } doc {:upsert true}))))
      (is (monger.result/updated-existing? (mc/update db collection { :language "Clojure" }      doc {:upsert true})))
      (monger.result/updated-existing? (mc/update db collection { :language "Clojure" } modified-doc {:multi false :upsert true}))
      (mc/remove db collection))))
