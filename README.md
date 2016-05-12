# Myriad

[![Build Status](https://travis-ci.org/apache/incubator-myriad.svg)](https://travis-ci.org/apache/incubator-myriad)

Myriad is a Mesos framework designed for scaling a YARN cluster on Mesos. Myriad can expand or shrink the resources managed by a YARN cluster in response to events as per configured rules and policies.

The name _Myriad_ means, _countless or extremely great number_. In context of the project, it allows one to expand overall resources managed by Mesos, even when the cluster under Mesos management runs other cluster managers like YARN.

**Please note: Myriad is not yet production ready. However, the project is rapidly progressing with some very useful features.** 

## Getting started

* [How Myriad works](docs/how-it-works.md)
* [Developing Myriad](docs/myriad-dev.md)
* [Local vagrant setup guide](docs/vagrant.md)
* [Myriad REST API](docs/API.md)
* [Myriad Dashboard Development](docs/myriad-dashboard.md)
* [Distribution of Node Manager Binaries](docs/myriad-remote-distribution-configuration.md)
* [Fine Grained Scaling](docs/myriad-fine-grained-scaling.md)
* [Deploying the Myriad Resource-Manger using Docker](docker/README.md)
* [Mesos, YARN and cgroups](docs/cgroups.md)

For up-to-date documentation, see [Apache Myriad](https://cwiki.apache.org/confluence/display/MYRIAD/Myriad+Home) on the wiki.

## Build Myriad
Please refer to the [build](docs/myriad-dev.md#step-1-build-myriad) section for steps to build Myriad.

## Roadmap
Please keep checking this section for updates.

- [x] NodeManager Profiles
- [x] Scale up/down Node Managers via REST API/Web UI
- [x] Framework re-conciliation & HA
- [x] ResourceManager failover/discovery using Marathon/Mesos-DNS
- [x] Fine-grained scaling
- [x] Remote distribution of NodeManager binaries
- [x] Framework checkpointing
- [x] Launch Job History Server
- [x] Constraints based Node Manager placement
- [x] Docker support
- [x] Support multi-tenancy for Node Managers
- [ ] Configuration store for storing rules and policies for clusters managed by Myriad

## Mailing Lists

* [Development Mailing List](mailto:dev@myriad.incubator.apache.org) ([Archive](https://mail-archives.apache.org/mod_mbox/myriad-dev/)) ([Subscribe](mailto:dev-subscribe@myriad.incubator.apache.org))

## Videos and Slides
* MesosCon 2014 - Running YARN alongside Mesos [(video)](https://www.youtube.com/watch?v=d7vZWm_xS9c) [(slides)](https://speakerdeck.com/mohit/running-yarn-alongside-mesos-mesoscon-2014)
* Mesos User Group, March 2015 - Myriad: Integrating Hadoop into the Datacenter [(video)](http://www.youtube.com/watch?v=UMu9n4f62GI)
* MesosCon, Seattle 2015 - Resource Sharing Beyond Boundaries [(video)](https://www.youtube.com/watch?v=lU2VE08fOD4) [(slides)](http://events.linuxfoundation.org/sites/events/files/slides/Apache_Myriad_MesosCon_2015.pdf)

## License

Apache Myriad is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

For additional information, see the LICENSE and NOTICE files.

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
