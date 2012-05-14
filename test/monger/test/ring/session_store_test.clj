(ns monger.test.ring.session-store-test
  (:require [monger core util]
            [monger.collection  :as mc]
            [monger.test.helper :as helper])  
  (:use clojure.test
        ring.middleware.session.store
        monger.ring.session-store))


(helper/connect!)

(defn purge-sessions
  [f]
  (mc/remove "web_sessions")
  (mc/remove "sessions")
  (f)
  (mc/remove "web_sessions")
  (mc/remove "sessions"))

(use-fixtures :each purge-sessions)


(deftest test-reading-a-session-that-does-not-exist
  (let [store (monger-store)]
    (is (= {} (read-session store "a-missing-key-1228277")))))


(deftest test-reading-a-session-that-does-exist
  (let [store (monger-store)
        sk    (write-session store nil {:library "Monger"})
        m     (read-session store sk)]
    (is sk)
    (is (and (:_id m) (:date m)))
    (is (= (dissoc m :_id :date)
           {:library "Monger"}))))


(deftest test-updating-a-session
  (let [store (monger-store)
        sk1   (write-session store nil {:library "Monger"})
        sk2   (write-session store sk1 {:library "Ring"})
        m     (read-session store sk2)]
    (is (and sk1 sk2))
    (is (and (:_id m) (:date m)))
    (is (= sk1 sk2))
    (is (= (dissoc m :_id :date)
           {:library "Ring"}))))


(deftest test-deleting-a-session
  (let [store (monger-store)
        sk    (write-session store nil {:library "Monger"})]
    (is (nil? (delete-session store sk)))
    (is (= {} (read-session store sk)))))
