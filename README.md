# Monger, a modern Clojure MongoDB Driver

Monger is an idiomatic Clojure MongoDB driver for a more civilized age.

It has batteries included, offers powerful expressive query DSL, strives to support every MongoDB 2.0+ feature and has sane defaults. Monger is built from the
ground up for Clojure 1.3+ and sits on top of the official MongoDB Java driver.


## Project Goals

There is one MongoDB client for Clojure that has been around since 2009. So, why create another one? Monger authors
wanted a client that will

 * Support most of MongoDB 2.0+ features but only those that really matter. Grouping the way it is done today, for example, does not (it is easier to just use Map/Reduce directly).
 * Be [well documented](http://clojuremongodb.info).
 * Be well tested.
 * Be maintained, do not carry technical debt from 2009 forever.
 * Target Clojure 1.3.0 and later from the ground up.
 * Integrate with libraries like clojure.data.json, Joda Time, [Ragtime](https://github.com/weavejester/ragtime).
 * Provide support for unit testing: factories/fixtures DSL, collection cleaner functions, clojure.test integration and so on.
 * Support URI connections to be friendly to Heroku and other PaaS providers.
 * Learn from other clients like the Java and Ruby ones.
 * Integrate usage of JavaScript files and ClojureScript (as soon as the compiler gets artifact it is possible to depend on for easy embedding).



## Community

[Monger has a mailing list](https://groups.google.com/forum/#!forum/clojure-mongodb). Feel free to join it and ask any questions you may have.

To subscribe for announcements of releases, important changes and so on, please follow [@ClojureWerkz](https://twitter.com/#!/clojurewerkz) on Twitter.


## Project Maturity

Monger is not a young project: it will be 1 year old around July 2012, with active production use from week 1.



## Artifacts

Monger artifacts are [released to Clojars](https://clojars.org/com.novemberain/monger). If you are using Maven, add the following repository
definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

### The Most Recent Release

With Leiningen:

    [com.novemberain/monger "1.1.1"]


With Maven:

    <dependency>
      <groupId>com.novemberain</groupId>
      <artifactId>monger</artifactId>
      <version>1.1.1</version>
    </dependency>



## Getting Started

Please refer to our [Getting Started guide](http://clojuremongodb.info/articles/getting_started.html). Don't hesitate to join our [mailing list](https://groups.google.com/forum/#!forum/clojure-mongodb) and ask questions, too!




## Documentation & Examples

Please visit our [documentation site](http://clojuremongodb.info/). Our [test suite](https://github.com/michaelklishin/monger/tree/master/test/monger/test) also has many code examples.



## Supported Clojure versions

Monger is built from the ground up for Clojure 1.3 and up.


## Continuous Integration Status

[![Continuous Integration status](https://secure.travis-ci.org/michaelklishin/monger.png)](http://travis-ci.org/michaelklishin/monger)



### Write Performance

Monger insert operations are efficient and have very little overhead compared to the underlying Java driver. Here
are some numbers on a MacBook Pro from fall 2010 with Core i7 and an Intel SSD drive:

```
Testing monger.test.stress
Inserting  1000  documents...
"Elapsed time: 25.699 msecs"
Inserting  10000  documents...
"Elapsed time: 135.069 msecs"
Inserting  100000  documents...
"Elapsed time: 515.969 msecs"
```

With the `SAFE` write concern, it takes roughly 0.5 second to insert 100,000 documents with Clojure 1.3.0.



## Monger Is a ClojureWerkz Project

Monger is part of the [group of Clojure libraries known as ClojureWerkz](http://clojurewerkz.org), together with
[Neocons](https://github.com/michaelklishin/neocons), [Langohr](https://github.com/michaelklishin/langohr), [Elastisch](https://github.com/clojurewerkz/elastisch), [Welle](https://github.com/michaelklishin/welle), [Quartzite](https://github.com/michaelklishin/quartzite) and several others.


## Development

Monger uses [Leiningen 2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md). Make sure you have it installed and then run tests against
supported Clojure versions using

    lein2 all test

Then create a branch and make your changes on it. Once you are done with your changes and all tests pass, submit a pull request
on Github.



## License

Copyright (C) 2011-2012 Michael S. Klishin

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.
