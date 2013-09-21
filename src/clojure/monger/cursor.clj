(ns monger.cursor
  "Helpers function for dbCursor object: creating new cursor, 
  CRUD functionality for cursor options
  Related documentation guides:
    * ...
  "
  (:import  [com.mongodb DBCursor Bytes]
            [java.util List Map]
            [java.lang Integer]
            [clojure.lang Keyword])
  (:require [monger.conversion :refer [to-db-object from-db-object as-field-selector]]))

(defn ^DBCursor make-db-cursor 
  "initializes new db-collection."
  ([^String collection] (make-db-cursor collection {} {}))
  ([^String collection ^Map ref] (make-db-cursor collection ref {}))
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
  "Applies cursor options with switch values, where true means switch on
  and false removes specified options from current cursor.
  example: (add-options db-cur {:notimeout true, :tailable false})
  returns cursor."
  (doseq [[opt value] (seq options)]
    (if (= true value)
      (add-option! db-cur opt)
      (remove-option! db-cur opt)))
  db-cur)

(defmethod add-options List [^DBCursor db-cur options]
  "Takes list of options to add current key"
  (doseq [opt (seq options)] 
    (add-option! db-cur opt))
  db-cur)

(defmethod add-options Integer [^DBCursor db-cur, option]
  "Takes com.mongodb.Byte value and adds it to current settings."
  (.addOption db-cur option)
  db-cur)

(defmethod add-options Keyword [^DBCursor db-cur, option]
  (add-option! db-cur option)
  db-cur)

(defmethod add-options :default [^DBCursor db-cur, options]
  (println "add-options dont support type for options: " (class options))
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

