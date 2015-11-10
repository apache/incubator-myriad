# Installing for Developers

## Setting up Editor Support

You are welcome to use any editor you like, however, many of the myriad developers use IntelliJ IDEA. The build process described below uses [gradle](https://gradle.org/).   The build script is configured with the [idea plugin](https://docs.gradle.org/current/userguide/idea_plugin.html) to create an intellij project structure.   This is accomplished with the following command:

```
./gradlew idea

# to open on a mac
open myriad.ipr
```

**NOTE:** If you are interested in using eclipse, there is a plugin for that.  Either send in a pull request, or send a message on the dev mail list that you would make use of it.

## Step 1: Build Myriad
 To build Myriad (both Myriad Scheduler and Executor), from the $PROJECT_HOME, run:

```
./gradlew build
```

At this point the jars are located in the following directories (relative to $PROJECT_HOME).

```
# scheduler jars
./myriad-scheduler/build/libs/

# executor jar
./myriad-executor/build/libs/

```

### Building Myriad Scheduler Only
Before building Myriad Scheduler, modify [myriad-config-default.yml](../myriad-scheduler/src/main/resources/myriad-config-default.yml) with the appropriate configuration properties. The build process`.gradlew build` command builds the myriad-x.x.x.jar file, download the runtime jars, and places them inside the **./build/libs/** directory (relative to the $PROJECT_HOME/myriad-scheduler directory).

To build Myriad Scheduler, from $PROJECT_HOME/myriad-scheduler run:

```bash
./gradlew build
```


### Building Myriad Executor Only

The `./gradlew build` command builds the **myriad-executor-runnable-xxx.jar** and place it inside the **$PROJECT_HOME/myriad-executor/build/libs/** directory.

To build Myriad Executor individually as a self-contained executor jar, from $PROJECT_HOME/myriad-executor, run:

```bash
./gradlew build
```


## Step 2: Deploy the Myriad Files

To deploy Myriad Scheduler and Executor files:

1. Copy the Myriad Scheduler jar files from the $PROJECT_HOME/myriad-scheduler/build/libs/ directory to the $YARN_HOME/share/hadoop/yarn/lib/ directory on all nodes in your cluster.
2. Copy the Myriad Executor myriad-executor-xxx.jar file from the $PROJECT_HOME/myriad-executor/build/libs/ directory to each mesos slave's $YARN_HOME/share/hadoop/yarn/lib/ directory.
3. Copy the myriad-config-default.yml file from $PROJECT_HOME/myriad-scheduler/build/src/main/resources/ directory to the $YARN_HOME/etc/hadoop directory.

For example:

```
cp myriad-scheduler/build/libs/*.jar /opt/hadoop-2.7.1/share/hadoop/yarn/lib/
cp myriad-executor/build/libs/myriad-executor-0.0.1.jar /opt/hadoop-2.7.1/share/hadoop/yarn/lib/
cp myriad-scheduler/build/resources/main/myriad-config-default.yml /opt/hadoop-2.7.1/etc/hadoop/
```

**NOTE:** For advanced users, you can also copy myriad-executor-xxx.jar to any other directory on a slave filesystem or it can be copied to HDFS as well. In either case, you need to update the executor's path property in the myriad-config-default.yml file and prepend the path with either file:// or hdfs://, as appropriate.


## Step 3: Configure the Myriad Defaults

As a minimum, the following Myriad configuration parameters must be set:

* mesosMaster
* zkServers
* YARN_HOME

Enabling Cgroups involves modifying the yarn-site.xml and **myriad-config-default.yml** files. If you plan on using Cgroups, you could set that property at this time. See [Configuring Cgroup](cgroups.md) for more information.

**NOTE:** By copying the **myriad-config-default.yml** file to the **/etc/hadoop** directory, you can make changes to the configuration file without having to rebuild Myriad. If you specify the Myriad configuration parameters before building Myriad, you must rebuild Myriad and redeploy the jar files. This is required because the **myriad-config-default.yml** file is embedded into the Myriad Scheduler jar.


## Step 4: Configure YARN to Use Myriad

In order to run Myriad, the following YARN properties must be modified on each node in the cluster:

* Edit the $YARN_HOME/etc/hadoop/hadoop-env.sh file and add the following:

```
export MESOS_NATIVE_JAVA_LIBRARY=/usr/local/lib/libmesos.so
```

* Edit the $YARN_HOME/etc/hadoop/yarn-site.xml file and add the following:


```
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
    <value>org.apache.myriad.scheduler.yarn.MyriadFairScheduler</value>
    <description>One can configure other scehdulers as well from following list: org.apache.myriad.scheduler.yarn.MyriadCapacityScheduler, org.apache.myriad.scheduler.yarn.MyriadFifoScheduler</description>
</property>
```


* Edit the **$YARN_HOME/etc/hadoop/mapred-site.xml** file and add the dynamic port assignment properties.

	1. On each node, change directory to $YARN_HOME/etc/hadoop.
	2. Copy mapred-site.xml.template to mapred-site.xml.
	3. Edit and add the following property to the mapred-site.xml file.

```
// Add following to $YARN_HOME/etc/hadoop/mapred-site.xml:
<!--This option enables dynamic port assignment by mesos -->
<property>
    <name>mapreduce.shuffle.port</name>
    <value>${myriad.mapreduce.shuffle.port}</value>
</property>
```


## Starting the Resource Manager
Myriad Scheduler runs inside Resource Manager as a plugin. To start the Resource Manager:

```bash
./sbin/yarn-daemon.sh start resourcemanager
```


##  Running Myriad Executor and Node Managers
Myriad Executor and Node Managers are launched automatically by Myriad Scheduler as a response to flexup and flex down behavior. See the [Myriad Cluster API](API.md).
