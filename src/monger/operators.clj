(ns monger.operators)

(defmacro ^{:private true} defoperator
  [operator]
  (let [op#     (str operator)
        op-sym# (symbol op#)]
    `(def ~op-sym# ~op#)))

;;
;; QUERY OPERATORS
;;

;; $gt is "greater than" comparator
;; $gte is "greater than or equals" comparator
;; $gt is "less than" comparator
;; $lte is "less than or equals" comparator
;;
;; EXAMPLES:
;;  (monger.collection/find "libraries" { :users { $gt 10 } })
;;  (monger.collection/find "libraries" { :users { $gte 10 } })
;;  (monger.collection/find "libraries" { :users { $lt 10 } })
;;  (monger.collection/find "libraries" { :users { $lte 10 } })
(defoperator $gt)
(defoperator $gte)
(defoperator $lt)
(defoperator $lte)

;; $all matches all values in the array
;;
;; EXAMPLES
;;   (mgcol/find-maps "languages" { :tags { $all [ "functional" "object-oriented" ] } } )
(defoperator $all)

;; The $in operator is analogous to the SQL IN modifier, allowing you to specify an array of possible matches.
;;
;; EXAMPLES
;;   (mgcol/find-maps "languages" { :tags { $in [ "functional" "object-oriented" ] } } )
(defoperator $in)

;; The $nin operator is similar to $in, but it selects objects for which the specified field does not
;; have any value in the specified array.
;;
;; EXAMPLES
;;   (mgcol/find-maps "languages" { :tags { $nin [ "functional" ] } } )
(defoperator $nin)

;; $ne is "non-equals" comparator
;;
;; EXAMPLES:
;;   (monger.collection/find "libraries" {$ne { :language "Clojure" }})
(defoperator $ne)

;; $elemMatch checks if an element in an array matches the specified expression
;;
;; EXAMPLES:
;;   ;; Matches element with :text "Nice" and :rating greater than or equal 1
;;   (monger.collection/find "comments" { $elemMatch { :text "Nice!" :rating { $gte 1 } } })
(defoperator $elemMatch)


;;
;; LOGIC OPERATORS
;;

;; $and lets you use a boolean and in the query. Logical and means that all the given expressions should be true for positive match.
;;
;; EXAMPLES:
;;
;;   ;; Matches all libraries where :language is "Clojure" and :users is greater than 10
;;   (monger.collection/find "libraries" { $and [{ :language "Clojure" } { :users { $gt 10 } }] })
(defoperator $and)

;; $or lets you use a boolean or in the query. Logical or means that one of the given expressions should be true for positive match.
;;
;; EXAMPLES:
;;
;;   ;; Matches all libraries whose :name is "mongoid" or :language is "Ruby"
;;   (monger.collection.find "libraries" { $or [ { :name "mongoid" } { :language "Ruby" } ] })
(defoperator $or)

;; @nor lets you use a boolean expression, opposite to "all" in the query (think: neither). Give $nor a list of expressions, all of which should
;;   be false for positive match.
;;
;; EXAMPLES:
;;
;;   (monger.collection/find "libraries" { $nor [{ :language "Clojure" } {:users { $gt 10 } } ]})
(defoperator $nor)

;;
;; ATOMIC MODIFIERS
;;

;; $inc increments one or many fields for the given value, otherwise sets the field to value
;;
;; EXAMPLES:
;;  (monger.collection/update "scores" { :_id user-id } { :score 10 } })
;;  (monger.collection/update "scores" { :_id user-id } { :score 20 :bonus 10 } })
(defoperator $inc)

;; $set sets an existing (or non-existing) field (or set of fields) to value
;; $set supports all datatypes.
;;
;; EXAMPLES:
;;   (monger.collection/update "things" { :_id oid } { $set { :weight 20.5 } })
;;   (monger.collection/update "things" { :_id oid } { $set { :weight 20.5 :height 12.5 } })
(defoperator $set)

;; $unset deletes a given field, non-existing fields are ignored.
;;
;; EXAMPLES:
;;   (monger.collection/update "things" { :_id oid } { $unset { :weight 1 } })
(defoperator $unset)

;; $rename renames a given field
;;
;; EXAMPLES:
;;   (monger.collection/update "things" { :_id oid } { $rename { :old_field_name "new_field_name" } })
(defoperator $rename)

;; $push appends _single_ value to field, if field is an existing array, otherwise sets field to the array [value] if field is not present.
;; If field is present but is not an array, an error condition is raised.
;;
;; EXAMPLES:
;;   (mgcol/update "docs" { :_id oid } { $push { :tags "modifiers" } })
(defoperator $push)

;; $pushAll appends each value in value_array to field, if field is an existing array, otherwise sets field to the array value_array
;; if field is not present. If field is present but is not an array, an error condition is raised.
;;
;; EXAMPLES:
;;   (mgcol/update coll { :_id oid } { $pushAll { :tags ["mongodb" "docs"] } })
(defoperator $pushAll)

;; $addToSet Adds value to the array only if its not in the array already, if field is an existing array, otherwise sets field to the
;; array value if field is not present. If field is present but is not an array, an error condition is raised.
;;
;; EXAMPLES:
;;   (mgcol/update coll { :_id oid } { $addToSet { :tags "modifiers" } })
(defoperator $addToSet)

;; $pop removes the last element in an array, if 1 is passed.
;; if -1 is passed, removes the first element in an array
;;
;; EXAMPLES:
;;   (mgcol/update coll { :_id oid } { $pop { :tags 1 } })
;;   (mgcol/update coll { :_id oid } { $pop { :tags 1 :categories 1 } })
(defoperator $pop)

;; $pull removes all occurrences of value from field, if field is an array. If field is present but is not an array, an error condition
;; is raised.
;;
;; EXAMPLES:
;;   (mgcol/update coll { :_id oid } { $pull { :measurements 1.2 } })
(defoperator $pull)

;; $pullAll removes all occurrences of each value in value_array from field, if field is an array. If field is present but is not an array
;; an error condition is raised.
;;
;; EXAMPLES:
;;   (mgcol/update coll { :_id oid } { $pull { :measurements 1.2 } })
;;   (mgcol/update coll { :_id oid } { $pull { :measurements { $gte 1.2 } } })
(defoperator $pullAll)

(defoperator $bit)
