;; Copyright (c) 2011-2012 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.internal.fn)


;;
;; Implementation
;;

(defn- apply-to-values [m f]
  "Applies function f to all values in map m"
  (into {} (for [[k v] m]
             [k (f v)])))


;;
;; API
;;

(defprotocol IFNExpansion
  (expand-all [x] "Invokes functions, recursively expands maps, evaluates all other values to themselves"))

(extend-protocol IFNExpansion
  java.lang.Integer
  (expand-all [i] i)

  java.lang.Long
  (expand-all [l] l)

  java.lang.String
  (expand-all [s] s)

  java.lang.Float
  (expand-all [f] f)

  java.lang.Double
  (expand-all [d] d)

  java.util.Map
  (expand-all [m] (apply-to-values m expand-all))

  clojure.lang.PersistentVector
  (expand-all [v] v)

  clojure.lang.APersistentMap
  (expand-all [m] (apply-to-values m expand-all))

  clojure.lang.IFn
  (expand-all [f] (f))

  Object
  (expand-all [x] x))
