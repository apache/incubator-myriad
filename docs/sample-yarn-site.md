# Sample: yarn-site.xml

The following is a sample yarn-site.xml file.



<pre>
&lt;?xml version="1.0" encoding="UTF-8"?>
&lt;configuration>

&lt;!-- Site-specific YARN configuration properties -->
   &ltproperty>
        &lt;name>yarn.nodemanager.aux-services&lt;/name>
        &lt;value>mapreduce_shuffle,myriad_executor&lt;/value>
        &lt;!-- If using MapR distro, please use the following value:
        &lt;value>mapreduce_shuffle,mapr_direct_shuffle,myriad_executor&lt;/value> -->
    &lt;/property>
    &lt;property>
        &lt;name>yarn.nodemanager.aux-services.mapreduce_shuffle.class&lt;/name>
        &lt;value>org.apache.hadoop.mapred.ShuffleHandler&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.nodemanager.aux-services.myriad_executor.class&lt;/name>
        &lt;value>org.apache.myriad.executor.MyriadExecutorAuxService&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.nm.liveness-monitor.expiry-interval-ms&lt;/name>
        &lt;value>2000&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.am.liveness-monitor.expiry-interval-ms&lt;/name>
        &lt;value>10000&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.resourcemanager.nm.liveness-monitor.interval-ms&lt;/name>
        &lt;value>1000&lt;/value>
    &lt;/property>
&lt;!-- (more) Site-specific YARN configuration properties -->
    &lt;property>
        &lt;name>yarn.nodemanager.resource.cpu-vcores&lt;/name>
        &lt;value>${nodemanager.resource.cpu-vcores}&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.nodemanager.resource.memory-mb&lt;/name>
        &lt;value>${nodemanager.resource.memory-mb}&lt;/value>
    &lt;/property>
&lt;!-- Dynamic Port Assignment enablement by Mesos -->
  &lt;property>
        &lt;name>yarn.nodemanager.address&lt;/name>
        &lt;value>${myriad.yarn.nodemanager.address}&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.nodemanager.webapp.address&lt;/name>
        &lt;value>${myriad.yarn.nodemanager.webapp.address}&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.nodemanager.webapp.https.address&lt;/name>
        &lt;value>${myriad.yarn.nodemanager.webapp.address}&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.nodemanager.localizer.address&lt;/name>
        &lt;value>${myriad.yarn.nodemanager.localizer.address}&lt;/value>
    &lt;/property>

&lt;!-- Myriad Scheduler configuration -->
    &lt;property>
        &lt;name>yarn.resourcemanager.scheduler.class&lt;/name>
        &lt;value>org.apache.myriad.scheduler.yarn.MyriadFairScheduler&lt;/value>
    &lt;/property>
&lt;!-- Needed for Fine Grain Scaling -->
    &lt;property>
  &lt;name>yarn.scheduler.minimum-allocation-vcores&lt;/name>
        &lt;value>0&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.scheduler.minimum-allocation-vcores&lt;/name>
        &lt;value>0&lt;/value>
    &lt;/property>
&lt;!-- Cgroups specific configuration -->
&lt;!--
    &lt;property>
        &lt;description>Who will execute(launch) the containers.&lt;/description>
        &lt;name>yarn.nodemanager.container-executor.class&lt;/name>
        &lt;value>${yarn.nodemanager.container-executor.class}&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;description>The class which should help the LCE handle resources.&lt;/description>
        &lt;name>yarn.nodemanager.linux-container-executor.resources-handler.class&lt;/name>
        &lt;value>${yarn.nodemanager.linux-container-executor.resources-handler.class}&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.nodemanager.linux-container-executor.cgroups.hierarchy&lt;/name>
        &lt;value>${yarn.nodemanager.linux-container-executor.cgroups.hierarchy}&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.nodemanager.linux-container-executor.cgroups.mount&lt;/name>
        &lt;value>${yarn.nodemanager.linux-container-executor.cgroups.mount}&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.nodemanager.linux-container-executor.cgroups.mount-path&lt;/name>
        &lt;value>${yarn.nodemanager.linux-container-executor.cgroups.mount-path}&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.nodemanager.linux-container-executor.group&lt;/name>
        &lt;value>${yarn.nodemanager.linux-container-executor.group}&lt;/value>
    &lt;/property>
    &lt;property>
        &lt;name>yarn.nodemanager.linux-container-executor.path&lt;/name>
        &lt;value>${yarn.home}/bin/container-executor&lt;/value>
    &lt;/property>
-->
&lt;/configuration>
</pre>

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


