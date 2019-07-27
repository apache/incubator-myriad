# Plain Apache Mesos Environment

This is the Vagrant-libvirt based environment for testing Apache Myriad
project in a Mesos cluster.

# Setup the environment

In order to use this vagrant environment we have to enable a suitable
environment. The key points of this environment are:

- **MESOS_ARCH**: This variable has to match with the VMs architecture defined in
  the config folder: *cluster-1m4a.yaml*, *cluster-3m4a.yaml*, and so on. For
  example **MESOS_ARCH: 1m4a** matchs with the architecture *cluster-1m4a.yaml*, which
  is a Mesos master node (1m), and four Mesos agents (4a).

- **HADOOP_VERSION**: This variable is used for downloading a binary distribution
  of Apache HAdoop.

- **ZOOKEEPER_VERSION**: For dowloading an Apache Zookeeper binary distribution.

```
$ source setup-env 

This Vagrant environment is ready for the following settings:                         

- MESOS_ARCH: 1m4a                         
- HADOOP_VERSION: 2.7.0                    
- ZOOKEEPER_VERSION: 3.4.11                

'vagrant up --provider=libvirt' and happy hacking!  
``` 

# Running the deployment

The VMs provisioning is based on Ansible, the provisioning is guided by the
main playbook **provisioning/site.yml** and one special **provisioing/inventory.py**
for creating a dynamic inventory. This dynamic inventory is created for
managing the Ansible provisioning with a multi-machine Vagrant environment,
because the Vagrant Ansible support is sometime problematic.

```
$ vagrant up  --provider=libvirt
[...]
PLAY RECAP
*********************************************************************
build                      : ok=36   changed=28   unreachable=0    failed=0   
mesos-a1                   : ok=28   changed=25   unreachable=0    failed=0   
mesos-a2                   : ok=31   changed=28   unreachable=0    failed=0   
mesos-a3                   : ok=31   changed=28   unreachable=0    failed=0   
mesos-a4                   : ok=31   changed=28   unreachable=0    failed=0   
mesos-m1                   : ok=41   changed=38   unreachable=0    failed=0 
```

## Final VMs set

```
$ vagrant status   
Current machine states:

build                     running (libvirt)
mesos-m1                  running (libvirt)
mesos-a1                  running (libvirt)
mesos-a2                  running (libvirt)
mesos-a3                  running (libvirt)
mesos-a4                  running (libvirt)
```

The VM dedicated for development is *build*, the development is maded in the
*/opt* folder:

```
$ vagrant ssh build
[vagrant@build ~]$ tree -d -L 1 /opt/
/opt/
├── hadoop
├── mesos
└── myriad
```

# Building the ecosystem

The strategy is to build the target Apache Mesos version from source, and 
build Myriad framework as follow:

## Building Apache Mesos

```
[vagrant@build ~]$ cd /opt/mesos
[vagrant@build ~]$ git checkout tags/1.5.0 -b 1.5.0
[vagrant@build ~]$ ./bootstrap
[vagrant@build ~]$ mkdir build
[vagrant@build ~]$ cd build
[vagrant@build ~]$ ../configure
[vagrant@build ~]$ make
[...]
running install_scripts
creating build/bdist.linux-x86_64/wheel/mesos.native-0.28.1.dist-info/WHEEL
make[2]: Leaving directory `/opt/mesos/build/src'
make[1]: Leaving directory `/opt/mesos/build/src'
```

## Building Myriad Framework

```
[vagrant@build myriad]$ cd /opt/myriad
[vagrant@build myriad]$ ./gradlew build
[...]
:myriad-scheduler:test                     
:myriad-scheduler:check                    
:myriad-scheduler:build                    

BUILD SUCCESSFUL                           

Total time: 3 mins 56.948 secs
```

# Running the ecosystem

The ecosystem was built from **build** VM, however the execution of ecosystem
is carried out from **mesos-m1** VM and mesos agents.

## Running Mesos 3rdparty shipped Zookeeper

We have to run a Zookeeper server, we can use the shipped Zookeeper expecified
at ZOOKEEPER_VERSION, or we can use the already shipped Zookeeper at 3rdparty
Mesos folder. Let see who to run the 3rdparty Zookeeper shipped with Mesos.

