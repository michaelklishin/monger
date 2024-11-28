#!/bin/sh

# Check which MongoDB shell is available
if command -v mongosh >/dev/null 2>&1; then
    MONGO_SHELL="mongosh"
elif command -v mongo >/dev/null 2>&1; then
    MONGO_SHELL="mongo"
else
    echo "Error: Neither mongo nor mongosh shell found. Please install MongoDB shell."
    exit 1
fi

# MongoDB Java driver won't run authentication twice on the same DB instance,
# so we need to use multiple DBs.
$MONGO_SHELL --eval 'db.createUser({"user": "clojurewerkz/monger", "pwd": "monger", roles: ["dbAdmin"], mechanisms: ["SCRAM-SHA-1"], passwordDigestor: "client"})' monger-test
$MONGO_SHELL --eval 'db.createUser({"user": "clojurewerkz/monger", "pwd": "monger", roles: ["dbAdmin"], mechanisms: ["SCRAM-SHA-1"], passwordDigestor: "client"})' monger-test2
$MONGO_SHELL --eval 'db.createUser({"user": "clojurewerkz/monger", "pwd": "monger", roles: ["dbAdmin"], mechanisms: ["SCRAM-SHA-1"], passwordDigestor: "client"})' monger-test3
$MONGO_SHELL --eval 'db.createUser({"user": "clojurewerkz/monger", "pwd": "monger", roles: ["dbAdmin"], mechanisms: ["SCRAM-SHA-1"], passwordDigestor: "client"})' monger-test4