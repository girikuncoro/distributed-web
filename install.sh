#!/bin/bash

###################### TA HAVE TO EDIT THIS ##################
# AWS credentials to connect with aws cli
AWS_KEY="AKIAI4FQXGCVF2BFTXYQ"
AWS_SECRET="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"

# S3 bucket name to bring war file and other stuffs in
S3_BUCKET="edu-cornell-cs-cs5300s16-gk256"
##############################################################

# deployable war file
WAR_FILE="proj1b.war"

# simpleDB domain name to hold ipAddress-svrID pairs
IPID_DOMAIN="SERVERIDS"

# simpleDB domain name to hold config variables
CONFIG_DOMAIN="RESILIENCY"

# file path to store tmp files and instance file
HOME_PATH="/var/tmp/"

# java version to install
JAVA_VER=jdk-8u60-linux-x64.rpm
JAVA_URL="http://download.oracle.com/otn-pub/java/jdk/8u60-b27/$JAVA_VER"

# file name to store ipAddress-svrID pairs in file system
INSTANCE_FILE=instances.txt

# file name to store config variables in file system
CONFIG_FILE=config.txt

# file name to reboot instance
REBOOT_FILE=reboot.sh

# file name for rebootNum
REBOOT_NUM=rebootNum.txt

# file name for install script, in case installation fails
INSTALL_FILE=install.sh

# print each cmd executed
set -ex

# install java8 to be compatible with tomcat8 and webapp
echo ">>>>>> Installing Java8 with RPM install"
wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" $JAVA_URL -P $HOME_PATH
yum -y localinstall "$HOME_PATH$JAVA_VER"

# install tomcat8
echo ">>>>>> Installing Tomcat8"
yum -y install tomcat8-webapps tomcat8-docs-webapp tomcat8-admin-webapps

# aws config credentials, enable simpleDB
echo ">>>>>> Configure AWS credentials"
aws configure set aws_access_key_id $AWS_KEY
aws configure set aws_secret_access_key $AWS_SECRET
aws configure set default.region us-east-1
aws configure set preview.sdb true

# getting config variables from simpleDB
echo ">>>>>> Getting number of instances required"
N_INSTANCE="$(aws sdb select --select-expression "SELECT * FROM $CONFIG_DOMAIN" --output text | grep -v "ITEMS" | grep -v "F" | grep -o '[0-9]')"

# install app code
echo ">>>>>> Installing war file from AWS S3"
S3_URL="$S3_BUCKET/$WAR_FILE"
aws s3 cp s3://$S3_URL $HOME_PATH
WAR_URL="$HOME_PATH$WAR_FILE"
cp $WAR_URL /usr/share/tomcat8/webapps

# get reboot script
echo ">>>>>> Getting reboot script from AWS S3"
S3REBOOT_URL="$S3_BUCKET/$REBOOT_FILE"
aws s3 cp s3://$S3REBOOT_URL $HOME_PATH
chmod +x "$HOME_PATH$REBOOT_FILE"

# determine internal IP of this instance, save it to file, overwrite if exist
echo ">>>>>> Getting local ip address of the instance"
IP_ADDR=local-ipv4
wget http://169.254.169.254/latest/meta-data/local-ipv4 -O "$HOME_PATH$IP_ADDR"

# assign serverID for this instance, save it to file, overwrite if exist
echo ">>>>>> Getting ami launch index of the instance"
AMI_IDX=ami-launch-index
wget http://169.254.169.254/latest/meta-data/ami-launch-index -O "$HOME_PATH$AMI_IDX"

# save ipAddr:svrID pairs to simpleDB if the instance hasn't done so
echo ">>>>>> Check if instance already exist in simpleDB"
IP_ADDR="$HOME_PATH$IP_ADDR"
IP_VAL="$(cat $IP_ADDR)"

AMI_IDX="$HOME_PATH$AMI_IDX"
AMI_VAL="$(cat $AMI_IDX)"

# ignore if it's already there, this might happen when install fails
EXIST="$(aws sdb get-attributes --domain-name $IPID_DOMAIN --item-name $IP_VAL --output text | wc -l)"
if [ $EXIST -eq 0 ]
then
	echo ">>>>>> Write ip and id pairs to simpleDB"
	aws sdb put-attributes --domain-name $IPID_DOMAIN --item-name $IP_VAL \
	    --attributes Name=$IP_VAL,Value=$AMI_VAL,Replace=true
else
	echo ">>>>>> Instance already exist, no need to update simpleDB"
fi

# wait for all instances to write
echo ">>>>>> Getting ip and id pairs from all instances"
CURR=0
while [ $CURR -lt $N_INSTANCE ]
do
    CURR="$(aws sdb select --select-expression "SELECT COUNT(*) FROM $IPID_DOMAIN" --output text | grep -o '[0-9]')"

    echo "Waiting for other instances"
    sleep 5
done

# write all ipAddress from simpleDB to file
echo ">>>>>> Writing all pairs into local file"
INSTANCE_FILE="$HOME_PATH$INSTANCE_FILE"
aws sdb select --select-expression "SELECT * FROM $IPID_DOMAIN" --output text | grep -v "ITEMS" > $INSTANCE_FILE
sed -i 's/ATTRIBUTES//g; s/^[ \t]*//' $INSTANCE_FILE

# write config variables from simpleDB to file
echo ">>>>>> Writing config file into local file"
CONFIG_FILE="$HOME_PATH$CONFIG_FILE"
aws sdb select --select-expression "SELECT * FROM $CONFIG_DOMAIN" --output text | grep -v "ITEMS" > $CONFIG_FILE
sed -i 's/ATTRIBUTES//g; s/^[ \t]*//' $CONFIG_FILE

# init reboot number only if not exist
REBOOT_FILE="$HOME_PATH$REBOOT_NUM"
if [ ! -f $REBOOT_FILE ]
then
	echo ">>>>>> Initialize the reboot number"
	echo 0 > $REBOOT_FILE
	chmod +x $REBOOT_FILE
else
	echo ">>>>>> Reboot file already exist"
fi

# getting installation script only if not exist
INSTALL_F="$HOME_PATH$INSTALL_FILE"
if [ ! -f $INSTALL_FILE ]
then
	echo ">>>>>> Getting installation script to local"
	S3_URL="$S3_BUCKET/$INSTALL_FILE"
	aws s3 cp s3://$S3_URL $HOME_PATH
	chmod +x $INSTALL_F
else
	echo ">>>>>> Install file already exist"
fi

# change permission to give access to the app
chmod 777 $REBOOT_NUM $INSTANCE_FILE $AMI_IDX $IP_ADDR

# start tomcat
echo ">>>>>> Start tomcat service"
service tomcat8 start
