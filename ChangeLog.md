## Changes between 3.5.x and 3.6.0 (unreleased)

### UUID Representation Option

Added a new connection option, `:uuid-representation`.

Contributed by @okorz001.

GitHub issue: [#212](https://github.com/michaelklishin/monger/issues/212)

### Operator List Update

For MongoDB 4.x.

Contributed by @mjrb.

GitHub issue: [#196](https://github.com/michaelklishin/monger/pull/196)

### Dependency Update

Contributed by @robhanlon22.

GitHub issue: [#206](https://github.com/michaelklishin/monger/pull/206)


## Changes between 3.1.x and 3.5.0 (Dec 10th, 2018)

### MongoDB Java Driver Update

MongoDB Java driver dependency has been updated to `3.9.x`.

This means that Monger now **requires JDK 8**.

Contributed by @Linicks.

### 3rd Party Library Compatibility

 * Cheshire `5.8.x`
 * clj-time `0.15.1`
 * ring-core `0.15.1`
 * Ragtime `0.7.x`.

### URI Connection Usability Improvement

URIs that don't specify a database will now be rejected as invalid.

Contributed by Chris Broome.


## Changes between 3.0.x and 3.1.0 (September 17th, 2016)

### MongoDB Java Driver Update

MongoDB Java driver dependency has been updated to `3.3.0`.

### Cursor Hinting Option Fix

Contributed by Stijn Opheide.

### Improved DBObject to Clojure Map conversion performance

New `from-db-object` implementation for `DBObject` avoids creation of an unnecessary
sequence and instead directly accesses `DBObject` instance in reduce. This should
offer performance improvement of about 20%. A performance test can be found
at [monger.test.stress-test](https://github.com/michaelklishin/monger/blob/master/test/monger/test/stress_test.clj).

Contributed by Juho Teperi.

### Authencation Function No Longer Ignores Credentials

In some cases Monger ignored provided credentials.

Contributed by Artem Chistyakov.

### Macro Type Hint Fixes

Contributed by Andre Ambrosio Boechat.



## Changes between 2.1.0 and 3.0.0

Monger 3.0 is based on the [MongoDB Java driver 3.0](https://www.mongodb.com/blog/post/introducing-30-java-driver)
and has some (relatively minor) **breaking API changes**.

### Error Handling Built Around Write Concerns

Monger no longer provides `monger.core/get-last-error`. It is no
longer needed: write concerns and exceptions is now the primary way for clients
to be notified of operation failures.

### New Authentication API

MongoDB 3.0 supports different authentication mechanisms. Multiple
credentials can be specified for a single connection. The client
and the server then can negotiate what authentication mechanism to use
and which set of credentials succeed.

Monger introduces a new namespace for credential instantiation:
`monger.credentials`. The most common function that relies on
authentication mechanism negotiation is `monger.credentials/for`:

``` clojure
(require '[monger.core :as mg])
(require '[monger.credentials :as mcr])

(let [creds (mcr/for "username" "db-name" "pa$$w0rd")
      conn  (mg/connect-with-credentials "127.0.0.1" creds)]
      )
```

`mg/connect-with-credentials` is the most convenient function to
connect with if you plan on using authentication.

When connecting using a URI, the API hasn't changed.

### monger.search is Gone

`monger.search` is gone. MongoDB 3.0 supports search queries
using regular query operators, namely `$text`. `monger.operators` is
extended to include `$text`, `$search`, `$language`, and `$natural`.

An example of a search query in 3.0:

``` clojure
(require '[monger.core :as mg])
(require '[monger.credentials :as mcr])
(require '[monger.collection :as mc])
(require '[monger.operators :refer [$text $search]])

(let [creds (mcr/for "username" "db-name" "pa$$w0rd")
      conn  (mg/connect-with-credentials "127.0.0.1" creds)
      db    (mg/get-db conn "db-name")]
  (mc/find-maps db "collection" {$text {$search "hello"}}))
```

### Add allow-disk-use and Cursor Options to Aggregates

`monger.collection/aggregate` now supports `:cursor` and `:allow-disk-use` options.

Contributed by Bartek Marcinowski.


### JSON Serialization of BSON Timestamps

JSON serialisation extensions now support BSON timestamps.

Contributed by Tom McMillen.



## Changes between 2.0.0 and 2.1.0

### Clojure 1.7 Compatibility

Monger now compiles with Clojure 1.7.

### MongoDB Java Driver Update

MongoDB Java driver dependency has been updated to `2.13.x`.

### $each Operator

The `$each` operator now can be used via `monger.operators`.

Contributed by Juha Jokimäki.


## Changes between 1.8.0 and 2.0.0

`2.0` is a major release that has **breaking public API changes**.

### Explicit Connection/DB/GridFS Argument

In Monger 2.0, all key public API functions require an explicit
DB/connection/GridFS object to be provided instead of relying on
a shared dynamic var. This makes Monger much easier to use with
systems such as Component and Jig, as well as concurrent
applications that need to work with multiple connections, database,
or GridFS filesystems.

In other words, instead of

``` clojure
(require '[monger.collection :as mc])

(mc/insert "libraries" {:name "Monger"})
```

it is now necessary to do

``` clojure
(require '[monger.collection :as mc])

(mc/insert db "libraries" {:name "Monger"})
```

This also means that `monger.core/connect!` and
`monger.core/connect-via-uri!` were removed, as was
`monger.multi` namespaces.

To connect to MongoDB, use `monger.core/connect`:

``` clojure
(require '[monger.core :as mg])

(let [conn (mg/connect)])
```

or `monger.core/connect-via-uri`:

``` clojure
(require '[monger.core :as mg])

(let [{:keys [conn db]} (mg/connect-via-uri "mongodb://clojurewerkz/monger:monger@127.0.0.1/monger-test4")])
```

To get a database reference, use `monger.core/get-db`, which now requires a connection
object:

``` clojure
(require '[monger.core :as mg])

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")])
```

### Options as Maps

Functions that take options now require a proper Clojure map instead of
pseudo keyword arguments:

``` clojure
# in Monger 1.x
(mc/update db coll {} {:score 0} :multi true)

# in Monger 2.x
(mc/update db coll {} {:score 0} {:multi true})
```



## Changes between 1.8.0-beta2 and 1.8.0

### Clojure 1.6

Monger now depends on `org.clojure/clojure` version `1.6.0`. It is
still compatible with Clojure 1.4 and if your `project.clj` depends on
a different version, it will be used, but 1.6 is the default now.

## Changes between 1.8.0-beta1 and 1.8.0-beta2

### monger.result Use with WriteConcerns is Deprecated

MongoDB Java driver 2.12.x [no longer guarantees connection affinity](https://github.com/mongodb/mongo-java-driver/releases/tag/r2.12.0-rc0) for thread pool
threads.

This means that `WriteConcern#getLastError` is no longer a safe from concurrency
hazards. Therefore the use of `monger.result` functions on `WriteConcern` instances
is now **deprecated** in MongoDB Java client and Monger.

### MongoDB Java Driver Update

MongoDB Java driver dependency has been [updated to 2.12.x](https://github.com/mongodb/mongo-java-driver/releases/tag/r2.12.0-rc0).

### Default WriteConcern Change

Monger now uses [`WriteConcern/ACKNOWLEDGED`](http://api.mongodb.org/java/2.12/com/mongodb/WriteConcern.html#ACKNOWLEDGED) by default. Functionality-wise
it is the same as `WriteConcern/SAFE` in earlier versions.


## Changes between 1.7.0 and 1.8.0-beta1

### monger.core/connect-via-uri

`monger.core/connect-via-uri` is a version of `monger.core/connect-via-uri!`
which returns the connection instead of mutating a var.

It should be used by projects that are built from reloadable
components, together with `monger.multi.*`.


## Changes between 1.7.0-beta1 and 1.7.0

### MongoDB Java Driver Update

MongoDB Java driver dependency has been [updated to 2.11.3](https://github.com/mongodb/mongo-java-driver/releases/tag/r2.11.3).

### Ragtime Dependency Dropped

Ragtime is now an optional dependency: if your project uses `monger.ragtime`, you
need to add Ragtime to your own `project.clj`:

``` clojure
[ragtime/ragtime.core          "0.3.4"]
```

### Validateur Dependency Dropped

[Validateur](http://clojurevalidations.info) is no longer a dependency.


## Changes between 1.6.0 and 1.7.0-beta1

### Fune Tuning Cursor Options

`monger.query` DSL now provides a way to fine tune database cursor
options:

``` clojure
(with-collection "products"
  ...
  (options {:notimeout true, :slaveok false}) ;; where keyword matches Bytes/QUERYOPTION_*
  (options [:notimeout :slaveok])
  (options com.mongodb.Bytes/QUERYOPTION_NOTIMEOUT) ;; support Java constants
  (options :notimeout)
  ...
```

`monger.cursor` is a new namespace that provides the plumbing for cursor
fine tuning but should not be widely used directly.



### Joda Time Integration Improvements: LocalDate

`LocalDate` instance serialization is now supported
by Monger Joda Time integration.

Contributed by Timo Sulg.


### Clojure 1.3 Is No Longer Supported

Monger now officially supports Clojure 1.4+.


### Cheshire Upgrade

[Cheshire](https://github.com/dakrone/cheshire) dependency has been upgraded to 5.2.0


### ClojureWerkz Support Upgrade

ClojureWerkz Support dependency has been updated to `0.19.0`.


### Validateur 1.5.0

[Validateur](https://github.com/michaelklishin/validateur) dependency has been upgraded to 1.5.0.



## Changes between 1.5.0 and 1.6.0

### monger.multi.collection

`monger.multi.collection` is a new namespace with functions that are very similar to those
in the `monger.collection` namespace but always take a database reference as an explicit argument.

They are supposed to be used in cases when relying on `monger.core/*mongodb-database*` is not
enough.

Erik Bakstad contributed most of this work.


### MongoDB Java Driver Update

MongoDB Java driver dependency has been [updated to 2.11.2](https://github.com/mongodb/mongo-java-driver/wiki/Release-Notes).


### monger.core/drop-db

`monger.core/drop-db` is a new function that drops a database by name.


### One More Cache Implementation

`monger.cache/db-aware-monger-cache-factory` will return a MongoDB-backed `clojure.core.cache`
implementation that can use any database:

``` clojure
(require '[monger.core  :as mg])
(require '[monger.cache :as cache])

(let [db   (mg/get-db "altcache")
      coll "cache_entries"
      c    (cache/db-aware-monger-cache-factory db coll)]
  (comment "This cache instance will use the altcache DB"))
```

### Ragtime changes

Bug fix: `monger.ragtime/applied-migration-ids` now returns a vector (instead of a set) in order to preserve the original creation order of the migrations.

Ragtime dependency has been updated to 0.3.3.


## Changes between 1.4.0 and 1.5.0

### Full Text Search Support

Full text search in MongoDB 2.4 can be used via commands but Monger 1.5 also provides
convenience functions in the `monger.search` namespace:

 * `monger.search/search` for performing queries
 * `monger.search/results-from` for obtaining hit documents sorted by score

``` clojure
(require '[monger.collection :as mc])
(require '[monger.search     :as ms])

(mc/ensure-index coll {:subject "text" :content "text"})
(mc/insert coll {:subject "hello there" :content "this should be searchable"})
(mc/insert coll {:subject "untitled" :content "this is just noize"})

(println (ms/results-from (ms/search coll "hello"))
```


### MongoDB Java Driver Update

MongoDB Java driver dependency has been [updated to 2.11.0](https://github.com/mongodb/mongo-java-driver/wiki/Release-Notes).


### New Geospatial Operators

`monger.operators` now defines a few more operators for convenience:

 * `$getWithin`
 * `$getIntersects`
 * `$near`

Of course, these and any other new operators can be passed as strings (e.g. `"$near"`)
as well.


### monger.core/admin-db

`monger.core/admin-db` is a new convenience function that returns the `admin` database
reference.

### monger.command/admin-command

`monger.command/admin-command` is a new convenience function for running commands
on the `admin` database.


### monger.core/mongo-options Updates

`monger.core/mongo-options` options are now up-to-date with the most recent
MongoDB Java driver.

### Factory DSL Is Gone

Monger's factory DSL (an undocumented experimental feature) has been removed from `monger.testkit`. It did
not work as well as we expected and there are better alternatives available now.


### Clojure 1.5 By Default

Monger now depends on `org.clojure/clojure` version `1.5.1`. It is still compatible with Clojure 1.3+ and if your `project.clj` depends
on a different version, it will be used, but 1.5 is the default now.

We encourage all users to upgrade to 1.5, it is a drop-in replacement for the majority of projects out there.

### Authentication On Default Database

`monger.core/authenticate` now has a 2-arity version that will authenticate
on the default database:

``` clojure
(let [username "myservice"
      pwd      "LGo5h#B`cTRQ>28tba6u"]
  (monger.core/use-db! "mydb")
  ;; authenticates requests for mydb
  (monger.core/authenticate username (.toCharArray pwd)))
```

### ClojureWerkz Support Upgrade

ClojureWerkz Support dependency has been updated to version `0.15.0`.
This means Monger now will use Cheshire `5.0.x`.


### Explicit DBCursor Closure by monger.collection/find-maps and the like

`monger.collection/find-maps` and the like will now explicitly close DB cursors.

GH issue: 47


## Changes between 1.3.0 and 1.4.0

### Cheshire Upgrade

`clojurewerkz.support.json` now requires [Cheshire] `5.0`. There were some incompatible changes
in Cheshire `5.0`, see [Cheshire change log](https://github.com/dakrone/cheshire/blob/master/ChangeLog.md#changes-between-cheshire-500-and-40x).


### data.json Dependency Fixes

`monger.json` no longer requires `data.json` to be present at compile time.


### MongoDB Java Driver Update

MongoDB Java driver dependency has been updated to 2.10.0.


### ClojureWerkz Support Upgrade

ClojureWerkz Support dependency has been updated to version `0.9.0`.



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

Because `clojure.data.json` is no longer a hard Monger dependency, you need to either add it as explicit
dependency to your project or switch to Cheshire.

To switch to Cheshire (you may need to update your code that uses `clojure.data.json` directly!),
add the following to your `:dependencies` list:

``` clojure
[cheshire "4.0.3"]
```

For `clojure.data.json` version `0.1.2.`:

``` clojure
[org.clojure/data.json "0.2.0"]
```


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
