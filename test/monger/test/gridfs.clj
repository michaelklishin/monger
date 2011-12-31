(ns monger.test.gridfs
  (:refer-clojure :exclude [count remove find])
  (:use [clojure.test]
        [monger.core :only [count]]
        [monger.test.fixtures]
        [monger.operators]
        [monger.gridfs :only (store make-input-file)])
  (:require [monger.gridfs :as gridfs]
            [monger.test.helper :as helper]
            [clojure.java.io :as io])
  (:import [java.io InputStream]
           [com.mongodb.gridfs GridFS GridFSInputFile]))


(defn purge-gridfs
  [f]
  (gridfs/remove-all)
  (f)
  (gridfs/remove-all))

(use-fixtures :each purge-gridfs)

(helper/connect!)



(deftest test-storing-files-to-gridfs-using-relative-fs-paths
  (let [input "./test/resources/mongo/js/mapfun1.js"]
    (is (= 0 (count (gridfs/all-files))))
    (gridfs/store (gridfs/make-input-file input)
                  (.setFilename "monger.test.gridfs.file1")
                  (.setContentType "application/octet-stream"))
    (is (= 1 (count (gridfs/all-files))))))


(deftest test-storing-files-to-gridfs-using-file-instances
  (let [input (io/as-file "./test/resources/mongo/js/mapfun1.js")]
    (is (= 0 (count (gridfs/all-files))))
    (gridfs/store (gridfs/make-input-file input)
                  (.setFilename "monger.test.gridfs.file2")
                  (.setContentType "application/octet-stream"))
    (is (= 1 (count (gridfs/all-files))))))

(deftest test-storing-bytes-to-gridfs
  (let [input (.getBytes "A string")]
    (is (= 0 (count (gridfs/all-files))))
    (store (make-input-file input)
      (.setFilename "monger.test.gridfs.file3")
      (.setContentType "application/octet-stream"))
    (is (= 1 (count (gridfs/all-files))))))
