# Using Myriad with Docker #

## Building the Resource Manager Docker

`./build-myriad.sh` will run the gradle scripts from the root myriad folder and compile all necessary libraries.

## Running the Resource Manager Docker

`docker run --name='myriad-resourcemanager' -t \ -v /path/to/yarn-site.xml:/myriad-conf/yarn-site.xml \ -v /path/to/myriad-config-default.yml:/myriad-conf/myriad-config-default.yml \ -e HADOOP_NAMENODE="10.100.3.237:9000" \ mesos/myriad-resourcemanager`

### Available Environment Variables
You can also pass in custom values via docker run for the following env vars: 
* HADOOP_HOME
* HADOOP_USER
* HADOOP_GROUP

#Configuration Guide#

In order for the ResourceManager to operate correctly, you will need to provide 2 configuration files:

* [myriad-config-default.yml](https://github.com/mesos/myriad/blob/phase1/src/main/resources/myriad-config-default.yml)
* modified [yarn-site.xml](https://github.com/mesos/myriad/blob/phase1/docs/myriad-dev.md)

If you already had a working Vagrant instance, you will need to run `vagrant reload` in order to allow zookeeper and hdfs port-forwarding.

Still having problems connecting to Zookeeper? Be sure that your zkServer and MesosMaster values are correct in the [myriad-config-default.yml](https://github.com/mesos/myriad/blob/phase1/src/main/resources/myriad-config-default.yml) file.
