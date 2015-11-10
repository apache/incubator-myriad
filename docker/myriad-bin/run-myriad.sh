#!/bin/bash
: '
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
'


echo "Starting Myriad..."

if [ ! -z $ALTERNATE_HADOOP_URL ]; then
  echo "OVERIDING URL"
  exec /install-yarn.sh $ALTERNATE_HADOOP_URL
  unset $ALTERNATE_HADOOP_URL
fi

export MYRIAD_CONFIG_FILE="/myriad-conf/myriad-config-default.yml"
export YARN_SITE="/myriad-conf/yarn-site.xml"

# modify the core-site.xml file within the docker
sed -i.bak "s=<configuration>=<configuration>\<property>\<name>fs\.default\.name\</name>\<value>hdfs://${HADOOP_NAMENODE}\</value>\</property>=g" /usr/local/hadoop/etc/hadoop/core-site.xml

# Copy the configs over
cp /myriad-conf/* /usr/local/hadoop/etc/hadoop/

# Ensure myriad conf file is present
if [ ! -f $MYRIAD_CONFIG_FILE ]; then
    echo "[FATAL] Myriad config not found! - ${MYRIAD_CONFIG_FILE}"
    exit 1
fi
if [ ! -f $YARN_SITE ]; then
    echo "[FATAL] yarn-site.xml config not found! - ${YARN_SITE}"
    exit 1
fi

if [ -z $HADOOP_NAMENODE ]; then
    echo "[FATAL] HADOOP_NAMENODE env variable not set!"
    exit 1
fi

#su - $HADOOP_USER /usr/local/hadoop/bin/yarn resourcemanager
/usr/local/hadoop/bin/yarn resourcemanager
