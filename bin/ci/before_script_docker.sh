#!/bin/sh

# Check which MongoDB shell is available in the container
if docker exec mongo_test which mongosh >/dev/null 2>&1; then
    MONGO_SHELL="mongosh"
elif docker exec mongo_test which mongo >/dev/null 2>&1; then
    MONGO_SHELL="mongo"
else
    echo "Error: Neither mongo nor mongosh shell found in the container."
    exit 1
fi

# MongoDB Java driver won't run authentication twice on the same DB instance,
# so we need to use multiple DBs.
docker exec mongo_test $MONGO_SHELL --eval 'db.createUser({"user": "clojurewerkz/monger", "pwd": "monger", roles: ["dbAdmin"], mechanisms: ["SCRAM-SHA-1"], passwordDigestor: "client"})' monger-test
docker exec mongo_test $MONGO_SHELL --eval 'db.createUser({"user": "clojurewerkz/monger", "pwd": "monger", roles: ["dbAdmin"], mechanisms: ["SCRAM-SHA-1"], passwordDigestor: "client"})' monger-test2
docker exec mongo_test $MONGO_SHELL --eval 'db.createUser({"user": "clojurewerkz/monger", "pwd": "monger", roles: ["dbAdmin"], mechanisms: ["SCRAM-SHA-1"], passwordDigestor: "client"})' monger-test3
docker exec mongo_test $MONGO_SHELL --eval 'db.createUser({"user": "clojurewerkz/monger", "pwd": "monger", roles: ["dbAdmin"], mechanisms: ["SCRAM-SHA-1"], passwordDigestor: "client"})' monger-test4