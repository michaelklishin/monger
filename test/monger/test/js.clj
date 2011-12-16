(ns monger.test.js
  (:require [monger js]
            [clojure.java.io :only [reader]])
  (:use [clojure.test]))


(deftest load-js-resource-using-path-on-the-classpath
  (are [c path] (= c (count (monger.js/load-resource path)))
       62 "resources/mongo/js/mapfun1.js"
       62 "resources/mongo/js/mapfun1"))
