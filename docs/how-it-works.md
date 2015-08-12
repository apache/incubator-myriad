# How it Works?

## Introduction

Myriad allows Mesos and YARN to co-exist and share resources with Mesos as the resource manager for the datacenter. Sharing resources between these two resource allocation systems improves overall cluster utilization and avoids statically partitioning resources amongst two separate clusters and resource managers.

The following diagram shows two resource managers running independently which results in a statically partitioned datacenter:

![Static Partition](images/static-partition.png)

## Advertising Resources: Mesos Slave and YARN’s Node Manager

The Mesos Slave and YARN’s Node Manager are processes that run on the host OS. Both processes advertise available resources to Mesos Master and YARN’s Resource Manager respectively. Each process can be configured to advertise a subset of resources. This ability is leveraged, in conjunction with cgroups, to allow Mesos Slave and YARN’s Node Manager to co-exist on a node. 

The following diagram showcases a node running YARN NodeManager as a Mesos Slave task:

![Node](images/node.png)

* Mesos Slave processes advertises all of a node’s resources (8 CPUs, 16 GB RAM) to Mesos Master. 
* The YARN Node Manager is started as a Mesos Task. This task is allotted (4 CPUs and 8 GB RAM) and the Node Manager is configured to only advertise 3 CPUs and 7 GB RAM. 
* The Node Manager is also configured to mount the YARN containers under the  [cgroup hierarchy](cgroups.md)  which stems from a Mesos task. 

For example:


```bash
/sys/fs/cgroup/cpu/mesos/node-manager-task-id/container-1
```


## High Level Design

One way to avoid static partitioning and to enable resource sharing when running two resource managers, is to let one resource manager be in absolute control of the datacenter’s resources. The other resource manager then manages a subset of resources, allocated to it through the primary resource manager. 

The following diagram shows a scenario where Mesos is used as the resource manager for the datacenter which allows both  Mesos and YARN to schedule tasks on any node.

![Generic Nodes](images/generic-nodes.png)

The following diagram gives an overview of how YARN can run along side Mesos:

![How it works](images/how-it-works.png)

Each node in the cluster has both daemons, Mesos Slave and YARN Node Manager, installed. By default, the Mesos slave daemon is started on each node and advertises all available resources to the Mesos Master.

Myriad launches NodeManager as a task under Mesos Slave:

1. Myriad makes a decision to launch a new NodeManager. 
	* It passes the required configuration and task launch information to the Mesos Master which forwards that to the Mesos Slave(s).
	* Mesos Slave launches Myriad Executor which manages the lifecycle of the NodeManager.
	* Myriad Executor upon launch, configures Node Manager (for example, specifying CPU and memory to advertise, cgroups hierarchy, and so on) and then launches it. For example: In the previous diagram, Node Manager is allotted 2.5 CPU and 2.5 GB RAM.
2. NodeManager, upon startup, advertises configured resources to YARN's Resource Manager. In the previous example, 2 CPU and 2 GB RAM are advertised. The rest of the resources are used by the Myriad Executor and NodeManager processes to run.
3. YARN's Resource Manager can launch containers now, via this Node Manager. The launched containers are mounted under the configured cgroup hierarchy. See [cgroups doc](cgroups.md) for more information.
