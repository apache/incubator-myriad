# Myriad Vagrant based testbed setup

This is a development environment for the Apache Myriad project based on
Vagrant and libvirt. 

You have to setup properly your environment in order to use Vagrant and
libvirt. Follow the steps in the nex section for different environments.

# Requeriments                   

Download Vagrant from HashiCorp site:

https://www.vagrantup.com/downloads.html

## For Ubuntu users              

```                              
$ sudo apt-get install software-properties-common                 
$ sudo apt-add-repository ppa:ansible/ansible                     
$ sudo apt-get update            
$ sudo apt-get install ansible   
$ sudo apt-get install qemu-kvm libvirt-bin libvirt-dev           
$ sudo apt-get install ruby-libvirt                               

$ vagrant plugin install vagrant-libvirt
Installing the 'vagrant-libvirt' plugin. This can take a few minutes...                                                             
$ vagrant plugin list            
vagrant-libvirt (0.0.40, system) 

$ sudo usermod -G libvirtd -a $USER                               
$ newgrp -                       
``` 

## For Fedora users              

``` 
dnf -y install qemu libvirt libvirt-devel ruby-devel gcc
dnf -y install libxslt-devel libxml2-devel libvirt-devel 
dnf -y install libguestfs-tools-c ruby-devel gcc
dnf -y install python2-yaml python2-paramiko
``` 

Testing Vagrant-libvirt:

``` 
$ mkdir test && cd test          
$ vagrant init centos/7          
$ vagrant up --provider libvirt  
$ vagrant box list 
centos/7                 (libvirt, 1702.01)                       
$ vagrant status                 
$ vagrant ssh                    
$ vagrant destroy                
$ cd .. && rm -fr test           
``` 

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
