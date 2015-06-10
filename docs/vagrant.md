# Vagrant setup

You can use following guide to setup a cluster in a virtual machine.

### Prerequisities
* Virtualbox
* Vagrant

To start the cluster run following:

```
vagrant up
```

At this point the VM will have a single node mesos cluster running.

To ssh in the cluster run following:

```
vagrant ssh
```

The password for vagrant user is 'vagrant'.

To setup YARN/Hadoop inside VM, run following:

```
cd /vagrant
./setup-yarn-1.sh
```

This will create a user hduser in group hadoop. Remember the password that you provide for this user.

Now, do following:

```
sudo su - hduser
cd /vagrant
./setup-yarn-2.sh
```

If everything goes fine you'll see following processes running (process ids will be different):

```
9844 Jps
6709 NameNode
6393 JobHistoryServer
6874 DataNode
```

To build myriad scheduler inside VM, you can do following:

```
cd /vagrant
./gradlew build
```

**Dealing with a build failure**
If you get a build failure which is not the build itself, but a failure to write to disk.  This can happen when you built outside the vagrant instance first.  Exit the user `hduser` by typing `exit` and build again as the `vagrant` user.   

At this point, myriad's scheduler jar and all the runtime dependencies will be available here: `/vagrant/myriad-scheduler/build/libs/*`. Please copy these jars to `$YARN_HOME/share/hadoop/yarn/lib/`.  The default `$YARN_HOME` is `/usr/local/hadoop/`.

```
cp /vagrant/myriad-scheduler/build/libs/* /usr/local/hadoop/share/hadoop/yarn/lib/
```

The self-contained myriad executor jar will be available here: `/vagrant/myriad-executor/build/libs/myriad-executor-runnable-x.y.z.jar`. Please copy this jar to `/usr/local/libexec/mesos/`.

```
sudo mkdir -p /usr/local/libexec/mesos/
sudo cp /vagrant/myriad-executor/build/libs/myriad-executor-runnable-0.0.1.jar /usr/local/libexec/mesos/
sudo chown hduser:hadoop -R /usr/local/libexec/mesos/
```

To configure YARN to use Myriad, please update ```$YARN_HOME/etc/hadoop/yarn-site.xml``` with following:

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

To configure Myriad itself, please add following file to ```$YARN_HOME/etc/hadoop/myriad-default-config.yml```:

```yml
mesosMaster: 10.141.141.20:5050
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
  path: file://localhost/usr/local/libexec/mesos/myriad-executor-runnable-0.0.1.jar
```

To launch Myriad, you can run following:

```
sudo su hduser
yarn-daemon.sh start resourcemanager
```

To check that things are running, from a browser on the host check out the following urls:

* [Myriad UI](http://10.141.141.20:8192/)
* [Mesos UI - Frameworks](http://10.141.141.20:5050/#/frameworks)

To shut down, from the vagrant ssh console:

```
yarn-daemon.sh stop resourcemanager

./shutdown.sh

exit

exit

vagrant halt
```