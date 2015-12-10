# Myriad REST API

To scale a cluster up or down, use the Cluster API. The Cluster API provide flexup and flex down capability the changes the size of one or more instances in a cluster. The instance size is a profile parameter that is a predefined value of zero, small, medium, and large. These predefined values are specified in the Myriad configuration file (myriad-config-default.yml). To specify the number of instances for a service, use the Service API. Services are configured in the Myriad configuration file (myriad-config-default.yml). To retrieve the Myriad configuration and the Myriad Scheduler state, use the Configuration API and State API.

The Myriad REST API provides the following functionality:

API | HTTP Method | URI | Description |
----|-------------|-----|-------------|
[Cluster](#cluster-api) | PUT | /api/cluster/flexup | Expands the cluster size |
[Cluster](#cluster-api) | PUT | /api/cluster/flexdown | Shrinks the cluster size |
[Service](#service-api) | PUT | /api/cluster/flexupservice | Increases the number of instances for a service. |
[Service](#service-api) | PUT | /api/cluster/flexdownservice | Shrinks the number of instances for a service. |
[Configuration](#configuration-api) | GET | /api/config | Retrieves the Myriad configuration. |
[State](#state-api) | GET | /api/state | Retrieves a snapshot of the Myriad Scheduler state. |
[Framework Shutdown](#framework-api) | GET | /api/framework/shutdown/framework | Shuts down Myriad framework. |



## Cluster API

The Cluster REST API uses the PUT /api/cluster/flexup and flexdown HTTP method and URI to expand and shrink the cluster size.

### HTTP Method and URI

```
PUT /api/cluster/flexup      // Expands the size of the YARN cluster.

PUT /api/cluster/flexdown    // Shrinks the size of the YARN cluster.
```

Parameters include:

Parameter | Description |
--------- | ----------- |
profile | (Required) If a profile value is not specified, the API returns an error. The profile indicates the amount of resources (CPU or memory) a Node Manager should advertise to the Resource Manager. Default profiles: zero, small, medium, large. These default profiles (zero, small, medium, and large) are defined in the myriad-config-default.yml file. The resources associated with these default profiles can be modified; additionally, new profiles can be defined. |
instances | (Required) The number of Node Managers instances to launch. Each Node Manager instance advertises the amount of resources specified in the profile. The value is a number in the range of zero (0) to the number of Mesos slave nodes. |
constraints | (Optional) Array definition for a single constraint using the LIKE operator constraint format: <mesos_slave_attribute|hostname> LIKE <value_regex>. The hostname constraint is used to launch Node Managerss on nodes whose hostname matches the regex passed in as value. See common Mesos slave attributes (http://mesos.apache.org/documentation/attributes-resources) for more information. |


### Syntax

```
<resource_manager_host>:8192/api/cluster/flexup
    profile=<zero|small|medium|large>
    instances=<integer>
    constraints=<["JSON array of strings"]>
<resource_manager_host>:8192/api/cluster/flexdown
    profile=<zero|small|medium|large>
    instances=<integer>
    constraints=<["JSON array of strings"]

```

### Request Examples

Curl request example to flexup two instances with the profile set to small:

```
curl -X PUT http://10.10.100.19:8192/api/cluster/flexup
    -d instances=2
    -d profile=small
```

Curl request example to flexdown one instance with the profile set to small:

```
curl -X PUT http://10.10.100.19:8192/api/cluster/flexdown
    -d instances=1
    -d profile=small
```

Curl request example to launch two (2) Node Managers with profile set to large only on specific hosts, host-120 through host-129:

```
curl -X PUT http://10.10.100.19:8192/api/cluster/flexdown
    -d instances=2
    -d profile=large
    -d constraints=["hostname LIKE host-12[0-9].example.com"]
```

Request header to flexup:

```
PUT /api/cluster/flexup HTTP/1.1
Host: 10.10.100.19:8192
Connection: keep-alive
Content-Length: 36
Origin: http://10.10.100.19:8192
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36
Content-Type: application/json
Accept: */*
Referer: http://10.10.100.19:8192/
Accept-Encoding: gzip, deflate, sdch
Accept-Language: en-US,en;q=0.8
```

Request header to flexdown:

```
PUT /api/cluster/flexdown HTTP/1.1
Host: 10.10.100.19:8192
Connection: keep-alive
Content-Length: 17
Origin: http://10.10.100.19:8192
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36
Content-Type: application/json
Accept: */*
Referer: http://10.10.100.19:8192/
Accept-Encoding: gzip, deflate, sdch
Accept-Language: en-US,en;q=0.8
```

Launches a Node Manager with the profile set to medium on any host in the Mesos cluster:

```
{profile: "medium", instances:1}
```

Launches a Node Manager with the profile set to small on any host in the Mesos cluster:

```
{
  "instances":1, "profile": "small"
}
```

Launches a Node Manager with the profile set to zero on any host in the Mesos cluster:

```
{
  "instances":1
}
```

Launches four (4) Node Managers with profile set to large only on specific hosts, host-120 through host-129:

```
{
    "instances":4,
    "profile": "large",
    "constraints": ["hostname LIKE host-12[0-9].example.com"]
}
```

Launches two (2) Node Managers with profile set to zero only on hosts sharing a common Mesos slave attribute (http://mesos.apache.org/documentation/attributes-resources):

```
{
    "instances":2,
    "profile": "zero",
    "constraints": ["hdfs LIKE true"]
}
```


### Response Example

```
202 ACCEPTED
```



## Service API

The Cluster REST API uses the PUT /api/cluster/flexup and flexdown HTTP method and URI to expand and shrink the cluster size.

### HTTP Method and URI

```
PUT /api/cluster/flexup      // Expands the size of the YARN cluster.

PUT /api/cluster/flexdown    // Shrinks the size of the YARN cluster.
```

Parameters include:


Parameter | Description |
--------- | ----------- |
profile	  | (Required) If a profile value is not specified, the API returns an error. The profile indicates the amount of resources (CPU or memory) a Node Manager should advertise to the Resource Manager. Default profiles: zero, small, medium, large. These default profiles (zero, small, medium, and large) are defined in the myriad-config-default.yml file. The resources associated with these default profiles can be modified; additionally, new profiles can be defined. |
instances| (Required) The number of Node Managers instances to launch. Each Node Manager instance advertises the amount of resources specified in the profile. The value is a number in the range of zero (0) to the number of Mesos slave nodes.|
constraints	| (Optional) Array definition for a single constraint using the LIKE operator constraint format: <mesos_slave_attribute|hostname> LIKE <value_regex>. The hostname constraint is used to launch Node Managerss on nodes whose hostname matches the regex passed in as value. See common Mesos slave attributes (http://mesos.apache.org/documentation/attributes-resources) for more information. |


### Syntax

 ```
 <resource_manager_host>:8192/api/cluster/flexup
    profile=<zero|small|medium|large>
    instances=<integer>
    constraints=<["JSON array of strings"]>
<resource_manager_host>:8192/api/cluster/flexdown
    profile=<zero|small|medium|large>
    instances=<integer>
    constraints=<["JSON array of strings"]>
```


### Request Examples

Curl request example to flexup two instances with the profile set to small:

```
curl -X PUT http://10.10.100.19:8192/api/cluster/flexup
    -d instances=2
    -d profile=small
```

Curl request example to flexdown one instance with the profile set to small:

```
curl -X PUT http://10.10.100.19:8192/api/cluster/flexdown
    -d instances=1
    -d profile=small
```

Curl request example to launch two (2) Node Managers with profile set to large only on specific hosts, host-120 through host-129:

```
curl -X PUT http://10.10.100.19:8192/api/cluster/flexdown
    -d instances=2
    -d profile=large
    -d constraints=["hostname LIKE host-12[0-9].example.com"]
```

Request header to flexup:

```
PUT /api/cluster/flexup HTTP/1.1
Host: 10.10.100.19:8192
Connection: keep-alive
Content-Length: 36
Origin: http://10.10.100.19:8192
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36
Content-Type: application/json
Accept: */*
Referer: http://10.10.100.19:8192/
Accept-Encoding: gzip, deflate, sdch
Accept-Language: en-US,en;q=0.8
```

Request header to flexdown:

```
PUT /api/cluster/flexdown HTTP/1.1
Host: 10.10.100.19:8192
Connection: keep-alive
Content-Length: 17
Origin: http://10.10.100.19:8192
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36
Content-Type: application/json
Accept: */*
Referer: http://10.10.100.19:8192/
Accept-Encoding: gzip, deflate, sdch
Accept-Language: en-US,en;q=0.8
```

Launches a Node Manager with the profile set to medium on any host in the Mesos cluster:

```
{profile: "medium", instances:1}
```

Launches a Node Manager with the profile set to small on any host in the Mesos cluster:

```
{
  "instances":1, "profile": "small"
}
```

Launches a Node Manager with the profile set to zero on any host in the Mesos cluster:

```
{
  "instances":1
}
```

Launches four (4) Node Managers with profile set to large only on specific hosts, host-120 through host-129:

```
{
    "instances":4,
    "profile": "large",
    "constraints": ["hostname LIKE host-12[0-9].example.com"]
}
```

Launches two (2) Node Managers with profile set to zero only on hosts sharing a common Mesos slave attribute (http://mesos.apache.org/documentation/attributes-resources):

```
{
    "instances":2,
    "profile": "zero",
    "constraints": ["hdfs LIKE true"]
}
```



### Response Example

```
202 ACCEPTED
```



## Configuration API

The State REST API uses the GET /api/config HTTP method and URI to retrieve configuration information.

### HTTP Method and URI

```
GET /api/config
```

### Syntax

```
<resource_manager_host>:<port>/api/config
```

### Request Example

URL request example:

```
http://10.10.100.19:8192/api/config
```

Curl request example:

```
curl http://10.10.100.19:8192/api/config | python -m json.tool
```

Request header:

```
GET /api/config HTTP/1.1
Host: 10.10.100.19:8192
Connection: keep-alive
Cache-Control: max-age=0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36
Accept-Encoding: gzip, deflate, sdch
Accept-Language: en-US,en;q=0.8
```

### Response Example
```
{
    "checkpoint": false,
    "frameworkFailoverTimeout": 43200000.0,
    "frameworkName": "MyriadAlpha",
    "frameworkRole": "",
    "frameworkSuperUser": {
        "present": true
    },
    "frameworkUser": {
        "present": true
    },
    "mesosAuthenticationPrincipal": "",
    "mesosAuthenticationSecretFilename": "",
    "mesosMaster": "zk://10.10.100.19:2181/mesos",
    "myriadExecutorConfiguration": {
        "jvmMaxMemoryMB": {
            "present": true
        },
        "nodeManagerUri": {
            "present": false
        },
    "nativeLibrary": "/usr/local/lib/libmesos.so",
    "nmInstances": {
        "medium": 1
    },
    "nodeManagerConfiguration": {
        "cgroups": {
            "present": true
        },
        "cpus": {
            "present": true
        },
        "jvmMaxMemoryMB": {
            "present": true
        },
        "jvmOpts": {
            "present": false
        }
    }
    "profiles": {

        "large": {
            "cpu": "10",
            "mem": "12288"
        },
        "medium": {
            "cpu": "4",
            "mem": "4096"
        },
        "small": {
            "cpu": "2",
            "mem": "2048"
        },
        "zero": {
            "cpu": "0",
            "mem": "0"
        }
    },
    "rebalancer": false,
    "haEnabled": false
    "restApiPort": 8192,
    "yarnEnvironment": {
        "YARN_HOME": "/root/hadoop-2.7.0"
    },
    "zkServers": "10.10.100.19:2181",
    "zkTimeout": 20000
}
```





## State API

The State REST API uses the GET /api/state HTTP method and URI to retrieve a snapshot of the Myriad Scheduler state including pending, staging, active, and killable tasks.

### HTTP Method and URI

```
GET /api/state
```

### Syntax

```
<resource_manager_host>:8192/api/state
```

### Request Example

URL request example:

```
http://10.10.100.19:8192/api/state
```

Curl request example:

```
curl http://10.10.100.19:8192/api/state | python -m json.tool
```

Request header:

```
GET /api/state HTTP/1.1
Host: 10.10.100.19:8192
Connection: keep-alive
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36
Accept-Encoding: gzip, deflate, sdch
Accept-Language: en-US,en;q=0.8
```

### Response Example

```
{
    "activeTasks": [],
    "killableTasks": [],
    "pendingTasks": [
        "nm.medium.ea564a5b-3a77-47dc-a7fe-2ff88ae2d5ed"
    ],
    "stagingTasks": []
}
```


## Framework API

The Framework REST API uses the GET /api/framework/shutdown/framework HTTP method to shut down myriad framework which shutdown web-server after stopping myriad driver, stop/clean all myriad tasks and clean myriad state-store (if any).

### HTTP Method and URI

```
GET /api/framework/shutdown/framework
```

### Syntax

```
<resource_manager_host>:8192/api/framework/shutdown/framework
```

### Request Example

URL request example:

```
http://10.10.101.137:8192/api/framework/shutdown/framework
```

Curl request example:

```
curl http://10.10.101.137:8192/api/framework/shutdown/framework | python -m json.tool
```

Request header:

```
GET /api/framework/shutdown/framework HTTP/1.1
User-Agent: curl/7.19.7 (x86_64-redhat-linux-gnu) libcurl/7.19.7 NSS/3.16.2.3 Basic ECC zlib/1.2.3 libidn/1.18 libssh2/1.4.2
Host: 10.10.101.137:8192
Accept: */*
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
