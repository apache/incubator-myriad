# Myriad Remote Distribution

The Myriad Scheduler can be configured to automatically download and run the hadoop yarn binaries and get the hadoop 
configuration from the resource manager. This means you won't have to install and configure hadoop yarn on each machine. 
Note this is a very new feature and the configuration options may change dramatically in the future.

# Myriad Remote Distribution Bundle Creation

We will assume you are using hadoop-2.5.0 downloaded from hadoop.apache.org.  Specific vendor versions should work but 
may require additional steps.  We will also assume hadoop is installed in `/opt/hadoop-2.5.0`, adjust this path to fit 
your installation.

First configure the Resource Manager as normal.

At this point, from the project root you build myriad with the commands:

```
./gradlew build  
```

and copy the jars and config onto your yarn classpath:

```
cp myriad-scheduler/build/libs/*.jar /opt/hadoop-2.5.0/share/hadoop/yarn/lib/
cp myriad-scheduler/src/main/resources/myriad-config-default.yml /opt/hadoop-2.5.0/share/hadoop/yarn/lib/
```

You will also need to place `myriad-executor-runnable-x.x.x.jar` in hdfs

```
hadoop fs -put myriad-executor/build/libs/myriad-executor-runnable-0.0.1.jar /dist
```

Edit `/opt/hadoop/etc/hadoop/myriad-config-default`.  For standard configuration see 
[myriad-configuration](myriad-configuration.md).  To enable remote binary distribution you must set the following options:

```YAML
frameworkSuperUser: admin # Must be root or have passwordless sudo on all nodes!
frameworkUser: hduser # Should be the same user running the resource manager.
                      # Must exist on all nodes and be in the 'hadoop' group
executor:  
  nodeManagerUri: hdfs://namenode:port/dist/hadoop-2.5.0.tar.gz  
  path: hdfs://namenode:port/dist/myriad-executor-runnable-0.0.1.jar
yarnEnvironment:  
  YARN_HOME: hadoop-2.5.0 # This should be relative if nodeManagerUri is set  
```

Configure `/opt/hadoop-2.5.0/etc/hadoop/yarn-site.xml` as instructed in: [myriad-configuration](myriad-configuration.md)

Create the tarball and place it in hdfs:

```
cd ~
sudo cp -rp /opt/hadoop-2.5.0 .
sudo rm hadoop-2.5.0/etc/hadoop/yarn-site.xml
sudo tar -zcpf ~/hadoop-2.5.0.tar.gz hadoop-2.5.0
hadoop fs -put ~/hadoop-2.5.0.tar.gz /dist
```

You can now start the resource manager and attempt to flex up the cluster!
