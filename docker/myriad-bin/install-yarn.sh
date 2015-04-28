#!/bin/bash

#ENVIRONMENT VARS
#HADOOP_NAMENODE="10.100.3.237:9000"

# YARN installer script for Apache Myriad Deploment
# VERSION 0.0.1

yum install -y tar curl

HADOOP_VER=2.5.2

# Add hduser user
groupadd hadoop
#adduser --ingroup hadoop hduser
useradd hduser -g hadoop

# Download Hadoop
#cd ~
#if [ ! -f hadoop-${HADOOP_VER}.tar.gz ]; then
#wget http://apache.osuosl.org/hadoop/common/hadoop-${HADOOP_VER}/hadoop-${HADOOP_VER}.tar.gz
#fi

tar vxzf hadoop-${HADOOP_VER}.tar.gz -C /usr/local
cd /usr/local
mv hadoop-${HADOOP_VER} hadoop
chown -R hduser:hadoop hadoop

# Init bashrc with hadoop env variables
sh -c 'echo export JAVA_HOME=/usr >> /home/hduser/.bashrc'
sh -c 'echo export HADOOP_INSTALL=/usr/local/hadoop >> /home/hduser/.bashrc'
sh -c 'echo export PATH=\$PATH:\$HADOOP_INSTALL/bin >> /home/hduser/.bashrc'
sh -c 'echo export PATH=\$PATH:\$HADOOP_INSTALL/sbin >> /home/hduser/.bashrc'
sh -c 'echo export HADOOP_MAPRED_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
sh -c 'echo export HADOOP_COMMON_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
sh -c 'echo export HADOOP_HDFS_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
sh -c 'echo export YARN_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
sh -c 'echo export HADOOP_COMMON_LIB_NATIVE_DIR=\$\{HADOOP_INSTALL\}/lib/native >> /home/hduser/.bashrc'
sh -c 'echo export HADOOP_OPTS=\"-Djava.library.path=\$HADOOP_INSTALL/lib\" >> /home/hduser/.bashrc'


# Setup Hadoop/YARN Configs
#<configuration><property><name>fs.default.name</name><value>hdfs://localhost:9000</value></property>
#sed -i.bak "s=<configuration>=<configuration>\<property>\<name>fs\.default\.name\</name>\<value>hdfs://${HADOOP_NAMENODE}\</value>\</property>=g" /usr/local/hadoop/etc/hadoop/core-site.xml
cat /usr/local/hadoop/etc/hadoop/core-site.xml
#runuser -l hduser "./usr/local/hadoop/bin/yarn resourcemanager"

echo "export MESOS_NATIVE_JAVA_LIBRARY=/usr/local/lib/libmesos.so" >> /usr/local/hadoop/etc/hadoop/hadoop-env.sh

echo "end of install-yarn.sh script"
