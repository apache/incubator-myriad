# Vagrant setup

You can use following guide to setup a cluster in a virtual machine.

### Prerequisities
* Virtualbox
* Vagrant

To start the cluster run following:
```shell
vagrant up
```
At this point the VM will have a single node mesos cluster running.

To ssh in the cluster run following:
```shell
vagrant ssh
```
The password for vagrant user is 'vagrant'.

To setup YARN/Hadoop inside VM, run following:
```shell
cd /vagrant
./setup-yarn-1.sh
```
This will create a user hduser in group hadoop. Remeber the password that you provide for this user.

Now, do following:
```shell
sudo su - hduser
cd /vagrant
./setup-yarn-2.sh
```
If everything goes fine you'll see following processes running (process ids will be different):
```shell
6337 ResourceManager
9844 Jps
6709 NameNode
6393 JobHistoryServer
6874 DataNode
```

To build myriad inside VM, you can do following:
```shell
cd /vagrant
gradle build
```

To build self-contained executable JAR, you can run following:
```shell
cd /vagrant
gradle capsule
```
At this point, a jar will be created here: build/libs/myriad-capsule-0.0.1.jar

