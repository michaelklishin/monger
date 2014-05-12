;; Copyright (c) 2011-2014 Michael S. Klishin
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
            [clojure.java.io :as io]
            [monger.conversion :refer :all]
            [clojurewerkz.support.fn :refer [fpartial]])
  (:import [com.mongodb DB DBObject]
           org.bson.types.ObjectId
           [com.mongodb.gridfs GridFS GridFSInputFile]
           [java.io InputStream ByteArrayInputStream File]))

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
  [^GridFS fs query]
  (.remove fs ^DBObject (to-db-object query)))

(defn remove-all
  [^GridFS fs]
  (remove fs {}))

(defn all-files
  ([^GridFS fs]
     (.getFileList fs (to-db-object {})))
  ([^GridFS fs query]
     (.getFileList fs query)))

(def ^{:private true} converter
  (fpartial from-db-object true))

(defn files-as-maps
  ([^GridFS fs]
     (files-as-maps fs {}))
  ([^GridFS fs query]
     (map converter (all-files fs (to-db-object query)))))


;;
;; Plumbing (low-level API)
;;

(defprotocol InputStreamFactory
  (^InputStream to-input-stream [input] "Makes InputStream out of the given input"))

(extend byte-array-type
  InputStreamFactory
  {:to-input-stream (fn [^bytes input]
                      (ByteArrayInputStream. input))})

(extend-protocol InputStreamFactory
  String
  (to-input-stream [^String input]
    (io/make-input-stream input {:encoding "UTF-8"}))

  File
  (to-input-stream [^File input]
    (io/make-input-stream input {:encoding "UTF-8"}))

  InputStream
  (to-input-stream [^InputStream input]
    input))

(defn ^GridFSInputFile make-input-file
  [^GridFS fs input]
  (.createFile fs (to-input-stream input)))

(defmacro store
  [^GridFSInputFile input & body]
  `(let [^GridFSInputFile f# (doto ~input ~@body)]
     (.save f# GridFS/DEFAULT_CHUNKSIZE)
     (from-db-object f# true)))

;;
;; Higher-level API
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

(defn find
  [^GridFS fs query]
  (.find fs (to-db-object query)))

(defn find-by-filename
  [^GridFS fs ^String filename]
  (.find fs (to-db-object {"filename" filename})))

(defn find-by-md5
  [^GridFS fs ^String md5]
  (.find fs (to-db-object {"md5" md5})))

(defn find-one
  [^GridFS fs query]
  (.findOne fs (to-db-object query)))

(defn find-maps
  [^GridFS fs query]
  (map converter (find fs query)))

(defn find-one-as-map
  [^GridFS fs query]
  (converter (find-one fs query)))

(defn find-by-id
  [^GridFS fs ^ObjectId id]
  (.findOne fs id))

(defn find-map-by-id
  [^GridFS fs ^ObjectId id]
  (converter (find-one fs id)))
