# Myriad - YARN Resource Manager
#
# VERSION 0.0.1

FROM centos
MAINTAINER Apache Myriad dev@myriad.incubator.apache.org

ENV HADOOP_VER="2.5.2"

# Install Hadoop & Dependencies
RUN yum -y update && yum install -y java-1.7.0-openjdk wget
RUN wget http://apache.osuosl.org/hadoop/common/hadoop-${HADOOP_VER}/hadoop-${HADOOP_VER}.tar.gz
RUN yum install -y tar

# Install Mesos
RUN rpm -Uvh http://repos.mesosphere.io/el/7/noarch/RPMS/mesosphere-el-repo-7-1.noarch.rpm
RUN yum -y install mesos

# Run local YARN install
ADD myriad-bin/install-yarn.sh /install-yarn.sh
RUN sh /install-yarn.sh

# Copy over myriad libraries
ADD /libs/myriad-executor-runnable-0.0.1.jar /usr/local/libexec/mesos/
ADD /libs/* /usr/local/hadoop/share/hadoop/yarn/lib/


# Initialize hadoop confs with env vars
ADD myriad-bin/run-myriad.sh /run-myriad.sh
RUN mkdir /myriad-conf/

# Run the YARN resource manager
CMD sh /run-myriad.sh 
