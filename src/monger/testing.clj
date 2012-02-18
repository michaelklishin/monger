;; Copyright (c) 2011-2012 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.testing
  (:require [monger collection]))


;;
;; API
;;

(defmacro defcleaner
  "Defines a fixture function that removes all documents from a collection. If collection is not specified,
   a conventionally named var will be used. Supposed to be used with clojure.test/use-fixtures but may
   be useful on its own.

   Examples:

   (defcleaner events)              ;; collection name will be taken from the events-collection var
   (defcleaner people \"accounts\") ;; collection name is given
  "
  [entities & coll-name]
  (let [coll-arg (if coll-name
                   (str (first coll-name))
                   (symbol (str entities "-collection")))
        fn-name  (symbol (str "purge-" entities))]
    `(defn ~fn-name
       [f#]
       (monger.collection/remove ~coll-arg)
       (f#)
       (monger.collection/remove ~coll-arg))))
