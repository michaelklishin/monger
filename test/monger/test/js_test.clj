(ns monger.test.js-test
  (:require monger.js
            [monger.test.helper :as helper])
  (:use clojure.test))

(helper/connect!)

(deftest load-js-resource-using-path-on-the-classpath
  (are [c path] (= c (count (monger.js/load-resource path)))
       62 "resources/mongo/js/mapfun1.js"
       62 "resources/mongo/js/mapfun1"))
