(ns monger.operators)

(defmacro defoperator
  [operator]
  (let [op#     (str operator)
        op-sym# (symbol op#)]
    `(def ~op-sym# (str ~op#))))

(defoperator $gt)
(defoperator $inc)
(defoperator $set)
(defoperator $unset)

(defoperator $lt)
(defoperator $lte)
(defoperator $all)
(defoperator $in)
(defoperator $set)
(defoperator $unset)
(defoperator $inc)
(defoperator $push)
(defoperator $pushAll)
(defoperator $addToSet)
(defoperator $pop)
(defoperator $pull)
(defoperator $pullAll)
(defoperator $rename)
