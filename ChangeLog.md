## Changes between 1.0.0-beta1 and 1.0.0-beta2

### 3-arity of monger.collection/find-one-as-map now takes a vector of fields

3-arity of monger.collection/find-one-as-map now takes a vector of fields
instead of `keywordize` to better fit a more commonly needed case.

``` clojure
;; 3-arity in 1.0.0-beta1
(monger.collection/find-one-as-map "documents" { :first_name "John" } false)
```

``` clojure
;; 3-arity in 1.0.0-beta2
(monger.collection/find-one-as-map "documents" { :first_name "John" } [:first_name, :last_name, :age])
```


If you need to use `keywordize`, use 4-arity:

``` clojure
(monger.collection/find-one-as-map "documents" { :first_name "John" } [:first_name, :last_name, :age] false)
```


### Query DSL has a way to specify if fields need to be keywordized

It is now possible to opt-out of field keywordization in the query DSL:
    
``` clojure
(with-collection coll
  (find {})
  (limit 3)
  (sort { :population -1 })
  (keywordize-fields false))
```

the default value is still true, field names will be converted to keywords.
