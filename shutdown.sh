set -e

# Stop HistoryServer
sudo -u hduser sh -c '/usr/local/hadoop/sbin/mr-jobhistory-daemon.sh stop historyserver'

# Stop DataNode
sudo -u hduser sh -c '/usr/local/hadoop/sbin/hadoop-daemons.sh stop datanode'

# Stop NameNode
sudo -u hduser sh -c '/usr/local/hadoop/sbin/hadoop-daemon.sh stop namenode'
