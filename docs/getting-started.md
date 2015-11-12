# Getting Started #

## Launching the Myriad Environment ##

The Myriad environment is established by running an initial application for the resource manager. This initial application name is specified in the yarn-site.xml file with the the yarn.resourcemanager.hostname property and the value, &lt;app-ID>&lt;framework>.mesos.

For example, if the value of  yarn.resourcemanager.hostname property is rm.marathon.mesos:

* rm - The ID given to the Resource Manager when launched using Marathon. Mesos-DNS constructs the Resource Manager hostname using the ID.
* marathon - The Mesos framework.

If you are using Marathon, launch Marathon and run an initial Resource Manager application. See Starting Resource Manager for more information.

## Ports ##

The ports used by Mesos, Marathon, and Myriad are:

Application | Port | Syntax |
----------------| ------ | ---------- |
Marathon | 8080 | http://&lt;IP address>:8080 |
Mesos | 5050 | http://&lt;IP address>:5050. Use the 5050 port and Frameworks directory. For example: http://&lt;IP address>:5050/#/frameworks (http://10.141.141.20:5050/#/frameworks) |
Myriad | 8192 | http://<IP address>:8192. For example: http://<ip address>:8192/ (http://10.141.141.20:8192/). Without the Mesos-DNS service, this IP is slave node's IP address where the Resource Manager is running. With the Mesos-DNS service, Mesos-DNS discovers the node where the Resource Manager is running by using Mesos DNS FQDN (&lt;app-ID>.&lt;framework>.mesos). |

## Launching Resource Manager ##

If you are using Marathon, launch Marathon and run an initial Resource Manager application. The Resource Manager can be launched or stopped from either the command line or the Marathon UI.

### Launching from the Command Line ###


To start the Resource Manager, run the YARN daemon from the command line:

```
yarn-daemon.sh start resourcemanager
```

To shut down he Resource Manager, run the YARN daemon from the command line:

```
yarn-daemon.sh stop resourcemanager
```

### Launching from Marathon ###

Alternatively, start and stop Myriad from the Marathon UI. See Marathon: Application Basics for more information. For example, create an application to start the Resource Manager:

```
cd hadoop-2.7.0/sbin && yarn-daemon.sh start resourcemanager
```

Alternatively, when launching the Resource Manager in an HA environment, specify value for the `yarn.resourcemanager.hostname` property. The hostname is the ID field specified when launching a Marathon application.

To initially launch the Resource Manager from Marathon:

1. Launch Marathon with &lt;host>:8080. For example: http://10.10.100.16:8080
2. Click on **New App**.
3. Create a new application for the resource manager and specify:
	* ID
	* CPU
	* Memory
	* Instances
	* Command

For example:

Parameter|Example Value|Description|
---------------|------------------|----------------|
ID | rm | The ID for the Resource Manager when launched using Marathon. Mesos-DNS constructs the Resource Manager hostname using the ID. |
CPU | 0.2 | Amount of CPU allocated. |
Memory | 2048 | Amount of memory allocated |
Instances | 1 | Number of instances to be launched. |
Command | env && yarn resourcemanager | Command to launch the resource manager. |

**Note:** If the yarn.resourcemanager.hostname property is not specified in the yarn-site.xml file, then the -DYARN_RESOURCEMANAGER_OPTS option must be specified. When the -Dyarn.resourcemanager.hostname property is specified when launching Resource Manager, Myriad propagates this value to the Node Managers launched via the flexup API. The Node Managers can discover back the Resource Manager using the hostname specified in -Dyarn.resourcemanager.hostname. If that hostname happens to be a Mesos DNS hostname, then when the Resource Manager moves to another node during failover, Node Managerss can still connect to the new Resource Manager using the same hostname. Marathon helps move the Resource Manager to a different node whenever it detects the failure of Resource Manager.

 For example:

```
env && export YARN_RESOURCEMANAGER_OPTS=-Dyarn.resourcemanager.hostname=rm.marathon.mesos && yarn resourcemanager
```

**Note:** Some applications might require the `yarn.resourcemanager.hostname` property to be explicitly specified as a command line option.


## Monitoring Myriad ##

Myriad provides monitoring capability via the Myriad user interface (UI) or the Myriad REST API. Through the Myriad UI, tasks can be monitored (Tasks panel) and resources can be scaled (Flex panel).

### User Interface ###


The Myriad button is the main console which show the API code for the flexup and flexdown feature. The Tasks panel directs you to the tasks that are active, killable, pending, and staging. This panel is useful for monitoring Myriad. The Config button directs you to the Config panel where the Myriad configuration defaults display.

### REST API ###


To monitor Myriad, see the Myriad State API and the Myriad Configuration API. The HTTP method and URIs for retrieving the Myriad state and configuration are:

```
GET /api/state
GET /api/config
 ```


## Running YARN Applications ##

Hadoop YARN applications can be run from the command line on any node in the cluster.
The following example uses TeraGen to generate input data for TeraSort:

```
hadoop jar HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-<version>.jar teragen 10000 /outDir
```

The following example uses TeraSort to sort the data using map/reduce:

```
hadoop jar HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-<version>.jar terasort /outDir /terasortOutDir
```

## Scaling Resources Dynamically ##

Myriad provides the capability for fine-grained or coarse-grained scaling via the Myriad user interface (UI) or the Myriad REST API. Scaling is done by using the flexup and flexdown feature. Flexup expands the cluster and flexdown shrinks the cluster. While doing so, the number of instances and size of the scale are specified.  The number of instances that you can scale are bound only by the number of nodes in the cluster. The instance size is a profile parameter that is a predefined value of zero, small, medium, and large. These predefined values are specified in the Myriad configuration file (**myriad-config-default.yml**).

During initial startup, Myriad launches one NodeManager instance with a profile of medium through the parameter, nmInstances, which is specified in the **myriad-config-default.yml**  file.

```
"nmInstances": {
    "medium": 1
  },
```

### Profile Defaults ###

```
"profiles": {
  "zero": {
    "cpu": "0",
    "mem": "0"
  },
  "small": {
    "cpu": "2",
    "mem": "2048"
  },
  "medium": {
    "cpu": "4",
    "mem": "4096"
  },
  "large": {
    "cpu": "10",
    "mem": "12288"
  }
},
```

### User Interface ###

The Myriad button is the main console which shows the API code for the flexup and flexdown feature. The Flex button directs you to the Flex panel where you can flexup and flex down instances. The Config button directs you to the Config panel where the Myriad configuration defaults display.
To flexup and flexdown instances via the Myriad UI, go to the Flex button on the top navigation bar.

### REST API ###

To scale a cluster up or down, use the Myriad Cluster API. The [Cluster API](API.md) provides flexup and flexdown capability that changes the size of one or more instances in a cluster. These predefined values are specified in the Myriad configuration file (**myriad-config-default.yml**). To retrieve the Myriad configuration and the Myriad Scheduler state, use the Configuration API and State API.

The HTTP method and URIs for flexing up and down are:

```
PUT /api/cluster/flexup

PUT /api/cluster/flexdown
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




