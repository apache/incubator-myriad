# Fine-grained Scaling Architecture
Myriad scheduler is comprised of components that interact with YARN and Mesos
services.

## Mesos Master Interactions

Class | Description |
------|------------ |
MyriadScheduler| This is the entry point to register a framework scheduler with Mesos. Upon registration, Mesos offers start to be received. |
OfferFeed |	Mesos offers are accepted and stashed here.  |
OfferLifecycleManager | Interface to retrieve stashed offers, return unused offers back to Mesos. |
ConsumedOffer | Represents offers that have been consumed for a slave prior to scheduling run. When offers are consumed, they increase the capacity of YARN Node Manager by that amount. Note that no Mesos tasks are launched when offers are consumed. They are launched only after the scheduling run. |


## Resource Manager Scheduler Interactions ##


Class | Description |
------|------------ |
Myriad Fair Scheduler, Myriad Capacity Scheduler | Extension point for YARN schedulers to enable setting up hooks into scheduler events. |
YARN Node Capacity Manager | Controls the YARN Scheduler's view of the cluster node capacity. YARN schedules containers on nodes based on available capacity. Myriad enables YARN cluster elasticity by dynamically growing and shrinking the node capacities. |


## Node Manager Interactions


Class | Description |
------|------------ |
NM HB Handler | Handles node manager heartbeat and responds to status updates of YARN containers. For running containers, it sends status update to Mesos Executor and for completed containers, it informs Pseudo Task Manager about available resources. |
Node Store | Tracks the set of nodes that are part of YARN cluster. |
Node | Tracks metadata about a node. |

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
