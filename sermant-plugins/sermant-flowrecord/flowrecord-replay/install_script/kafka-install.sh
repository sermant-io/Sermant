# kafka集群安装部署脚本
# 1.第一个参数version为kafka版本，第二个参数为安装目录绝对路径，第三个参数为当前broker.id的编号(最好与zk对应)，第四个参数为zk集群节点配置信息
# 2.脚本与安装包放在同一目录下，分别在每个节点执行该脚本
# 3.脚本执行方式：例如三个节点的第一个节点，sh kafka-install.sh 2.13-2.8.1 /home/kafka 0 100.10.126.140:2181,100.10.126.141:2181,100.10.126.142:2181
# 4.本脚本安装完成并执行kafka启动命令后结束

# 接收命令参数
version=$1
basePath=$2
brokerId=$3
zkInfos=$4

# 获取本机ip
ip=$(ifconfig -a | grep inet | grep -v 127.0.0.1 | grep -v inet6 | awk '{print $2}' | tr -d "addr:")

# 解压 kafka
function install() {
    if [ -d $basePath/kafka_$version ]; then
        echo "kafka无需解压安装，已存在"
    else
        if [ -f kafka_$version.tgz ]; then
            tar -zxvf kafka_$version.tgz -C $basePath
            echo "kafka解压安装完成"
        else
            echo "kafka安装包不在该目录下"
        fi
    fi
}

# 修改 server.properties
function kafkaConfig() {
    cd $basePath/kafka_$version/config/ || return
    sed -e "s@^zookeeper.connect=localhost:2181@zookeeper.connect=$zkInfos@" \
        -e "s@^broker.id=0@broker.id=$brokerId@" \
        -i.bak \
        server.properties
    echo listeners=PLAINTEXT://$ip:9092 >>server.properties
    echo "kafka集群配置完成"
}

# 启动kafka
function start() {
    cd $basePath/kafka_$version/bin || return
    sh kafka-server-start.sh -daemon ../config/server.properties
    echo "kafka已启动"
}

# 安装kafka
install
# kafka集群配置
kafkaConfig
# 启动本节点kafka
start
