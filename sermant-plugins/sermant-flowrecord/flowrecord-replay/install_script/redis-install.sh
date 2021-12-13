# redis 集群安装部署脚本
# 1.第一个参数version为redis版本，第二个参数为安装目录绝对路径
# 2.脚本与安装包放在同一目录下，分别在每个节点执行该脚本
# 3.脚本执行方式：sh redis-install.sh 6.2.6 /home/redis
# 4.本脚本安装完成并执行redis启动命令后结束
# 5.在所有节点安装并启动之后，手动创建redis集群命令：redis-cli --cluster create {ip}:{port} {ip}:{port} --cluster-replicas 1
# 6.查询集群状态redis-cli -c -h {ip} -p {port} cluster nodes

# 接收参数
version=$1
basePath=$2

# 获取本机ip
ip=$(ifconfig -a | grep inet | grep -v 127.0.0.1 | grep -v inet6 | awk '{print $2}' | tr -d "addr:")

# 安装redis
function install() {
    if [ -d $basePath/redis-$version ] && [ -x $basePath/redis-$version/src/redis-server ]; then
        echo "redis无需编译"
    else
        if [ -f redis-$version.tar.gz ]; then
            tar -zxvf redis-$version.tar.gz -C $basePath
            cd $basePath/redis-$version || return
            make
            echo "redis已经编译安装完成"
        else
            echo "redis安装包不在该目录下"
        fi
    fi
}

# redis集群相关配置
function redisConfig() {
    cd $basePath/redis-$version || return
    dirname="6379 6380"

    for i in ${dirname}; do
        mkdir -p redis_cluster/${i}
        dodir=$basePath/redis-$version/redis_cluster/${i}
        cp $basePath/redis-$version/src/redis-server ${dodir}/
        cp redis.conf ${dodir}/
        sed -e "s@^port 6379@port ${i}@" \
            -e "s@^bind 127.0.0.1@bind ${ip}@" \
            -e "s@^protected-mode yes@protected-mode no@" \
            -e "s@^daemonize no@daemonize yes@" \
            -e "s@^# cluster-enabled yes@cluster-enabled yes@" \
            -e "s@^appendonly no@appendonly yes@" \
            -i.bak \
            redis_cluster/${i}/redis.conf
    done
    echo "redis集群配置完成"
}

function redisStart() {
    cd $basePath/redis-$version/redis_cluster/6379 || return
    redis-server redis.conf
    echo "启动redis 6379端口"
    sleep 2
    cd $basePath/redis-$version/redis_cluster/6380 || return
    redis-server redis.conf
    echo "启动redis 6380端口"

}

# 安装编译redis
install
# 配置redis集群
redisConfig
# 启动本节点redis实例
redisStart
