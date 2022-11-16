#! /bin/bash

#灰度策略
strategy=$1
# 策略值
value=$2

bash apache-zookeeper-3.6.3-bin/bin/zkCli.sh -server 127.0.0.1:2181 <<EOF
create /app=default&environment=
create /app=default&environment=/sermant.plugin.router "strategy: ${strategy}"
create /app=default&environment=/sermant.plugin.registry "strategy: ${strategy}"
get /app=default&environment=/sermant.plugin.router
get /app=default&environment=/sermant.plugin.registry
quit
EOF