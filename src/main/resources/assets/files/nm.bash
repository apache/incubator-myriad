#!/usr/bin/env bash

export ROLE='hduser'
export YARN_HOME="/usr/local/hadoop"
export YARN_SBIN=YARN_HOME + "/sbin"
export BASHRC = '/home/' + ROLE + '/.bashrc'
export YARN_Cexport ONFIG=YARN_HOME+"/etc/hadoop"
export YARN_SITE=YARN_CONFIG+"/yarn-site.xml"
export SIZE=".tiny"

CGROUP_DIR_TASK="/sys/fs/cgroup/cpu/mesos/$MY_TASK_ID"
CGROUP_DIR_NM=CGROUP_DIR_TASK+'/hadoop-yarn'
configure = Process(
name = 'configure',
cmdline = "rm %s; cp %s %s" % (YARN_SITE, YARN_SITE+SIZE, YARN_SITE)
)

make_cgroups_dir = Process(
name = 'make_cgroups_dir',
cmdline = "MY_TASK_ID=`pwd | awk -F'/' '{ print $(NF-1) }'` && echo %s && echo 'hadoop' | sudo -S chown -R root:root %s && echo 'hadoop' | sudo -S chmod -R 777 %s && mkdir -p %s && echo 'hadoop' | sudo -S chown -R root:root %s && echo 'hadoop' | sudo -S chmod -R 777 %s" % (CGROUP_DIR_NM, CGROUP_DIR_TASK, CGROUP_DIR_TASK, CGROUP_DIR_NM, CGROUP_DIR_TASK, CGROUP_DIR_TASK)
)

configure_cgroups = Process(
name = 'configure_cgroups',
cmdline = "MY_TASK_ID=`pwd | awk -F'/' '{ print $(NF-1) }'` && echo 'hadoop' | sudo -S sed -i \"s@mesos.*/hadoop-yarn@mesos/$MY_TASK_ID/hadoop-yarn@g\" /usr/local/hadoop/etc/hadoop/yarn-site.xml"
)

start = Process(
name = 'start',
cmdline = "source %s; /usr/local/hadoop/bin/yarn nodemanager" % (BASHRC)
)

task = Task(
processes = [configure, make_cgroups_dir, configure_cgroups, start],
constraints = order(configure, make_cgroups_dir, configure_cgroups, start)
)

export(task)