# Using Myriad with Docker #
Docker is the easiest way to from 0 to Yarn on Mesos within minutes. 

## Building the Resource Manager Docker
Run the following command:
```./gradlew buildRMDocker```
This will build the ResourceManager from src and save the image as *mesos/myriad*.

#Configuration Guide#

In order for the ResourceManager to operate correctly, you will need to provide 2 configuration files. These files will need to mounted from a directory into */myriad-conf* within the docker container.

* [myriad-config-default.yml](https://github.com/mesos/myriad/blob/phase1/myriad-scheduler/src/main/resources/myriad-config-default.yml)
* modified [yarn-site.xml](https://github.com/mesos/myriad/blob/phase1/docs/myriad-dev.md)


## Running the Resource Manager Docker

```bash
docker run --net=host --name='myriad-resourcemanager' -t \
  -v /path/to/configs:/myriad-conf \
  mesos/myriad
  ```

#Environment Variables#
* *ALTERNATE_HADOOP_URL* : Optional - Allows user to override the hadoop distribution used by Myriad. This will download the *.tar.gz file to be used as the hadoop distribution of choice for Myriad. 

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
