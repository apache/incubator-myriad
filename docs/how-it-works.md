# How it works ?

### Advertising resources: Mesos Slave and YARN’s Node Manager

Mesos Slave and YARN’s Node Manager are processes that run on the host OS, both advertises available resources to Mesos Master and YARN’s Resource Manager respectively. Each process can be configured to advertise a subset of resources. We can leverage this ability, in conjunction with cgroups, to allow Mesos Slave and YARN’s Node Manager to co-exist on a node. The diagram below showcases a node running YARN NodeManager as a Mesos Slave task:

![Node](images/node.png)

Let Mesos Slave be the processes that advertises all of a node’s resources (8 CPUs, 16 GB RAM) to Mesos Master. Now, let's start YARN Node Manager as a Mesos Task. This task is allotted (4 CPUs and 8 GB RAM), and the Node Manager is configured to only advertise 3 CPUs and 7 GB RAM. The Node Manager is also configured to mount the YARN containers under the [cgroup hierarchy](cgroups.md) which stems from a Mesos task. Ex:

```bash
/sys/fs/cgroup/cpu/mesos/node-manager-task-id/container-1
```

Doing above allows Mesos Slave and Node Manager to co-exist on the same node, in a non-intrusive way. Our architecture leverages this strategy; we'll explore how this fits into the bigger picture.

### High Level Design

One way to avoid static partitioning and to enable resource sharing when running two resource managers, is to let one resource manager be in absolute control of datacenter’s resources. The other resource manager then manages a subset of resources, allocated to it through the primary resource manager. Let's consider a scenario where Mesos is used as the resource manager for the datacenter. In the diagram below, both, Mesos and YARN, can schedule tasks on any node.

![Generic Node](images/generic-node.md)
