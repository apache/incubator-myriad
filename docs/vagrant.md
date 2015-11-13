# Installing using Vagrant

This section provides information for setting up a cluster in a virtual machine. Setting up a cluster in a virtual machine involves the following:


## Prerequisities
* Virtualbox
* Vagrant

## Starting the Cluster ##

To start the cluster run following:

```
vagrant up
```

At this point the VM will have a single node Mesos cluster running and a single node
HDFS cluster running. You can modify the Mesos and Hadoop versions by editing the
corresponding variables in the `Vagrantfile`.

Check that Mesos and Hadoop are running with a browser:

[Hadoop/HDFS namenode](http://10.141.141.20:50070)
[Mesos Master](http://10.141.141.20:5050/)

To ssh in the cluster, run following:

```
vagrant ssh
```

The password for vagrant user is **vagrant** if prompted
You can switch to the hadoop user when needed to launch hadoop processes

```
sudo su - hduser
```

## Configuring Myriad ##

### Step 1: Build Myriad ###

To build Myriad Scheduler inside VM, run the gradlew build:

```
cd /vagrant
./gradlew build
```

**NOTE:** If build failure failure occurs, the issue is not with the build itself, but a failure to write to disk.  This can happen when you built outside the vagrant instance first.  Exit the user `hduser` by typing `exit` and build again as the `vagrant` user.

### Step 2: Deploy the Myriad Files ###

The Myriad Schedule and Executer jar files and all the runtime dependences as well as the Myriad configuration file must be copied to $YARN_HOME.

* The Myriad Scheduler jar and all the runtime dependencies are located at:

```
/vagrant/myriad-scheduler/build/libs/*
```
* The Myriad configuration file is located at:

```
/vagrant/myriad-scheduler/src/main/resources/myriad-config-default.yml
```

* The Myriad Executor jar file are located at:

```
/vagrant/myriad-scheduler/build/libs/myriad-executor-0.1.0.jar
```

For example, the files are copied to the following locations:

```
cp /vagrant/myriad-scheduler/build/libs/* $YARN_HOME/share/hadoop/yarn/lib/
cp /vagrant/myriad-executor/build/libs/myriad-executor-0.1.0.jar $YARN_HOME/share/hadoop/yarn/lib/
cp /vagrant/myriad-scheduler/src/main/resources/myriad-config-default.yml $YARN_HOME/etc/hadoop/
```


## Step 3: Configure the Myriad Defaults ##

 As a minimum, the following Myriad configuration parameters must be set:

* mesosMaster
* zkServers
* YARN_HOME

**NOTE:** Enabling Cgroups involves modifying the **yarn-site.xml** and **myriad-config-default.yml** files. If you plan on using Cgroups, you could set that property at this time. See [Configuring Cgroups](cgroups.md) for more information.

To configure Myriad itself, update **$YARN_HOME/etc/hadoop/myriad-default-config.yml** with the following content:

```yml
mesosMaster: <mesos Master IP address>:5050
checkpoint: false
frameworkFailoverTimeout: 43200000
frameworkName: MyriadAlpha
nativeLibrary: /usr/local/lib/libmesos.so
zkServers: localhost:2181
zkTimeout: 20000
profiles:
  small:
    cpu: 2
    mem: 2048
  medium:
    cpu: 4
    mem: 4096
  large:
    cpu: 10
    mem: 12288
rebalancer: true
nodemanager:
  jvmMaxMemoryMB: 1024
  user: hduser
  cpus: 0.2
  cgroups: false
executor:
  jvmMaxMemoryMB: 256
  path: file://localhost/usr/local/libexec/mesos/myriad-executor-runnable-0.1.0.jar
```


### Step 4: Configure YARN to Use Myriad ###

To configure YARN to use Myriad, update **$YARN_HOME/etc/hadoop/yarn-site.xml** with following content:

```xml
<property>
<name>yarn.nodemanager.resource.cpu-vcores</name>
<value>${nodemanager.resource.cpu-vcores}</value>
</property>
<property>
<name>yarn.nodemanager.resource.memory-mb</name>
<value>${nodemanager.resource.memory-mb}</value>
</property>
<!-- The following properties enable dynamic port assignment by Mesos -->
<property>
    <name>yarn.nodemanager.address</name>
    <value>${myriad.yarn.nodemanager.address}</value>
</property>
<property>
    <name>yarn.nodemanager.webapp.address</name>
    <value>${myriad.yarn.nodemanager.webapp.address}</value>
</property>
<property>
    <name>yarn.nodemanager.webapp.https.address</name>
    <value>${myriad.yarn.nodemanager.webapp.address}</value>
</property>
<property>
    <name>yarn.nodemanager.localizer.address</name>
    <value>${myriad.yarn.nodemanager.localizer.address}</value>
</property>
<!-- The following properties configure Myriad Scheduler  -->
<property>
<name>yarn.resourcemanager.scheduler.class</name>
<value>org.apache.myriad.scheduler.yarn.MyriadFairScheduler</value>
<description>One can configure other schedulers as well from following list: org.apache.myriad.scheduler.yarn.MyriadCapacityScheduler, org.apache.myriad.scheduler.yarn.MyriadFifoScheduler</description>
</property>
```


## Getting Started ##

### Launching
To launch Myriad, run the following:

```
sudo su hduser
yarn-daemon.sh start resourcemanager
```

### Verifying Activity
To check that things are running, from a browser on the host check out the following urls:

* Myriad UI - Use the 8192 port. For example: <pre> http://<IP address>:8192/ (http://10.141.141.20:8192/)</pre>
	* Without the Mesos-DNS service, this IP is slave node's IP address where the ResourceManager is running.
	* With the Mesos-DNS service, Mesos-DNS discovers the node where the ResourceManager is running by using Mesos DNS FQDN (<app>.<framework>.mesos).
* Mesos UI - Use the 5050 port and Frameworks directory. For example: <pre>http://<IP address>:5050/#/frameworks (http://10.141.141.20:5050/#/frameworks).</pre>

### Shutting Down ###

To shut down, from the vagrant ssh console, run the following:

```
yarn-daemon.sh stop resourcemanager
sh ./vagrant/shutdown.sh
exit
exit
vagrant halt
```
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
