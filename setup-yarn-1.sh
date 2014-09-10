set -e

HADOOP_VER=2.5.0

cd ~/

sudo apt-get update

sudo apt-get install openssh-server

# Add hduser user
sudo addgroup hadoop
sudo adduser --ingroup hadoop hduser
sudo adduser hduser sudo

# Setup password-less auth
sudo -u hduser ssh-keygen -t rsa -P ''
sudo sh -c 'cat /home/hduser/.ssh/id_rsa.pub >> /home/hduser/.ssh/authorized_keys'