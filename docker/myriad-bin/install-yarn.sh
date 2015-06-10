#!/bin/bash

# YARN installer script for Apache Myriad Deployment
# VERSION 0.0.1

yum install -y tar curl

HADOOP_VER=2.5.2

# Put in env defaults if they are missing
export HADOOP_GROUP=${HADOOP_GROUP:='hadoop'}
export HADOOP_USER=${HADOOP_USER:='hduser'}
export HADOOP_HOME=${HADOOP_HOME:='/usr/local/hadoop'}


# Add hduser user
groupadd $HADOOP_GROUP
useradd $HADOOP_USER -g $HADOOP_GROUP
mkdir /home/${HADOOP_USER}
chown -R $HADOOP_USER:$HADOOP_GROUP /home/${HADOOP_USER}

# Extract Hadoop
tar vxzf hadoop-${HADOOP_VER}.tar.gz -C /tmp
mv /tmp/hadoop-${HADOOP_VER} ${HADOOP_HOME}
chown -R ${HADOOP_USER}:${HADOOP_GROUP} ${HADOOP_HOME}

# Init bashrc with hadoop env variables
sh -c 'echo export JAVA_HOME=/usr >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export PATH=\$PATH:\${HADOOP_HOME}/bin >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export PATH=\$PATH:\${HADOOP_HOME}/sbin >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_MAPRED_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_COMMON_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_HDFS_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export YARN_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_COMMON_LIB_NATIVE_DIR=\$\{HADOOP_HOME\}/lib/native >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_OPTS=\"-Djava.library.path=\${HADOOP_HOME}/lib\" >> /home/${HADOOP_USER}/.bashrc'


# Link Mesos Libraries
echo "export MESOS_NATIVE_JAVA_LIBRARY=/usr/local/lib/libmesos.so" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh

echo "end of install-yarn.sh script"
