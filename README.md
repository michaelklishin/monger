# Monger

Monger is an idiomatic Clojure wrapper around MongoDB Java driver. It offers powerful expressive query DSL, strives to support
every MongoDB 2.0+ feature and is well maintained.

[![Continuous Integration status](https://secure.travis-ci.org/michaelklishin/monger.png)](http://travis-ci.org/michaelklishin/monger)


## Project Goals

There is one MongoDB client for Clojure that has been around since 2009. So, why create another one? Monger authors
wanted a client that will

 * Support most of MongoDB 2.0+ features but only those that really matter. Grouping the way it is done today, for example, does not (it is easier to just use Map/Reduce directly).
 * Be well documented.
 * Be well tested.
 * Be maintained, do not carry technical debt from 2009 forever.
 * Integrate with libraries like clojure.data.json and Joda Time.
 * Provide support for unit testing: factories/fixtures DSL, collection cleaner functions, clojure.test integration and so on.
 * Integrate usage of JavaScript files and ClojureScript (as soon as the compiler gets artifact it is possible to depend on for easy embedding).
 * Learn from other clients like the Java and Ruby ones.
 * Target Clojure 1.3.0 and later from the ground up.


## Documentation & Examples

We are working on documentation guides & examples site for the 1.0 release. In the meantime, please refer to the [test suite](https://github.com/michaelklishin/monger/tree/master/test/monger/test) for code examples.

## Community

[Monger has a mailing list](https://groups.google.com/forum/#!forum/clojure-monger). Feel free to join it and ask any questions you may have.

To subscribe for announcements of releases, important changes and so on, please follow [@ClojureWerkz](https://twitter.com/#!/clojurewerkz) on Twitter.


## This is a Work In Progress

Core Monger APIs are stabilized but it is still a work in progress. Keep that in mind. 1.0 will be released in 2012
together with documentation guides and dedicated website.


## Artifacts

### The Most Recent Release

With Leiningen:

    [com.novemberain/monger "1.0.0-beta2"]


With Maven:

    <dependency>
      <groupId>com.novemberain</groupId>
      <artifactId>monger</artifactId>
      <version>1.0.0-beta2</version>
    </dependency>


### Snapshots

If you are comfortable with using snapshots, snapshot artifacts are [released to Clojars](https://clojars.org/com.novemberain/monger) every 24 hours.

With Leiningen:

    [com.novemberain/monger "1.0.0-SNAPSHOT"]


With Maven:

    <dependency>
      <groupId>com.novemberain</groupId>
      <artifactId>monger</artifactId>
      <version>1.0.0-SNAPSHOT</version>
    </dependency>


## Supported Clojure versions

Monger is built from the ground up for Clojure 1.3 and up.


## Connecting to MongoDB

Monger supports working with multiple connections and/or databases but is optimized for applications that only use one connection
and one database.

``` clojure
(ns my.service.server
  (:require [monger core util]))

;; localhost, default port
(monger.core/connect!)

;; given host, given port
(monger.core/connect! { :host "db.megacorp.internal" :port 7878 })
```

To set default database Monger will use, use `monger.core/get-db` and `monger.core/set-db!` functions in combination:

``` clojure
(ns my.service.server
  (:require [monger core]]))

;; localhost, default port
(monger.core/connect!)
(monger.core/set-db! (monger.core/get-db "monger-test"))
```

To set default write concern, use `monger.core/set-default-write-concern!` function:

``` clojure
(monger.core/set-default-write-concern! WriteConcern/FSYNC_SAFE)
```

By default Monger will use `WriteConcern/SAFE` as write concern. We believe that MongoDB Java driver (as well as other
official drivers) are using very unsafe defaults when no exceptions are raised, even for network issues. This does not sound
like a good default for most applications: many applications use MongoDB because of the flexibility, not extreme write throughput
requirements.

## Inserting Documents

To insert documents, use `monger.collection/insert` and `monger.collection/insert-batch` functions.

``` clojure
(ns my.service.server
  (:use [monger.core :only [connect! connect set-db! get-db]]
        [monger.collection :only [insert insert-batch]])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))

;; localhost, default port
(connect!)
(set-db! (monger.core/get-db "monger-test"))

;; without document id
(insert "document" { :first_name "John" :last_name "Lennon" })

;; multiple documents at once
(insert-batch "document" [{ :first_name "John" :last_name "Lennon" }
                          { :first_name "Paul" :last_name "McCartney" }])

;; with explicit document id
(insert "documents" { :_id (ObjectId.) :first_name "John" :last_name "Lennon" })

;; with a different write concern
(insert "documents" { :_id (ObjectId.) :first_name "John" :last_name "Lennon" } WriteConcern/JOURNAL_SAFE)

;; with a different database
(let [archive-db (get-db "monger-test.archive")]
  (insert archive-db "documents" { :first_name "John" :last_name "Lennon" } WriteConcern/NORMAL))
```

### Write Performance

Monger insert operations are efficient and have very little overhead compared to the underlying Java driver. Here
are some numbers on a MacBook Pro from fall 2010 with Core i7 and an Intel SSD drive:

```
Testing monger.test.stress
Inserting  1000  documents...
"Elapsed time: 38.317 msecs"
Inserting  10,000  documents...
"Elapsed time: 263.827 msecs"
Inserting  100,000  documents...
"Elapsed time: 1679.828 msecs"
```

With the `SAFE` write concern, it takes roughly 1.7 second to insert 100,000 documents.


## Regular Finders

`monger.collection` namespace provides several finder functions that try to follow MongoDB query language as closely as possible,
even when providing shortcuts for common cases.

``` clojure
(ns my.service.finders
  (:require [monger.collection :as mc])
  (:use     [monger.operators]))

;; find one document by id, as Clojure map
(mc/find-map-by-id "documents" (ObjectId. "4ec2d1a6b55634a935ea4ac8"))

;; find one document by id, as `com.mongodb.DBObject` instance
(mc/find-by-id "documents" (ObjectId. "4ec2d1a6b55634a935ea4ac8"))

;; find one document as Clojure map
(mc/find-one-as-map "documents" { :_id (ObjectId. "4ec2d1a6b55634a935ea4ac8") })

;; find one document by id, as `com.mongodb.DBObject` instance
(mc/find-one "documents" { :_id (ObjectId. "4ec2d1a6b55634a935ea4ac8") })


;; all documents  as Clojure maps
(mc/find-maps "documents")

;; all documents  as `com.mongodb.DBObject` instances
(mc/find "documents")

;; with a query, as Clojure maps
(mc/find-maps "documents" { :year 1998 })

;; with a query, as `com.mongodb.DBObject` instances
(mc/find "documents" { :year 1998 })

;; with a query that uses operators
(mc/find "products" { :price_in_subunits { $gt 4000 $lte 1200 } })

;; with a query that uses operators as strings
(mc/find "products" { :price_in_subunits { "$gt" 4000 "$lte" 1200 } })
```


## Powerful Query DSL

Every application that works with data stores has to query them. As a consequence, having an expressive powerful query DSL is a must
for client libraries like Monger.

Here is what monger.query DSL feels like:

``` clojure
(with-collection "movies"
  (find { :year { $lt 2010 $gte 2000 }, :revenue { $gt 20000000 } })  
  (fields [ :year :title :producer :cast :budget :revenue ])
  (sort { :revenue -1 })
  (skip 10)
  (limit 20)
  (hint "year-by-year-revenue-idx")
  (snapshot))
```

It is easy to add new DSL elements, for example, adding pagination took literally less than 10 lines of Clojure code. Here is what
it looks like:

``` clojure
(with-collection coll
                  (find {})
                  (paginate :page 1 :per-page 3)
                  (sort { :title 1 })
                  (read-preference ReadPreference/PRIMARY))
```

Query DSL supports composition, too:

``` clojure
(let
    [top3               (partial-query (limit 3))
     by-population-desc (partial-query (sort { :population -1 }))
     result             (with-collection coll
                          (find {})
                          (merge top3)
                          (merge by-population-desc))]
  ;; ...
  )
```

More code examples can be found [in our test suite](https://github.com/michaelklishin/monger/tree/master/test/monger/test).


## Updating Documents

Use `monger.collection/update` and `monger.collection/save`.


## Removing Documents

Use `monger.collection/remove`.


## Counting Documents

Use `monger.collection/count`, `monger.collection/empty?` and `monger.collection/any?`.


## Determening Whether Operation Succeeded (or Failed)

To be documented.


## Validators with Validateur

Monger relies on [Validateur](http://github.com/michaelklishin/validateur) for data validation.

To be documented.


## Integration With Popular Libraries

Because Monger was built for Clojure 1.3 and later, it can take advantage of relatively new powerful Clojure features such as protocols.


### Integration with clojure.data.json

Monger was created for AMQP and HTTP services that use JSON to serialize message payloads. When serializing documents to JSON, developers
usually want to represent `com.mongodb.ObjectId` instances as strings in resulting JSON documents. Monger integrates with [clojure.data.json](http://github.com/clojure/data.json) to
make that effortless.

Just load `monger.json` namespace and it will extend `clojure.data.json/WriteJSON` protocol to support `com.mongodb.ObjectId` instance. Then
functions like `clojure.data.json/write-json` will be able to serialize object ids as strings exactly the way you expect it to be.

``` clojure
(ns my.service.handlers
  ;; Make clojure.data.json aware of ObjectId instances
  (:require [monger.json]))
```


### Integration with Joda Time

Monger provides the `monger.joda-time` namespace that extend its own Clojure-to-DBObject conversion protocols as well as
[clojure.data.json](http://github.com/clojure/data.json) `WriteJSON` protocol to handle `org.joda.time.DateTime` instances. To use it, make sure that
you have JodaTime and clojure.data.json on your dependencies list then load `monger.joda-time` like so

``` clojure
(ns my.service.handlers
  ;; Make Monger conversion protocols and clojure.data.json aware of JodaTime's DateTime instances
  (:require [monger.joda-time]))
```

Now `clojure.data.json/write-json` and related functions will serialize JodaTime date time objects using [ISO8601 date time format](http://joda-time.sourceforge.net/apidocs/org/joda/time/format/ISODateTimeFormat.html). In addition, functions that convert MongoDB documents to
Clojure maps will instantiate JodaTime date time objects from `java.util.Date` instances MongoDB Java driver uses.



## Factories/Fixtures DSL (For Unit Testing)

To be documented.


## Map/Reduce. Using JavaScript Resources.

To be documented.


## Operations On Indexes

To be documented.


## Database Commands

To be documented.


## GridFS Support

To be documented.


## Helper Functions

To be documented.


## Monger Is a ClojureWerkz Project

Neocons is part of the group of libraries known as ClojureWerkz, together with
[Neocons](https://github.com/michaelklishin/neocons), [Langohr](https://github.com/michaelklishin/langohr), [Elastisch](https://github.com/clojurewerkz/elastisch), [Quartzite](https://github.com/michaelklishin/quartzite) and several others.


## Development

Monger uses [Leiningen 2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md). Make sure you have it installed and then run tests against
supported Clojure versions using

    lein2 all test

Then create a branch and make your changes on it. Once you are done with your changes and all tests pass, submit a pull request
on Github.



## License

Copyright (C) 2011-2012 Michael S. Klishin

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.
