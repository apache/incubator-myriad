#!/bin/bash -v
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

echo "zk://127.0.0.1:2181/mesos" > /etc/mesos/zk
#echo "$@ master" >> /etc/hosts
echo "10.141.141.20 master" >> /etc/hosts
echo 10.141.141.20 | sudo tee /etc/mesos-master/ip
echo master | sudo tee /etc/mesos-master/hostname

echo "export HADOOP_HOME=/usr/local/hadoop" >> /root/.bashrc
echo "export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64" >> /root/.bashrc
echo "export PATH=$PATH:$HADOOP_HOME/bin:$JAVA_HOME/bin" >> /root/.bashrc

# keep mesos slave from starting here
echo manual | sudo tee /etc/init/mesos-slave.override

sudo start mesos-master
