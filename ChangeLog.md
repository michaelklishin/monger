## Changes between 1.0.0-beta2 and 1.0.0-beta3

### Support serialization of Clojure ratios

Documents that contain Clojure ratios (for example, `26/5`) now can be converted to DBObject instances
and thus stored. On load, ratios will be presented as doubles: this way we ensure interoperability with
other languages and clients.


### Factories/fixtures DSL

When working with even moderately complex data sets, fixture data quickly becomes difficult to
maintain. Monger 1.0.0-beta3 introduce a new factories DSL that is inspired by (but does not try to
copy) Ruby's Factory Girl and similar libraries.

It includes support dynamically evaluated attributes and support for two most common techniques for
implementing associations between documents.

Here is what it feels like:

``` clojure
(defaults-for "domains"
  :ipv6-enabled false)

(factory "domains" "clojure"
         :name       "clojure.org"
         :created-at (-> 2 days ago)
         :embedded   [(embedded-doc "pages" "http://clojure.org/lisp")
                      (embedded-doc "pages" "http://clojure.org/jvm_hosted")
                      (embedded-doc "pages" "http://clojure.org/runtime_polymorphism")])

(factory "domains" "elixir"
         :name     "elixir-lang.org"
         :created-at (fn [] (now))
         :topics     (fn [] ["programming" "erlang" "beam" "ruby"])
         :related    {
                      :terms (fn [] ["erlang" "python" "ruby"])
                      })

(factory "pages" "http://clojure.org/rationale"
         :name "/rationale"
         :domain-id (parent-id "domains" "clojure"))
(factory "pages" "http://clojure.org/jvm_hosted"
         :name "/jvm_hosted")
(factory "pages" "http://clojure.org/runtime_polymorphism"
         :name "/runtime_polymorphism")
(factory "pages" "http://clojure.org/lisp"
         :name "/lisp")


(build "domains" "clojure" :created-at (-> 2 weeks ago))
(seed  "pages" "http://clojure.org/rationale")
```

### Leiningen 2

Monger now uses [Leiningen 2](https://github.com/technomancy/leiningen/wiki/Upgrading).


### monger.core/set-connection!

monger.core/set-connection! allows you to instantiate connection object (com.mongodb.Mongo instances) any
way you like and then use it as default Monger connection. MongoDB Java driver provides many ways to instantiate
and fine tune connections, this is the easiest way for Monger to support them all.

### 2-arity for monger.core/connect and monger.core/connect!

monger.core/connect now has 2-arity that accepts `com.mongodb.ServerAddresss` and `com.mongodb.MongoOptions`
instances and allows you fine tune parameters like socket and connection timeouts, default `:w` value, connection threads
settings and so on.

`monger.core/mongo-options` and `monger.core/server-address` are helper functions that instantiate those classes from
paramters passed as Clojure maps, for convenience.




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



### monger.collection/find-by-id and /find-map-by-id fail fast when id is nil

monger.collection/find-by-id and /find-map-by-id now will throw IllegalArgumentException when id is nil


### monger.collection/find-map-by-id no longer ignore fields argument

monger.collection/find-map-by-id no longer ignore fields argument. Contributed by Toby Hede.


### Meet monger.db and monger.command

`monger.db` namespace was added to perform operations like adding users or dropping databases. Several functions from
`monger.core` will eventually be moved there, but not for 1.0. 

`monger.command` namespace includes convenience methods for issuing MongoDB commands.

Both are contributed by Toby Hede.


### New convenience functions: monger.collection/update-by-id, /remove-by-id

`monger.collection/update-by-id` is a new convenience function for updating a single document with
given ObjectId. `monger.collection/remove-by-id` is its counterpart for removing documents.


### monger.core/get-db-names

monger.core/get-db-names returns a set of databases. Contributed by Toby Hede.

