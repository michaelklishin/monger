# Monger, a modern Clojure MongoDB Driver

Monger is an idiomatic [Clojure MongoDB driver](http://clojuremongodb.info) for a more civilized age.

It has batteries included, offers powerful expressive query DSL, strives to support every MongoDB 2.0+ feature and has sane defaults. Monger is built from for modern Clojure versions and sits on top of the official MongoDB Java driver.


## Project Goals

There is one MongoDB client for Clojure that has been around since 2009. So, why create another one? Monger authors
wanted a client that will

 * Support most of MongoDB 2.0+ features, focus on those that really matter.
 * Be [well documented](http://clojuremongodb.info).
 * Be [well tested](https://github.com/michaelklishin/monger/tree/master/test/monger/test).
 * Target modern Clojure versions.
 * Be as close to the Mongo shell query language as practical
 * Integrate with libraries like Joda Time, [Cheshire](https://github.com/dakrone/cheshire), clojure.data.json, [Ragtime](https://github.com/weavejester/ragtime).
 * Support URI connections to be friendly to Heroku and other PaaS providers.
 * Not carry technical debt from 2009 forever.
 * Integrate usage of JavaScript files and ClojureScript (as soon as the compiler gets artifact it is possible to depend on for easy embedding).



## Project Maturity

Monger is not a young project: started in July 2011, it is over 3
years old with active production use from week 1.


## Artifacts

Monger artifacts are [released to
Clojars](https://clojars.org/com.novemberain/monger). If you are using
Maven, add the following repository definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

### The Most Recent Release

With Leiningen:

    [com.novemberain/monger "3.0.0-rc1"]

With Maven:

    <dependency>
      <groupId>com.novemberain</groupId>
      <artifactId>monger</artifactId>
      <version>3.0.0-rc1</version>
    </dependency>



## Getting Started

Please refer to our [Getting Started
guide](http://clojuremongodb.info/articles/getting_started.html). Don't
hesitate to join our [mailing
list](https://groups.google.com/forum/#!forum/clojure-mongodb) and ask
questions, too!


## Documentation & Examples

Please see our [documentation guides site](http://clojuremongodb.info/) and [API reference](http://reference.clojuremongodb.info).

Our [test
suite](https://github.com/michaelklishin/monger/tree/master/test/monger/test)
also has many code examples.


## Community

[Monger has a mailing
list](https://groups.google.com/forum/#!forum/clojure-mongodb). Feel
free to join it and ask any questions you may have.

To subscribe for announcements of releases, important changes and so
on, please follow [@ClojureWerkz](https://twitter.com/#!/clojurewerkz)
on Twitter.


## Supported Clojure versions

Monger requires Clojure 1.6+. The most recent
stable release is highly recommended.


## Continuous Integration Status

[![Continuous Integration status](https://secure.travis-ci.org/michaelklishin/monger.svg)](http://travis-ci.org/michaelklishin/monger)
[![Dependencies Status](http://jarkeeper.com/michaelklishin/monger/status.svg)](http://jarkeeper.com/michaelklishin/monger)


## Monger Is a ClojureWerkz Project

Monger is part of the [group of Clojure libraries known as ClojureWerkz](http://clojurewerkz.org), together with
[Cassaforte](http://clojurecassandra.info), [Langohr](http://clojurerabbitmq.info), [Elastisch](http://clojureelasticsearch.info), [Titanium](http://titanium.clojurewerkz.org), [Quartzite](http://clojurequartz.info) and several others.



## Development

Monger uses [Leiningen 2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md). Make sure you have it installed and then run tests against
supported Clojure versions using

    lein all do clean, javac, test

Then create a branch and make your changes on it. Once you are done with your changes and all tests pass, submit a pull request
on Github.



## License

Copyright (C) 2011-2015 [Michael S. Klishin](http://twitter.com/michaelklishin), Alex Petrov, and the ClojureWerkz team.

Double licensed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html) (the same as Clojure) or
the [Apache Public License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
