# Myriad development

## Build instructions
System requirements:
* JDK 1.8+
* Gradle

To build project run:
```bash
gradle build
```

To build a self-contained jar, run:
```bash
gradle capsule
```

To run project:
```bash
export MESOS_NATIVE_JAVA_LIBRARY=/usr/local/lib/libmesos.so
# On Mac: export MESOS_NATIVE_JAVA_LIBRARY=/usr/local/lib/libmesos.dylib
java -Dmyriad.config=location/of/config.yml -jar myriad-capsule-x.x.x.jar
```

## Sample config

```yaml
mesosMaster: localhost:5050
checkpoint: false
frameworkFailoverTimeout: 43200000
frameworkName: MyriadAlpha
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
```
