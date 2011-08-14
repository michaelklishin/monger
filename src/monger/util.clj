(ns monger.util
  (:import (java.security SecureRandom) (java.math.BigInteger)))

;;
;; API
;;

(defn ^String random-str
  "Generates a secure random string"
  [^long n, ^long num-base]
  (.toString (new BigInteger n (SecureRandom.)) num-base))




(defmacro with-ns
  "Evaluates body in another namespace.  ns is either a namespace object or a symbol.
   This makes it possible to define functions in namespaces other than the current one."
  [ns & body]
  `(binding [*ns* (the-ns ~ns)]
     ~@(map (fn [form] `(eval '~form)) body)))
