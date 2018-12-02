;; This source code is dual-licensed under the Apache License, version
;; 2.0, and the Eclipse Public License, version 1.0.
;;
;; The APL v2.0:
;;
;; ----------------------------------------------------------------------------------
;; Copyright (c) 2011-2018 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
;; ----------------------------------------------------------------------------------
;;
;; The EPL v1.0:
;;
;; ----------------------------------------------------------------------------------
;; Copyright (c) 2011-2018 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team.
;; All rights reserved.
;;
;; This program and the accompanying materials are made available under the terms of
;; the Eclipse Public License Version 1.0,
;; which accompanies this distribution and is available at
;; http://www.eclipse.org/legal/epl-v10.html.
;; ----------------------------------------------------------------------------------

(ns monger.joda-time
    "An optional convenience namespaces for applications that heavily use dates and would prefer use JodaTime types
    transparently when storing and loading them from MongoDB and serializing to JSON and/or with Clojure reader.

    Enables automatic conversion of JodaTime date/time/instant instances to JDK dates (java.util.Date) when documents
    are serialized and the other way around when documents are loaded. Extends clojure.data.json/Write-JSON protocol for
    JodaTime types.

    To use it, make sure you add dependencies on clj-time (or JodaTime) and clojure.data.json."
  (:import [org.joda.time DateTime DateTimeZone ReadableInstant]
           [org.joda.time.format ISODateTimeFormat])
  (:require [monger.conversion :refer :all]))

;;
;; API
;;

(extend-protocol ConvertToDBObject
  org.joda.time.base.AbstractInstant
  (to-db-object [^AbstractInstant input]
    (to-db-object (.toDate input)))
  org.joda.time.base.AbstractPartial
  (to-db-object [^AbstractPartial input]
    (to-db-object (.toDate input))))

(extend-protocol ConvertFromDBObject
  java.util.Date
  (from-db-object [^java.util.Date input keywordize]
    (org.joda.time.DateTime. input)))



;;
;; Reader extensions
;;

(defmethod print-dup org.joda.time.base.AbstractInstant
  [^org.joda.time.base.AbstractInstant d out]
  (print-dup (.toDate d) out))


(defmethod print-dup org.joda.time.base.AbstractPartial
  [^org.joda.time.base.AbstractPartial d out]
  (print-dup (.toDate d) out))

;;
;; JSON serialization
;;

(require 'clojurewerkz.support.json)
