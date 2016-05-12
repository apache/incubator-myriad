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

# Put in env defaults if they are missing
export YARN_USER=${YARN_USER:='yarn'}
export YARN_UID=${YARN_UID:='107'}
export YARN_GROUP=${YARN_GROUP:='yarn'}
export HADOOP_GROUP=${HADOOP_GROUP:='hadoop'}
export HADOOP_GID=${HADOOP_GID:='112'}
export YARN_GID=${YARN_GID:='113'}
export HADOOP_HOME=${HADOOP_HOME:='/usr/local/hadoop'}
export MESOS_NATIVE_LIBRARY=${MESOS_NATIVE_LIBRARY:='/usr/local/lib/libmesos.so'}

# Add hduser user
groupadd ${HADOOP_GROUP} -g ${HADOOP_GID}
groupadd ${YARN_GROUP} -g ${YARN_GID}
useradd ${YARN_USER} -g ${YARN_GROUP} -G ${HADOOP_GROUP} -u ${YARN_UID} -s /bin/bash
mkdir /home/${YARN_USER}
chown -R ${YARN_USER}:${YARN_GROUP} /home/${YARN_USER}

#set permissions
chown -R root:root ${HADOOP_HOME}
chmod -R g-w /usr/local/
chown root:${YARN_GROUP} ${HADOOP_HOME}/bin/container-executor
chmod 6050 ${HADOOP_HOME}/bin/container-executor

# Init bashrc with hadoop env variables
sh -c 'echo export JAVA_HOME=/usr >> /home/${YARN_USER}/.bashrc'
sh -c 'echo export MESOS_NATIVE_LIBRARY=/usr >> /home/${YARN_USER}/.bashrc'
sh -c 'echo export HADOOP_HOME=\${HADOOP_HOME} >> /home/${YARN_USER}/.bashrc'
sh -c 'echo export PATH=\$PATH:\${HADOOP_HOME}/bin >> /home/${YARN_USER}/.bashrc'
sh -c 'echo export PATH=\$PATH:\${HADOOP_HOME}/sbin >> /home/${YARN_USER}/.bashrc'
sh -c 'echo export HADOOP_MAPRED_HOME=\${HADOOP_HOME} >> /home/${YARN_USER}/.bashrc'
sh -c 'echo export HADOOP_COMMON_HOME=\${HADOOP_HOME} >> /home/${YARN_USER}/.bashrc'
sh -c 'echo export HADOOP_HDFS_HOME=\${HADOOP_HOME} >> /home/${YARN_USER}/.bashrc'
sh -c 'echo export YARN_HOME=\${HADOOP_HOME} >> /home/${YARN_USER}/.bashrc'
sh -c 'echo export HADOOP_COMMON_LIB_NATIVE_DIR=\$\{HADOOP_HOME\}/lib/native >> /home/${YARN_USER}/.bashrc'
sh -c 'echo export HADOOP_OPTS=\"-Djava.library.path=\${HADOOP_HOME}/lib\" >> /home/${YARN_USER}/.bashrc'

echo "end of configure-yarn.sh script"
