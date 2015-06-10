# Myriad REST API

* [Cluster](#cluster-api)
  * [PUT /api/cluster/flexup: Expands the size of cluster with {clusterId}](#put-apiclusterflexup)
  * [PUT /api/cluster/flexdown: Shrinks the size of cluster with {clusterId}](#put-apiclusterflexdown)

* [State](#state-api)
  * [GET /api/state: Returns a snapshot of scheduler state](#get-apistate)
  
## Cluster API

### PUT /api/cluster/flexup
Expands the size of the cluster

Request:
```json
{
  "instances":1, "profile": "small"
}
```

Response:
```
200 OK
```

### PUT /api/cluster/flexdown
Shrinks the size of cluster

Request:
```json
{
  "instances":1
}
```

Response:
```
200 OK
```

## State API

### GET /api/state

Response:
```json
{
  "pendingTasks":[

  ],
  "stagingTasks":[

  ],
  "activeTasks":[

  ],
  "killableTasks":[

  ]
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
    "profiles": {
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
    "rebalancer": false,
    "nativeLibrary": "/usr/local/lib/libmesos.so",
    "zkServers": "10.10.30.131:5181",
    "zkTimeout": 20000,
    "restApiPort": 8192,
    "yarnEnvironment": {
        "YARN_HOME": "/opt/mapr/hadoop/hadoop-2.5.1/",
        "YARN_NODEMANAGER_OPTS": "-Dyarn.nodemanager.resources.io-spindles=4.0 -Dyarn.resourcemanager.hostname=10.10.30.132"
    },
    "mesosAuthenticationPrincipal": "",
    "mesosAuthenticationSecretFilename": "",
    "nodeManagerConfiguration": {
        "jvmMaxMemoryMB": {
            "present": true
        },
        "user": {
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
        "path": "file:///root/myriad-executor-runnable-0.0.1.jar"
    }
}
```