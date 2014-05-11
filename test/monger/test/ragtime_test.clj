(ns monger.test.ragtime-test
  (:require [monger.core :as mg]
            [monger.collection  :as mc]
            monger.ragtime
            [ragtime.core :refer :all]
            [clojure.test :refer :all]))


(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (defn purge-collections
    [f]
    (mc/remove db "meta.migrations")
    (f)
    (mc/remove db "meta.migrations"))

  (use-fixtures :each purge-collections)

  (when-not (get (System/getenv) "CI")
    (deftest test-add-migration-id
      (let [coll "meta.migrations"
            key  "1"]
        (mc/remove db coll {})
        (is (not (mc/any? db coll {:_id key})))
        (is (not (some #{key} (applied-migration-ids db))))
        (add-migration-id db key)
        (is (mc/any? db coll {:_id key}))
        (is (some #{key} (applied-migration-ids db)))))


    (deftest test-remove-migration-id
      (let [coll "meta.migrations"
            key  "1"]
        (mc/remove db coll {})
        (add-migration-id db key)
        (is (mc/any? db coll {:_id key}))
        (is (some #{key} (applied-migration-ids db)))
        (remove-migration-id db key)
        (is (not (some #{key} (applied-migration-ids db))))))


    (deftest test-migrations-ordering
      (let [coll "meta.migrations"
            all-keys  [ "9" "4" "7" "1" "5" "3" "6" "2" "8"]]
        (mc/remove db coll {})

        (doseq [key all-keys]
          (add-migration-id db key))

        (doseq [key all-keys]
          (is (mc/any? db coll {:_id key}))
          (is (some #{key} (applied-migration-ids db))))

        (testing "Applied migrations must come out in creation order"
          (is (= all-keys (applied-migration-ids db))))))))
