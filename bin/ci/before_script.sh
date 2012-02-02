#!/bin/sh

# MongoDB Java driver won't run authentication twice on the same DB instance,
# so we need to use multiple DBs.
mongo --eval 'db.addUser("clojurewerkz/monger", "monger")' monger-test
mongo --eval 'db.addUser("clojurewerkz/monger", "monger")' monger-test2
mongo --eval 'db.addUser("clojurewerkz/monger", "monger")' monger-test3
mongo --eval 'db.addUser("clojurewerkz/monger", "monger")' monger-test4
