;; Copyright (c) 2011-2012 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.js
  "Kept for backwards compatibility. Please use clojurewerkz.support.js from now on."
  (:require [clojurewerkz.support.js :as js]))



;;
;; API
;;

(defn load-resource
  "Loads a JavaScript resource (file from the classpath) and returns its content as a string.
   The .js suffix at the end may be omitted.

   Used primarily for map/reduce queries."
  (^String [^String path]
           (js/load-resource path)))
