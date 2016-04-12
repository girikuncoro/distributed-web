#!/bin/bash

HOME_PATH="/var/tmp/"
REBOOT_FILE=rebootNum.txt

# get current reboot number
REBOOT_NUM="$(cat $HOME_PATH$REBOOT_FILE)"

# increment reboot number by 1
REBOOT_NUM=$((REBOOT_NUM+1))

# rewrite rebootNum.txt
echo $REBOOT_NUM > "$HOME_PATH$REBOOT_FILE"

# restart tomcat
sudo service tomcat8 stop
sudo service tomcat8 start