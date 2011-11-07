(ns monger.operators)

(defmacro defoperator
  [operator]
  (let [operator-name (symbol (str operator))]
  `(defn ~operator-name
     [arg#]
     { (str '~operator-name) arg# }
        )))
(defoperator $gt)
(defoperator $inc)
(defoperator $set)
(defoperator $unset)

;; $lt
;; $lte
;; $all
;; $in
;; $set
;; $unset
;; $inc
;; $push
;; $pushAll
;; $addToSet
;; $pop
;; $pull
;; $pullAll
;; $rename