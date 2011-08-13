(ns monger.convertion
  (:import (com.mongodb DBObject BasicDBObject)
           (clojure.lang IPersistentMap Keyword)))

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
  (to-db-object [#^Keyword o] (.getName o))
  
  IPersistentMap
  (to-db-object [#^IPersistentMap input]
    (let [o (BasicDBObject.)]
      (doseq [[k v] input]
        (.put o (to-db-object k) (to-db-object v)))
      o)))




(defprotocol ConvertFromDBObject
  (from-db-object [input] "Converts given DBObject instance to a piece of Clojure data"))
