;; This source code is dual-licensed under the Apache License, version
;; 2.0, and the Eclipse Public License, version 1.0.
;;
;; The APL v2.0:
;;
;; ----------------------------------------------------------------------------------
;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
;; ----------------------------------------------------------------------------------
;;
;; The EPL v1.0:
;;
;; ----------------------------------------------------------------------------------
;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team.
;; All rights reserved.
;;
;; This program and the accompanying materials are made available under the terms of
;; the Eclipse Public License Version 1.0,
;; which accompanies this distribution and is available at
;; http://www.eclipse.org/legal/epl-v10.html.
;; ----------------------------------------------------------------------------------

(ns monger.operators
  "Provides vars that represent various MongoDB operators, for example, $gt or $in or $regex.
   They can be passed in queries as strings but using vars from this namespace makes the code
   a bit cleaner and closer to what you would see in a MongoDB shell query.

   Related documentation guide: http://clojuremongodb.info/articles/querying.html")

(defmacro ^{:private true} defoperator
  [operator]
  `(def ^{:const true} ~(symbol (str operator)) ~(str operator)))

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
;;   (monger.collection/find "libraries" { :language { $ne "Clojure" }})
(defoperator $ne)

;; $elemMatch checks if an element in an array matches the specified expression
;;
;; EXAMPLES:
;;   ;; Matches element with :text "Nice" and :rating greater than or equal 1
;;   (monger.collection/find "comments" { $elemMatch { :text "Nice!" :rating { $gte 1 } } })
(defoperator $elemMatch)

(defoperator $regex)
(defoperator $options)

;; Matches documents that satisfy a JavaScript expression.
;;
;; EXAMPLES:
;;
;;   (monger.collection/find "people" { $where "this.placeOfBirth === this.address.city" })
(defoperator $where)

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

(defoperator $mul)

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

;; $setOnInsert assigns values to fields during an upsert only when using the upsert option to the update operation performs an insert.
;; New in version 2.4. http://docs.mongodb.org/manual/reference/operator/setOnInsert/
;;
;; EXAMPLES:
;;   (monger.collection/find-and-modify "things" {:_id oid} {$set {:lastseen now} $setOnInsert {:firstseen now}} :upsert true)
(defoperator $setOnInsert)

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

;; $each is a modifier for the $push and $addToSet operators for appending multiple values to an array field.
;; Without the $each modifier $push and $addToSet will append an array as a single value.
;; MongoDB 2.4 adds support for the $each modifier to the $push operator.
;; In MongoDB 2.2 the $each modifier can only be used with the $addToSet operator.
;;
;; EXAMPLES:
;;   (mgcol/update coll { :_id oid } { $push { :tags { $each ["mongodb" "docs"] } } })
(defoperator $each)

;; $pushAll appends each value in value_array to field, if field is an existing array, otherwise sets field to the array value_array
;; if field is not present. If field is present but is not an array, an error condition is raised.
;; Deprecated since MongoDB 2.4, $push with $each modifier should be used instead.
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

(defoperator $exists)
(defoperator $mod)
(defoperator $size)
(defoperator $type)
(defoperator $not)


;;
;; Aggregation in 2.2
;;

(defoperator $match)
(defoperator $project)
(defoperator $limit)
(defoperator $skip)
(defoperator $unwind)
(defoperator $group)
(defoperator $sort)

(defoperator $cmp)

(defoperator $min)
(defoperator $max)
(defoperator $avg)
(defoperator $sum)

(defoperator $first)
(defoperator $last)

(defoperator $add)
(defoperator $divide)
(defoperator $multiply)
(defoperator $substract)

(defoperator $strcasecmp)
(defoperator $substr)
(defoperator $toLower)
(defoperator $toUpper)

(defoperator $dayOfMonth)
(defoperator $dayOfWeek)
(defoperator $dayOfYear)
(defoperator $hour)
(defoperator $minute)
(defoperator $month)
(defoperator $second)
(defoperator $millisecond)
(defoperator $week)
(defoperator $year)
(defoperator $isoDate)


(defoperator $ifNull)
(defoperator $cond)

;; Geospatial
(defoperator $geoWithin)
(defoperator $geoIntersects)
(defoperator $near)
(defoperator $nearSphere)
(defoperator $geometry)
(defoperator $maxDistance)
(defoperator $minDistance)
(defoperator $center)
(defoperator $centerSphere)
(defoperator $box)
(defoperator $polygon)

(defoperator $slice)

;; full text search
(defoperator $text)
(defoperator $search)
(defoperator $language)
(defoperator $natural)

;; $currentDate operator sets the value of a field to the current date, either as a Date or a timestamp. The default type is Date.
;;
;; EXAMPLES:
;;   (mgcol/update coll { :_id oid } { $currentDate { :lastModified true } })
(defoperator $currentDate)