```
$ vagrant ssh mesos-m1
[vagrant@mesos-m1 ~]$ cd /opt/mesos/build/3rdparty/zookeeper-3.4.8/
[vagrant@mesos-m1 zookeeper-3.4.5]$ cp conf/zoo_sample.cfg conf/zoo.cfg
[vagrant@mesos-m1 zookeeper-3.4.5]$ echo "server.1=mesos-m1:2888:3888" >> conf/zoo.cfg
[vagrant@mesos-m1 zookeeper-3.4.5]$ bin/zkServer.sh start
[vagrant@mesos-m1 zookeeper-3.4.5]$ echo ruok | nc 127.0.0.1 2181
imok
[vagrant@mesos-m1 ~]$ jps
16290 Jps
16253 QuorumPeerMain
```

## Running Apache Mesos

- At mesos-m1

```
[vagrant@mesos-m1 ~]$ cd /opt/mesos/build
[vagrant@mesos-m1 build]$ sudo ./bin/mesos-master.sh --ip=100.0.10.101 --work_dir=/var/lib/mesos --zk=zk://mesos-m1:2181/mesos --quorum=1
```

- At agents mesos-a[1..4]:

```
[vagrant@mesos-a1 ~]$ cd /opt/mesos/build
[vagrant@mesos-a1 build]$ sudo ./bin/mesos-slave.sh --master=zk://mesos-m1:2181/mesos --work_dir=/var/lib/mesos --resources='mem:4000'
```

*Note*: mesos-slave.sh was changed for newer versions of Mesos for mesos-agent.sh

The Mesos Master web interface can be accessed via the URL:

http://100.0.10.101:5050

## Running Hadoop HDFS

The Ansible provisioning must have Apache HDFS already running in the platform.
For checking we can do the following:

- At mesos-m1 master:

```
[vagrant@mesos-m1 ~]$ su - hdfs
Password: (vagrant is the password)
[hdfs@mesos-m1 ~]$ jps
21249 NameNode
16374 Jps
21448 SecondaryNameNode
[hdfs@mesos-m1 hadoop]$ cd /opt/hadoop
[hdfs@mesos-m1 hadoop]$ bin/hdfs dfsadmin -printTopology
Rack: /default-rack
   100.0.10.103:50010 (mesos-a2)
   100.0.10.104:50010 (mesos-a3)
   100.0.10.105:50010 (mesos-a4)
[hdfs@mesos-m1 hadoop]$ bin/hdfs dfsadmin -report
[...]
```

The NN web interface can be accessed via the URL:

http://100.0.10.101:50070/

*Note*: Hadoop HDFS is running in mesos-a2, mesos-a3 and mesos-a4. 
The Mesos agent mesos-a1 is dedicated for running Mesos tasks not related 
with Hadoop.

*Note*: You can stop or start HDFS manually from master node with:

```
[hdfs@mesos-m1 hadoop]$ cd /opt/hadoop
[hdfs@mesos-m1 hadoop]$ sbin/stop-dfs.sh
[hdfs@mesos-m1 hadoop]$ sbin/start-dfs.sh
```


## Running Hadoop YARN Resource Manager (RM) with Myriad

- At mesos-m1:

You have to insert the following environment variable:

```
[vagrant@mesos-m1 ~]$ grep MESOS /opt/hadoop/etc/hadoop/hadoop-env.sh
export MESOS_NATIVE_JAVA_LIBRARY=/opt/mesos/build/src/.libs/libmesos.so
```

```
cd /opt

# Copy the Myriad Scheduler
cp -vf myriad/myriad-scheduler/build/libs/* hadoop/share/hadoop/yarn/lib/

#  Copy the Myriad Executor
cp -vf myriad/myriad-executor/build/libs/myriad-executor-<version>.jar hadoop/share/hadoop/yarn/lib/

# Hadoop 2.7.x:
$ su - yarn
Password: (vagrant is the password)
[yarn@mesos-m1 ~]$ cd /opt/hadoop/
[yarn@mesos-m1 hadoop]$ sbin/yarn-daemon.sh --config /opt/hadoop/etc/hadoop/ start resourcemanager
[yarn@mesos-m1 hadoop]$ jps                
19283 ResourceManager                      
19519 Jps 
```

The YARN web interface can be accessed via the URL:

http://100.0.10.101:8088

The Myriad web interface can be accessd via the URL:

http://100.0.10.101:8192

