(ns monger.convertion
  (:import (com.mongodb DBObject BasicDBObject BasicDBList)
           (clojure.lang IPersistentMap Keyword)
           (java.util List Map)))

(defprotocol ConvertToDBObject
  (to-db-object [input] "Converts given piece of Clojure data to BasicDBObject MongoDB Java driver uses"))

(extend-protocol ConvertToDBObject
  nil
  (to-db-object [input]
    input)

  Object
  (to-db-object [input]
    input)

  Keyword
  (to-db-object [#^Keyword input] (.getName input))

  IPersistentMap
  (to-db-object [#^IPersistentMap input]
    (let [o (BasicDBObject.)]
      (doseq [[k v] input]
        (.put o (to-db-object k) (to-db-object v)))
      o))

  List
  (to-db-object [#^List input] (map to-db-object input)))




(declare associate-pairs)
(defprotocol ConvertFromDBObject
  (from-db-object [input keywordize] "Converts given DBObject instance to a piece of Clojure data"))

(extend-protocol ConvertFromDBObject
  nil
  (from-db-object [input keywordize] input)

  Object
  (from-db-object [input keywordize] input)

  Map
  (from-db-object [#^Map input keywordize]
    (associate-pairs (.entrySet input) keywordize))

  List
  (from-db-object [#^List input keywordize]
    (vec (map #(from-db-object % keywordize) input)))

  BasicDBList
  (from-db-object [#^BasicDBList input keywordize]
    (vec (map #(from-db-object % keywordize) input)))

  DBObject
  (from-db-object [#^DBObject input keywordize]
    ;; DBObject provides .toMap, but the implementation in
    ;; subclass GridFSFile unhelpfully throws
    ;; UnsupportedOperationException
    (associate-pairs (for [key-set (.keySet input)] [key-set (.get input key-set)])
                     keywordize)))


(defn- associate-pairs [pairs keywordize]
  ;; Taking the keywordize test out of the fn reduces derefs
  ;; dramatically, which was the main barrier to matching pure-Java
  ;; performance for this marshalling
  (reduce (if keywordize
            (fn [m [#^String k v]]
              (assoc m (keyword k) (from-db-object v true)))
            (fn [m [#^String k v]]
              (assoc m k (from-db-object v false))))
          {} (reverse pairs)))

