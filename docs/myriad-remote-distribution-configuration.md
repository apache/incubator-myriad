# Myriad Remote Distribution

The Myriad Scheduler can be configured to automatically download and run the hadoop yarn binaries. This means you won't have to install and configure hadoop yarn on each machine.  Note this is a very new feature and the configuration options may change dramatically in the future.

# Myriad Remote Distribution Bundle Creation

We will assume you are using hadoop-2.5.0 downloaded from hadoop.apache.org.  Specific vendor versions should work but may require additional steps.  We will also assume hadoop is installed in `/opt/hadoop-2.5.0`, adjust this path to fit your installation.

First configure the Resource Manager as normal..

At this point you build myriad with the commands:
```Shell
./gradlew build  
./gradlew capsuleExecutor  
```
and copy the jars onto your yarn classpath:
```Shell
cp build/libs/*.jar /opt/hadoop-2.5.0/share/hadoop/yarn/lib/
cp src/main/resources/myriad-config-default.xml
```


In the /opt/hadoop/etc/hadoop edit myriad-config-default.  For standard configuration see myriad-configuration(myriad-configuration.md).  To enable remote binaray distribution you must set the following options:
```YAML
executor:  
  nodeManagerUri: hdfs://namenode:port/dist/hadoop-2.5.0.tar.gz  
  path: hadoop-2.5.0/share/hadoop/yarn/lib/myriad-executor-0.0.1.jar #this should be relative if nodeManagerUri is set
yarnEnvironment:  
  YARN_HOME: hadoop-2.5.0 #this should be relative if nodeManagerUri is set  
```

It's strongly advised to set both FrameworkSuperUser and FrameworkUser.

`frameworkSuperUser` must exist on all slave nodes myriad can run on.  If FrameworkSuperUser is not specified it defaults to the user running the resource manager.

`frameworkUser` must exist on all all slave nodes myriad can run on. If FrameworkSuperUser is not specified it defaults to the FrameworkSuper (but myriad executor is run as the user and not sudo).


Configure `/opt/hadoop-2.5.0/etc/hadoop/yarn-site.xml` as instructed in: myriad-configuration(myriad-configuration.md).

cd to `/opt`, create the tarball and place it in hdfs:
```Shell
cd ~
sudo cp -rp /opt/hadoop-2.5.0
sudo rm hadoop-2.5.0/etc/hadoop/*.xml.
sudo tar -zcpf ~/hadoop-2.5.0.tar.gz hadoop-2.5.0
hadoop fs -put ~/hadoop-2.5.0.tar.gz /dist
```
You can now start the resource manager and attempt to flex up the cluster!
