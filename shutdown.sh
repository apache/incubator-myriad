set -e

# Start HistoryServer
sudo -u hduser sh -c '/usr/local/hadoop/sbin/mr-jobhistory-daemon.sh stop historyserver'

# Start DataNode
sudo -u hduser sh -c '/usr/local/hadoop/sbin/hadoop-daemons.sh stop datanode'

# Start NameNode
sudo -u hduser sh -c '/usr/local/hadoop/sbin/hadoop-daemon.sh stop namenode'
