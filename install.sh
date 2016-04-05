#!/bin/bash

WAR_FILE = "hello.war"  # deployable war file
S3_BUCKET = "edu-cornell-cs-cs5300s16-gk256"
DB_DOMAIN = "giri-sdb"
DB_ITEM_NAME = "serverIDs"

AWS_KEY = "AKIAI4FQXGCVF2BFTXYQ"
AWS_SECRET = "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"

# exit if any line fails, and print each cmd executed
set -ex

# install tomcat
yum -y install tomcat8-webapps tomcat8-docs-webapp tomcat8-admin-webapps

# aws config credentials, enable simpleDB
aws configure set aws_access_key_id $AWS_KEY
aws configure set aws_secret_access_key $AWS_SECRET
aws configure set default.region us-east-1
aws configure set preview.sdb true

# install app code
aws s3 s3://$S3_BUCKET/$WAR_FILE ~
sudo cp ~/$WAR_FILE /usr/share/tomcat8/webapps

#aws s3 cp s3://edu-cornell-cs-cs5300s16-gk256/hello.war ~
#sudo cp ~/hello.war /usr/share/tomcat8/webapps

# determine internal IP of this instance, save it to file
wget http://169.254.169.254/latest/meta-data/local-ipv4 -P ~

# assign serverID for this instance, save it to file
wget http://169.254.169.254/latest/meta-data/ami-launch-index -P ~

######## create-domain should be done by launch-script ###########
# aws sdb delete-domain --domain-name $DB_DOMAIN
# aws sdb create-domain --domain-name $DB_DOMAIN

# save serverID:ipAddress pairs to simpleDB
aws sdb put-attributes --domain-name $DB_DOMAIN --item-name $DB_ITEM_NAME \
    --attributes Name=`cat ~/ami-launch-index`,Value=`cat ~/local-ipv4`,Replace=true

# read all ipAddress from simpleDB to file
# TODO: wait until all instances finished writing

# start tomcat
sudo service tomcat8 start
