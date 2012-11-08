## Changes between 1.3.4 and 1.3.5

No changes yet.


## Changes between 1.3.3 and 1.3.4

### data.json Dependency Fixes

`monger.json` no longer requires `data.json` to be present at compile time.


## Changes between 1.3.2 and 1.3.3

### ClojureWerkz Support Upgrade

ClojureWerkz Support dependency has been updated to version `0.9.0`.


## Changes between 1.3.1 and 1.3.2

### MongoDB Java Driver Update

MongoDB Java driver dependency has been updated to 2.9.3.


## Changes between 1.3.0 and 1.3.1

### MongoDB Java Driver Update

MongoDB Java driver dependency has been updated to 2.9.2.


### ClojureWerkz Support Upgrade

ClojureWerkz Support dependency has been updated to version `0.8.0`.


## Changes between 1.2.0 and 1.3.0

### monger.core/disconnect!

`monger.core/disconnect!` closes the default database connection.


### Ragtime 0.3.0

Ragtime dependency has been updated to 0.3.0.


### MongoDB Java Driver Update

MongoDB Java driver dependency has been updated to 2.9.2.


### Cheshire Support

`monger.json` and `monger.joda-time` will now use [Cheshire](https://github.com/dakrone/cheshire) if it is available. [clojure.data.json](https://github.com/clojure/data.json)
is no longer a hard dependency (but still supported if available).


### ClojureWerkz Support 0.7.0

ClojureWerkz Support dependency has been updated to version `0.7.0`.



## Changes between 1.1.0 and 1.2.0

### Clojure 1.4 By Default

Monger now depends on `org.clojure/clojure` version `1.4.0`. It is still compatible with Clojure 1.3 and if your `project.clj` depends
on 1.3, it will be used, but 1.4 is the default now.

We encourage all users to upgrade to 1.4, it is a drop-in replacement for the majority of projects out there.


### monger.joda-time no longer requires clojure.data.json

`monger.joda-time` no longer requires `clojure.data.json`. If `clojure.data.json` is available, it will be loaded
and extended. If not, `monger.joda-time` will only extend Clojure reader and BSON dates serialization/deserialization.


### MongoDB Java driver 2.9.0

MongoDB Java driver dependency has been updated to 2.9.0.


### Eliminated Reflection Warnings in monger.joda-time

`monger.joda-time` functions no longer result in reflective method calls.

Contributed by [Baishampayan Ghose](https://github.com/ghoseb).


### ClojureWerkz Support 0.6.0

ClojureWerkz Support dependency has been updated to version `0.6.0`.


### Monger Query DSL now supports low level options on cursors

For example:

``` clojure
(with-collection coll
                  (find {})
                  (paginate :page 1 :per-page 3)
                  (sort { :title 1 })
                  (read-preference ReadPreference/PRIMARY)
                  (options com.mongodb.Bytes/QUERYOPTION_NOTIMEOUT))
```

### monger.collection/insert-and-return no longer forcefully replaces existing document id

`monger.collection/insert-and-return` now preserves existing document ids, just like `monger.collection/save-and-return` does.




## Changes between 1.1.0-rc1 and 1.1.0

No changes.



## Changes between 1.1.0-beta2 and 1.1.0-rc1

### monger.collection/save-and-return

`monger.collection/save-and-return` is a new function that to `monger.collection/save` is what `monger.collection/insert-and-return`
is to `monger.collection/insert`. See Monger 1.1.0-beta1 changes or function documentation strings for more information.




## Changes between 1.1.0-beta1 and 1.1.0-beta2

### Support for passing keywords as collection names

It is now possible to use Clojure keywords as collection names with `monger.collection` functions.
For example, `monger.collection/insert-and-return` that's given collection name as `:people` will store
treat it as `people` (by applying [clojure.core/name](http://clojuredocs.org/clojure_core/clojure.core/name) to the argument).


## Changes between 1.1.0-alpha3 and 1.1.0-beta1

### monger.collection/insert-and-return

`monger.collection/insert-and-return` is a new function that solves the biggest complain about Monger's `monger.collection/insert` behavior
from Monger 1.0 users. Because `monger.collection/insert` returns a write result and is supposed to be used with Validateur and
`monger.result/ok?` and similar functions, it is hard to retrieve object id in case it wasn't explicitly passed in.

This resulted in code that looks more or less like this:

``` clojure
(let [oid    (ObjectId.)
      result (merge doc {:_id oid)]
  (monger.collection/insert "documents" result)
  result)
```

To solve this problem, we introduce a new function, `monger.collection/insert-and-return`, that returns the exact inserted document
as an immutable Clojure map. The `:_id` key will be available on the returned map, even if wasn't present and had to be generated.

`monger.collection/insert` behavior stays the same both because of backwards compatibility concerns and because there are valid cases
when a user may want to have the write result returned.



## Changes between 1.1.0-alpha2 and 1.1.0-alpha3

### Clojure reader extensions

`monger.joda-time` now extends Clojure reader for Joda Time types so the new Clojure reader-based
Ring session store can store Joda dates/time values.


## Changes between 1.0.0 and 1.1.0-alpha2

### Alternative, Clojure reader-based Ring session store implementation

Monger 1.1 will have an alternative Ring session store uses Clojure reader serialization

This way libraries like Friend, that use namespaced keywords (like `::identity`) and other
Clojure-specific data structures will work well with Monger.

Current store will strip off namespace information from namespaced keywords
because clojure.core/name work that way. For example:


``` clojure
(name ::identity)
```

Reported by Julio Barros.



## Changes between 1.0.0-rc2 and 1.0.0

### Extended support for BSON serialization for Joda Time types

`monger.joda-time` previously only extended BSON (DBObjects) conversion protocol for `org.joda.time.DateTime`. While `DateTime` is the most
commonly used type in JodaTime, plenty of other types are also used and may need to be stored in MongoDB documents.

Now Monger handles serialization for all JodaTime types that inherit from `org.joda.time.base.AbstractInstant`, for example, `org.joda.time.DateTime`
and `org.joda.time.DateMidnight`.


## Changes between 1.0.0-rc1 and 1.0.0-rc2

### Ragtime integration

Monger now provides an adapter for [Ragtime, a generic Clojure library for data migrations](https://github.com/weavejester/ragtime) (evolutions).

It is in the `monger.ragtime` namespace. To use Ragtime with Monger, you need to add dependencies
on both Ragtime core and Monger to your project. An example with Leiningen:

``` clojure
:dependencies [[org.clojure/clojure       "1.4.0"]
               [com.novemberain/monger    "1.0.0-rc2"]
               [ragtime/ragtime.core      "0.2.0"]]
```

Then require `monger.ragtime` and use Ragtime as usual, passing it a database instance
you get via `monger.core/get-db`.

Monger will persist information about migrations using the `FSYNC_SAFE` write concern.


### Query DSL no longer seq()s the cursor

Query DSL will no longer apply `clojure.core/seq` to the underlying cursor, thus guaranteeing to return an empty
sequence when there are no results. This gives developers better control over what do they want to get back:
an empty sequence or nil. In the latter case, they will just manually apply `clojure.core/seq` to the
result.



### More flexible monger.collection/ensure-index and monger.collection/create-index

`monger.collection/ensure-index` and `monger.collection/ensure-index` now accept fields to index as a collection
(e.g. a vector) as well as a map. It is convenient when creating multiple single-field indexes at once.



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

