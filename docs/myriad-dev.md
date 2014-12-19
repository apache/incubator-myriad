# Myriad development

## Build instructions
System requirements:
* JDK 1.7+
* Gradle
* Hadoop 2.5.0


### Building Myriad Scheduler
To build scheduler run:

```bash
gradle build
```

This will build myriad-x.x.x.jar and download the runtime jars and place them inside ```PROJECT_HOME/build/libs/``` directory.

### Building Myriad Executor
To build self-contained executor jar, run:

```bash
gradle capsuleExecutor
```

This will build myriad-executor-x.x.x.jar and place it inside ```PROJECT_HOME/build/libs/``` directory. 

### Deploying Myriad

Myriad deployment involves deployment of Myriad Scheduler and Myriad Executor.

To deploy Myriad Executor, please follow following steps:

1. Build Myriad Executor
2. Copy myriad-executor-xxx.jar to each mesos slave's ```/usr/local/libexec/mesos``` directory. 

Note: For advanced readers one can also copy myriad-executor-xxx.jar to any other directory on slave filesystem, or it can be copied to HDFS as well. In either case, one needs to update the executor's path property in myriad-config-default.yml file, and prepend the path with either ```file://``` or ```hdfs://```, as appropriate.  

To deploy Myriad Scheduler, please follow following steps:

1. Build Myriad Scheduler
2. Copy all jars inside ```PROJECT_HOME/build/libs/``` directory, to YARN ResourceManager's classpath. For ex: Copy all jars to ```$YARN_HOME/share/hadoop/yarn/lib/```

To run Myriad Scheduler, you need to follow following steps:

1. Add ```MESOS_NATIVE_JAVA_LIBRARY``` environment variable to ResourceManager's environment variables, for ex: Add following to ```$YARN_HOME/etc/hadoop/hadoop-env.sh```: 

```bash
export MESOS_NATIVE_JAVA_LIBRARY=/usr/local/lib/libmesos.so
```

2. Add following to ```$YARN_HOME/etc/hadoop/yarn-site.xml```:

```xml
<property>
    <name>yarn.nodemanager.resource.cpu-vcores</name>
    <value>${nodemanager.resource.cpu-vcores}</value>
</property>
<property>
    <name>yarn.nodemanager.resource.memory-mb</name>
    <value>${nodemanager.resource.memory-mb}</value>
</property>

<!-- Configure Myriad Scheduler here -->
<property>
    <name>yarn.resourcemanager.scheduler.class</name>
    <value>com.ebay.myriad.scheduler.yarn.MyriadFairScheduler</value>
    <description>One can configure other scehdulers as well from following list: com.ebay.myriad.scheduler.yarn.MyriadCapacityScheduler, com.ebay.myriad.scheduler.yarn.MyriadFifoScheduler</description>
</property>
```

Optional: If you would like to enable cgroups, please add following to ```yarn-site.xml```:

```xml
<!-- Cgroups specific configuration -->
<property>
    <description>who will execute(launch) the containers.</description>
    <name>yarn.nodemanager.container-executor.class</name>
    <value>${yarn.nodemanager.container-executor.class}</value>
</property>
<property>
    <description>The class which should help the LCE handle resources.</description>
    <name>yarn.nodemanager.linux-container-executor.resources-handler.class</name>
    <value>${yarn.nodemanager.linux-container-executor.resources-handler.class}</value>
</property>
<property>
    <name>yarn.nodemanager.linux-container-executor.cgroups.hierarchy</name>
    <value>${yarn.nodemanager.linux-container-executor.cgroups.hierarchy}</value>
</property>
<property>
    <name>yarn.nodemanager.linux-container-executor.cgroups.mount</name>
    <value>${yarn.nodemanager.linux-container-executor.cgroups.mount}</value>
</property>
<property>
    <name>yarn.nodemanager.linux-container-executor.cgroups.mount-path</name>
    <value>${yarn.nodemanager.linux-container-executor.cgroups.mount-path}</value>
</property>
<property>
    <name>yarn.nodemanager.linux-container-executor.group</name>
    <value>${yarn.nodemanager.linux-container-executor.group}</value>
</property>
<property>
    <name>yarn.nodemanager.linux-container-executor.path</name>
    <value>${yarn.home}/bin/container-executor</value>
</property>
```

And, following to ```$YARN_HOME/etc/hadoop/myriad-config-default.yml```:

```yaml
...
nodemanager:
    cgroups: true
...
```

3. To run Myriad, just start ResourceManager:

```bash
yarn-daemon.sh start resourcemanager
```

## Sample config

```yaml
mesosMaster: 10.0.2.15:5050
checkpoint: false
frameworkFailoverTimeout: 43200000
frameworkName: MyriadAlpha
nativeLibrary: /usr/local/lib/libmesos.so
zkServers: localhost:2181
zkTimeout: 20000
profiles:
  small:
    cpu: 1
    mem: 1100
  medium:
    cpu: 2
    mem: 2048
  large:
    cpu: 4
    mem: 4096
rebalancer: true
nodemanager:
  jvmMaxMemoryMB: 1024
  user: hduser
  cpus: 0.2
  cgroups: false
executor:
  jvmMaxMemoryMB: 256
  path: file://localhost/usr/local/libexec/mesos/myriad-executor-0.0.1.jar
```
