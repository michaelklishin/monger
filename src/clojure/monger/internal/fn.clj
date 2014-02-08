;; Copyright (c) 2011-2014 Michael S. Klishin
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

(defn fpartial
  "Like clojure.core/partial but prepopulates last N arguments (first is passed in later)"
  [f & args]
  (fn [arg & more] (apply f arg (concat args more))))

(defprotocol IFNExpansion
  (expand-all      [x] "Replaces functions with their invocation results, recursively expands maps, evaluates all other values to themselves")
  (expand-all-with [x f] "Replaces functions with their invocation results that function f is applied to, recursively expands maps, evaluates all other values to themselves"))

(extend-protocol IFNExpansion
  java.lang.Integer
  (expand-all      [i]   i)
  (expand-all-with [i f] i)

  java.lang.Long
  (expand-all      [l] l)
  (expand-all-with [l f] l)

  java.lang.String
  (expand-all      [s] s)
  (expand-all-with [s f] s)

  java.lang.Float
  (expand-all      [fl] fl)
  (expand-all-with [fl f] fl)

  java.lang.Double
  (expand-all      [d] d)
  (expand-all-with [d f] d)

  ;; maps are also functions, so be careful here. MK.
  clojure.lang.IPersistentMap
  (expand-all      [m]   (apply-to-values m expand-all))
  (expand-all-with [m f] (apply-to-values m (fpartial expand-all-with f)))

  clojure.lang.PersistentVector
  (expand-all      [v]   (map expand-all v))
  (expand-all-with [v f] (map (fpartial expand-all-with f) v))

  ;; this distinguishes functions from maps, sets and so on, which are also
  ;; clojure.lang.AFn subclasses. MK.
  clojure.lang.AFunction
  (expand-all      [f]          (f))
  (expand-all-with [f expander] (expander f))

  Object
  (expand-all      [x] x)
  (expand-all-with [x f] x))
