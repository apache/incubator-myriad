# Myriad REST API

* [Clusters](#clusters-api)
  * [POST /api/clusters: Registers a new YARN](#post-apiclusters)
  * [GET /api/clusters: Lists all registered clusters](#get-apiclusters)
  * [GET /api/clusters/{clusterId}: Lists the cluster with {clusterId}](#get-apiclustersclusterid)
  * [PUT /api/clusters/{clusterId}/flexup: Expands the size of cluster with {clusterId}](#put-apiclustersclusteridflexup)
  * [PUT /api/clusters/{clusterId}/flexdown: Shrinks the size of cluster with {clusterId}](#put-apiclustersclusteridflexdown)
  * [DELETE /api/clusters/{clusterId}: Unregisters YARN cluster with {clusterId}. Also, kills all the nodes.](#delete-apiclustersclusterid)

* [State](#state-api)
  * [GET /api/state: Returns a snapshot of scheduler state](#get-apistate)
  
## Clusters API

### POST /api/clusters
Registers a new YARN cluster

Request:
```json
{
  "clusterName": "yarn1", 
  "resourceManagerHost": "localhost", 
  "resourceManagerPort": "8088"
}
```

Response:
```json
{
  "clusterId": "cluster-uuid"
}
```

### GET /api/clusters
Lists all registered clusters

Response:
```json
{
  "clusters":[
    {
      "clusterId":"a8993acd-864d-4101-b87d-1c9429637fdb",
      "clusterName":"yarn1",
      "nodes":[

      ],
      "resourceManagerHost":"localhost",
      "resourceManagerPort":"8088",
      "minQuota":0.0
    }
  ]
}
```

### GET /api/clusters/{clusterId}
Lists the cluster with {clusterId}

Response:
```json
{
  "cluster":{
    "clusterId":"a8993acd-864d-4101-b87d-1c9429637fdb",
    "clusterName":"yarn1",
    "nodes":[

    ],
    "resourceManagerHost":"localhost",
    "resourceManagerPort":"8088",
    "minQuota":0.0
  }
}
```

### PUT /api/clusters/{clusterId}/flexup
Expands the size of cluster with {clusterId}

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

### PUT /api/clusters/{clusterId}/flexdown
Shrinks the size of cluster with {clusterId}

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

### DELETE /api/clusters/{clusterId}
Unregisters YARN cluster with {clusterId}. Also, kills all the nodemanagers.

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
