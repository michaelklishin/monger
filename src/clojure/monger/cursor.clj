;; Copyright (c) 2011-2014 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.cursor
  "Helper-functions for dbCursor object: 
    * to initialize new cursor, 
    * for CRUD functionality of options of dbCursor"
  (:import  [com.mongodb DBCursor Bytes]
            [java.util List Map]
            [java.lang Integer]
            [clojure.lang Keyword])
  (:require [monger.core]
            [monger.conversion :refer [to-db-object from-db-object as-field-selector]]))

(defn ^DBCursor make-db-cursor 
  "initializes new db-cursor."
  ([^String collection]
     (make-db-cursor collection {} {}))
  ([^String collection ^Map ref]
     (make-db-cursor collection ref {}))
  ([^String collection ^Map ref fields] 
    (.find
      (.getCollection monger.core/*mongodb-database* (name collection))
      (to-db-object ref)
      (as-field-selector fields)))) 

(def cursor-options {:awaitdata Bytes/QUERYOPTION_AWAITDATA
                     ;;:exhaust   Bytes/QUERYOPTION_EXHAUST - not human settable
                     :notimeout Bytes/QUERYOPTION_NOTIMEOUT
                     :oplogreplay Bytes/QUERYOPTION_OPLOGREPLAY
                     :partial Bytes/QUERYOPTION_PARTIAL
                     :slaveok Bytes/QUERYOPTION_SLAVEOK
                     :tailable Bytes/QUERYOPTION_TAILABLE})

(defn get-options
  "Returns map of cursor's options with current state."
  [^DBCursor db-cur]
  (into {}
    (for [[opt option-mask] cursor-options]
      [opt (< 0 (bit-and (.getOptions db-cur) option-mask))])))

(defn add-option! [^DBCursor db-cur, ^String opt]
  (.addOption db-cur (get cursor-options (keyword opt) 0)))

(defn remove-option! [^DBCursor db-cur, ^String opt]
  (.setOptions db-cur (bit-and-not (.getOptions db-cur)
                                   (get cursor-options (keyword opt) 0))))

(defmulti add-options (fn [db-cur opts] (class opts)))
(defmethod add-options Map [^DBCursor db-cur options]
  "Changes options by using map of settings, which key specifies name of settings 
  and boolean value specifies new state of the setting.
  usage: 
    (add-options db-cur {:notimeout true, :tailable false})
  returns: 
    ^DBCursor object."
  (doseq [[opt value] (seq options)]
    (if (= true value)
      (add-option! db-cur opt)
      (remove-option! db-cur opt)))
  db-cur)

(defmethod add-options List [^DBCursor db-cur options]
  "Takes list of options and activates these options
  usage:
    (add-options db-cur [:notimeout :tailable])
  returns:
    ^DBCursor object"
  (doseq [opt (seq options)] 
    (add-option! db-cur opt))
  db-cur)

(defmethod add-options Integer [^DBCursor db-cur, option]
  "Takes com.mongodb.Byte value and adds it to current settings.
  usage:
    (add-options db-cur com.mongodb.Bytes/QUERYOPTION_NOTIMEOUT)
  returns:
    ^DBCursor object"
  (.addOption db-cur option)
  db-cur)

(defmethod add-options Keyword [^DBCursor db-cur, option]
  "Takes just one keyword as name of settings and applies it to the db-cursor.
  usage:
    (add-options db-cur :notimeout)
  returns:
    ^DBCursor object"
  (add-option! db-cur option)
  db-cur)

(defmethod add-options :default [^DBCursor db-cur, options]
  "Using add-options with not supported type of options just passes unchanged cursor"
  db-cur)

(defn ^DBCursor reset-options
  "Resets cursor options to default value and returns cursor"
  [^DBCursor db-cur]
  (.resetOptions db-cur)
  db-cur)

(defmulti format-as (fn [db-cur as] as))

(defmethod format-as :map [db-cur as]
  (map #(from-db-object %1 true) db-cur))

(defmethod format-as :seq [db-cur as]
  (seq db-cur))

(defmethod format-as :default [db-cur as]
  db-cur)

