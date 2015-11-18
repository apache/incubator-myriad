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
# Put in env defaults if they are missing
export HADOOP_GROUP=${HADOOP_GROUP:='hadoop'}
export HADOOP_USER=${HADOOP_USER:='yarn'}
export HADOOP_HOME=${HADOOP_HOME:='/usr/local/hadoop'}
export USER_UID=${USER_UID:='113'}
export GROUP_UID=${GROUP_GID:='112'}

# Add hduser user
echo "Creating $HADOOP_USER user.."
groupadd $HADOOP_GROUP -g ${GROUP_UID}
useradd $HADOOP_USER -g $HADOOP_GROUP -u ${USER_UID} -s /bin/bash -d /home/${HADOOP_USER}
mkdir /home/${HADOOP_USER}
chown -R $HADOOP_USER:$HADOOP_GROUP /home/${HADOOP_USER}

echo "end of create-user.sh script"
