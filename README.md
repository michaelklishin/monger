# Monger

Monger is an idiomatic Clojure wrapper around MongoDB Java driver.


## Project Goals

There is one MongoDB client for Clojure that has been around since 2009. So, why create another one? Monger authors
wanted a client that will

 * Support most of MongoDB 2.0+ features but only those that really matter. Grouping the way it is done today, for example, does not (it is easier to just use Map/Reduce directly).
 * Be well documented.
 * Be well tested.
 * Be maintained, not carry technical debt from 2009 forever.
 * Integrate usage of JavaScript files and ClojureScript (as soon as the compiler gets artifact it is possible to depend on for easy embedding).
 * Learn from other clients like the Java and Ruby ones.
 * Target Clojure 1.3.0 and later from the ground up.

## Usage

We are working on documentation guides & examples site for the 1.0 release. Please refer to the test suite for code examples.


## This is a Work In Progress

Core Monger APIs are stabilized but it is still a work in progress. Keep that in mind. 1.0 will be released in late 2011.


## Artifacts

Snapshot artifacts are [released to Clojars](https://clojars.org/com.novemberain/monger) every 24 hours.

With Leiningen:

    [com.novemberain/monger "0.11.0-SNAPSHOT"]


With Maven:

    <dependency>
      <groupId>com.novemberain</groupId>
      <artifactId>monger</artifactId>
      <version>0.11.0-SNAPSHOT</version>
    </dependency>


## Continuous Integration

[![Continuous Integration status](https://secure.travis-ci.org/michaelklishin/monger.png)](http://travis-ci.org/michaelklishin/monger)


CI is hosted by [travis-ci.org](http://travis-ci.org)



## Supported Clojure versions

Monger is built from the ground up for Clojure 1.3 and up.


## License

Copyright (C) 2011 Michael S. Klishin

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.
