#!/bin/bash

###################### TA HAVE TO EDIT THIS ##################
# number of instances to launch
N=5

# resiliency to maintain
F=2

# S3 bucket name to bring war file and other stuffs in
S3_BUCKET="edu-cornell-cs-cs5300s16-sz428"

# keypair to ssh instance, important for reboot process
# .pem extension not required
KEYPAIR="proj1bfinal"
##############################################################

# deployable war file
WAR_FILE="proj1b.war"

# simpleDB domain name to hold ipAddress-svrID pairs
IPID_DOMAIN="SERVERIDS"
CONFIG_DOMAIN="RESILIENCY"

# aws config params
INSTALL_FILE="install.sh"
INSTANCE_TYPE="ami-08111162"

# reboot file
REBOOT_FILE="reboot.sh"

# aws credentials should be in ~/.aws/credentials
# enable simpleDB and us-east to work with configured image-id
aws configure set default.region us-east-1
aws configure set preview.sdb true

# upload war file and scripts to simpleDB
echo ">>>>>> Uploading war file, install script and reboot script to AWS S3"
aws s3 cp $WAR_FILE s3://${S3_BUCKET}/$WAR_FILE
aws s3 cp $REBOOT_FILE s3://${S3_BUCKET}/$REBOOT_FILE
aws s3 cp $INSTALL_FILE s3://${S3_BUCKET}/$INSTALL_FILE

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
echo ">>>>>> Launching N instances of EC2"
aws ec2 run-instances --security-groups default --image-id $INSTANCE_TYPE --count $N --instance-type t2.micro --user-data file://$INSTALL_FILE --key-name $KEYPAIR
