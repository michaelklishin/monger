;; This source code is dual-licensed under the Apache License, version
;; 2.0, and the Eclipse Public License, version 1.0.
;;
;; The APL v2.0:
;;
;; ----------------------------------------------------------------------------------
;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
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
;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team.
;; All rights reserved.
;;
;; This program and the accompanying materials are made available under the terms of
;; the Eclipse Public License Version 1.0,
;; which accompanies this distribution and is available at
;; http://www.eclipse.org/legal/epl-v10.html.
;; ----------------------------------------------------------------------------------

(ns monger.result
  "Provides functions that determine if a query (or other database operation)
   was successful or not.

   Related documentation guides:

   * http://clojuremongodb.info/articles/inserting.html
   * http://clojuremongodb.info/articles/updating.html
   * http://clojuremongodb.info/articles/commands.html
   * http://clojuremongodb.info/articles/mapreduce.html"
  (:import [com.mongodb WriteResult CommandResult])
  (:require monger.conversion))

;;
;; API
;;

(defprotocol WriteResultPredicates
  (acknowledged?     [input] "Returns true if write result is a success")
  (updated-existing? [input] "Returns true if write result has updated an existing document"))

(extend-protocol WriteResultPredicates
  WriteResult
  (acknowledged?
    [^WriteResult result]
    (.wasAcknowledged result))
  (updated-existing?
    [^WriteResult result]
    (.isUpdateOfExisting result))

  CommandResult
  (acknowledged?
    [^CommandResult result]
    (.ok result)))
