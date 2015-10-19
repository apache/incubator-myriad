# Using Myriad with Docker #

## Building the Resource Manager Docker

`./build-myriad.sh` will run the gradle scripts from the root myriad folder and compile all necessary libraries.

#Configuration Guide#

In order for the ResourceManager to operate correctly, you will need to provide 2 configuration files:

* [myriad-config-default.yml](https://github.com/mesos/myriad/blob/phase1/myriad-scheduler/src/main/resources/myriad-config-default.yml)
* modified [yarn-site.xml](https://github.com/mesos/myriad/blob/phase1/docs/myriad-dev.md)

## Running the Resource Manager Docker

```bash
docker run --net=host --name='myriad-resourcemanager' -t \
  -v /path/to/configs:/myriad-conf \
  -e HADOOP_NAMENODE="10.100.3.237:9000" \
  mesos/myriad-resourcemanager
  ```

If you already had a working Vagrant instance, you will need to run `vagrant reload` in order to allow zookeeper and hdfs port-forwarding.

Still having problems connecting to Zookeeper? Be sure that your zkServer and MesosMaster values are correct in the [myriad-config-default.yml](https://github.com/mesos/myriad/blob/phase1/myriad-scheduler/src/main/resources/myriad-config-default.yml) file.
