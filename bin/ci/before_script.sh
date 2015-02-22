#!/bin/sh

# MongoDB seems to need some time to boot first. MK.
sleep 5

# MongoDB Java driver won't run authentication twice on the same DB instance,
# so we need to use multiple DBs.
mongo --eval 'db.createUser("clojurewerkz/monger", "monger")' monger-test
mongo --eval 'db.createUser("clojurewerkz/monger", "monger")' monger-test2
mongo --eval 'db.createUser("clojurewerkz/monger", "monger")' monger-test3
mongo --eval 'db.createUser("clojurewerkz/monger", "monger")' monger-test4
