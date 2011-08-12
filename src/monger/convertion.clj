(ns monger.convertion
  (:import (com.mongodb DBObject BasicDBObject)
           (java.util Map List)))

(defprotocol ConvertToDBObject
  (to-db-object [input] "Converts given piece of Clojure data to BasicDBObject MongoDB Java driver uses"))

(extend-protocol ConvertToDBObject
  nil
  (to-db-object [input]
    input)
  Object
  (to-db-object [input]
    input)
  Map
  (to-db-object [input]
    {}))
          



(defprotocol ConvertFromDBObject
  (from-db-object [input] "Converts given DBObject instance to a piece of Clojure data"))
