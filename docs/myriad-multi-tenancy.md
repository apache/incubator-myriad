# Myriad Multi-tenacy

Myriad supports multi-tenancy in the following ways:
- Myriad uses Mesos to assign ports for the Node Managers.  This means if to Node Managers run on the same host, they will both be able to 
bind to a port and communicate
- Myriad provides mechanisms to distribute the hadoop binaries and configurations using [tarballs](https://github.com/apache/incubator-myriad/blob/master/docs/myriad-remote-distribution-configuration.md) or [Docker](https://github.com/apache/incubator-myriad/blob/master/docker/README.md).

This means it's entirely possible to run multiple Myriad Clusters on the same hardware. One potential issue to be aware of however is it 
is often important when configuring yarn to use multiple directories.  If one needs to do this one should be careful and not use the directories 
for to different Myriad frameworks.