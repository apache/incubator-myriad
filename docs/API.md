# Myriad REST API

* [Cluster](#cluster-api)
  * [PUT /api/cluster/flexup: Expands the size of cluster with {clusterId}](#put-apiclusterflexup)
  * [PUT /api/cluster/flexdown: Shrinks the size of cluster with {clusterId}](#put-apiclusterflexdown)

* [State](#state-api)
  * [GET /api/state: Returns a snapshot of scheduler state](#get-apistate)
  
## Cluster API

### PUT /api/cluster/flexup
Expands the size of the YARN cluster

Launch a Node Manager with ```small``` profile on **ANY HOST** in the Mesos cluster:
```json
{
  "instances":1, "profile": "small"
}
```
Launch 4 Node Managers with ```large``` profile **ONLY on hosts ```host-120 through host-129```**:
```json
{
  "instances":4, "profile": "large", "constraints": ["hostname LIKE host-12[0-9].example.com"]
}
```

Launch 2 Node Managers with ```zero``` profile **ONLY on hosts sharing a common Mesos [slave attribute](http://mesos.apache.org/documentation/attributes-resources)**
```json
{
  "instances":2, "profile": "zero", "constraints": ["hdfs LIKE true"]
}
```

Response:
```
202 ACCEPTED
```

### PUT /api/cluster/flexdown
Shrinks the size of the YARN cluster

Shutdown a Node Manager with ```small``` profile running on **ANY HOST** in the Mesos cluster:
```json
{
  "instances":1, "profile": "small"
}
```
Shutdown 4 Node Managers with ```large``` profile running **ONLY on hosts ```host-120 through host-129```**:
```json
{
  "instances":4, "profile": "large", "constraints": ["hostname LIKE host-12[0-9].example.com"]
}
```

Shutdown 2 Node Managers with ```zero``` profile running **ONLY on hosts sharing a common Mesos [slave attribute](http://mesos.apache.org/documentation/attributes-resources)**
```json
{
  "instances":2, "profile": "zero", "constraints": ["hdfs LIKE true"]
}
```

Response:
```
202 ACCEPTED
```

## State API

### GET /api/state

Response:
```json
{
    "pendingTasks": [
        "nm.zero.e9c65a2a-5b05-4459-ab0d-e9bb12c529d4",
        "nm.zero.394fe61c-4b42-40d2-8e87-bf199e644d40",
        "nm.zero.1354e9cc-356a-4dd9-ae1d-d28ee930266c"
    ],
    "stagingTasks": [],
    "activeTasks": [
        "nm.zero.324592be-b5c5-4a6f-b3de-29602d8d30e8",
        "nm.zero.849335b7-630d-4652-bf49-f8a3e25f72e0",
        "nm.medium.f6938f3b-c4e5-476a-b9a8-ce8995d2ef6c"
    ],
    "killableTasks": []
}
```
## Configuration API

### GET /api/config

Sample Response:
```json
{
    "mesosMaster": "10.10.30.131:5050",
    "checkpoint": false,
    "frameworkFailoverTimeout": 43200000,
    "frameworkName": "MyriadAlpha",
    "frameworkRole": "",
    "frameworkUser": {
        "present": true
    },
    "frameworkSuperUser": {
        "present": true
    },
    "profiles": {
        "zero": {
            "cpu": "0",
            "mem": "0"
        },
        "small": {
            "cpu": "1",
            "mem": "1100"
        },
        "medium": {
            "cpu": "2",
            "mem": "2048"
        },
        "large": {
            "cpu": "4",
            "mem": "4096"
        }
    },
    "nmInstances": {
        "medium": 1
    },
    "rebalancer": false,
    "nativeLibrary": "/usr/local/lib/libmesos.so",
    "zkServers": "10.10.30.131:5181",
    "zkTimeout": 20000,
    "restApiPort": 8192,
    "yarnEnvironment": {
        "YARN_HOME": "/opt/mapr/hadoop/hadoop-2.7.0/",
        "YARN_NODEMANAGER_OPTS": "-Dnodemanager.resource.io-spindles=4.0"
    },
    "mesosAuthenticationPrincipal": "",
    "mesosAuthenticationSecretFilename": "",
    "haenabled": true,
    "nodeManagerConfiguration": {
        "jvmMaxMemoryMB": {
            "present": true
        },
        "cpus": {
            "present": true
        },
        "jvmOpts": {
            "present": false
        },
        "cgroups": {
            "present": true
        }
    },
    "myriadExecutorConfiguration": {
        "jvmMaxMemoryMB": {
            "present": true
        },
        "path": "file:///root/myriad-executor-runnable-0.0.1.jar",
        "nodeManagerUri": {
            "present": false
        }
    }
}
```