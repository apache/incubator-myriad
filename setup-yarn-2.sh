set -e

HADOOP_VER=2.5.2

# Download Hadoop
cd ~
if [ ! -f hadoop-${HADOOP_VER}.tar.gz ]; then
	wget http://apache.osuosl.org/hadoop/common/hadoop-${HADOOP_VER}/hadoop-${HADOOP_VER}.tar.gz
fi

sudo tar vxzf hadoop-${HADOOP_VER}.tar.gz -C /usr/local
cd /usr/local
sudo mv hadoop-${HADOOP_VER} hadoop
sudo chown -R hduser:hadoop hadoop

# Init bashrc with hadoop env variables
sudo sh -c 'echo export JAVA_HOME=/usr/lib/jvm/java-8-oracle >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_INSTALL=/usr/local/hadoop >> /home/hduser/.bashrc'
sudo sh -c 'echo export PATH=\$PATH:\$HADOOP_INSTALL/bin >> /home/hduser/.bashrc'
sudo sh -c 'echo export PATH=\$PATH:\$HADOOP_INSTALL/sbin >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_MAPRED_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_COMMON_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_HDFS_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
sudo sh -c 'echo export YARN_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_COMMON_LIB_NATIVE_DIR=\$\{HADOOP_INSTALL\}/lib/native >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_OPTS=\"-Djava.library.path=\$HADOOP_INSTALL/lib\" >> /home/hduser/.bashrc'

# Modify JAVA_HOME in hadoop-env
cd /usr/local/hadoop/etc/hadoop
sudo -u hduser sed -i.bak s=\${JAVA_HOME}=/usr/lib/jvm/java-8-oracle/=g hadoop-env.sh
pwd

/usr/local/hadoop/bin/hadoop version

# Update configuration
sudo -u hduser sed -i.bak 's=<configuration>=<configuration>\<property>\<name>fs\.default\.name\</name>\<value>hdfs://localhost:9000\</value>\</property>=g' core-site.xml
sudo -u hduser sed -i.bak 's=<configuration>=<configuration>\<property>\<name>yarn\.nodemanager\.aux-services</name>\<value>mapreduce_shuffle</value>\</property>\<property>\<name>yarn.nodemanager.aux-services.mapreduce.shuffle.class</name>\<value>org\.apache\.hadoop\.mapred\.ShuffleHandler</value>\</property>=g' yarn-site.xml

sudo -u hduser cp mapred-site.xml.template mapred-site.xml
sudo -u hduser sed -i.bak 's=<configuration>=<configuration>\<property>\<name>mapreduce\.framework\.name</name>\<value>yarn</value>\</property>=g' mapred-site.xml

cd ~
sudo -u hduser sh -c 'mkdir -p mydata/hdfs/namenode'
sudo -u hduser sh -c 'mkdir -p mydata/hdfs/datanode'
sudo chown -R hduser:hadoop /home/hduser/mydata

cd /usr/local/hadoop/etc/hadoop
sudo -u hduser sed -i.bak 's=<configuration>=<configuration>\<property>\<name>dfs\.replication</name>\<value>1\</value>\</property>\<property>\<name>dfs\.namenode\.name\.dir</name>\<value>file:/home/hduser/mydata/hdfs/namenode</value>\</property>\<property>\<name>dfs\.datanode\.data\.dir</name>\<value>file:/home/hduser/mydata/hdfs/datanode</value>\</property>=g' hdfs-site.xml


# Format NameNode
sudo -u hduser sh -c '/usr/local/hadoop/bin/hdfs namenode -format'

# Start NameNode
sudo -u hduser sh -c '/usr/local/hadoop/sbin/hadoop-daemon.sh start namenode'

# Start DataNode
sudo -u hduser sh -c '/usr/local/hadoop/sbin/hadoop-daemons.sh start datanode'

# Start HistoryServer
sudo -u hduser sh -c '/usr/local/hadoop/sbin/mr-jobhistory-daemon.sh start historyserver'

# Check status
sudo -u hduser -u hduser jps
