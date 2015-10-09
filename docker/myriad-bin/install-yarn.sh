#!/bin/bash


echo "Installing Yarn...."

##
# YARN installer script for Apache Myriad Deployment
##

#HADOOP_TARBALL_URL="http://172.31.1.11/hadoop-2.7.0.tar.gz"
HADOOP_VER="2.7.1"

HADOOP_TARBALL_URL=http://apache.osuosl.org/hadoop/common/hadoop-${HADOOP_VER}/hadoop-${HADOOP_VER}.tar.gz
HADOOP_TARBALL_URL=http://192.168.99.100/files/hadoop-2.7.1.tar.gz

if [ ! -z "$1" ];then
  echo "I FOUND THE ARGUMENT"
  HADOOP_TARBALL_URL=$1
  echo "Deleting previous hadoop home"
  rm -rf ${HADOOP_HOME}
  else
    echo "DIDNT FIND THE ARGUMENT. BAILING"
    #exit 1
fi




if [ -z ${HADOOP_TARBALL_URL} ];
then
  if [ -z ${HADOOP_VER} ];then
    echo "[FATAL] HADOOP_VER is not set. Unable to download hadoop tarball."
  fi
  HADOOP_TARBALL_URL=http://apache.osuosl.org/hadoop/common/hadoop-${HADOOP_VER}/hadoop-${HADOOP_VER}.tar.gz
fi

# Download the tarball
wget -O /opt/hadoop.tgz ${HADOOP_TARBALL_URL}
HADOOP_BASENAME=`basename ${HADOOP_TARBALL_URL} .tar.gz`

# Put in env defaults if they are missing
export HADOOP_GROUP=${HADOOP_GROUP:='hadoop'}
export HADOOP_USER=${HADOOP_USER:='hduser'}
export HADOOP_HOME=${HADOOP_HOME:='/usr/local/hadoop'}


# Add hduser user
groupadd $HADOOP_GROUP
useradd $HADOOP_USER -g $HADOOP_GROUP
#mkdir /home/${HADOOP_USER}
chown -R $HADOOP_USER:$HADOOP_GROUP /home/${HADOOP_USER}

# Extract Hadoop
tar vxzf /opt/hadoop.tgz -C /tmp
#mv /tmp/hadoop-${HADOOP_VER} ${HADOOP_HOME}
echo "Moving /tmp/hadoop-${HADOOP_BASENAME} to ${HADOOP_HOME}"
mv /tmp/${HADOOP_BASENAME} ${HADOOP_HOME}
ls -lath ${HADOOP_HOME}

mkdir /home/$HADOOP_USER
chown -R ${HADOOP_USER}:${HADOOP_GROUP} ${HADOOP_HOME}

# Init bashrc with hadoop env variables
sh -c 'echo export JAVA_HOME=/usr >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export PATH=\$PATH:\${HADOOP_HOME}/bin >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export PATH=\$PATH:\${HADOOP_HOME}/sbin >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_MAPRED_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_COMMON_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_HDFS_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export YARN_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_COMMON_LIB_NATIVE_DIR=\$\{HADOOP_HOME\}/lib/native >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_OPTS=\"-Djava.library.path=\${HADOOP_HOME}/lib\" >> /home/${HADOOP_USER}/.bashrc'


# Link Mesos Libraries
touch ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh
echo "export MESOS_NATIVE_JAVA_LIBRARY=/usr/local/lib/libmesos.so" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh

# Ensure the hadoop-env is executable
chmod +x ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh

echo "end of install-yarn.sh script"
