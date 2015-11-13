# Configuring for HA #

The Myriad high availability (HA) feature provides no job failure or downtime in case of failure. In addition, self recovery from a failure is provided to restore it back to a highly available state after the failure.

A Myriad HA environment allows the Node Managers to reconnect to the new Resource Manager instance upon failover.


On failover, the following occurs:

   * Marathon re-launches the Resource Manager as a new task.
   * Mesos-DNS updates the IP address for the Resource Manager Mesos task to the new IP address.

**Note:** All clients that are connected to Resource Manager continue to work as long as the FQDN (for example, rm.marathon.mesos) is used to connect to the Resource Manager.

## Prerequisites ##
   * Deploy mesos-master, mesos-slave (per node), zookeeper, marathon, and mesos-dns on your cluster.

## Setting Up Mesos-DNS ##

**Step 1:** Create a directory for Mesos-DNS. For example, /etc/mesos-dns.

**Step 2:** Install Mesos-DNS on one node in your cluster.

**Step 3:** Configure Mesos-DNS by providing the required parameters in the /etc/mesos-dns/config.json file. See the [Mesos-DNS configuration](http://mesosphere.github.io/mesos-dns/docs/configuration-parameters.html) documentation for more information. The following example parameters represent a minimum configuration.

```
{
    "zk": "zk:10.10.100.19:2181/mesos",
    "refreshSeconds": 60,
    "ttl": 60,
    "domain": "mesos",
    "port": 53,
    "resolvers": ["10.10.1.10"],
    "timeout": 5,
}
```

**Step 4:** If you are on Linux, add the following Mesos-DNS name server to the /etc/resolv.conf file (at the top of the file) on all cluster nodes and clients. For example, clients running RM UI, Myriad UI, and so on.

```
 nameserver <mesos-dnsIP address>
```

**Note:** Add the entries at the top (in the beginning) of the /etc/resolv.conf file. If the entries are not at the top, Mesos-DNS may not work correctly.

## Configuring HA ##
Configuring Myriad for HA involves adding HA configuration properties to the $YARN_HOME/etc/hadoop/yarn-site.xml file and the $YARN_HOME/etc/hadoop/myriad-config-default.yml file.

### Modify yarn-site.xml ###

To the $YARN_HOME/etc/hadoop/yarn-site.xml file, add the following properties:

<pre>
&lt;!--  HA configuration properties -->
 &lt;property>
    &lt;name>yarn.resourcemanager.store.class&lt;/name>
    &lt;value>org.apache.hadoop.yarn.server.resourcemanager.recovery.MyriadFileSystemRMStateStore&lt;/value>
&lt;/property>
&lt;property>
    &lt;name>yarn.resourcemanager.fs.state-store.uri&lt;/name>
           &lt;!-- Path on HDFS, MapRFS etc -->
    &lt;value>/var/mapr/cluster/yarn/rm/system&lt;/value>
&lt;/property>
&lt;property>
    &lt;name>yarn.resourcemanager.recovery.enabled&lt;/name>
    &lt;value>true&lt;/value>
&lt;/property>
&lt;!-- If using MapR distro
 &lt;property>
    &lt;name>yarn.resourcemanager.ha.custom-ha-enabled&lt;/name>
    &lt;value>false&lt;/value>
 &lt;/property> -->
</pre>


### Modify myriad-config-default.yml ###

To the $YARN_HOME/etc/hadoop/myriad-config-default.yml file, modify the following values:

```
frameworkFailoverTimeout: <non-zero value>
haEnabled: true
```

**Note:** The Myriad Mesos frameworkFailoverTimeout parameter is specified in milliseconds. This paramenter indicates to Mesos that Myriad will failover within this time interval.

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

