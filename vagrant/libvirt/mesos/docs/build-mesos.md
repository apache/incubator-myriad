# Building Apache Mesos

Note: Last tested Mesos version for Myriad -> 0.28.1

Notes about build Apache Mesos and some development hacking notes.

# Building with autotools system

```
# Change working directory.
$ cd mesos

# Bootstrap (Only required if building from git repository).
$ ./bootstrap

# Configure and build.
$ mkdir build
$ cd build
$ ../configure
$ make

# Run test suite.
$ make check

# Install (Optional).
$ make install
```

# Building with new CMake system

```
curl -O https://cmake.org/files/v3.9/cmake-3.9.4-Linux-x86_64.tar.gz
tar xvzf cmake-3.9.4-Linux-x86_64.tar.gz 
export PATH=$PATH:$HOME/cmake-3.9.4-Linux-x86_64/bin

cd mesos
mkdir build && cd build
cmake ..
cmake --build .


For cleaning generated build system files:

rm build/CMakeCache.txt
```

# Running Apache Mesos

## Master

```
cd /home/vagrant/mesos.git/build
sudo ./bin/mesos-master.sh --ip=100.0.10.101 --work_dir=/var/lib/mesos
```

## Agents

```
cd /home/vagrant/mesos.git/build
sudo ./bin/mesos-agent.sh --master=100.0.10.101:5050 --work_dir=/var/lib/mesos
```

## Running with 3rdparty shipped Zookeeper

At master:

```
cd /home/vagrant/mesos.git/build/3rdparty/zookeeper-3.4.8
cp conf/zoo_sample.cfg conf/zoo.cfg
echo "server.1=mesos-m1:2888:3888" >> conf/zoo.cfg
bin/zkServer.sh start
echo ruok | nc 127.0.0.1 2181

sudo ./bin/mesos-master.sh --ip=100.0.10.101 --work_dir=/var/lib/mesos --zk=zk://mesos-m1:2181/mesos --quorum=1
```

At agents:

```
sudo ./bin/mesos-agent.sh --master=zk://mesos-m1:2181/mesos --work_dir=/var/lib/mesos
```

# Debugging

```
gdbserver -> exec ${LIBTOOL} --mode=execute gdbserver 100.0.10.101:1234 \
  /home/vagrant/mesos.git/build/src/mesos-master "${@}"


(gdb) target remote mesos-m1:1234
(gdb) continue
(gdb) monitor exit
```

# Development Environment with Vim


## Building clang-format

```
svn co http://llvm.org/svn/llvm-project/llvm/trunk llvm
cd llvm/tools
svn co http://llvm.org/svn/llvm-project/cfe/trunk clang
cd ../..
mkdir build
cd build
cmake -G "Unix Makefiles" ../llvm
```

## Code navigation: cscope and tags

```
sudo yum install cscope ctags -y

cd build
make cscope
make ctags
```

## Vimrc with clang-format for C++11 and Google Style

```
cd 
curl -O https://raw.githubusercontent.com/javiroman/reproducible-research/master/vim-tmux/vimrc
mv vimrc .vimrc
mkdir -p .vim/bundle
git clone https://github.com/VundleVim/Vundle.vim.git ~/.vim/bundle/Vundle.vim 
vim
:PluginInstall 
or from command line: vim +PluginInstall +qall
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
