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

#Environment Variables#

* *HADOOP_NAMENODE* : *Required*
* *ALTERNATE_HADOOP_URL* : Optional - Allows user to override the hadoop distribution used by Myriad.


If you already had a working Vagrant instance, you will need to run `vagrant reload` in order to allow zookeeper and hdfs port-forwarding.

Still having problems connecting to Zookeeper? Be sure that your zkServer and MesosMaster values are correct in the [myriad-config-default.yml](https://github.com/mesos/myriad/blob/phase1/myriad-scheduler/src/main/resources/myriad-config-default.yml) file.

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
