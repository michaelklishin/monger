#!/bin/sh

# MongoDB Java driver won't run authentication twice on the same DB instance,
# so we need to use multiple DBs.
docker exec mongo_test mongosh --eval 'db.createUser({"user": "clojurewerkz/monger", "pwd": "monger", roles: ["dbAdmin"], mechanisms: ["SCRAM-SHA-1"], passwordDigestor: "client"})' monger-test
docker exec mongo_test mongosh --eval 'db.createUser({"user": "clojurewerkz/monger", "pwd": "monger", roles: ["dbAdmin"], mechanisms: ["SCRAM-SHA-1"], passwordDigestor: "client"})' monger-test2
docker exec mongo_test mongosh --eval 'db.createUser({"user": "clojurewerkz/monger", "pwd": "monger", roles: ["dbAdmin"], mechanisms: ["SCRAM-SHA-1"], passwordDigestor: "client"})' monger-test3
docker exec mongo_test mongosh --eval 'db.createUser({"user": "clojurewerkz/monger", "pwd": "monger", roles: ["dbAdmin"], mechanisms: ["SCRAM-SHA-1"], passwordDigestor: "client"})' monger-test4
