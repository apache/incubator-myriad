echo
echo "This Vagrant environment is ready for the following settings:"
echo

[ -z $MESOS_ARCH ] || echo "- MESOS_ARCH: $MESOS_ARCH"
[ -z $HADOOP_VERSION ] || echo "- HADOOP_VERSION: $HADOOP_VERSION"
[ -z $ZOOKEEPER_VERSION ] || echo "- ZOOKEEPER_VERSION: $ZOOKEEPER_VERSION"

echo
echo "'vagrant up --provider=libvirt' and happy hacking!"
