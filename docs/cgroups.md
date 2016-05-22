# Configuring Cgroups
** CGroups are currently not supported when using Docker, we are aware this feature may be of interest and will add soon ** 
A node within a cluster is the location where tasks are run. The tasks are launched with help of a daemon which resides inside the node. This daemon, in the case of Mesos, is Mesos Slave, and in case of YARN, is NodeManager.

The Cgroups Linux kernel feature allows aggregating or partitioning a set of tasks and their future children into hierarchical groups with respect to one or more subsystems. For example, when Cgroups is enabled for the CPU subsystem, and a task is launched by Mesos Slave, it goes under the following hierarchy:

```bash
/sys/fs/cgroup/cpu/mesos/<parent-task-id>
```

During the lifecycle of this task, if it launches one or more children, they get mounted under the parent task’s hierarchy and can be configured to only use as much resources as the parent task is allowed to:

```bash
/sys/fs/cgroup/cpu/mesos/<parent-mesos-id>/hadoop-yarn/<child-yarn-id-1>
/sys/fs/cgroup/cpu/mesos/<parent-mesos-id>/hadoop-yarn/<child-yarn-id-2>
```

![cgroups hierarchy](images/cgroups.png)

## Enabling Cgroups for mesos-slave
To enable cgroups for mesos-slave, start the slave with following flag:

```bash
--isolation=cgroups/cpu,cgroups/mem
```

## Enabling Cgroups for YARN NodeManager

Enabling Cgroups for YARN NodeManager involves:
* Modifying the $YARN_HOME/etc/hadoop/myriad-config-default.yml file.
* Modifying the $YARN_HOME/etc/hadoop/yarn-site.xml file.

### Modify container-executor.cfg
```
yarn.nodemanager.linux-container-executor.group=yarn #should match yarn.nodemanager.linux-container-executor.group in yarn-site.xml
banned.users=
min.user.id=1000
```
### Verify Permissions

The paths to container-executor.cfg and container-executor must be owned and writable only by root.  The container-executor 
should have user-ownership by root and group ownership by the user running YARN (often yarn or hduser), which should match the 
yarn.nodemanager.linux-container-executor.group in yarn-site.xml and yarn.nodemanager.linux-container-executor.group in 
container-executor.cfg. Further the permission of container-executor should be r-Sr-s---.  
```
chmod 6050 container-executor
```
If using remote distribution be sure to use the -p option of tar (as root) to perserve the suid bit.

### Modify Myriad-Config-default.yml ###

Modify the $YARN_HOME/etc/hadoop/myriad-config-default.yml file by adding the following content:

```
...
frameworkSuperUser: root  # Must be root or have passwordless sudo.
nodemanager:
  cgroupPath: /path/to/cgroup # default is /sys/fs/cgroup
...
```

### Modify yarn-site.yml
Modify the `$YARN_HOME/etc/hadoop/yarn-site.xml` file by adding the following content:

```xml
<!-- Cgroups configuration -->
<property>
<description>who will execute(launch) the containers.</description>
<name>yarn.nodemanager.container-executor.class</name>
<value>org.apache.hadoop.yarn.server.nodemanager.LinuxContainerExecutor</value>
</property>
<property>
<description>The class which should help the LCE handle resources.</description>
<name>yarn.nodemanager.linux-container-executor.resources-handler.class</name>
<value>org.apache.hadoop.yarn.server.nodemanager.util.CgroupsLCEResourcesHandler</value>
</property>
<property>
<name>yarn.nodemanager.linux-container-executor.group</name>
<value>yarn</value>
</property>
<property>
<name>yarn.nodemanager.linux-container-executor.path</name>
<value>${yarn.home}/bin/container-executor</value>
</property>
<property>
<name>yarn.nodemanager.linux-container-executor.cgroups.hierarchy</name>
<value>${yarn.nodemanager.linux-container-executor.cgroups.hierarchy}</value>
</property>

<!-- Optional parameters, usually unnecessary
<property>
<name>yarn.nodemanager.linux-container-executor.cgroups.mount</name>
<value>true</value>
</property>
<property>
<name>yarn.nodemanager.linux-container-executor.cgroups.mount-path</name>
<value>/sys/fs/cgroup</value>
<description>/sys/fs/cgroup and /cgroup are most common values</description>
</property>
-->
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

