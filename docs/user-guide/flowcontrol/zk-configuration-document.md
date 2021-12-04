# 基于ZK配置流控规则

### 1、环境准备

- [Open Jdk](http://openjdk.java.net/) / [Oracle Jdk](https://www.oracle.com/java/technologies/downloads/) / Huawei Jdk
- [Zookeeper](https://zookeeper.apache.org/releases.html)
- [Maven](https://maven.apache.org/)
- [Demo应用](../../../javamesh-plugins/javamesh-flowcontrol/flowcontrol-demos/flowcontrol-demo)

### 2、下载源码并打包插件

#### 2.1 **下载源码**

```shell
git clone -b develop https://github.com/huaweicloud/java-mesh.git
```

#### 2.2 **打包插件**

```shell
mvn clean package -Dpmd.skip=true -Dtest.skip=true -Drat.skip=true
```

完成打包后，在`JavaMesh`根目录生成目录`javamesh-agent-2.0.5`

### 3、配置配置中心

配置`JavaMesh`配置中心类型与地址，配置文件路劲`/JavaMesh/javamesh-agent-2.0.5/agent/config/config.properties`

```properties
dynamicconfig.timeout_value=30000
dynamicconfig.default_group=java-mesh
# Zookeeper配置中心地址
dynamicconfig.zookeeper_uri=zookeeper://127.0.0.1:2181
# 配置中心类型
# ZOO_KEEPER: zookeeper配置中心
# KIE: kie配置中心
dynamicconfig.dynamic_config_type=ZOO_KEEPER
# 多个地址使用逗号隔开，例如http://127.0.0.1:30110,http://127.0.0.1:30111
dynamicconfig.kie_url=http://127.0.0.1:30110
```

### 4、打包并启动应用

#### 4.1 打包[Demo应用](../../../javamesh-plugins/javamesh-flowcontrol/flowcontrol-demos/flowcontrol-demo)

#### 4.2 启动应用

```shell
java -javaagent:JavaMesh路径\javamesh-agent-2.0.5\agent\javamesh-agent.jar=appName=flowControlDemo -Dproject.name=flowControlDemo -jar FlowControlDemo.jar
```

其中:

- **JavaMesh路径:** 即源码路径
- **project.name:** 指定监听的服务名

#### 4.3 验证应用是否可用

访问地址

- **localhost:8080/flow**  流控接口，返回`I am flow`
- **localhost:8080/degrade** 熔断测试接口, 返回`I am degrader`

### 5、配置规则

规则说明参考[流控文档](./flowcontrol.md#2配置流控规则)

#### 5.1 确定ZK配置路径

首先需确定当前的应用服务名，即`${project.name}`指定的服务名，根据不同的限流规则配置不同的子节点，路径如下：

- 流控  `FlowRule`, 则ZK配置路径为`/service=${project.name}/FlowRule`
- 熔断  `DegradeRule`, 则ZK配置路径为`/service=${project.name}/DegradeRule`

#### 5.2 配置规则

1. 进入zookeeper客户端

   参考如下命令:

   ```shell
   zkCli.sh -server localhost:2181
   ```

2. 创建对应该应用的根路径

   参考如下命令:

   ```shell
   create /service=flowControlDemo ""
   ```

3. 配置流控规则

   流控规则如下：

   ```json
   [{"grade":1,"resource":"/flow","count":1}]
   ```

   规则解释: 即针对接口`/flow`1秒内通过的请求数超过1QPS，触发流控

   配置规则路径命令如下：

   ```shell
   create /service=flowControlDemo/FlowRule [{"grade":1,"resource":"/flow","count":1}]
   ```

   其中`FlowRule`为流控路径名称，不可修改

4. 配置熔断规则

   规则内容如下：

   ```json
   [{"grade":0,"resource":"/degrade","slowRatioThreshold":0.1,"timeWindow":10,"statIntervalMs":10000,"minRequestAmount":3,"count":100.0}]
   ```

   规则解释：针对`/degrade`接口，在10S内，若超过100毫秒的请求接口比例超过10%，则触发熔断

   配置规则路径命令如下：

   ```shell
   create /service=flowControlDemo/DegradeRule [{"grade":0,"resource":"/degrade","slowRatioThreshold":0.1,"timeWindow":10,"statIntervalMs":10000,"minRequestAmount":3,"count":100.0}]
   ```

   

### 6、验证规则是否生效

请求`/flow`, `/degrade`分别测试流控与熔断能力

当请求被限流时，响应`flow limited`

当请求被熔断时， 响应`Degraded and blocked`, 当经过timeWindow后才可再次尝试请求
