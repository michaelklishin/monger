(ns monger.gridfs
  (:refer-clojure :exclude [remove find])
  (:require [monger.core]
            [clojure.java.io :as io])
  (:use [monger.conversion])
  (:import [com.mongodb DBObject]
           [com.mongodb.gridfs GridFS GridFSInputFile]
           [java.io InputStream File]))

;;
;; Implementation
;;

(def
  ^{:doc "Type object for a Java primitive byte array."
    :private true
    }
  byte-array-type (class (make-array Byte/TYPE 0)))

(def ^:dynamic *chunk-size* (* 2 1024 1024))

;; ...



;;
;; API
;;


(defn remove
  ([]
     (remove {}))
  ([query]
     (.remove ^GridFS monger.core/*mongodb-gridfs* ^DBObject (to-db-object query))))

(defn remove-all
  []
  (remove {}))

(defn all-files
  ([]
     (.getFileList ^GridFS monger.core/*mongodb-gridfs*))
  ([query]
     (.getFileList ^GridFS monger.core/*mongodb-gridfs* query)))


(defprotocol GridFSInputFileFactory
  (^GridFSInputFile make-input-file [input] "Makes GridFSInputFile out of given input"))

(extend byte-array-type
  GridFSInputFileFactory
  { :make-input-file (fn [^bytes input]
                       (.createFile ^GridFS monger.core/*mongodb-gridfs* input)) })

(extend-protocol GridFSInputFileFactory
  String
  (make-input-file [^String input]
    (.createFile ^GridFS monger.core/*mongodb-gridfs* ^InputStream (io/make-input-stream input { :encoding "UTF-8" })))

  File
  (make-input-file [^File input]
    (.createFile ^GridFS monger.core/*mongodb-gridfs* ^InputStream (io/make-input-stream input { :encoding "UTF-8" })))

  InputStream
  (make-input-file [^InputStream input]
    (.createFile ^GridFS monger.core/*mongodb-gridfs* ^InputStream input)))


(defmacro store
  [^GridFSInputFile input & body]
  `(let [^GridFSInputFile f# (doto ~input ~@body)]
     (.save f# *chunk-size*)
     (from-db-object f# true)))


(defprotocol Finders
  (find     [input] "Finds multiple files using given input (an ObjectId, filename or query)")
  (find-one [input] "Finds one file using given input (an ObjectId, filename or query)"))

(extend-protocol Finders
  String
  (find [^String input]
    (vec (.find ^GridFS monger.core/*mongodb-gridfs* input)))
  (find-one [^String input]
    (.findOne ^GridFS monger.core/*mongodb-gridfs* input))

  org.bson.types.ObjectId
  (find-one [^org.bson.types.ObjectId input]
    (.findOne ^GridFS monger.core/*mongodb-gridfs* input))


  DBObject
  (find [^DBObject input]
    (vec (.find ^GridFS monger.core/*mongodb-gridfs* input)))
  (find-one [^DBObject input]
    (.findOne ^GridFS monger.core/*mongodb-gridfs* input))

  clojure.lang.PersistentArrayMap
  (find [^clojure.lang.PersistentArrayMap input]
    (find (to-db-object input))))

