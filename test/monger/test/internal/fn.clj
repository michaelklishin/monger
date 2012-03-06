(ns monger.test.internal.fn
  (:use [clojure.test]
        [monger.internal.fn]))


(deftest test-recursive-function-values-expansion
  (are [i o] (is (= (expand-all i) o))
       { :int (fn [] 1) :str "Clojure" :float (Float/valueOf 11.0)  } { :int 1 :str "Clojure" :float (Float/valueOf 11.0 )}
       { :long (fn [] (Long/valueOf 11)) } { :long (Long/valueOf 11) }
       {
        :i 1
        :l (Long/valueOf 1111)
        :s "Clojure"
        :d (Double/valueOf 11.1)
        :f (Float/valueOf 2.5)
        :v [1 2 3]
        :dyn-i (fn [] 1)
        :dyn-s (fn [] "Clojure (expanded)")
        :m { :nested "String" }
        :dyn-m { :abc (fn [] :abc) :nested { :a { :b { :c (fn [] "d") } } } }
        }
       {
        :i 1
        :l (Long/valueOf 1111)
        :s "Clojure"
        :d (Double/valueOf 11.1)
        :f (Float/valueOf 2.5)
        :v [1 2 3]
        :dyn-i 1
        :dyn-s "Clojure (expanded)"
        :m { :nested "String" }
        :dyn-m {
                :abc :abc
                :nested { :a { :b { :c "d" } } }
                }
        }))
