# Hercules-ngrinder
基于NGrinder3.4.2开发的后端服务项目，已去掉NGrinder原生前段页面代码
## 快速开始
### 一. 依赖环境
    （1）JDK1.8  
    （2）Tomcat8.5以上
    （3）MySQL8.0
    （4）Maven 3.5以上
### 二. 中间件配置
#### 1. MySql配置
 创建名为hercules的数据库，该数据库无法自动创建，需要人工提前创建
```
create database `hercules` character set utf8 collate utf8_general_ci;

```
#### 2. Tomcat配置
 Tomcat除了正常配置外，为了满足能执行maven_project类型的脚本，需要在tomcat启动时添加maven.home参数
 执行命令：
```
vim ${tomcat_home}/bin/catalina.sh
```
 添加如下内容到文件里面：
```
# maven.home根据实际环境配置，这里只是举例
JAVA_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=200m -Dmaven.home=/usr/local/apache-maven-3.8.3"
#CATALINA_OPTS="-server -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8899"

# 以下路径根据实际环境配置，这里只是举例
export TOMCAT_HOME=/usr/local/apache-tomcat-8.5.71
export CATALINA_HOME=/usr/local/apache-tomcat-8.5.71
export CATALINA_BASE=/usr/local/apache-tomcat-8.5.71

```
#### 3. Maven配置
 maven需要在maven.home/conf/settings.xml中配置好仓库服务器和本地仓库
 以下内容为举例：
```
    <localRepository>/opt/maven-respository</localRepository>

    <mirrors>
        <!-- mirror
         | Specifies a repository mirror site to use instead of a given repository. The repository that
         | this mirror serves has an ID that matches the mirrorOf element of this mirror. IDs are used
         | for inheritance and direct lookup purposes, and must be unique across the set of mirrors.
         |
		-->

        <mirror>
            <id>nexus-aliyun-central</id>
            <name>aliyun4</name>
            <url>http://maven.aliyun.com/nexus/content/repositories/central/</url>
            <mirrorOf>central</mirrorOf>
        </mirror>
    </mirrors>
```
### 三. NGrinder Controller部署
#### 1. 新增nGrinder的home目录
```
mkdir /usr/local/nGrinder/ngrinder
mkdir /usr/local/nGrinder/ngrinder_ex
```

#### 2. 设置nGrinder的环境变量
这里是演示实例，所以只设置了一下临时变量，用户可考虑直接在/etc/profile配置成环境变量
```
export NGRINDER_HOME=/usr/local/nGrinder/ngrinder
export NGRINDER_EX_HOME=/usr/local/nGrinder/ngrinder_ex
```
**不配置以上变量会默认在用户home目录下面生成数据文件夹**
**~/.ngrinder和~/.ngrinder_ex**
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
database.url_option=useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Shanghai

# you should provide id / password who has a enough permission to create tables in the given db.
database.username=root
database.password=123456
```

#### 4. 在项目最上层目录使用maven命令打包
 代码仓地址
```
https://github.com/huaweicloud/Sermant/tree/develop
```
 改代码配置文件sermant-plugins/sermant-hercules/ngrinder/ngrinder-controller/src/main/resources/websocket.properties
```

# web端接收任务状态变动通知的接口uri
task.status.update.notify.uri=/argus/api/task/ws

# 需要接收任务改变通知的web地址，其实就是hercules前端集群机器的地址
websocket.notify.host=100.94.169.125:9091,100.94.169.124:9091
```
 修改配置之后使用maven打包,在sermant-plugins/sermant-hercules/ngrinder目录下执行命令
```mvn clean package -Dmaven.test.skip=true```
> 待打包完成之后，ngrinder-controller模块中的ngrinder-controller-3.4.2.war部署到tomcat即可。

### 三. 部署agent
#### 1. 在上面项目启动之后，压测引擎页面下载agent
![img.png](agent_download_guide.png)
#### 2. 上传agent的tar包到指定服务器之后解压，执行结果如下
```
[root@ecs-flow-0005 ngrinder-agent]# pwd
/opt/package/ngrinder-agent
[root@ecs-flow-0005 ngrinder-agent]# ll
total 496
-rw-r--r-- 1 root root    535 Oct 14 10:16 __agent.conf
drwxr-xr-x 2 root root   4096 Oct  8 11:26 lib
-rwxr-xr-x 1 root root    367 Aug 24 15:30 run_agent.bat
-rwxr-xr-x 1 root root     83 Aug 24 15:30 run_agent_bg.sh
-rwxr-xr-x 1 root root    237 Aug 24 15:30 run_agent_internal.bat
-rwxr-xr-x 1 root root     99 Aug 24 15:30 run_agent_internal.sh
-rwxr-xr-x 1 root root    312 Aug 24 15:30 run_agent.sh
-rw-r--r-- 1 root root 463149 Oct 28 20:06 run.log
-rwxr-xr-x 1 root root    135 Aug 24 15:30 stop_agent.bat
-rwxr-xr-x 1 root root    136 Aug 24 15:30 stop_agent.sh
[root@ecs-flow-0005 ngrinder-agent]#

```
#### 3. agent启动命令参数解析
```
Usage: run_agent_bg.sh [options]
  Options:
    -ah, --agent-home
       this agent's unique home path. The default is ~/.ngrinder_agent
    -ch, --controller-host
       controller host or ip.
    -cp, --controller-port
       controller port.
    -hi, --host-id
       this agent's unique host id
    -o, --overwrite-config
       overwrite overwrite the existing .ngrinder_agent/agent.conf with the
       local __agent.conf
    -r, --region
       region
    -s, --silent
       silent mode
    -v, --version
       show version
    -help, -?, -h
       prints this message
```
#### 4. 当然也可以通过配置文件启动，修改agent当前目录中的__agent.conf文件，内容如下，然后使用[run_agent_bg.sh -o -ah ${home_path}]启动
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
>${home_path}就是该agent配置和数据保存的目录，切记一定要指定清楚

到此处，nGrinder controller和agent就部署完毕！
