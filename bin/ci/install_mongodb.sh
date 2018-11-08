#!/bin/sh

#sudo apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com --recv 0C49F3730359A14518585931BC711F9BA15703C6
echo "deb http://repo.mongodb.org/apt/ubuntu trusty/mongodb-org/3.4 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org.list

sudo apt-get update
sudo apt-get install -y mongodb-org
