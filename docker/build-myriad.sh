#!/bin/bash
#
# init.sh 
#
# Builds myriad and prepares temp directories for building a myriad docker image.
# usage: init.sh <--skipBuild>
#
 
if [ ! "$1" == "--skipBuild" ]; then
	../gradlew build -b ../build.gradle
	../gradlew build capsuleExecutor -b ../build.gradle
fi

# Create the temp directories
if [ ! -d "config" ]; then
	mkdir config
fi

if [ ! -d "libs" ]; then
	mkdir libs
fi

# Copy over the myriad config
# TODO: this sucks. we need to put this in a distributed store to be pulled down automatically
#cp ../src/main/resources/myriad-config-default.yml config/
#cp ../src/main/resources/yarn-site-default.xml config/yarn-site.xml

# Copy over the Java Libraries
cp -rf ../build/libs/* libs/

echo "Init complete! " #Modify config/myriad-default-config.yml to your liking before building the docker image"
