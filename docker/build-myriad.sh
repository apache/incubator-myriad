#!/bin/bash
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
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
cp -rf ../myriad-executor/build/libs/* libs/
echo "Init complete! " #Modify config/myriad-default-config.yml to your liking before building the docker image"
