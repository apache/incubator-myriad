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

set -e

echo "10.141.141.20" > /etc/mesos-slave/ip
echo "cgroups/cpu,cgroups/mem" > /etc/mesos-slave/isolation
echo "mesos" > /etc/mesos-slave/containerizers
echo "/usr/local/hadoop" > /etc/mesos-slave/hadoop_home

echo "export HADOOP_HOME=/usr/local/hadoop" >> /root/.bashrc
echo "export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64" >> /root/.bashrc

echo "zk://127.0.0.1:2181/mesos" | sudo tee /etc/mesos/zk
echo "localhost" | sudo tee /etc/mesos-slave/hostname
echo "cpus:2;mem:2048" | sudo tee /etc/mesos-slave/resources
echo manual | sudo tee /etc/init/mesos-master.override
echo manual | sudo tee /etc/init/zookeeper.override


start mesos-slave
