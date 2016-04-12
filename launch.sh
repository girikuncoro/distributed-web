#!/bin/bash

# AWS credentials to connect with aws cli
# It's actually dangerous to store credentials in Github
# TODO: generate new keys and think of another way to store
AWS_KEY="AKIAI4FQXGCVF2BFTXYQ"
AWS_SECRET="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"

# S3 bucket name to bring war file and other stuffs in
S3_BUCKET="edu-cornell-cs-cs5300s16-gk256"

# simpleDB domain name to hold ipAddress-svrID pairs
DB_DOMAIN="LSI"

# number of instances to launch
N=3

# aws config params
INSTALL_FILE="install.sh"
INSTANCE_TYPE="ami-08111162"

# aws config credentials, enable simpleDB
aws configure set aws_access_key_id $AWS_KEY
aws configure set aws_secret_access_key $AWS_SECRET
aws configure set default.region us-east-1
aws configure set preview.sdb true

# reset simpleDB
aws sdb delete-domain --domain-name $DB_DOMAIN
aws sdb create-domain --domain-name $DB_DOMAIN

# launch N instances
aws ec2 run-instances --image-id $INSTANCE_TYPE --count $N --instance-type t2.micro --user-data file://$INSTALL_FILE