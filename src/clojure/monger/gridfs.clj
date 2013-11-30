;; Copyright (c) 2011-2012 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.gridfs
  "Provides functions and macros for working with GridFS: storing files in GridFS, streaming files from GridFS,
   finding stored files.

   Related documentation guide: http://clojuremongodb.info/articles/gridfs.html"
  (:refer-clojure :exclude [remove find])
  (:require monger.core
            [clojure.java.io :as io])
  (:use monger.conversion
        [clojurewerkz.support.fn :only [fpartial]])
  (:import [com.mongodb DB DBObject]
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

;; ...



;;
;; API
;;


(defn remove
  ([]
     (remove {}))
  ([query]
     (.remove ^GridFS monger.core/*mongodb-gridfs* ^DBObject (to-db-object query)))
  ([^GridFS fs query]
     (.remove fs ^DBObject (to-db-object query))))

(defn remove-all
  ([]
     (remove {}))
  ([^GridFS fs]
     (remove fs {})))

(defn all-files
  ([]
     (.getFileList ^GridFS monger.core/*mongodb-gridfs*))
  ([query]
     (.getFileList ^GridFS monger.core/*mongodb-gridfs* query))
  ([^GridFS fs query]
     (.getFileList fs query)))

(def ^{:private true} converter
  (fpartial from-db-object true))

(defn files-as-maps
  ([]
     (map converter (all-files)))
  ([query]
     (map converter (all-files (to-db-object query))))
  ([^GridFS fs query]
     (map converter (all-files fs (to-db-object query)))))


;;
;; Plumbing (low-level API)
;;

(defprotocol GridFSInputFileFactory
  (^com.mongodb.gridfs.GridFSInputFile make-input-file [input] "Makes GridFSInputFile out of the given input"))

(extend byte-array-type
  GridFSInputFileFactory
  {:make-input-file (fn [^bytes input]
                      (.createFile ^GridFS monger.core/*mongodb-gridfs* input))})

(extend-protocol GridFSInputFileFactory
  String
  (make-input-file [^String input]
    (.createFile ^GridFS monger.core/*mongodb-gridfs* ^InputStream (io/make-input-stream input {:encoding "UTF-8"})))

  File
  (make-input-file [^File input]
    (.createFile ^GridFS monger.core/*mongodb-gridfs* ^InputStream (io/make-input-stream input {:encoding "UTF-8"})))

  InputStream
  (make-input-file [^InputStream input]
    (.createFile ^GridFS monger.core/*mongodb-gridfs* ^InputStream input)))


(defmacro store
  [^GridFSInputFile input & body]
  `(let [^GridFSInputFile f# (doto ~input ~@body)]
     (.save f# GridFS/DEFAULT_CHUNKSIZE)
     (from-db-object f# true)))


;;
;; "New" DSL, a higher-level API
;;

(defn save
  [^GridFSInputFile input]
  (.save input GridFS/DEFAULT_CHUNKSIZE)
  (from-db-object input true))

(defn filename
  [^GridFSInputFile input ^String s]
  (.setFilename input s)
  input)

(defn content-type
  [^GridFSInputFile input ^String s]
  (.setContentType input s)
  input)

(defn metadata
  [^GridFSInputFile input md]
  (.setMetaData input (to-db-object md))
  input)

(defmacro store-file
  [^GridFSInputFile input & body]
  `(let [f# (-> ~input ~@body)]
     (save f#)))


;;
;; Finders
;;

(defprotocol Finders
  (find     [input] "Finds multiple files using given input (an ObjectId, filename or query)")
  (find-one [input] "Finds one file using given input (an ObjectId, filename or query)")
  (find-maps       [input] "Finds multiple files using given input (an ObjectId, filename or query), returning a Clojure map")
  (find-one-as-map [input] "Finds one file using given input (an ObjectId, filename or query), returning a Clojure map"))

(extend-protocol Finders
  String
  (find [^String input]
    (.find ^GridFS monger.core/*mongodb-gridfs* input))
  (find-one [^String input]
    (.findOne ^GridFS monger.core/*mongodb-gridfs* input))
  (find-maps [^String input]
    (map converter (find input)))
  (find-one-as-map [^String input]
    (converter (find-one input)))

  org.bson.types.ObjectId
  (find-one [^org.bson.types.ObjectId input]
    (.findOne ^GridFS monger.core/*mongodb-gridfs* input))
  (find-one-as-map [^org.bson.types.ObjectId input]
    (converter (find-one input)))


  DBObject
  (find [^DBObject input]
    (.find ^GridFS monger.core/*mongodb-gridfs* input))
  (find-one [^DBObject input]
    (.findOne ^GridFS monger.core/*mongodb-gridfs* input))
  (find-maps [^DBObject input]
    (map converter (find input)))
  (find-one-as-map [^DBObject input]
    (converter (find-one input)))

  ;; using java.util.Map here results in (occasional) recursion
  clojure.lang.IPersistentMap
  (find [^java.util.Map input]
    (find (to-db-object input)))
  (find-one [^java.util.Map input]
    (find-one (to-db-object input)))
  (find-maps [^java.util.Map input]
    (find-maps (to-db-object input)))
  (find-one-as-map [^java.util.Map input]
    (find-one-as-map (to-db-object input))))

