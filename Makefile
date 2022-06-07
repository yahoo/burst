SHELL=/bin/bash
ROOT=/home/y
YAHOO_CFG=$(ROOT)/share/yahoo_cfg

include $(YAHOO_CFG)/screwdriver/Make.rules

screwdriver: build
	echo "screwdriver"

build:
	mvn versions:set -DnewVersion=`git_auto_version -p v.`
	mvn -Dscrewdriver -B clean package

publish:
	mvn -Dscrewdriver -B deploy
