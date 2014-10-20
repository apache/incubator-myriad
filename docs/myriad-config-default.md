# Myriad Configuration Properties

Myriad's component that plugs into Resource Manager, exposes configuration properties that admins can modify. 
It expects a file ```myriad-config-default.yml``` to be present on the Resource Manager's java classpath. 

* A good practice is to place this file under ```$YARN_HOME/etc/hadoop/```, since this directory holds 
YARN's configuration files and is already present on Resource Manager's classpath.

## Properties

```yaml

mesosMaster: 10.0.2.15:5050
checkpoint: false
frameworkFailoverTimeout: 43200000
frameworkName: MyriadAlpha
nativeLibrary: /usr/local/lib/libmesos.so
zkServers: localhost:2181
zkTimeout: 20000
profiles:
  small:
    cpu: 1
    mem: 1100
  medium:
    cpu: 2
    mem: 2048
  large:
    cpu: 4
    mem: 4096
rebalancer: false
nodemanager:
  jvmMaxMemoryMB: 1024
  user: hduser
  cpus: 0.2
  cgroups: false
executor:
  jvmMaxMemoryMB: 256
  path: file://localhost/usr/local/libexec/mesos/myriad-executor-0.0.1.jar
yarnEnvironment:
  YARN_HOME: /usr/local/hadoop

```