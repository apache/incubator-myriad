# Mesosphere DC/OS Community Environment

This is the Vagrant-libvirt based environment for testing Apache Myriad
project in a DC/OS cluster.

# Setup the environment

In order to use this vagrant environment we have to enable a suitable
environment. The key points of this environment are:

- **DCOS_ARCH**: This variable has to match with the VMs architecture defined in
  the config folder: *cluster-1m4a1p.yaml*, *cluster-3m4a1p.yaml*, and so on. For
  example **DCOS_ARCH: 1m4a1p** matchs with the architecture *cluster-1m4a.yaml*, which
  is a DC/OS master node (1m), four DC/OS private agents (4a), and one DC/OS
  public agent (1p).

- **DCOS_VERSION**: This variable set the DC/OS community to download. The next
  section shows an example of configuration.

```
$ source setup-env 

This Vagrant environment is ready for the following settings:                         

- DCOS_ARCH: 1m4a1p                        
- DCOS_VERSION: 1.11.0                     
- HADOOP_VERSION: 2.7.0                    

'vagrant up --provider=libvirt' and happy hacking! 
``` 

# Running the deployment

```
$ vagrant up  --provider=libvirt
[...]
PLAY RECAP
*********************************************************************
bt                         : ok=36   changed=28   unreachable=0    failed=0   
m1                         : ok=28   changed=25   unreachable=0    failed=0   
a1                         : ok=31   changed=28   unreachable=0    failed=0   
a2                         : ok=31   changed=28   unreachable=0    failed=0   
a3                         : ok=31   changed=28   unreachable=0    failed=0   
a4                         : ok=31   changed=28   unreachable=0    failed=0   
p1                         : ok=31   changed=28   unreachable=0    failed=0   
```

## Final VMs set

```
$ vagrant status   
Current machine states:

bt                        running (libvirt)
m1                        running (libvirt)
a1                        running (libvirt)
a2                        running (libvirt)
a3                        running (libvirt)
a4                        running (libvirt)
p1                        running (libvirt)
```

## Running DC/OS CLI

```
$ vagrant ssh bt
[vagrant@bt ~]$ dcos auth login
If your browser didn't open, please go to the following link:

    http://m1/login?redirect_uri=urn:ietf:wg:oauth:2.0:oob

[vagrant@bt ~]$ dcos node
   HOSTNAME         IP                          ID                    TYPE              REGION  ZONE  
 100.1.10.102  100.1.10.102  637fb1cd-f4d4-427a-9418-092b80eb6000-S2  agent             None    None  
 100.1.10.103  100.1.10.103  637fb1cd-f4d4-427a-9418-092b80eb6000-S3  agent             None    None  
 100.1.10.104  100.1.10.104  637fb1cd-f4d4-427a-9418-092b80eb6000-S0  agent             None    None  
 100.1.10.105  100.1.10.105  637fb1cd-f4d4-427a-9418-092b80eb6000-S1  agent             None    None  
 100.1.10.106  100.1.10.106  637fb1cd-f4d4-427a-9418-092b80eb6000-S4  agent             None    None  
master.mesos.  100.1.10.101    5ee304dc-18a0-4f7f-b1ef-591c249cbbeb   master (leader)   None    None 
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
