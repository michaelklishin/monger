(ns monger.util
  (:import (java.security SecureRandom) (java.math.BigInteger)))

;;
;; API
;;

(defn ^String random-str
  "Generates a secure random string"
  [^long n, ^long num-base]
  (.toString (new BigInteger n (SecureRandom.)) num-base))