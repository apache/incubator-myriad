# Configuring for JobHistoryServer and Other Services #

Services, such as JobHistoryServer, can be launched as a task from the Myriad Framework. To configure services, define the service in the Myriad configuration file, **myriad-config-default.yml**. Once defined, the Myriad Service REST API can be used to launch an instance as a task for the service.

Advantages include:

* In the case of multiple Myriad clusters, services can use resources dedicated to a specific Myriad cluster.
* When deconstructing a Myriad cluster, the service can be stopped with the cluster.
* All services for a particular Myriad cluster go under that Myriad Mesos framework.

To define a service in the myriad-config-default.yml file, add the following properties:

```
<!-- Define services as a task -->
services:
    serviceName:            # Name of the service
        jvmMaxMemoryMB:     # Memory needed for service
        cpus:               # CPU needed for Service
        ports:              # Map of ports: port property and value
                            # (Default: 0 which indicates that port will be assigned randomly)
                            # Uses the syntax, <name>: <value>
        envSettings:        # Any environment settings
        taskName:           # Again service name
        maxInstances:       # If defined maximum number of instances this service can have per myriad framework
        command:            # Command to be executed
        serviceOptsName:    # Name of the env. variable that may need to be set for the service that will include env. settings

The following example defines the parameter for JobHistoryServer and TimeLineServer tasks:
<!-- Define services as a task -->
services:
    jobhistory:
        jvmMaxMemoryMB: 1024
        cpus: 1
        ports:
            myriad.mapreduce.jobhistory.admin.address: 0
            myriad.mapreduce.jobhistory.address: 0
            myriad.mapreduce.jobhistory.webapp.address: 0
        envSettings: -Dcluster.name.prefix=/mycluster
        taskName: jobhistory
    timelineserver:
        jvmMaxMemoryMB: 1024
        cpus: 1
        envSettings: -Dcluster.name.prefix=/mycluster2
        taskName: timelineserver

```