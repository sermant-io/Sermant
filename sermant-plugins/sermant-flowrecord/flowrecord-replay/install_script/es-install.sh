# es 集群安装部署脚本（以root用户执行）
# 1.第一个参数version为es版本，第二个参数为安装目录绝对路径，第三个参数为节点id（从1开始），第四个参数为集群所有节点ip（逗号隔开）
# 2.脚本与安装包放在同一目录下，分别在每个节点执行该脚本
# 3.脚本执行方式：例如三个节点的第一个节点， sh es-install.sh 7.15.1 /home/es 1 127.0.0.1,128.0.0.1,129.0.0.1
# 4.本脚本安装完成并以新创建的esuser用户执行es启动命令后结束
# 5.集群密码配置需额外手动配置

# 接收参数
version=$1
basePath=$2
nodeId=$3
hostList=$4

# 安装elasticsearch
function install() {
    if [ -d $basePath/elasticsearch-$version ]; then
        echo "elasticsearch无需解压安装，已存在"
    else
        if [ -f elasticsearch-$version-linux-x86_64.tar.gz ]; then
            tar -zxvf elasticsearch-$version-linux-x86_64.tar.gz -C $basePath
            echo "elasticsearch已安装完成"
        else
            echo "elasticsearch安装包不在该目录下"
        fi
    fi
}

# 修改 elasticsearch.yml
function esConfig() {
    cd $basePath/elasticsearch-$version/config || return
    echo "cluster.name: esCluster" >>elasticsearch.yml
    echo "node.name: node-$nodeId" >>elasticsearch.yml
    echo "node.master: true" >>elasticsearch.yml
    echo "node.data: true" >>elasticsearch.yml
    echo "node.max_local_storage_nodes: 3" >>elasticsearch.yml
    echo "network.host: 0.0.0.0" >>elasticsearch.yml
    echo "http.port: 9200" >>elasticsearch.yml
    echo "transport.tcp.port: 9300" >>elasticsearch.yml

    i=2
    nodes='"'"node-1"'"'
    serverArray=(${hostList//,/ })
    while (($i <= ${#serverArray[@]})); do
        nodes=${nodes}', ''"'"node-$i"'"'
        let "i++"
    done
    echo "cluster.initial_master_nodes: [$nodes]" >>elasticsearch.yml

    hosts=""
    for var in ${serverArray[@]}; do
        if [[ ${hosts} == "" ]]; then
            hosts='"'"${var}"'"'
        else
            hosts=${hosts}', ''"'"${var}"'"'
        fi
    done
    echo "discovery.seed_hosts: [$hosts]" >>elasticsearch.yml

    # 开启密码配置，待手动配置好密码之后取消注释
    echo "#xpack.security.enabled: true" >>elasticsearch.yml
    echo "#xpack.security.transport.ssl.enabled: true" >>elasticsearch.yml
    echo "#xpack.security.transport.ssl.verification_mode: certificate" >>elasticsearch.yml
    echo "#xpack.security.transport.ssl.keystore.path: elastic-certificates.p12" >>elasticsearch.yml
    echo "#xpack.security.transport.ssl.truststore.path: elastic-certificates.p12" >>elasticsearch.yml

    # 修改 vm.max_map_count
    echo vm.max_map_count=262144 >>/etc/sysctl.conf
    sysctl -p
}

# 创建用户并赋予elasticsearch-$version文件夹权限
function createUser() {
    # es不允许用root用户启动，因此创建一个esuser用户
    USER_COUNT=$(cat /etc/passwd | grep '^esuser:' -c)
    USER_NAME='esuser'
    if [ $USER_COUNT -ne 1 ]; then
        useradd $USER_NAME
        echo "123456" | passwd $USER_NAME --stdin
        echo "esuser创建成功"
    else
        echo 'esuser已存在'
    fi
    chown -R esuser:esuser $basePath/elasticsearch-$version
}

function start() {
    cd $basePath/elasticsearch-$version/bin || return
    sudo -u esuser ./elasticsearch -d
    echo "es已执行启动"
}

# 安装es
install
# 配置集群
esConfig
# 创建非root用户
createUser
# 启动es
start
