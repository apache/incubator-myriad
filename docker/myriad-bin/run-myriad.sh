#!/bin/bash

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

# Ensure that the hadoop user is specified
HADOOP_USER=${HADOOP_USER:='hduser'}

su - $HADOOP_USER /usr/local/hadoop/bin/yarn resourcemanager
