# Myriad - YARN Resource Manager
#
# VERSION 0.0.1

FROM debian
MAINTAINER Apache Myriad dev@myriad.incubator.apache.org

ENV HADOOP_USER="yarn"
ENV HADOOP_VER="2.7.0"

# Setup mesosphere repositories
RUN apt-get -y update
RUN apt-get install -y openjdk-7-jre-headless wget lsb-release
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF
RUN DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]') CODENAME=$(lsb_release -cs) && echo "deb http://repos.mesosphere.com/${DISTRO} ${CODENAME} main" | tee /etc/apt/sources.list.d/mesosphere.list
RUN apt-get -y update

# Create the yarn user
ADD myriad-bin/create-user.sh /create-user.sh
RUN sh /create-user.sh

# Install Mesos
RUN apt-get install -y mesos curl tar

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
USER yarn
CMD sh /run-myriad.sh 
