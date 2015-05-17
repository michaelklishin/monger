;; Copyright (c) 2011-2015 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.credentials
  "Helper functions for instantiating various types
   of credentials."
  (:require [clojurewerkz.support.chars :refer :all])
  (:import [com.mongodb MongoCredential]))

;;
;; API
;;

(defn ^MongoCredential for
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

