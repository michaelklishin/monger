(ns monger.test.gridfs-test
  (:refer-clojure :exclude [count remove find])
  (:require [monger.gridfs :as gridfs]
            [monger.test.helper :as helper]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [monger.core :refer [count]]
            [monger.test.fixtures :refer :all]
            [monger.operators :refer :all]
            [monger.conversion :refer :all]
            [monger.gridfs :refer [store make-input-file store-file filename content-type metadata]])
  (:import [java.io InputStream File FileInputStream]
           [com.mongodb.gridfs GridFS GridFSInputFile GridFSDBFile]))

(defn purge-gridfs*
  []
  (gridfs/remove-all))

(defn purge-gridfs
  [f]
  (gridfs/remove-all)
  (f)
  (gridfs/remove-all))

(use-fixtures :each purge-gridfs)

(helper/connect!)



(deftest ^{:gridfs true} test-storing-files-to-gridfs-using-relative-fs-paths
  (let [input "./test/resources/mongo/js/mapfun1.js"]
    (is (= 0 (count (gridfs/all-files))))
    (store (make-input-file input)
           (.setFilename "monger.test.gridfs.file1")
           (.setContentType "application/octet-stream"))
    (is (= 1 (count (gridfs/all-files))))))


(deftest ^{:gridfs true} test-storing-files-to-gridfs-using-file-instances
  (let [input (io/as-file "./test/resources/mongo/js/mapfun1.js")]
    (is (= 0 (count (gridfs/all-files))))
    (store-file (make-input-file input)
                (filename "monger.test.gridfs.file2")
                (content-type "application/octet-stream"))
    (is (= 1 (count (gridfs/all-files))))))

(deftest ^{:gridfs true} test-storing-bytes-to-gridfs
  (let [input (.getBytes "A string")
        md    {:format "raw" :source "AwesomeCamera D95"}
        fname  "monger.test.gridfs.file3"
        ct     "application/octet-stream"]
    (is (= 0 (count (gridfs/all-files))))
    (store-file (make-input-file input)
                (filename fname)
                (metadata md)
                (content-type "application/octet-stream"))
    (let [f (first (gridfs/files-as-maps))]
      (is (= ct (:contentType f)))
      (is (= fname (:filename f)))
      (is (= md (:metadata f))))
    (is (= 1 (count (gridfs/all-files))))))

(deftest ^{:gridfs true} test-storing-files-to-gridfs-using-absolute-fs-paths
  (let [tmp-file (File/createTempFile "monger.test.gridfs" "test-storing-files-to-gridfs-using-absolute-fs-paths")
        _        (spit tmp-file "Some content")
        input    (.getAbsolutePath tmp-file)]
    (is (= 0 (count (gridfs/all-files))))
    (store-file (make-input-file input)
                (filename "monger.test.gridfs.file4")
                (content-type "application/octet-stream"))
    (is (= 1 (count (gridfs/all-files))))))

(deftest ^{:gridfs true} test-storing-files-to-gridfs-using-input-stream
  (let [tmp-file (File/createTempFile "monger.test.gridfs" "test-storing-files-to-gridfs-using-input-stream")
        _        (spit tmp-file "Some other content")]
    (is (= 0 (count (gridfs/all-files))))
    (store-file (make-input-file (FileInputStream. tmp-file))
                (filename "monger.test.gridfs.file4b")
                (content-type "application/octet-stream"))
    (is (= 1 (count (gridfs/all-files))))))



(deftest ^{:gridfs true} test-finding-individual-files-on-gridfs
  (testing "gridfs/find-one"
    (purge-gridfs*)    
    (let [input   "./test/resources/mongo/js/mapfun1.js"
          ct     "binary/octet-stream"
          fname  "monger.test.gridfs.file5"
          md5    "14a09deabb50925a3381315149017bbd"
          stored (store-file (make-input-file input)
                             (filename fname)
                             (content-type ct))]
      (is (= 1 (count (gridfs/all-files))))
      (is (:_id stored))
      (is (:uploadDate stored))
      (is (= 62 (:length stored)))
      (is (= md5 (:md5 stored)))
      (is (= fname (:filename stored)))
      (is (= ct (:contentType stored)))
      (are [a b] (is (= a (:md5 (from-db-object (gridfs/find-one b) true))))
           md5 (:_id stored)
           md5 fname
           md5 (to-db-object {:md5 md5}))))
  (testing "gridfs/find-one-as-map"
    (purge-gridfs*)
    (let [input   "./test/resources/mongo/js/mapfun1.js"
          ct      "binary/octet-stream"
          fname "monger.test.gridfs.file6"
          md5      "14a09deabb50925a3381315149017bbd"
          stored  (store-file (make-input-file input)
                              (filename fname)
                              (metadata (to-db-object {:meta "data"}))
                              (content-type ct))]
      (is (= 1 (count (gridfs/all-files))))
      (is (:_id stored))
      (is (:uploadDate stored))
      (is (= 62 (:length stored)))
      (is (= md5 (:md5 stored)))
      (is (= fname (:filename stored)))
      (is (= ct (:contentType stored)))
      (let [m (gridfs/find-one-as-map {:filename fname})]
        (is (= {:meta "data"} (:metadata m))))
      (are [a query] (is (= a (:md5 (gridfs/find-one-as-map query))))
           md5 (:_id stored)
           md5 fname
           md5 {:md5 md5}))))

(deftest ^{:gridfs true} test-finding-multiple-files-on-gridfs
  (let [input   "./test/resources/mongo/js/mapfun1.js"
        ct      "binary/octet-stream"
        md5      "14a09deabb50925a3381315149017bbd"
        stored1  (store-file (make-input-file input)
                             (filename "monger.test.gridfs.file6")
                             (content-type ct))
        stored2  (store-file (make-input-file input)
                             (filename "monger.test.gridfs.file7")
                             (content-type ct))
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


(deftest ^{:gridfs true} test-removing-multiple-files-from-gridfs
  (let [input   "./test/resources/mongo/js/mapfun1.js"
        ct      "binary/octet-stream"
        md5      "14a09deabb50925a3381315149017bbd"
        stored1  (store-file (make-input-file input)
                             (filename "monger.test.gridfs.file8")
                             (content-type ct))
        stored2  (store-file (make-input-file input)
                             (filename "monger.test.gridfs.file9")
                             (content-type ct))]
    (is (= 2 (count (gridfs/all-files))))
    (gridfs/remove { :filename "monger.test.gridfs.file8" })
    (is (= 1 (count (gridfs/all-files))))
    (gridfs/remove { :md5 md5 })
    (is (= 0 (count (gridfs/all-files))))))
