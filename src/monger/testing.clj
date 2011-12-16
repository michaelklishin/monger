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
