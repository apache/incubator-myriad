# Control plane algorithm for rebalancing cluster capacity

_This is a working draft_

Notes:
- Each registered YARN ResourceManager will have a minimum quota associated with them. Quota can be expressed in terms of CPU or Memory. OR Minimum number of NodeManagers which are of profile X or higher.
- Myriad will monitor the registered ResourceManagers to determine if the ResourceManager needs more resources or if the ResourceManager have excess unused resources. And, then it will use this information to flex-up or flex-down the NodeManager, either horizontall or vertically.
- When making a decision to flex-down NodeManagers, algorithm will take into consideration the kind of containers that are currently running under the chosen NodeManager.
  - If NodeManager is running a AppMaster container, it will be skipped, as killing it will lead to a killing of all it's child containers. Although, this needs to be reconsidered once [YARN-1489](https://issues.apache.org/jira/browse/YARN-1489) gets resolved.
  - A NodeManger should be chosen for flex-down, if reconfiguring and restarting it has minimum impact compared to other NodeManagers. This strategy can be reconsidered once [YARN-1336](https://issues.apache.org/jira/browse/YARN-1336) gets resolved.

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
