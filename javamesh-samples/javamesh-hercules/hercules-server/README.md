# Hercules-ngrinder
基于NGrinder3.4.3开发的后端服务项目，已去掉NGrinder原生前段页面代码
## 快速开始
### 一. 依赖环境
    （1）JDK1.8  
    （2）Tomcat8.5以上
    （3）MySQL8.0
### 二. NGrinder Controller部署
#### 1. 新增nGrinder的home目录
```
mkdir /usr/local/nGrinder/ngrinder
mkdir /usr/local/nGrinder/ngrinder_ex
```

#### 2. 设置nGrinder的环境变量
```
export NGRINDER_HOME=/usr/local/nGrinder/ngrinder
export NGRINDER_EX_HOME=/usr/local/nGrinder/ngrinder_ex
```

#### 3. 在/usr/local/nGrinder/ngrinder中新增配置文件database.conf
```
# H2 / cubrid / mysql can be set
database.type=mysql

# for cubrid. You should configure the following.
# database.url=localhost:33000:ngrinder

# for H2 remote connection, You should configure like followings.
# You can see how to run the H2 DB server by yourself in http://www.h2database.com/html/tutorial.html#using_server
# If this is not set, ngrinder will create the embedded DB.
# Specify database url
database.url=127.0.0.1:3306/hercules

# if you want to use HA mode in cubrid, you should enable following
# database.url_option=&althosts=secondary_ip:port

# you should provide id / password who has a enough permission to create tables in the given db.
database.username=root
database.password=123456
```

#### 4. 在项目最上层目录使用maven命令打包
```mvn clean package -Dmaven.test.skip=true```
> 待打包完成之后，把hercules-controller模块中的hercules-controller-0.0.1.war部署到tomcat即可。

### 三. 部署agent
#### 1. 新增ngrinder_agent的home目录
```
mkdir /usr/local/nGrinder/ngrinder_agent
```
#### 2. 设置ngrinder_agent的环境变量
```export NGRINDER_AGENT_HOME=/usr/local/nGrinder/ngrinder_agent```
#### 3. 在/usr/local/nGrinder/ngrinder_agent中新增配置文件agent.conf
```
common.start_mode=agent
# controller ip
agent.controller_host=127.0.0.1

# controller port for agent
agent.controller_port=16001

# agent region
agent.region=NONE

#agent.host_id=
#agent.server_mode=true

# provide more agent java execution option if necessary.
#agent.java_opt=
# set following false if you want to use more than 1G Xmx memory per a agent process.
#agent.limit_xmx=true
# please uncomment the following option if you want to send all logs to the controller.
#agent.all_logs=true
# some jvm is not compatible with DNSJava. If so, set this false.
#agent.enable_local_dns=false
```
#### 4. 启动ngrinder-agent
1) 将ngrinder-agent-3.4.2.tar文件上传到虚机任意位置
    >此tar包暂时还未支持页面下载
2) 解压ngrinder-agent-3.4.2.tar
3) 进入解压目录执行命令
```nohup sh run_agent.sh > run.log 2>&1 &```

到此处，nGrinder controller和agent就部署完毕！
