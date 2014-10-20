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
