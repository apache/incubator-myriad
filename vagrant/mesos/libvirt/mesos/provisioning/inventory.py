#!/usr/bin/env python2
# -*- coding: utf-8 -*-
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

import yaml
import os
import sys
import paramiko
import socket
import time
import logging


def get_cluster_config_file():
    '''Returns the file used as MESOS architecture'''
    try:
        ret = os.environ["MESOS_ARCH"]
    except KeyError:
        logging.debug(
            "[Ansible] Please set environment variable MESOS_ARCH")
        sys.exit(1)

    return "config/cluster-" + ret + ".yaml"


def get_cluster_config_yml():
    '''Returns a list with hostname and IP'''
    with open(get_cluster_config_file(), 'r') as f:
        ret = yaml.load(f)

    return ret


def check_ssh(ip, user, key_file, initial_wait=0, interval=0, retries=1):
    logging.debug("[Ansible] checking SSH availability for %s", ip)
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    time.sleep(initial_wait)

    for x in range(retries):
        try:
            ssh.connect(ip, username=user, key_filename=key_file)
            return True
        except (paramiko.BadHostKeyException,
                paramiko.AuthenticationException,
                paramiko.SSHException,
                socket.error) as e:
            logging.debug(e)
            time.sleep(interval)

    return False


def check_ssh_available(cluster_yml):
    ssh_key_path = os.environ["HOME"] + "/.vagrant.d/insecure_private_key"

    # check if the hosts are ssh accesibles
    for item in cluster_yml:
        if check_ssh(item.get('ip'),
                     "vagrant",
                     ssh_key_path,
                     2, 2, 3):
            logging.debug(
                "[Ansible] %s: SSH is OK for provisioning", item)
        else:
            logging.debug("[Ansible] %s: SSH not ready", item)
            return False

    return True


logging.basicConfig(filename='.vagrant/inventory.log', level=logging.DEBUG)

all_vm_accesibles = False
logging.debug('[Ansible] getting host list from configuration')
cluster_yml = get_cluster_config_yml()


logging.debug("[Ansible] Sanity check loop for Ansible hosts")
while not all_vm_accesibles:
    logging.debug("[Ansible] Waiting for SSH to become available in all hosts")
    if check_ssh_available(cluster_yml):
        all_vm_accesibles = True


class InventoryTemplate:
    '''
    {
    "all": {
        "hosts": ["bt","a1","a2","p1","m1","m2","m3"],
        "vars": {
                    "ansible_user": "vagrant",
                    "ansible_become": "true"
                }
    },
    "build-host": {
        "hosts": ["build"]
    },
    "mesos-masters": {
        "hosts": ["master-m1","master-m3","master-m2"]
    },
    "mesos-agents": {
        "hosts": ["agent-a1","agent-a2"]
    },
    "_meta": {
        "hostvars": {
               "build": {"ansible_host": "192.168.121.56"},
               "master-a1": {"ansible_host": "192.168.121.30"},
               "master-a2": {"ansible_host": "192.168.121.248"},
               "master-a3": {"ansible_host": "192.168.121.112"},
               "agent-a1": {"ansible_host": "192.168.121.80"},
               "agent-a2": {"ansible_host": "192.168.121.105"},
               "agent-a3": {"ansible_host": "192.168.121.150"}
            }
        }
    }
    '''

    _template = """
    {
        "all": {
            "hosts": [%(_get_all|_pattern_a)s],
            "vars": {
                "ansible_user": "vagrant",
                "ansible_become": "true"
            }
        },
        "build-host": {
            "hosts": ["build"]
        },
        "mesos-masters": {
            "hosts": [%(_get_masters|_pattern_a)s]
        },
        "mesos-agents": {
            "hosts": [%(_get_agents|_pattern_a)s]
        },
        "_meta": {
            "hostvars": {
                %(_get_all_hostip|_pattern_b)s
            }
        }
    }
    """

    def __init__(self, dict={}):
        self.dict = dict

    def __str__(self):
        return self._template % self

    def __getitem__(self, key):
        return self._process(key.split("|"))

    def _process(self, l):
        arg = l[0]
        if len(l) == 1:
            if arg in self.dict:
                return self.dict[arg]
            elif hasattr(self, arg) and callable(getattr(self, arg)):
                return getattr(self, arg)()
            else:
                raise KeyError(arg)
        else:
            func = l[1]
            return getattr(self, func)(self._process([arg]))

    def _get_all(self):
        cad = []
        for i in cluster_yml:
            cad.append(i.get('name'))
        return cad

    def _get_masters(self):
        cad = []
        for i in cluster_yml:
            if i.get('type') == 'master':
                cad.append(i.get('name'))
        return cad

    def _get_agents(iself):
        cad = []
        for i in cluster_yml:
            if i.get('type') == 'agent':
                cad.append(i.get('name'))
        return cad

    def _get_all_hostip(self):
        cad = []
        for i in cluster_yml:
            cad.append("\"%s\": {\"ansible_host\": \"%s\"}" % (i.get('name'),
                                                               i.get('ip')))
        return cad

    def _pattern_a(self, l):
        return ",".join(["\"%s\"" % x for x in l])

    def _pattern_b(self, l):
        return ",".join(["%s" % x for x in l])


print InventoryTemplate()
