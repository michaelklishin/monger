(ns monger.ring.session-store
  (:require [ring.middleware.session.store :as ringstore]
            [monger.collection             :as mc])
  (:use monger.conversion)
  (:import [java.util UUID Date]))

;;
;; Implementation
;;

(def ^{:const true}
  default-session-store-collection "web_sessions")




;;
;; API
;;

(defrecord MongoDBSessionStore [^String collection-name])

(extend-protocol ringstore/SessionStore
  MongoDBSessionStore

  (read-session [store key]
    (if-let [m (and key
                    (mc/find-one-as-map (.collection-name store) {:_id key}))]
      m
      {}))

  (write-session [store key data]
    (let [key  (or key (str (UUID/randomUUID)))]
      (mc/save (.collection-name store) (assoc data :date (Date.) :_id key))
      key))

  (delete-session [store key]
    (mc/remove-by-id (.collection-name store) key)
    nil))


(defn monger-store
  ([]
     (MongoDBSessionStore. default-session-store-collection))
  ([^String s]
     (MongoDBSessionStore. s)))