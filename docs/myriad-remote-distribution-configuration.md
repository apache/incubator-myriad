#Myriad Remote Distribution

The Myriad Scheduler can be configured to automatically download and run the hadoop yarn binaries. This
means you won't have to install and configure hadoop yarn on each machine.  However, this comes at the cost of
downloading the binaries each time a node manager is spawned.  One should consider this tradeoff carefully.  Note
this is a very new feature and the configuration options may change dramatically in the future.

# Myriad Remote Distribution Bundle Creation

We will assume you are using hadoop-2.5.0 downloaded from hadoop.apache.org.  Specific vendor versions should 
work but may require additional steps.  We will also assume use installed hadoop in /opt/hadoop-2.5.0, adjust
this path to fit your installation.

First configure the Resource Manager as normal.

In the myriad folder edit src/main/resources/myriad-config-default.  For standard configuration see
myriad-configuration(myriad-configuration.md).  To enable remote distribution you must set the following options:

nodemanager:
  user: hduser
  group:hadoop #only relevant if remoteDistribution is true
executor:
  #The following should be uncommented if using a remotely distributed URI
  remoteDistribution: true
  nodeManagerUri: hdfs://namenode:port/dist/hadoop-2.5.0.tar.gz
  command: hadoop-2.5.0/share/hadoop/yarn/lib/myriad-executor-0.0.1.jar
yarnEnvironment:
  YARN_HOME: hadoop-2.5.0 #this should be relative if remoteDistribution is set to true

Also note if 
executor:
    user: "notroot"
is set, the user must has passwordless sudo on all slave nodes myriad can run on.

At this point you build myriad with the commands:
./gradlew build
./gradlew capsuleExecutor
and copy the jars onto your yarn classpath:
cp build/libs/*.jar /opt/hadoop-2.5.0/share/hadoop/yarn/lib/

Configure hadoop-2.5.0/etc/hadoop/yarn-site.xml as instructed in: myriad-configuration(myriad-configuration.md).

cd to opt, create the tarball and place it in hdfs:
tar -zcpf ~/hadoop-2.5.0.tar.gz hadoop-2.5.0
hdfs -put ~/hadoop-2.5.0.tar.gz /dist

You can now start the resource manager and attempt to flex up the cluster!