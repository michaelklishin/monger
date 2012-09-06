;; Copyright (c) 2011-2012 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:doc "Provides clojure.data.json/Write-JSON protocol extension for MongoDB-specific types, such as
            org.bson.types.ObjectId"}
  monger.json
  (:import org.bson.types.ObjectId))

;;
;; API
;;

(require 'clojurewerkz.support.json)

(try
  (require 'clojure.data.json)
  (catch Throwable t
    false))

(try
  (extend-protocol clojure.data.json/Write-JSON
    ObjectId
    (write-json [^ObjectId object out escape-unicode?]
      (clojure.data.json/write-json (.toString object) out escape-unicode?)))
  (catch Throwable _
    false))

(try
  (require 'cheshire.custom)
  (catch Throwable t
    false))

(try
  (cheshire.custom/add-encoder ObjectId
                               (fn [^ObjectId oid ^com.fasterxml.jackson.core.json.WriterBasedJsonGenerator generator]
                                 (.writeString generator (.toString oid))))
  (catch Throwable t
    false))
