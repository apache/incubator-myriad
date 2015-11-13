# Myriad Fine-grained Scaling

The objective of fine-grained scaling is to bring elasticity of resources between YARN and other Mesos frameworks. With fine-grained scaling, YARN takes resource offers from Mesos and runs enough containers (YARN tasks) that the offers can hold and release the resources back to Mesos once the containers finish.

* Node Managers that register with the Resource Manager with (0 memory, 0 CPU) are eligible for fine-grained scaling, that is, Myriad expands and shrinks the capacity of the Node Managers with the resources offered by Mesos. Further, Myriad ensures that YARN containers are launched on the Node Managers only if Mesos offers enough resources on the slave nodes running those Node Managers.
* A zero profile, as well as small, medium, and large profiles are defined in the Myriad configuration file, myriad-config-default.yml. A zero profile allows administrators to launch Node Managers with (0 memory, 0 CPU) capacities. To modify the profile, use the Cluster REST /api/cluster/flexup command).
* Node Managers that register with the Resource Manager with more than (0 memory, 0 CPU) are not eligible for fine-grained scaling. For example, Myriad does not expand and shrink the capacity of the Node Managers. Node Managers are typically launched with a low, medium, or high profile.

## Fine-grained Scaling Behavior

The administrator launches Node Managers with zero capacity (via the REST /api/cluster/flexup command) and the Node Managers report zero capacity to the Resource Manager upon registration.

When a user submits an application to YARN (for example, a MapReduce job), the following occurs:

1. The application is added to the Resource Manager's scheduling pipeline.
	* If a Node Manager has a zero profile, the YARN scheduler (for example. FairShareScheduler) does not allocate any application containers.
	* If a Node Manager has a non-zero capacity (low, medium, or high profiles), containers might be allocated for those Node Managers depending on their free capacity.
2. Myriad receives resource offers from Mesos for slave nodes running zero profile Node Managers.
3. The offered resources are projected to the YARN Scheduler as available capacity of the zero profile Node Manager. For example, if Mesos offers (10G memory, 4CPU) for a given node, then the capacity of the zero profile Node Manager running on that node increases to (10G memory, 4 CPU).
4. The YARN Scheduler allocates a few containers for the zero profile Node Manager.
	* For each allocated container, Myriad spins up a placeholder Mesos task that holds on to Mesos resources as long as the corresponding YARN container is alive. The placeholder tasks are launched in a single shot, corresponding to the containers that YARN allocates.
	* Node Managers become aware of container allocations via YARN's heart-beat mechanism.
	* Myriad ensures that Node Managers are made aware of container allocations only after the corresponding placeholder Mesos tasks are launched.
6. When containers finish, Myriad sends out finished status updates to Mesos for the corresponding placeholder tasks.
7. Mesos takes back the resources from Myriad after receiving a finished status update.

## Trying out Fine-grained Scaling

1. Spin up Resource Manager with Myriad Scheduler plugged into it.
2. Flexup a few Node Managers using [/api/cluster/flexup](API.md) with zero profile: `{"instances":3, "profile": "zero"}`
Note: The zero profile Node Managers advertise zero resources to Resource Manager (the Resource Manager Nodes UI shows this).

3. Submit a MapReduce job to the Resource Manager.
	* The zero profile Node Managers advertise zero resources to Resource Manager (the Resource Manager Nodes UI shows this).
	* When Mesos offers resources to Myriad, the Mesos UI shows placeholder Mesos tasks (prefixed with "yarn_") for each yarn container allocated using those offers.
	* The Resource Manager's UI shows these containers allocated to the zero profile Node Manager nodes.
	* The placeholder Mesos tasks typically finish when the YARN containers finish.
	* The job should finish successfully (although some Node Managers were originally launched with zero (0) capacities).


### Sample Mesos Tasks Screenshot

![mesos_tasks_for_yarn_containers](https://cloud.githubusercontent.com/assets/3505177/7049736/d7995bf8-ddd0-11e4-850d-c59bca1fd1bf.png)

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
