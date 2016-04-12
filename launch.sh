#!/bin/bash

# number of instances to launch
N=3

# resiliency to maintain
F=1

# simpleDB domain name to hold ipAddress-svrID pairs
IPID_DOMAIN="SERVERIDS"
CONFIG_DOMAIN="RESILIENCY"

# aws config params
INSTALL_FILE="install.sh"
INSTANCE_TYPE="ami-08111162"

# aws credentials should be in ~/.aws/credentials
# enable simpleDB and us-east to work with configured image-id
aws configure set default.region us-east-1
aws configure set preview.sdb true

# reset simpleDB
echo ">>>>>> Cleaning IPID simpleDB domain"
aws sdb delete-domain --domain-name $IPID_DOMAIN
aws sdb create-domain --domain-name $IPID_DOMAIN

echo ">>>>>> Cleaning CONFIG simpleDB domain"
aws sdb delete-domain --domain-name $CONFIG_DOMAIN
aws sdb create-domain --domain-name $CONFIG_DOMAIN

# put config variables to simpleDB
echo ">>>>>> Writing config variables to simpleDB"
aws sdb put-attributes --domain-name $CONFIG_DOMAIN --item-name N \
    --attributes Name=N,Value=$N,Replace=true
aws sdb put-attributes --domain-name $CONFIG_DOMAIN --item-name F \
    --attributes Name=F,Value=$F,Replace=true

# launch N instances
echo ">>>>>> Lunching N instances of EC2"
aws ec2 run-instances --image-id $INSTANCE_TYPE --count $N --instance-type t2.micro --user-data file://$INSTALL_FILE