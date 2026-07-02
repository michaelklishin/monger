#!/bin/sh

# Waits for the mongo_test container to accept connections before the
# test-user provisioning step runs against it.

for attempt in $(seq 1 30); do
    if docker exec mongo_test mongosh --quiet --eval 'db.adminCommand({ ping: 1 })' >/dev/null 2>&1; then
        exit 0
    fi

    if docker exec mongo_test mongo --quiet --eval 'db.adminCommand({ ping: 1 })' >/dev/null 2>&1; then
        exit 0
    fi

    sleep 1
done

echo "Error: timed out waiting for MongoDB to accept connections." >&2
exit 1
