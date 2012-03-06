(ns monger.test.internal.fn
  (:use [clojure.test]
        [monger.internal.fn]))


(deftest test-expand-all
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
        :dyn-v [(fn [] 10) (fn [] 20) (fn [] 30)]
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
        :dyn-v [10 20 30]
        :dyn-i 1
        :dyn-s "Clojure (expanded)"
        :m { :nested "String" }
        :dyn-m {
                :abc :abc
                :nested { :a { :b { :c "d" } } }
                }
        }))

(deftest test-expand-all-with
  (let [expander-fn (fn [v]
                      (* 3 v))]
  (are [i o] (is (= (expand-all-with i expander-fn) o))
       { :a 1 :int (fn [] 3) } { :a 1 :int 9 }
       { :v [(fn [] 1) (fn [] 11)] :m { :inner (fn [] 3) } :s "Clojure" } { :v [3 33] :m { :inner 9 } :s "Clojure" })))
