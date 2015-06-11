# Myriad Configuration Properties

Myriad Scheduler (the component that plugs into Resource Manager process), exposes configuration properties that admins can modify. 
It expects a file ```myriad-config-default.yml``` to be present on the Resource Manager's java classpath. 

Currently, this file is built into Myriad Scheduler jar. So, if you need to modify some of the properties in this file, please modify them **before** building Myriad Scheduler.

## Properties

```yaml

# Address of the mesos master - <IP:port> or ZooKeeper path
mesosMaster: 10.0.2.15:5050
# mesosMaster: zk://10.0.2.15:2181/mesos

# Whether to check point myriad's mesos framework or not
checkpoint: false

# Myriad's mesos framework failover timeout in milliseconds. This tells mesos
# to expect myriad would failover within this time interval.
frameworkFailoverTimeout: 43200000

# Myriad's mesos framework name.
frameworkName: MyriadAlpha

# Myriad's mesos framework role.
frameworkRole: someRoleName

# User the Node Manager will run as (Defaults to user running the resource manager if absent,  necessary for remote distribution).
frameworkUser: someUserName

# User that gets the nodeManagerUri and sets up the directories for Node Manager, must have passwordless sudo (Necessary only for remote distribution, otherwise ignored).
frameworkSuperUser: someUserNameWithSudo

# Myriad's REST-ful services port mapping.
restApiPort: 8192

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
  cpus: 0.2             # CPU needed by NM process.
  cgroups: false        # Whether NM should support CGroups. If set to 'true', myriad automatically 
                        # configures yarn-site.xml to attach YARN's cgroups under Mesos' cgroup hierarchy.
executor:
  jvmMaxMemoryMB: 256   # Xmx for myriad's executor that launches Node Manager.
  path: file://localhost/usr/local/libexec/mesos/myriad-executor-runnable-0.0.1.jar  # Path for the myriad's executor binary.
                                                                            # Also supports, hdfs:// notation.
  # These are for remote distribution. Hdfs is assumed, but http, file, and ftp are also possible.
  # path: hdfs://namenode:port/dist/myriad-executor-runnable-x.x.x.jar # Path for the myriad's executor binary. 
  # nodeManagerUri: hdfs://namenode:port/dist/hadoop-2.5.0.tar.gz # the uri to d/l hadoop from   # Path to the Hadoop tarball

# Environment variables required to launch Node Manager process. Admin can also pass other environment variables to NodeManager.
yarnEnvironment:
  YARN_HOME: /usr/local/hadoop # Or /opt/mapr/hadoop/hadoop-2.5.1/ if using MapR's Hadoop
  # YARN_HOME: hadoop-2.5.0 # Should be relative nodeManagerUri is set
  YARN_NODEMANAGER_OPTS: -Dnodemanager.resource.io-spindles=4.0 # Required only if using MapR's Hadoop
  # JAVA_HOME: /usr/lib/jvm/java-default # System dependent, but sometimes necessary

# Authentication principal for Myriad's mesos framework
mesosAuthenticationPrincipal: some_principal

# Authentication secret filename for Myriad's mesos framework
mesosAuthenticationSecretFilename: /path/to/secret/filename

```
