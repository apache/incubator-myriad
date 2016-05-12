# Installing for Administrators

The Myriad Scheduler can be configured to automatically download and run the Hadoop YARN binaries and get the Hadoop configuration from the resource manager. This means you won't have to install and configure Hadoop YARN on each machine.
This information involves bundling Myriad and creating a tarball.

* [Assumptions](#assumptions)
* [Building the Myriad Remote Distribution Bundle](#build-the-myriad-remote-distribution-bundle)
	* 	[Step 1: Build Myriad](#build-myriad)
	* 	[Step 2: Deploy the Myriad Files](#deploy-the-myriad-files)
	* 	[Step 3: Configure the Myriad Defaults](#configure-the-myriad-defaults)
	* 	[Step 4: Configure YARN to Use Myriad](#configure-yarn-to-use-myriad)
	* 	[Step 5: Create the Tarball](#create-the-tarball)
* [Getting Started](#getting-started)

## Assumptions

The following are assumptions about your environment:

* You are using hadoop-2.7.0 downloaded from [hadoop.apache.org](http://hadoop.apache.org).  Specific vendor versions should work but may require additional steps.

## Building the Myriad Remote Distribution Bundle ##
Before building Myriad, configure the Resource Manager as you normally would.

### Step 1: Build Myriad
From the project root you build Myriad with the commands

```
./gradlew build
```

### Step 2: Deploy the Myriad Files

To deploy the Myriad Scheduler and Executor files, copy the jar filess and configuration file to the following locations:

```
cp myriad-scheduler/build/libs/*.jar /opt/hadoop-2.7.0/share/hadoop/yarn/lib/
cp myriad-executor/build/libs/myriad-executor-0.1.0.jar /opt/hadoop-2.7.0/share/hadoop/yarn/lib/
cp myriad-scheduler/build/resources/main/myriad-config-default.yml /opt/hadoop-2.7.0/etc/hadoop/
```

### Step 3: Configure the Myriad Defaults

Edit the **$YARN_HOME/etc/hadoop/myriad-config-default.yml** file to configure the default parameters. See the sample [Myriad configuration file](myriad-configuration.md) for more information. To enable remote binary distribution, you must set the following options:


```YAML
frameworkSuperUser: admin              # Must be root or have passwordless sudo on all nodes!
frameworkUser: hduser                  # Should be the same user running the resource manager.
                                       # Must exist on all nodes and be in the 'hadoop' group
#servedConfigPath: /dist/config.tgz
#servedBinaryPath: /dist/hadoop-2.7.0.tar.gz
executor:
  nodeManagerUri: hdfs://namenode:port/dist/hadoop-2.7.0.tar.gz
  #nodeManagerUri: http://resourcemanager/api/binary.tgz
  #configUri: http://resourcemanager/api/config.tgz
yarnEnvironment:
  YARN_HOME: hadoop-2.7.0                # This should be relative if nodeManagerUri is set
  JAVA_HOME: /usr/
  #HADOOP_CONF_DIR: config
```


### Step 4: Configure YARN to Use Myriad ###

Modify the  **YARN_HOME/etc/hadoop/yarn-site.xml** file. See [Myriad yarn-site.xml file](sample-yarn-site.md)


### Step 5: Create the Tarball ###

The tarball has all of the files needed for the Node Managers and  Resource Managers. The following shows how to create the tarball and place it in HDFS:

```
cd ~
sudo cp -rp /opt/hadoop-2.7.0 .
sudo rm ~/hadoop-2.7.0/etc/hadoop/yarn-site.xml
sudo tar -zcpf ~/hadoop-2.7.0.tar.gz hadoop-2.7.0
hadoop fs -put ~/hadoop-2.7.0.tar.gz /dist
```

Alternatively, one can serve the tar ball directly from Myriad using the `servedBinaryPath` parameter (see the commented line in the code block of step 3).

### Advanced Configuration ###

By default Myriad has the Node Managers pull the yarn-site.xml from the resource managers configuration url.  While this is convenient,
it is sometimes preferable to package and distribute the hadoop configuration files separately.  Myriad provides these facilities using 
`servedConfigPath` and `configUri` options and using `yarnEnvironment` to pass `HADOOP_CONF_DIR` (see the commented lines in the code block of step 3).

By default Myriad assumes that Java is preinstalled on each Mesos slave.  While this is convenient,
it is sometimes preferable to package and distribute the JDK seperately.  Myriad provides this facility using 
`jvmUri` option and using `yarnEnvironment` to pass `JAVA_HOME` (see the commented lines in the code block of step 3).


## Getting Started ##

You can now start the resource manager and attempt to flexup or flexdown the cluster. See  [Getting Started](getting-started.md) for information about using Myriad. See the [Myriad Cluster API](API.md) for more information about scaling.

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
