# Monger

Monger is an idiomatic Clojure wrapper around MongoDB Java driver. It offers powerful expressive query DSL, strives to support
every MongoDB 2.0+ feature and is well maintained.


## Project Goals

There is one MongoDB client for Clojure that has been around since 2009. So, why create another one? Monger authors
wanted a client that will

 * Support most of MongoDB 2.0+ features but only those that really matter. Grouping the way it is done today, for example, does not (it is easier to just use Map/Reduce directly).
 * Be well documented.
 * Be well tested.
 * Be maintained, not carry technical debt from 2009 forever.
 * Integrate with libraries like clojure.data.json and Joda Time.
 * Integrate usage of JavaScript files and ClojureScript (as soon as the compiler gets artifact it is possible to depend on for easy embedding).
 * Learn from other clients like the Java and Ruby ones.
 * Target Clojure 1.3.0 and later from the ground up.


## Usage

We are working on documentation guides & examples site for the 1.0 release. In the meantime, please refer to the [test suite](https://github.com/michaelklishin/monger/tree/master/test/monger/test) for code examples.


## Powerful Query DSL

Every application that works with data stores has to query them. As a consequence, having an expressive powerful query DSL is a must
for client libraries like Monger.

Here is what monger.query DSL feels like:

``` clojure
(with-collection "docs"
  (find { :inception_year { $lt 2000 $gte 2011 } })
  (fields [ :inception_year :name ])
  (skip 10)
  (limit 20)
  (batch-size 50)
  (hint "my-index-name")
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


## Integration With Popular Libraries

Because Monger was built for Clojure 1.3 and later, it can take advantage of relatively new powerful Clojure features such as protocols.


### Integration with clojure.data.json

Monger was created for AMQP and HTTP services that use JSON to serialize message payloads. When serializing documents to JSON, developers
usually want to represent `com.mongodb.ObjectId` instances as strings in resulting JSON documents. Monger integrates with [clojure.data.json](http://github.com/clojure/clojure.data.json) to
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
[clojure.data.json](http://github.com/clojure/clojure.data.json) `WriteJSON` protocol to handle `org.joda.time.DateTime` instances. To use it, make sure that
you have JodaTime and clojure.data.json on your dependencies list then load `monger.joda-time` like so

``` clojure
(ns my.service.handlers
  ;; Make clojure.data.json aware of ObjectId instances
  (:require [monger.joda-time]))
```

Now `clojure.data.json/write-json` and related functions will serialize JodaTime date time objects using [ISO8601 date time format](http://joda-time.sourceforge.net/apidocs/org/joda/time/format/ISODateTimeFormat.html). In addition, functions that convert MongoDB documents to
Clojure maps will instantiate JodaTime date time objects from `java.util.Date` instances MongoDB Java driver uses.



## This is a Work In Progress

Core Monger APIs are stabilized but it is still a work in progress. Keep that in mind. 1.0 will be released in early 2012
together with documentation guides and dedicated website.


## Artifacts

Snapshot artifacts are [released to Clojars](https://clojars.org/com.novemberain/monger) every 24 hours.

With Leiningen:

    [com.novemberain/monger "1.0.0-SNAPSHOT"]


With Maven:

    <dependency>
      <groupId>com.novemberain</groupId>
      <artifactId>monger</artifactId>
      <version>1.0.0-SNAPSHOT</version>
    </dependency>


## Continuous Integration

[![Continuous Integration status](https://secure.travis-ci.org/michaelklishin/monger.png)](http://travis-ci.org/michaelklishin/monger)


CI is hosted by [travis-ci.org](http://travis-ci.org).



## Supported Clojure versions

Monger is built from the ground up for Clojure 1.3 and up.


## License

Copyright (C) 2011 Michael S. Klishin

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.
