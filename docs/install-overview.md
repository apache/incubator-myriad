# Installation Overview #

## System Requirements ##

* JDK 1.7+
* Gradle
* Hadoop 2.7.1

## Operating Systems ##

* RedHat/CentOS 6.x and 7.x
* Ubuntu 12.04, 14.04, 15.04
* Debian 8

## Myriad Requirements ##

* Hadoop HDFS
* Zookeeper
* Mesos with Marathon (if using Marathon for monitoring, otherwise, just Mesos). See Mesosphere Open Source.
* Mesos-DNS (Used for High Availability)
* Myriad


## Ports Used ##

* Marathon -- 8080
* Mesos -- 5050
* Myriad -- 8192 

**Note:** If your environment has both Marathon and Spark installed on the same node, a conflict occurs because the default port for both is 8080. To resolve this conflict, change the port for one of the applications.
 
## General Tasks ##

The following is an overview of the general installation and configuration tasks needed for setting up and configuring Myriad:

1. Download HDFS binaries on your node(s) and set $JAVA_HOME.
2. Install Mesos and Marathon (if using Marathon for monitoring) on your master node.
3. Install zookeeper on your master node and configure mesos-master.
4. Install Mesos on your slave nodes and create the cluster.
5. Download Myriad binaries and build Myriad.
6. Configure HDFS to use Myriad. For example, properties in the Hadoop yarn-site.yml file. The files that you modify depends on the environment that you set up.
Install and configure Mesos-DNS for Myriad High Availability (if using HA).


## Myriad Components ##

* **Myriad Scheduler** - This component plugs into Resource Manager process and negotiates resources from Mesos by implementing Mesos Scheduler and SchedulerDriver interfaces. It is responsible to launch Node Manager processes via Mesos.
* **Myriad Executor** - This component plugs into the NodeManager process via the YARN AuxiliaryService interface.

The project is a multi-project build using gradle. You can go to the $PROJECT_HOME (which is where you cloned the Git project to) and type `./gradlew build` to build all projects or you can go into each individual project and build them separately.
