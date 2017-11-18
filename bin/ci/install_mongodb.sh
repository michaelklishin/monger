#!/bin/sh

sudo apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10
echo "deb http://repo.mongodb.org/apt/debian/dists/wheezy/mongodb-org/3.5/ main" | sudo tee /etc/apt/sources.list.d/mongodb-org.list

sudo apt-get update
sudo apt-get install -y mongodb-org
