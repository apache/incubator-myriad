# -*- mode: ruby -*-
# # vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

$provision_script = <<SCRIPT

PREFIX="PROVISIONER:"

set -e

echo "${PREFIX} Installing pre-reqs..."

# For installing Java 8
add-apt-repository ppa:webupd8team/java

# For Mesos
apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF
DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]')
CODENAME=$(lsb_release -cs)
echo "deb http://repos.mesosphere.io/${DISTRO} ${CODENAME} main" | sudo tee /etc/apt/sources.list.d/mesosphere.list

apt-get -y update
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
apt-get -y install oracle-java8-installer
apt-get -y install oracle-java8-set-default
apt-get -y install libcurl3
apt-get -y install zookeeperd
apt-get -y install aria2

MESOS_VERSION="0.21.1"
echo "${PREFIX}Installing mesos version: ${MESOS_VERSION}..."
apt-get -y install mesos
echo "Done"

ln -s /usr/lib/jvm/java-8-oracle/jre/lib/amd64/server/libjvm.so /usr/lib/libjvm.so

echo "${PREFIX}Starting mesos master"
start mesos-master

echo "${PREFIX}Starting mesos slave"
start mesos-slave

echo "${PREFIX}Successfully provisioned machine for Myriad development"

SCRIPT

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/trusty64"
  config.vm.box_url = "https://vagrantcloud.com/ubuntu/boxes/trusty64"

  # Consistent IP for demos and tutorials (10.141.141.10 is mesosphere playa address)
  config.vm.network :private_network, ip: "10.141.141.20"

  # Forward mesos master and slave ports
  config.vm.network "forwarded_port", guest: 5005, host: 5005
  config.vm.network "forwarded_port", guest: 5050, host: 5050
  config.vm.network "forwarded_port", guest: 5051, host: 5051

  # Forward myriad web and admin ports
  config.vm.network "forwarded_port", guest: 8080, host: 8080
  config.vm.network "forwarded_port", guest: 8081, host: 8081

  # Forward YARN/Hadoop ports
  config.vm.network "forwarded_port", guest: 50070, host: 50070
  config.vm.network "forwarded_port", guest: 50075, host: 50075
  config.vm.network "forwarded_port", guest: 8088, host: 8088
  config.vm.network "forwarded_port", guest: 8042, host: 8042
  config.vm.network "forwarded_port", guest: 19888, host: 19888
  config.vm.network "forwarded_port", guest: 8192, host: 8192

  config.vm.provision "shell", inline: $provision_script

  # Configure VM resources
  config.vm.provider :virtualbox do |vb|
    vb.customize ["modifyvm", :id, "--memory", "4096"]
    vb.customize ["modifyvm", :id, "--cpus", "2"]
  end
end
