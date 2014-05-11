(ns monger.test.ring.clojure-session-store-test
  (:require [monger.core :as mg]
            [monger.collection  :as mc]
            [clojure.test :refer :all]
            [ring.middleware.session.store :refer :all]
            [monger.ring.session-store :refer :all]))


(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (defn purge-sessions
  [f]
  (mc/remove db "sessions")
  (f)
  (mc/remove db "sessions"))

(use-fixtures :each purge-sessions)


(deftest test-reading-a-session-that-does-not-exist
  (let [store (session-store db "sessions")]
    (is (= {} (read-session store "a-missing-key-1228277")))))


(deftest test-reading-a-session-that-does-not-exist-given-db
  (let [store (session-store db "sessions")]
    (is (= {} (read-session store "a-missing-key-1228277")))))


(deftest test-reading-a-session-that-does-exist
  (let [store (session-store db "sessions")
        sk    (write-session store nil {:library "Monger"})
        m     (read-session store sk)]
    (is sk)
    (is (and (:_id m)))
    (is (= (dissoc m :_id)
           {:library "Monger"}))))


(deftest test-reading-a-session-that-does-exist-given-db
  (let [store (session-store db "sessions")
        sk    (write-session store nil {:library "Monger"})
        m     (read-session store sk)]
    (is sk)
    (is (and (:_id m)))
    (is (= (dissoc m :_id)
           {:library "Monger"}))))


(deftest test-updating-a-session
  (let [store (session-store db "sessions")
        sk1   (write-session store nil {:library "Monger"})
        sk2   (write-session store sk1 {:library "Ring"})
        m     (read-session store sk2)]
    (is (and sk1 sk2))
    (is (and (:_id m)))
    (is (= sk1 sk2))
    (is (= (dissoc m :_id)
           {:library "Ring"}))))


(deftest test-updating-a-session-given-db
  (let [store (session-store db "sessions")
        sk1   (write-session store nil {:library "Monger"})
        sk2   (write-session store sk1 {:library "Ring"})
        m     (read-session store sk2)]
    (is (and sk1 sk2))
    (is (and (:_id m)))
    (is (= sk1 sk2))
    (is (= (dissoc m :_id)
           {:library "Ring"}))))


(deftest test-deleting-a-session
  (let [store (session-store db "sessions")
        sk    (write-session store nil {:library "Monger"})]
    (is (nil? (delete-session store sk)))
    (is (= {} (read-session store sk)))))


(deftest test-deleting-a-session
  (let [store (session-store db "sessions")
        sk    (write-session store nil {:library "Monger"})]
    (is (nil? (delete-session store sk)))
    (is (= {} (read-session store sk))))))
