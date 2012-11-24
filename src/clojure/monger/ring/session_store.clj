(ns monger.ring.session-store
  (:require [ring.middleware.session.store :as ringstore]
            [monger.collection             :as mc])
  (:use monger.conversion)
  (:import [java.util UUID Date]
           ring.middleware.session.store.SessionStore))

;;
;; Implementation
;;

(def ^{:const true}
  default-session-store-collection "web_sessions")




;;
;; API
;;

;; this session store stores Clojure data structures using Clojure reader. It will correctly store every
;; data structure Clojure reader can serialize and read but won't make the data useful to applications
;; in other languages.

(defrecord ClojureReaderBasedMongoDBSessionStore [^String collection-name])

(defmethod print-dup java.util.Date
  [^java.util.Date d ^java.io.OutputStream out]
  (.write out
          (str "#="
               `(java.util.Date. ~(.getYear d)
                                 ~(.getMonth d)
                                 ~(.getDate d)
                                 ~(.getHours d)
                                 ~(.getMinutes d)
                                 ~(.getSeconds d)))))

(defmethod print-dup org.bson.types.ObjectId
  [oid out]
  (.write out
          (str "#="
               `(org.bson.types.ObjectId. ~(str oid)))))


(extend-protocol ringstore/SessionStore
  ClojureReaderBasedMongoDBSessionStore

  (read-session [store key]
    (if key
      (if-let [m (mc/find-one-as-map (.collection-name store) {:_id key})]
        (read-string (:value m))
        {})
      {}))

  (write-session [store key data]
    (let [date  (Date.)
          key   (or key (str (UUID/randomUUID)))
          value (binding [*print-dup* true]
                  (pr-str (assoc data :_id key)))]
      (mc/save (.collection-name store) {:_id key :value value :date date})
      key))

  (delete-session [store key]
    (mc/remove-by-id (.collection-name store) key)
    nil))


(defn session-store
  ([]
     (ClojureReaderBasedMongoDBSessionStore. default-session-store-collection))
  ([^String s]
     (ClojureReaderBasedMongoDBSessionStore. s)))


;; this session store won't store namespaced keywords correctly but stores results in a way
;; that applications in other languages can read. DO NOT use it with Friend.

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