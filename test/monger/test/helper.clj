(ns monger.test.helper
  (:require [monger core util])
  (:import  [com.mongodb WriteConcern]))

(def connected (atom false))
(defn connected?
  []
  @connected)

(defn connect!
  []
  (when-not (connected?)
    (do
      (monger.core/connect!)
      (monger.core/set-db! (monger.core/get-db "monger-test"))
      (monger.core/set-default-write-concern! WriteConcern/SAFE))))