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
 - *mongodb-write-concern* (we recommend to use WriteConcern/SAFE by default to make sure your data was written)

