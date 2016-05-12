# Using Myriad with Docker #
Docker is the easiest way to from 0 to Yarn on Mesos within minutes. 

# Building the Resource Manager Docker

YARN runs as a user and group, and expects consistent uid and gids in HDFS and accross the cluster.  Hence it is necessary 
to edit docker/Dockerfile and modify the following:
```
ENV YARN_USER="yarn"
ENV YARN_UID="107"
ENV HADOOP_GROUP="hadoop"
ENV HADOOP_GID="112"
ENV YARN_GROUP="yarn"
ENV YARN_UID="113"
```

The run the following commands:
```bash
./gradlew -P dockerTag=username/myriad buildRMDocker
docker push username/myriad
```

This will build the ResourceManager from src, save, and push the image as *username/myriad*.

# Configuration Guide

It is assumed you already have hdfs and Mesos running.  For more information about Apache Mesos visit the [website](http://mesos.apache.org). 
If you need to setup hdfs, consider using the [hdfs-mesos framework](https://github.com/mesosphere/hdfs).

In order for the ResourceManager to operate correctly, you will need to provide 5 configuration files. These files will 
need to mounted from a directory into `/usr/local/hadoo/etc` of the docker container.

* [myriad-config-default.yml](https://github.com/apacher/incubator-myriad/blob/phase1/myriad-scheduler/src/main/resources/myriad-config-default.yml) (template provided)
* [yarn-site.xml](https://github.com/apache/incubator-myriad/blob/phase1/docs/myriad-dev.md) (template provided)
* mapred-site.xml (template provided)
* hdfs-site.xml (used for hdfs)
* core-site.xml (used for hdfs)

Edit and copy the file templates along with the hdfs.xml and core-site.xml from your hdfs configuration into a directory named `config`.  Additional, files maybe necessary such as rack.sh, log4j.properties, 
hadoop-env.sh, and yarn-env.sh depending on your configuration. Create a second directoy called `dist`. Then tar the files using `tar -zcf dist\config.tgz config`.

# Running the Resource Manager Docker

From the directories containing `dist` and `config` execute:
```bash
docker run --net=host -v $PWD/dist -v $PWD/config:/usr/local/hadoop/etc/hadoop --name='myriad-resourcemanager' -t \
  <username>/myriad
```
# Updating configuration

There is no need to to rebuild the docker image to update YARN's configuration, simply edit the files in config and repackage using 
`tar -zcf dist\config.tgz config`.  To update the configuration on the Nodemanagers they will need to be flexed down and flexed up, to 
 update the resource manager the docker container will need to be restarted:

```bash
docker kill myriad-resourcemanager
docker run --net=host -v $PWD/dist -v $PWD/config:/usr/local/hadoop/etc/hadoop --name='myriad-resourcemanager' -t \
  <username>/myriad
```
 
---
<sub>
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

<sub>
  http://www.apache.org/licenses/LICENSE-2.0

<sub>
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
