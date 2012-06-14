;; Original author is Andrew Boekhoff
;;
;; Portions of the code are Copyright (c) 2009 Andrew Boekhoff
;; Copyright (c) 2011-2012 Michael S. Klishin
;;
;; Permission is hereby granted, free of charge, to any person obtaining a copy
;; of this software and associated documentation files (the "Software"), to deal
;; in the Software without restriction, including without limitation the rights
;; to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
;; copies of the Software, and to permit persons to whom the Software is
;; furnished to do so, subject to the following conditions:
;;
;; The above copyright notice and this permission notice shall be included in
;; all copies or substantial portions of the Software.
;;
;; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
;; IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
;; FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
;; AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
;; LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
;; OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
;; THE SOFTWARE.

(ns monger.conversion
  (:import [com.mongodb DBObject BasicDBObject BasicDBList DBCursor]
           [clojure.lang IPersistentMap Named Keyword Ratio]
           [java.util List Map Date Set]
           org.bson.types.ObjectId))

(defprotocol ConvertToDBObject
  (^com.mongodb.DBObject to-db-object [input] "Converts given piece of Clojure data to BasicDBObject MongoDB Java driver uses"))

(extend-protocol ConvertToDBObject
  nil
  (to-db-object [input]
    input)

  String
  (to-db-object [^String input]
    input)

  java.util.Date
  (to-db-object [^java.util.Date input]
    input)

  Ratio
  (to-db-object [^Ratio input]
    (double input))

  Keyword
  (to-db-object [^Keyword input] (.getName input))

  Named
  (to-db-object [^Named input] (.getName input))

  IPersistentMap
  (to-db-object [^IPersistentMap input]
    (let [o (BasicDBObject.)]
      (doseq [[k v] input]
        (.put o (to-db-object k) (to-db-object v)))
      o))

  List
  (to-db-object [^List input] (map to-db-object input))

  Set
  (to-db-object [^Set input] (map to-db-object input))

  DBObject
  (to-db-object [^DBObject input] input)

  com.novemberain.monger.DBRef
  (to-db-object [^com.novemberain.monger.DBRef dbref]
    dbref)

  Object
  (to-db-object [input]
    input))




(declare associate-pairs)
(defprotocol ConvertFromDBObject
  (from-db-object [input keywordize] "Converts given DBObject instance to a piece of Clojure data"))

(extend-protocol ConvertFromDBObject
  nil
  (from-db-object [input keywordize] input)

  Object
  (from-db-object [input keywordize] input)

  Map
  (from-db-object [^Map input keywordize]
    (associate-pairs (.entrySet input) keywordize))

  List
  (from-db-object [^List input keywordize]
    (vec (map #(from-db-object % keywordize) input)))

  BasicDBList
  (from-db-object [^BasicDBList input keywordize]
    (vec (map #(from-db-object % keywordize) input)))

  com.mongodb.DBRef
  (from-db-object [^com.mongodb.DBRef input keywordize]
    (com.novemberain.monger.DBRef. input))

  DBObject
  (from-db-object [^DBObject input keywordize]
    ;; DBObject provides .toMap, but the implementation in
    ;; subclass GridFSFile unhelpfully throws
    ;; UnsupportedOperationException. This part is taken from congomongo and
    ;; may need revisiting at a later point. MK.
    (associate-pairs (for [key-set (.keySet input)] [key-set (.get input key-set)])
                     keywordize)))


(defn- associate-pairs [pairs keywordize]
  ;; Taking the keywordize test out of the fn reduces derefs
  ;; dramatically, which was the main barrier to matching pure-Java
  ;; performance for this marshalling. Taken from congomongo. MK.
  (reduce (if keywordize
            (fn [m [^String k v]]
              (assoc m (keyword k) (from-db-object v true)))
            (fn [m [^String k v]]
              (assoc m k (from-db-object v false))))
          {} (reverse pairs)))



(defprotocol ConvertToObjectId
  (^org.bson.types.ObjectId to-object-id [input] "Instantiates ObjectId from input unless the input itself is an ObjectId instance. In that case, returns input as is."))

(extend-protocol ConvertToObjectId
  String
  (to-object-id [^String input]
    (ObjectId. input))

  Date
  (to-object-id [^Date input]
    (ObjectId. input))

  ObjectId
  (to-object-id [^ObjectId input]
    input))



(defprotocol FieldSelector
  (^com.mongodb.DBObject as-field-selector [input] "Converts values to DBObject that can be used to specify a list of document fields (including negation support)"))

(extend-protocol FieldSelector
  DBObject
  (as-field-selector [^DBObject input]
    input)

  List
  (as-field-selector [^List input]
    (to-db-object (zipmap input (repeat 1))))

  Object
  (as-field-selector [input]
    (to-db-object input)))
