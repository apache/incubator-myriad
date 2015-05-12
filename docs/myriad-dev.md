# Myriad development

## Setting up Editor Support

You are welcome to use any editor you like, however many of the myriad developers use IntelliJ IDEA.   The build process described below uses [gradle](https://gradle.org/).   The build script is configured with the [idea plugin](https://docs.gradle.org/current/userguide/idea_plugin.html) to create an intellij project structure.   This is accomplished with the following command:

```
./gradlew idea

# to open on a mac
open myriad.ipr
```

**NOTE:** if you are interested in using eclipse, there is a plugin for that.  Either send in a pull request, or send a message on the dev mail list that you would make use of it.

## Build instructions
System requirements:
* JDK 1.7+
* Gradle
* Hadoop 2.5.0

Myriad requires two components to be built:
* **Myriad Scheduler** - This component plugs into Resource Manager process and negotiates resources from Mesos. It is responsible to launch Node Manager processes via Mesos.
* **Myriad Executor** - This component implements [Mesos Executor](http://mesos.apache.org/api/latest/java/org/apache/mesos/Executor.html) interface. It is launched by Myriad Scheduler via Mesos and runs as a separate process on each mesos-slave. Myriad Executor is responsible for launching Node Manager process as a Mesos Task.

The project is a multi-project build using gradle.  You can go to the `$PROJECT_HOME` (which is where you cloned the git project to) and type `./gradlew build` which will build all projects or you can go into each individual project and build them separately.

### Build All 
**Before** building you may still want to follow the instructions in **Building Myriad Scheduler** in order to build with the appropriate yml file.

To build all, from the `$PROJECT_HOME`, run:

```
./gradlew build
```

At this point the jars will be located in the following directories (relative to $PROJECT_HOME).

```
# scheduler jars
./myriad-scheduler/build/libs/

# executor jar
./myriad-executor/build/libs/

```

### Building Myriad Scheduler
**Before** building Myriad Scheduler, please modify [myriad-config-default.yml](../myriad-scheduler/src/main/resources/myriad-config-default.yml) with appropriate configuration properties (Please read  [Myriad Configuration Properties](myriad-configuration.md)). This is needed because currently myriad-config-default.yml will be embedded into Myriad Scheduler jar.

To build Myriad Scheduler, from `$PROJECT_HOME/myriad-scheduler` run:

```bash
./gradlew build
```

This will build myriad-x.x.x.jar and download the runtime jars and place them inside `./build/libs/` directory (relative to `$PROJECT_HOME/myriad-scheduler`).

### Building Myriad Executor
To build self-contained executor jar, from `$PROJECT_HOME/myriad-executor` run:

```bash
./gradlew build
```

This will build myriad-executor-runnable-xxx.jar and place it inside `PROJECT_HOME/myriad-executor/build/libs/` directory. 

### Deploying Myriad Scheduler

To deploy Myriad Scheduler, please follow the below steps:

1. Build Myriad Scheduler
2. Copy all jars under `PROJECT_HOME/myriad-scheduler/build/libs/` directory, to YARN ResourceManager's classpath. For ex: Copy all jars to `$YARN_HOME/share/hadoop/yarn/lib/`

### Deploying Myriad Executor
To deploy Myriad Executor, please follow the below steps:

1. Build Myriad Executor
2. Copy myriad-executor-runnable-xxx.jar from `PROJECT_HOME/myriad-executor/build/libs/` to each mesos slave's `/usr/local/libexec/mesos` directory. 

Note: For advanced readers one can also copy myriad-executor-runnable-xxx.jar to any other directory on slave filesystem, or it can be copied to HDFS as well. In either case, one needs to update the executor's path property in myriad-config-default.yml file, and prepend the path with either `file://` or `hdfs://`, as appropriate.  

### Running Myriad Scheduler
To run Myriad Scheduler, you need to follow following steps:

* Add `MESOS_NATIVE_JAVA_LIBRARY` environment variable to ResourceManager's environment variables, for ex: Add following to `$YARN_HOME/etc/hadoop/hadoop-env.sh`: 

```bash
export MESOS_NATIVE_JAVA_LIBRARY=/usr/local/lib/libmesos.so
```

* Add following to `$YARN_HOME/etc/hadoop/yarn-site.xml`:

```xml
<property>
    <name>yarn.nodemanager.resource.cpu-vcores</name>
    <value>${nodemanager.resource.cpu-vcores}</value>
</property>
<property>
    <name>yarn.nodemanager.resource.memory-mb</name>
    <value>${nodemanager.resource.memory-mb}</value>
</property>
<!--These options enable dynamic port assignment by mesos -->
<property>
    <name>yarn.nodemanager.address</name>
    <value>${myriad.yarn.nodemanager.address}</value>
</property>
<property>
    <name>yarn.nodemanager.webapp.address</name>
    <value>${myriad.yarn.nodemanager.webapp.address}</value>
</property>
<property>
    <name>yarn.nodemanager.webapp.https.address</name>
    <value>${myriad.yarn.nodemanager.webapp.address}</value>
</property>
<property>
    <name>yarn.nodemanager.localizer.address</name>
    <value>${myriad.yarn.nodemanager.localizer.address}</value>
</property>

<!-- Configure Myriad Scheduler here -->
<property>
    <name>yarn.resourcemanager.scheduler.class</name>
    <value>com.ebay.myriad.scheduler.yarn.MyriadFairScheduler</value>
    <description>One can configure other scehdulers as well from following list: com.ebay.myriad.scheduler.yarn.MyriadCapacityScheduler, com.ebay.myriad.scheduler.yarn.MyriadFifoScheduler</description>
</property>

```

* Add following to ```$YARN_HOME/etc/hadoop/mapred-site.xml```:

```xml
<!--This option enables dynamic port assignment by mesos -->
<property>
    <name>mapreduce.shuffle.port</name>
    <value>${myriad.mapreduce.shuffle.port}</value>
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

And, following to `$YARN_HOME/etc/hadoop/myriad-config-default.yml`:

```yaml
...
nodemanager:
    cgroups: true
...
```

* Start Resource Manager. Myriad Scheduler will run inside Resource Manager as a plugin.

```bash
yarn-daemon.sh start resourcemanager
```
### Running Myriad Executor / Node Managers
Myriad Executor and Node Managers are launched automatically by Myriad Scheduler as a response to [flexup REST API](API.md).