# Export local repository to VM environment

For an useful development environment, we can sync a local folder into the VM
named "build". We can export our Myriad local repository by means of the following
environment variable:

export MYRIAD_SOURCES=/home/user/incubator-myriad

This folder will be mounted (NFS mount) from the host machine to the guest
machine ("build" VM in our case).

Vagrant has built-in support to orchestrate the configuration of the NFS server
on the host and guest for you.

Before using synced folders backed by NFS, the host machine must have NFS
server installed. The following an example for a RPM based machine:

```
sudo yum install nfs-utils libnfsidmap

sudo systemctl enable rpcbind
sudo systemctl enable nfs-server

sudo systemctl start rpcbind
sudo systemctl start nfs-server
sudo systemctl start rpc-statd
sudo systemctl start nfs-idmapd

sudo firewall-cmd --permanent --add-service=nfs
sudo firewall-cmd --permanent --add-service=mountd
sudo firewall-cmd --permanent --add-service=rpc-bind
sudo firewall-cmd --reload

sudo firewall-cmd --list-all
```

# Stopping VMs and starting 

When you stop/start the VMs

```
$ vagrant halt
$ vagrant up
```

You have to start manually some of the daemons in order to continue
The operation such as the provisioning phase did it. You have to
follow the following steps:

- Start Zookeeper:

```
$ vagrant ssh mesos-m1
[vagrant@mesos-m1 ~]$ cd /opt/mesos/build/3rdparty/zookeeper-3.4.8/
[vagrant@mesos-m1 zookeeper-3.4.5]$ bin/zkServer.sh start
[vagrant@mesos-m1 zookeeper-3.4.5]$ echo ruok | nc 127.0.0.1 2181
imok
```

- Start Hadoop

```
$ vagrant ssh mesos-m1
[vagrant@mesos-m1 ~]$ su - hdfs (vagrant password)
[hdfs@mesos-m1 ~]$ cd /opt/hadoop
[hdfs@mesos-m1 hadoop]$ sbin/start-dfs.sh
Starting namenodes on [mesos-m1]
mesos-m1: starting namenode, logging to /opt/hadoop/logs/hadoop-hdfs-namenode-mesos-m1.out
mesos-a2: starting datanode, logging to /opt/hadoop/logs/hadoop-hdfs-datanode-mesos-a2.out
mesos-a4: starting datanode, logging to /opt/hadoop/logs/hadoop-hdfs-datanode-mesos-a4.out
mesos-a3: starting datanode, logging to /opt/hadoop/logs/hadoop-hdfs-datanode-mesos-a3.out
Starting secondary namenodes [0.0.0.0] 0.0.0.0: starting secondarynamenode, logging to /opt/hadoop/logs/hadoop-hdfs-secondarynamenode-mesos-m1.out 
[hdfs@mesos-m1 hadoop]$ jps
11872 Jps
11756 SecondaryNameNode
11550 NameNode
```

- Start Mesos: Master at mesos-m1

```
$ vagrant ssh mesos-m1
[vagrant@mesos-m1 ~]$ cd /opt/mesos/build
[vagrant@mesos-m1 build]$ sudo ./bin/mesos-master.sh --ip=100.0.10.101 --work_dir=/var/lib/mesos --zk=zk://mesos-m1:2181/mesos --quorum=1
```

- Start Mesos: Agents at mesos-a[1..4]:

```
[vagrant@mesos-a1 ~]$ cd /opt/mesos/build
[vagrant@mesos-a1 build]$ sudo ./bin/mesos-slave.sh --master=zk://mesos-m1:2181/mesos --work_dir=/var/lib/mesos --resources='mem:4000'
```

- Start YARN Resource Manager:

```
$ vagrant ssh mesos-m1
[vagrant@mesos-m1 ~]$ su - yarn (vagrant password)
[yarn@mesos-m1 ~]$ cd /opt/hadoop
[yarn@mesos-m1 ~]$ sbin/yarn-daemon.sh --config /opt/hadoop/etc/hadoop/ start resourcemanager && tail -f logs/yarn-yarn-resourcemanager-mesos-m1.log
```

# Happy Hacking

At this point you have a local development environment ready for Myriad
Framework hacking!

---
<sub>
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

<sub>
  http://www.apache.org/licenses/LICENSE-2.0

<sub>
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
