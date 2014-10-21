# Myriad Configuration Properties

Myriad's component that plugs into Resource Manager, exposes configuration properties that admins can modify. 
It expects a file ```myriad-config-default.yml``` to be present on the Resource Manager's java classpath. 

* A good practice is to place this file under ```$YARN_HOME/etc/hadoop/```, since this directory holds 
YARN's configuration files and is already present on Resource Manager's classpath.

## Properties

```yaml

# Address of the mesos master
mesosMaster: 10.0.2.15:5050
# mesosMaster: zk://10.0.2.15:2181/mesos

# Whether to check point myriad's mesos framework or not
checkpoint: false

# Myriad's mesos framework failover timeout in milliseconds. This tells mesos
# to expect myriad would failover within this time interval.
frameworkFailoverTimeout: 43200000

# Myriad's mesos framework name.
frameworkName: MyriadAlpha

# Address of the ZK ensemble (separate by comma, if multiple zk servers are used)
zkServers: localhost:2181

# ZK Session timeout
zkTimeout: 20000

# The node manager profiles. The REST API to flex up expects one of the profiles defined here.
# Admin can define custom profiles (requires restart of Resource Manager)
profiles:
  small:
    cpu: 1
    mem: 1024
  medium:
    cpu: 2
    mem: 2048
  large:
    cpu: 4
    mem: 4096

# Whether to turn on myriad's auto-rebalancer feature. 
# Currently it's work-in-progress and should be set to 'false'.    
rebalancer: false

# Properties for the Node Manager process that's launched by myriad as a result of 'flex up' REST call.
nodemanager:
  jvmMaxMemoryMB: 1024  # Xmx for NM JVM process.
  user: hduser          # The user to run NM process as.
  cpus: 0.2             # CPU needed by NM process.
  cgroups: false        # Whether NM should support CGroups. If set to 'true', myriad automatically 
                        # configures yarn-site.xml to attach YARN's cgroups under Mesos' cgroup hierarchy.
executor:
  jvmMaxMemoryMB: 256   # Xmx for myriad's executor that launches Node Manager.
  path: file://localhost/usr/local/libexec/mesos/myriad-executor-0.0.1.jar  # Path for the myriad's executor binary.
                                                                            # Also supports, hdfs:// notation.

# Environment variables required to launch Node Manager process. Admin can also pass other environment variables to NodeManager.
yarnEnvironment:
  YARN_HOME: /usr/local/hadoop

```
