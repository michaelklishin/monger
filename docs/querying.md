# Introduction

Monger uses functional approach and wraps most of Java Driver's API into several namespaces:

  - monger.core: Core methods (connection and database management)
  - monger.collection: Collection operations (insert, find, count, update, save, remove, index etc.)
  - monger.conversions: Helper methods to convert Clojure maps to DBObject and vice versa. Most of time not used directly, since you can get by manipulating hashes only.
  - monger.query: Cursors and complex queries.
  - monger.result: Auxilitary functions for getting the truth out of received MongoDB results.
  - monger.util: Various utility methods.

# Connecting to MongoDB

Monger users several dynamic variables to simplify your common workflows:
 - *mongodb-host*
 - *mongodb-port*
 - *mongodb-connection*
 - *mongodb-database*
 - *mongodb-write-concern* (we recommend to use WriteConcern/SAFE by default to make sure your data was written

You can work with as many connections as you want, but since most use-cases are for just one database, Monger provides *mongodb-connection* and *mongodb-database* to make it easier for you to connect. In order to perform an initial connection, simply do:

    (monger.core/connect!)
    (monger.core/set-db! (monger.core/get-db "database-name"))

This will connect you to MongoDB server on localhost, port 27071, set that connection to *mongodb-connection*, and create DB entity for "database-name" and preserve it in *mongodb-database*.

If you have more than one database to connect, use monger.core/connect and monger.core/set-db! accordingly.

   (let [ default-mongodb-connection (monger.core/connect)
          my-first-database (monger.core/get-db "my-first-database-name")

          nonlocal-mongodb-connection (monger.core/connect :host "my-mongo-server.local")
          my-second-database (monger.core/get-db "my-second-database-name") ])

So, now you have 2 connections and 2 databases, you can manipulate them independently.

# Working with collections

Monger is made to make your common workflows easier. You can use power of clojure maps to write and query your data. monger.conversions namespace contains several protocols that allow you to convert Clojure data to BasicDBObject that's used by MongoDB Java driver and back. Most of time you don't have to call them directly, but if you implement some more complex workflows, you may possibly need them.


## drop
## create
## exists?

## Query syntax

If you know how to use MongoDB console, you already know how to write Monger queries. In order to start writing queries, you need to know syntax for arrays, maps and operatros.

  { :language "Clojure" }
  ;; { language: "Clojure"}

  { :tags { $all [ "functional" "object-oriented" ] } }
  ;; { $all: [ "functional", "object-oriented" ] }

## Querying collections

### find
### find-one
### find-by-id
### count
### distinct

### Query operators

## Inserting records

### Single
### Batch

## remove

## update

### Atomic modifiers

## save

# Indexing

## Creating index on the collection

## GeoSpatial Indices


# Map/Reduce

# Working with Dates, Joda Time

# Working with commands

# Replica Sets

# Shards

When working with shards, you don't have to make your client to be aware of them. They are transparent and invisible to client, and everything is handled by mongos.

