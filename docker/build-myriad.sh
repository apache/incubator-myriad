#!/bin/bash
#
# init.sh 
#
# Builds myriad and prepares temp directories for building a myriad docker image.
# usage: init.sh <--skipBuild>
#
 
if [ ! "$1" == "--skipBuild" ]; then
	../gradlew build -b ../build.gradle
fi

# Create the temp directories
if [ ! -d "config" ]; then
	mkdir config
fi

if [ ! -d "libs" ]; then
	mkdir libs
fi

# Copy over the Java Libraries
cp -rf ../myriad-scheduler/build/libs/* libs/
cp -rf ../myriad-executor/build/libs/myriad-executor-runnable-0.0.1.jar libs/

echo "Init complete! " #Modify config/myriad-default-config.yml to your liking before building the docker image"
