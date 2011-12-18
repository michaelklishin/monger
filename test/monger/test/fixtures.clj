(ns monger.test.fixtures
  (:require [monger.collection :as mgcol])
  (:use     [monger.testing]))

;;
;; fixture functions
;;

(defcleaner people    "people")
(defcleaner docs      "docs")
(defcleaner things    "things")
(defcleaner libraries "libraries")
(defcleaner scores    "scores")
(defcleaner locations "locations")