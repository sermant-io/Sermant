#! /bin/bash

#灰度策略
strategy=$1
# 环境
environment=$2

bash apache-zookeeper-3.8.0-bin/bin/zkCli.sh -server 127.0.0.1:2181 <<EOF
create /app=default&environment=${environment}
create /app=default&environment=${environment}/sermant.plugin.registry "strategy: ${strategy}"
get /app=default&environment=/sermant.plugin.registry
quit
EOF
