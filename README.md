# Myriad

[![Build Status](https://travis-ci.org/mesos/myriad.svg)](https://travis-ci.org/mesos/myriad)

Myriad is a mesos framework designed for scaling a YARN cluster on Mesos. Myriad can expand or shrink the resources managed by a YARN cluster in response to events as per configured rules and policies.

The name _Myriad_ means, _countless or extremely great number_. In context of the project, it allows one to expand overall resources managed by Mesos, even when the cluster under mesos management runs other cluster managers like YARN.

**Please note: Myriad is a work in progress, and should not be used in production at this point.**

## Getting started

* [How Myriad works](docs/how-it-works.md)
* [Developing Myriad](docs/myriad-dev.md)
* [Local vagrant setup guide](docs/vagrant.md)
* [Myriad REST API](docs/API.md)
* [Auto-scaling YARN cluster](docs/control-plane-algorithm.md)
* [Mesos, YARN and cgroups](docs/cgroups.md)
* [Myriad Dashboard Development](docs/myriad-dashboard.md)

## Roadmap
Myriad is a work in progress, please keep checking this section for updates.

- [x] Custom Executor for managing NodeManager
- [x] Support unique constraint to let only one node-manager run on a slave
- [x] Framework checkpointing
- [x] NodeManager Profiles
- [ ] High Availability mode for framework
- [ ] Framework re-conciliation
- [ ] Fine-grained scaling
- [ ] Support multi-tenancy for node-managers
- [ ] Configuration store for storing rules and policies for clusters managed by Myriad

## Videos and Slides
* MesosCon 2014 - Running YARN alongside Mesos [(video)](https://www.youtube.com/watch?v=d7vZWm_xS9c) [(slides)](https://speakerdeck.com/mohit/running-yarn-alongside-mesos-mesoscon-2014)
* Mesos User Group, March 2015 - Myriad: Integrating Hadoop into the Datacenter [(video)](http://www.youtube.com/watch?v=UMu9n4f62GI)
