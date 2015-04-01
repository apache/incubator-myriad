# Myriad Remote Distribution

The Myriad Scheduler can be configured to automatically download and run the hadoop yarn binaries. This means you won't have to install and configure hadoop yarn on each machine.  Note this is a very new feature and the configuration options may change dramatically in the future.

# Myriad Remote Distribution Bundle Creation

We will assume you are using hadoop-2.5.0 downloaded from hadoop.apache.org.  Specific vendor versions should work but may require additional steps.  We will also assume hadoop is installed in `/opt/hadoop-2.5.0`, adjust this path to fit your installation.

First configure the Resource Manager as normal.

In the myriad folder edit src/main/resources/myriad-config-default.  For standard configuration see myriad-configuration(myriad-configuration.md).  To enable remote binaray distribution you must set the following options:
```YAML
nodemanager:  
  user: hduser #This is the user the nodemanager runs as, if nodeManagerUri is present ownership will of YARN_HOME will be set to this user.
  group:hadoop #If nodeManagerUri is present group ownership will of YARN_HOME will be set to this group and $YARN_HOME/bin/container-executor will be set g+rs
  #Note both user and group must exist on all slaves.
executor:  
  nodeManagerUri: hdfs://namenode:port/dist/hadoop-2.5.0.tar.gz  
  path: hadoop-2.5.0/share/hadoop/yarn/lib/myriad-executor-0.0.1.jar #this should be relative if nodeManagerUri is set
yarnEnvironment:  
  YARN_HOME: hadoop-2.5.0 #this should be relative if nodeManagerUri is set  
```
Also note if 
```YAML
executor:  
    user: "notroot"
```
is set, the user must have passwordless sudo on all slave nodes myriad can run on.  
At this point you build myriad with the commands:
```Shell
./gradlew build  
./gradlew capsuleExecutor  
```
and copy the jars onto your yarn classpath:
```Shell
cp build/libs/*.jar /opt/hadoop-2.5.0/share/hadoop/yarn/lib/
```

Configure `/opt/hadoop-2.5.0/etc/hadoop/yarn-site.xml` as instructed in: myriad-configuration(myriad-configuration.md).

cd to `/opt`, create the tarball and place it in hdfs:
```Shell
cd /opt
tar -zcpf ~/hadoop-2.5.0.tar.gz hadoop-2.5.0
hdfs -put ~/hadoop-2.5.0.tar.gz /dist
```
You can now start the resource manager and attempt to flex up the cluster!