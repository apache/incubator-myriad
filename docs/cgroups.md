# Configuring Cgroups

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

### Modify Myriad-Config-default.yml ###

Modify the $YARN_HOME/etc/hadoop/myriad-config-default.yml file by adding the following content:

```
...
nodemanager:
cgroups: true
...
```


### Modify yarn-site.yml
Modify the `$YARN_HOME/etc/hadoop/yarn-site.xml` file by adding the following content:

```xml
<!-- Cgroups configuration -->
<property>
<description>who will execute(launch) the containers.</description>
<name>yarn.nodemanager.container-executor.class</name>
<value>${yarn.nodemanager.container-executor.class}</value>
</property>
<property>
<description>The class which should help the LCE handle resources.</description>
<name>yarn.nodemanager.linux-container-executor.resources-handler.class</name>
<value>${yarn.nodemanager.linux-container-executor.resources-handler.class}</value>
</property>
<property>
<name>yarn.nodemanager.linux-container-executor.cgroups.hierarchy</name>
<value>${yarn.nodemanager.linux-container-executor.cgroups.hierarchy}</value>
</property>
<property>
<name>yarn.nodemanager.linux-container-executor.cgroups.mount</name>
<value>${yarn.nodemanager.linux-container-executor.cgroups.mount}</value>
</property>
<property>
<name>yarn.nodemanager.linux-container-executor.cgroups.mount-path</name>
<value>${yarn.nodemanager.linux-container-executor.cgroups.mount-path}</value>
</property>
<property>
<name>yarn.nodemanager.linux-container-executor.group</name>
<value>${yarn.nodemanager.linux-container-executor.group}</value>
</property>
<property>
<name>yarn.nodemanager.linux-container-executor.path</name>
<value>${yarn.home}/bin/container-executor</value>
</property>
```


