(ns monger.test.fixtures
  (:use [monger.testkit :only [defcleaner]]))

;;
;; fixture functions
;;

(defcleaner people    "people")
(defcleaner docs      "docs")
(defcleaner things    "things")
(defcleaner libraries "libraries")
(defcleaner scores    "scores")
(defcleaner locations "locations")
(defcleaner domains   "domains")
(defcleaner pages     "pages")

(defcleaner cached    "cached")

(defcleaner migrations "meta.migrations")
