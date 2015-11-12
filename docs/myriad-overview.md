# Myriad Overview #

Apache Myriad enables the co-existence of Apache Hadoop and Apache Mesos on the physical infrastructure. By running Hadoop YARN as a Mesos framework, YARN applications and Mesos frameworks can run side-by-side, dynamically sharing cluster resources.

With Apache Myriad, you can:

* Run your operational applications (including those running in Docker) side-by-side with your analytic applications.
* Achieve Hadoop multi-tenancy by provisioning logical Hadoop clusters for each user or group.

## Key Features ##


Key features include:

* YARN running as a Mesos Framework, with resource manager and node managers running inside Mesos containers.

* YARN clusters running on Mesos that can allocate resources in one of the following ways:
	* Static - Administrators can use an API or a GUI to add or remove node managers or auxiliary services like the JobHistoryServer.
	* Fine-grained - Administrators can provision thin node managers that are dynamically resized based on application demand.
* High Availability (HA) and graceful restart of YARN daemons.
* Ability to launch multiple YARN clusters on the same set of nodes.
* Support for YARN FairScheduler and all functionality such as hierarchical queues with weights.
* Ability to deploy YARN Resource Manager using Marathon. This feature leverages Marathon's dynamic scheduling, process supervision, and integration with service discovery (Mesos-DNS).
* Ability to run MapReduce v2 and associated libraries such as Hive, Pig, and Mahout.

## Use Cases ##

### Infrastructure Consolidation ###


Increasingly, organizations are developing microservice-oriented applications using tools like Docker and deploying them using schedulers like Mesos. At the same time, these organizations are processing large amounts of data using tools like Hadoop scheduled by YARN.
Deploying parallel infrastructures can be costly and inefficient. Both front-end web applications and analytics workloads tend to be bursty, having periods of heavy utilization followed by periods of light utilization. This forces IT departments to size each cluster based on peak utilization, letting resources go under-utilized much of the time. Worse, these parallel infrastructures force users to constantly deal with moving data back and forth between clusters.
With Myriad, these organizations can deploy, manage, and monitor a single cluster that supports both Docker-based microservices deployed via Mesos frameworks like Marathon and YARN-based processing applications like MapReduce and Spark. All applications are fully isolated using Linux containers, ensuring that analytics workloads don’t interfere with operational applications or vice versa. With fine-grained scaling, analytics workloads can consume large amounts of available resources when they need them, releasing them back to the shared pool when they are not. In addition, distributed, shared data services can be provisioned on the shared cluster, eliminating data movement between applications and analytics.

### Hadoop Multi-Tenancy ###

As organizations become more reliant on data processing technologies like Hadoop, it is common to encounter “cluster-sprawl” situations, where several different clusters are deployed to support different business groups or different lifecycle stages (such as development, test, and production) where each group or lifecycle stage is running a different version. Each new cluster requires new servers to be purchased and maintained, and large amounts of data to be copied over to support new use cases.
Using Myriad, these organizations can save money and increase agility by provisioning multiple logical Hadoop clusters on a single physical Mesos cluster with either shared or dedicated data services. Each logical cluster can be tailored to the end user, with a custom configuration and security policy, while running a specific version, and with either static or dynamic resources allocated to it.
In a multi-tenant environment, this model means that a shared pool of resources can be shared among many data processing frameworks, with each capable of allocating additional resources when needed and releasing them when not. The top-level Mesos scheduler ensures fairness in the case that multiple frameworks are competing for resources.
In case of a version migration (for example, upgrading only one of two Hadoop clusters), this model means that logical Hadoop clusters of different versions can be deployed side by side on top of the same shared data. Users can migrate workloads from old versions to new versions gradually, add resources to the new cluster, and take resources away from the old cluster. After all workloads are moved over, the old cluster can be decommissioned.

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
