# zookeeper集群安装部署脚本
# 1.第一个参数version为zookeeper版本，第二个参数为安装目录绝对路径，第三个参数为当前zk节点的编号(从0开始)，第四个参数为集群节点配置信息
# 2.脚本与安装包放在同一目录下，分别在每个节点执行该脚本
# 3.脚本执行方式：例如三个节点的第一个节点，sh zookeeper-install.sh 3.6.3 /home/zookeeper 0 0=100.10.126.140:2888:3888,1=100.10.126.141:2888:3888,2=100.10.126.142:2888:3888
# 4.本脚本安装完成并执行zookeeper启动命令后结束

# 接收命令参数
version=$1
basePath=$2
myId=$3
serverInfos=$4

# 解压 zk
function install() {
  if [ -d $basePath/apache-zookeeper-$version-bin ]; then
    echo "zookeeper无需解压安装，已存在"
  else
    if [ -f apache-zookeeper-$version-bin.tar.gz ]; then
      tar -zxvf apache-zookeeper-$version-bin.tar.gz -C $basePath
      echo "zookeeper解压安装完成"
    else
      echo "zookeeper安装包不在该目录下"
    fi
  fi
}

function zkConfig() {
  #解析 serverInfo 成每个配置，添加到 zoo.cfg
  cd $basePath/apache-zookeeper-$version-bin/conf || return
  cp zoo_sample.cfg zoo.cfg
  echo "copy zoo.cfg."
  serverArray=(${serverInfos//,/ })
  for var in ${serverArray[@]}; do
    echo "server."$var >>zoo.cfg
  done
  echo "modify config."

  #创建集群的 myid 文件，并将本机的 myid 值写入
  if [ ! -d "/tmp/zookeeper" ]; then
    mkdir /tmp/zookeeper
  fi
  cd /tmp/zookeeper || return
  if [ ! -f "/tmp/zookeeper/myid" ]; then
    touch myid
    echo "create myid."
  fi
  echo $myId >myid
  echo "modify myid."
  echo "zk集群配置成功"
}

# 启动zookeeper
function start() {
  # 启动 zk（单节点启动后可能会显示启动失败，实际上已经启动，可通过查询进程确认，集群启动成功后再查看状态应该是正常的）
  cd $basePath/apache-zookeeper-$version-bin/bin || return
  sh zkServer.sh start
  sh zkServer.sh status
}

# 安装zookeeper
install
# 配置集群
zkConfig
# 启动zookeeper
start
