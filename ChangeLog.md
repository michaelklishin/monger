## Changes between 1.0.0-beta8 and 1.0.0-rc1

### Documentation improvements

[Documentation guides](http://clojuremongodb.info) have been greatly improved and now include
several new guides:

 * [Storing files on GridFS with Clojure](http://clojuremongodb.info/articles/gridfs.html)
 * [Using Map/Reduce](http://clojuremongodb.info/articles/mapreduce.html)
 * [Using MongoDB Commands](http://clojuremongodb.info/articles/commands.html)
 * [Using MongoDB 2.2 Aggregation Framework with Clojure](http://clojuremongodb.info/articles/aggregation.html)


### monger.core/current-db

`monger.core/current-db` returns the currently used database.


### monger.core/use-db!

`monger.core/use-db!` composes `monger.core/set-db!` and `monger.core/get-db`:

``` clojure
(ns my.service
  (:use [monger.core :only [set-db! use-db! get-db]]))

;; equivalent
(use-db! "my_product")
(set-db! (get-db "my_product"))
```

### monger.result/ok? now works on Clojure maps

`monger.result/ok?` has been implemented for Clojure maps.




## Changes between 1.0.0-beta7 and 1.0.0-beta8

### GridFS support improvements

Monger finally has a higher-level DSL for storing files on GridFS

``` clojure
(ns my.service
  (:use [monger.gridfs :only [store-file make-input-file filename content-type metadata]]))

;; store a file from a local FS path with the given filename, content type and metadata
(store-file (make-input-file "/path/to/a/local/file.png")
  (filename "image.png")
  (metadata {:format "png"})
  (content-type "image/png"))
```

There are also querying improvements: `monger.gridfs/find-maps` and `monger.gridfs/find-one-as-map` are new functions that were added.
They serve the same purposes as `monger.collection/find-maps` and
`monger.collection/find-one-as-map`, making it easy to work with Clojure data structures all the time.

`monger.gridfs/files-as-maps` works the same way as `monger.gridfs/all-files` but returns results as Clojure maps. It is to
`monger.gridfs/all-files` what `monger.collection/find-maps` is to `monger.collection/find`.


### MongoDB 2.1/2.2 Aggregation Framework support

`monger.collection/aggregate` provides a convenient way to run [aggregation queries](http://docs.mongodb.org/manual/reference/aggregation/).

``` clojure
(ns my.service
  (:require [monger.collection :as mc])
  (:use monger.operators))

;; single stage pipeline
(mc/aggregate "docs" [{$project {:subtotal {$multiply ["$quantity", "$price"]}
                                                       :_id     "$state"}}])

;; two stage pipeline
(mc/aggregate "docs" [{$project {:subtotal {$multiply ["$quantity", "$price"]}
                                                       :_id      1
                                                       :state   1}}
                                            {$group   {:_id   "$state"
                                                       :total {$sum "$subtotal"}}}])
```

The following couple of tests demonstrates aggregation queries with some sample data:

``` clojure
(deftest ^{:edge-features true} test-basic-projection-with-multiplication
  (let [collection "docs"
        batch      [{ :state "CA" :quantity 1 :price 199.00 }
                    { :state "NY" :quantity 2 :price 199.00 }
                    { :state "NY" :quantity 1 :price 299.00 }
                    { :state "IL" :quantity 2 :price 11.50  }
                    { :state "CA" :quantity 2 :price 2.95   }
                    { :state "IL" :quantity 3 :price 5.50   }]
        expected    [{:_id "NY" :subtotal 398.0}
                     {:_id "NY" :subtotal 299.0}
                     {:_id "IL" :subtotal 23.0}
                     {:_id "CA" :subtotal 5.9}
                     {:_id "IL" :subtotal 16.5}
                     {:_id "CA" :subtotal 199.0}]]
    (mc/insert-batch collection batch)
    (let [result (vec (mc/aggregate "docs" [{$project {:subtotal {$multiply ["$quantity", "$price"]}
                                                       :_id     "$state"}}]))]
      (is (= expected result)))))


(deftest ^{:edge-features true} test-basic-total-aggregation
  (let [collection "docs"
        batch      [{ :state "CA" :quantity 1 :price 199.00 }
                    { :state "NY" :quantity 2 :price 199.00 }
                    { :state "NY" :quantity 1 :price 299.00 }
                    { :state "IL" :quantity 2 :price 11.50  }
                    { :state "CA" :quantity 2 :price 2.95   }
                    { :state "IL" :quantity 3 :price 5.50   }]
        expected    [{:_id "CA", :total 204.9} {:_id "IL", :total 39.5} {:_id "NY", :total 697.0}]]
    (mc/insert-batch collection batch)
    (let [result (vec (mc/aggregate "docs" [{$project {:subtotal {$multiply ["$quantity", "$price"]}
                                                       :_id      1
                                                       :state   1}}
                                            {$group   {:_id   "$state"
                                                       :total {$sum "$subtotal"}}}]))]
      (is (= expected result)))))
```

The aggregation framework is an edge feature that will be available in MongoDB 2.2.


### More Operators

Two new operator macros: `$regex`, `$options` and those used by the upcoming
[MongoDB 2.2 Aggregation framework](http://docs.mongodb.org/manual/applications/aggregation/).



## Changes between 1.0.0-beta6 and 1.0.0-beta7

### Replica sets support

Monger can now connect to replica sets using one or more seeds when
calling `monger.core/connect` with a collection of server addresses
instead of just a single one:

``` clojure
(ns my.service
  (:use monger.core))

;; Connect to a single MongoDB instance
(connect (server-address "127.0.0.1" 27017) (mongo-options))

;; Connect to a replica set
(connect [(server-address "127.0.0.1" 27017) (server-address "127.0.0.1" 27018)]
         (mongo-options))
```

`monger.core/connect!` works exactly the same way.

Contributed by [Baishampayan Ghose](https://github.com/ghoseb).


### ring.session.store implementation

Monger now features a [Ring session store](https://github.com/mmcgrana/ring/blob/master/ring-core/src/ring/middleware/session/store.clj) implementation. To use it, require `monger.ring.session-store` and use
`monger.ring.session-store/monger-store` function like so:

``` clojure
(ns my.service
  (:use monger.ring.session-store))

(let [store (monger-store "web_sessions")]
  ...)
```



## Changes between 1.0.0-beta5 and 1.0.0-beta6

### find-and-modify support

`monger.collection/find-and-modify` function implements [atomic Find and Modify](http://www.mongodb.org/display/DOCS/findAndModify+Command) command.
It is similar to the "regular" update operations but atomically modifies a document (at most one) and returns it.

An example:

``` clojure
(mgcol/find-and-modify "scoreboard" {:name "Sophie Bangs"} {$inc {:level 1}} :return-new true)
```

Contributed by [Baishampayan Ghose](https://github.com/ghoseb).


### monger.js is deprecated

`monger.js` namespace is kept for backwards compatibility but is deprecated in favor of [ClojureWerkz Support](http://github.com/clojurewerkz/support)
that now has exactly the same function in `clojurewerkz.support.js`. To add Support to your project with Leiningen, use

``` clojure
[clojurewerkz/support "0.3.0"]
```


### Validateur 1.1.0

[Validateur](https://github.com/michaelklishin/validateur) dependency has been upgraded to 1.1.0.



## Changes between 1.0.0-beta4 and 1.0.0-beta5

### More Operators

Several new operator macros: `$size`, `$exists`, `$mod`, `$type`, `$not`.


### Clojure sets now can be serialized

Monger now supports serialization for all classes that implement `java.util.Set`, including Clojure sets.


### Capped collection support

`monger.collection/create` provides a way to create collections with fine-tuned attributes (for example, capped collections)
without having to use Java driver API.


### clojure.core.cache integration

`monger.cache` is a new namespace for various cache implementations that adhere to the [clojure.core.cache](github.com/clojure/core.cache) `CacheProtocol` protocol
and sit on top of MongoDB


### Clojure symbols now can be serialized

Monger now supports serialization for all classes that implement `clojure.lang.Named`, not just keywords.


### Improved serialization performance

Thanks to faster paths for serialization of strings and dates (java.util.Date), mean time to insert 100,000 documents went down from
about 1.7 seconds to about 0.5 seconds.



## Changes between 1.0.0-beta3 and 1.0.0-beta4

### Support for URI connections (and thus PaaS provides like Heroku)

`monger.core/connect-via-uri!` is a new function that combines `monger.core/connect!`, `monger.core/set-db!` and `monger.core/authenticate`
and works with string URIs like `mongodb://userb71148a:0da0a696f23a4ce1ecf6d11382633eb2049d728e@cluster1.mongohost.com:27034/app81766662`.

It can be used to connect with or without authentication, for example:

``` clojure
;; connect without authentication
(monger.core/connect-via-uri! "mongodb://127.0.0.1/monger-test4")

;; connect with authentication
(monger.core/connect-via-uri! "mongodb://clojurewerkz/monger!:monger!@127.0.0.1/monger-test4")

;; connect using connection URI stored in an env variable, in this case, MONGOHQ_URL
(monger.core/connect-via-uri! (System/genenv "MONGOHQ_URL"))
```

It is also possible to pass connection options as query parameters:

``` clojure
(monger.core/connect-via-uri! "mongodb://localhost/test?maxPoolSize=128&waitQueueMultiple=5;waitQueueTimeoutMS=150;socketTimeoutMS=5500&autoConnectRetry=true;safe=false&w=1;wtimeout=2500;fsync=true")
```


## Changes between 1.0.0-beta2 and 1.0.0-beta3

### Support for field negation in queries

Previously to load only a subset of document fields with Monger, one had to specify them all. Starting
with 1.0.0-beta3, Monger supports [field negation](http://www.mongodb.org/display/DOCS/Retrieving+a+Subset+of+Fields#RetrievingaSubsetofFields-FieldNegation) feature of MongoDB: it is possible to exclude
certain fields instead.

To do so, pass a map as field selector, with fields that should be omitted set to 0:

``` clojure
;; will retrieve all fields except body
(monger.collection/find-one-map "documents" {:author "John"} {:body 0})
```


### Validateur 1.1.0-beta1

[Validateur](https://github.com/michaelklishin/validateur) dependency has been upgraded to 1.1.0-beta1.


### Index Options support for monger.collection/ensure-index and /create-index

`monger.collection/ensure-index` and `/create-index` now accept index options as additional argument.
**Breaking change**: 3-arity versions of those functions now become 4-arity versions.


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

