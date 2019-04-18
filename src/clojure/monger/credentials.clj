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

(ns monger.credentials
  "Helper functions for instantiating various types
   of credentials."
  (:require [clojurewerkz.support.chars :refer :all])
  (:import [com.mongodb MongoCredential]))

;;
;; API
;;

(defn ^MongoCredential create
  "Creates a MongoCredential instance with an unspecified mechanism.
   The client will negotiate the best mechanism based on the
   version of the server that the client is authenticating to."
  [^String username ^String database pwd]
  (MongoCredential/createCredential username database (to-char-array pwd)))

(defn ^MongoCredential x509
  "Creates a MongoCredential instance for the X509-based authentication
   protocol."
  [^String username]
  (MongoCredential/createMongoX509Credential username))

