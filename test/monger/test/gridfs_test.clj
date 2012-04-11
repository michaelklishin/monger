(ns monger.test.gridfs-test
  (:refer-clojure :exclude [count remove find])
  (:use clojure.test
        [monger.core :only [count]]
        monger.test.fixtures
        [monger operators conversion]
        [monger.gridfs :only (store make-input-file)])
  (:require [monger.gridfs :as gridfs]
            [monger.test.helper :as helper]
            [clojure.java.io :as io])
  (:import [java.io InputStream File FileInputStream]
           [com.mongodb.gridfs GridFS GridFSInputFile GridFSDBFile]))


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
    (store (make-input-file input)
      (.setFilename "monger.test.gridfs.file1")
      (.setContentType "application/octet-stream"))
    (is (= 1 (count (gridfs/all-files))))))


(deftest test-storing-files-to-gridfs-using-file-instances
  (let [input (io/as-file "./test/resources/mongo/js/mapfun1.js")]
    (is (= 0 (count (gridfs/all-files))))
    (store (make-input-file input)
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

(deftest test-storing-files-to-gridfs-using-absolute-fs-paths
  (let [tmp-file (File/createTempFile "monger.test.gridfs" "test-storing-files-to-gridfs-using-absolute-fs-paths")
        _        (spit tmp-file "Some content")
        input    (.getAbsolutePath tmp-file)]
    (is (= 0 (count (gridfs/all-files))))
    (store (make-input-file input)
      (.setFilename "monger.test.gridfs.file4")
      (.setContentType "application/octet-stream"))
    (is (= 1 (count (gridfs/all-files))))))

(deftest test-storing-files-to-gridfs-using-input-stream
  (let [tmp-file (File/createTempFile "monger.test.gridfs" "test-storing-files-to-gridfs-using-input-stream")
        _        (spit tmp-file "Some other content")]
    (is (= 0 (count (gridfs/all-files))))
    (store (make-input-file (FileInputStream. tmp-file))
      (.setFilename "monger.test.gridfs.file4b")
      (.setContentType "application/octet-stream"))
    (is (= 1 (count (gridfs/all-files))))))



(deftest test-finding-individual-files-on-gridfs
  (let [input   "./test/resources/mongo/js/mapfun1.js"
        ct      "binary/octet-stream"
        filename "monger.test.gridfs.file5"
        md5      "14a09deabb50925a3381315149017bbd"
        stored  (store (make-input-file input)
                  (.setFilename filename)
                  (.setContentType ct))]
    (is (= 1 (count (gridfs/all-files))))
    (is (:_id stored))
    (is (:uploadDate stored))
    (is (= 62 (:length stored)))
    (is (= md5 (:md5 stored)))
    (is (= filename (:filename stored)))
    (is (= ct (:contentType stored)))
    (are [a b] (is (= a (:md5 (from-db-object (gridfs/find-one b) true))))
         md5 (:_id stored)
         md5 filename
         md5 (to-db-object { :md5 md5 }))))

(deftest test-finding-multiple-files-on-gridfs
  (let [input   "./test/resources/mongo/js/mapfun1.js"
        ct      "binary/octet-stream"
        md5      "14a09deabb50925a3381315149017bbd"
        stored1  (store (make-input-file input)
                   (.setFilename "monger.test.gridfs.file6")
                   (.setContentType ct))
        stored2  (store (make-input-file input)
                   (.setFilename "monger.test.gridfs.file7")
                   (.setContentType ct))
        list1    (gridfs/find "monger.test.gridfs.file6")
        list2    (gridfs/find "monger.test.gridfs.file7")
        list3    (gridfs/find "888000___.monger.test.gridfs.file")
        list4    (gridfs/find { :md5 md5 })]
    (is (= 2 (count (gridfs/all-files))))
    (are [a b] (is (= (map #(.get ^GridFSDBFile % "_id") a)
                      (map :_id b)))
         list1 [stored1]
         list2 [stored2]
         list3 []
         list4 [stored1 stored2])))


(deftest test-removing-multiple-files-from-gridfs
  (let [input   "./test/resources/mongo/js/mapfun1.js"
        ct      "binary/octet-stream"
        md5      "14a09deabb50925a3381315149017bbd"
        stored1  (store (make-input-file input)
                   (.setFilename "monger.test.gridfs.file8")
                   (.setContentType ct))
        stored2  (store (make-input-file input)
                   (.setFilename "monger.test.gridfs.file9")
                   (.setContentType ct))]
    (is (= 2 (count (gridfs/all-files))))
    (gridfs/remove { :filename "monger.test.gridfs.file8" })
    (is (= 1 (count (gridfs/all-files))))
    (gridfs/remove { :md5 md5 })
    (is (= 0 (count (gridfs/all-files))))))
