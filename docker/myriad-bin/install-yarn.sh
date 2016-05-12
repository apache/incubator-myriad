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
##
# YARN installer script for Apache Myriad Deployment
##
export HADOOP_VER=${HADOOP_VER:='2.7.0'}
export HADOOP_HOME=${HADOOP_HOME:='/usr/local/hadoop'}

echo "Installing Yarn...."
if [ ! -z $1 ];then
  HADOOP_URL=$1
else
  HADOOP_URL=http://apache.osuosl.org/hadoop/common/hadoop-${HADOOP_VER}/hadoop-${HADOOP_VER}.tar.gz
fi
HADOOP_TGZ=`basename ${HADOOP_URL}`
HADOOP_BASENAME=`basename ${HADOOP_URL} .tar.gz`

# Extract Hadoop
echo "Downloading ${HADOOP_TGZ} from ${HADOOP_URL}"
wget ${HADOOP_URL}
tar xzf ${HADOOP_TGZ} -C /tmp
mv /tmp/${HADOOP_BASENAME} ${HADOOP_HOME}
#Remove tarball
rm -f ${HADOOP_TGZ}

# Link Mesos Libraries
echo "export JAVA_HOME=/usr" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh
echo "export MESOS_NATIVE_JAVA_LIBRARY=/usr/local/lib/libmesos.so" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh
# Ensure the hadoop-env is executable
chmod +x ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh
echo "end of install-yarn.sh script"
