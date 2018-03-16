# -*- mode: ruby -*-
# # vi: set ft=ruby :

#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

VAGRANTFILE_API_VERSION = "2"

HADOOP_VERSION="2.7.0"
PRIVATE_IP="10.141.141.20"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/trusty64"
  config.vm.box_url = "https://vagrantcloud.com/ubuntu/boxes/trusty64"
  config.vm.network :private_network, ip: "#{PRIVATE_IP}"

  # Configure VM resources
  config.vm.provider :virtualbox do |vb|
    vb.name = "myriad-dev"
    vb.customize ["modifyvm", :id, "--memory", "4096"]
    vb.customize ["modifyvm", :id, "--cpus", "2"]
    vb.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
    vb.customize ["modifyvm", :id, "--natdnsproxy1", "on"]
  end

  if Vagrant.has_plugin?("vagrant-cachier")
    # Configure cached packages to be shared between instances of the same base box.
    config.cache.scope = :box
  end

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
  config.vm.network "forwarded_port", guest: 2181, host: 2181

  # install software
  config.vm.provision "shell", path: "vagrant/install_default_jdk.sh"
  config.vm.provision "shell", path: "vagrant/install_mesos.sh"
  config.vm.provision "shell", path: "vagrant/install_docker.sh"
  config.vm.provision "shell", path: "vagrant/install_hadoop.sh", args: ["#{HADOOP_VERSION}"]

  # start things up
  config.vm.provision "shell", path: "vagrant/start_mesos_master.sh"
  config.vm.provision "shell", path: "vagrant/start_mesos_slave.sh"
  config.vm.provision "shell", path: "vagrant/format_namenode.sh"
  config.vm.provision "shell", path: "vagrant/start_namenode.sh"
  config.vm.provision "shell", path: "vagrant/start_datanode.sh"
  config.vm.provision "shell", path: "vagrant/start_historyserver.sh"

end
