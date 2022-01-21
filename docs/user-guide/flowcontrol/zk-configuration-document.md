# 基于ZK配置流控规则

### 1、环境准备

- [Open Jdk](http://openjdk.java.net/) / [Oracle Jdk](https://www.oracle.com/java/technologies/downloads/) / Huawei Jdk
- [Zookeeper](https://zookeeper.apache.org/releases.html)
- [Maven](https://maven.apache.org/)
- [Demo应用](../../../sermant-plugins/sermant-flowcontrol/flowcontrol-demos/flowcontrol-demo)

### 2、下载源码并打包插件

#### 2.1 **下载源码**

```shell
git clone -b develop https://github.com/huaweicloud/Sermant.git
```

#### 2.2 **打包插件**

```shell
mvn clean package -Dpmd.skip=true -Dtest.skip=true -Drat.skip=true
```

完成打包后，在`Sermant`根目录生成目录`sermant-agent-x.x.x`

### 3、配置配置中心

配置`Sermant`配置中心类型与地址，配置文件路劲`/Sermant/sermant-agent-x.x.x/agent/config/config.properties`

```properties
dynamicconfig.timeout_value=30000
dynamicconfig.default_group=sermant
# Zookeeper配置中心地址
dynamicconfig.zookeeper_uri=zookeeper://127.0.0.1:2181
# 配置中心类型
# ZOO_KEEPER: zookeeper配置中心
# KIE: kie配置中心
dynamicconfig.dynamic_config_type=ZOO_KEEPER
# 多个地址使用逗号隔开，例如http://127.0.0.1:30110,http://127.0.0.1:30111
dynamicconfig.kie_url=http://127.0.0.1:30110
```

修改`${agent路径}/pluginPackge/flowcontrol/config/config.yaml`

```yaml
flow.control.plugin:
  flowFramework: SENTINEL
  useAgentConfigCenter: ${plugin.flowcontrol.use.agent.config_center:true} # 使用agent配置中心
```

### 4、打包并启动应用

#### 4.1 打包[Demo应用](../../../sermant-plugins/sermant-flowcontrol/flowcontrol-demos/flowcontrol-demo)

#### 4.2 启动应用

```shell
java -javaagent:Sermant路径\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=flowControlDemo -Dproject.name=flowControlDemo -jar FlowControlDemo.jar
```

其中:

- **Sermant路径:** 即源码路径
- **project.name:** 指定监听的服务名

#### 4.3 验证应用是否可用

访问地址

- **localhost:8080/flow**  流控接口，返回`I am flow`
- **localhost:8080/degrade** 熔断测试接口, 返回`I am degrader`

### 5、配置规则

#### 5.1 确定ZK配置路径

首先需确定当前的应用服务名，即`${project.name}`指定的服务名，根据不同的限流规则配置不同的子节点，路径如下：

- 流控  `FlowRule`, 则ZK配置路径为`/service=${project.name}/FlowRule`
- 熔断  `DegradeRule`, 则ZK配置路径为`/service=${project.name}/DegradeRule`
- 隔离仓 `IsolateRule`, 则ZK配置路径为`/service=${project.name}/IsolateRule`

> 说明：三类能力优先级：`流控 > 隔离仓 > 熔断`
>
> 若想单独测试一个能力，仅配置一项即可，或者配置不同的接口

#### 5.2 配置规则

1. **进入zookeeper客户端**

   参考如下命令:

   ```shell
   zkCli.sh -server localhost:2181
   ```

2. **创建对应该应用的根路径**

   参考如下命令:

   ```shell
   create /service=flowControlDemo ""
   ```

3. **配置流控规则**

   规则说明：

   | 规则配置项 | 说明                                                        |
   | ---------- | ----------------------------------------------------------- |
   | grade      | 整形，流控类型，0-基于线程数限流， 1-基于QPS限流            |
   | resource   | 字符串，拦截接口，例如`/flow`                               |
   | count      | 双精度，请求阈值，当单位时间内请求超过超过count时，触发流控 |

   

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

4. **配置熔断规则**

   规则说明：

   | 规则配置项         | 说明                                                         |
   | ------------------ | ------------------------------------------------------------ |
   | grade              | 整形，熔断规则类型，0-基于响应时间，1-基于异常率， 2-基于异常数 |
   | resource           | 同流控，例如`/degrade`                                       |
   | slowRatioThreshold | 双精度，慢调用比例，仅当grade=0场景生效                      |
   | timeWindow         | 整形，时间窗口，单位S，熔断恢复间隔，触发熔断后，经过timeWindow再次尝试请求 |
   | statIntervalMs     | 整形，单位统计时间，单位MS，每一个statIntervalMs作为熔断统计周期 |
   | minRequestAmount   | 同流控                                                       |

   

   规则内容如下：

   ```json
   [{"grade":0,"resource":"/degrade","slowRatioThreshold":0.1,"timeWindow":10,"statIntervalMs":10000,"minRequestAmount":3,"count":100.0}]
   ```

   规则解释：针对`/degrade`接口，在10S内，若超过100毫秒的请求接口比例超过10%，则触发熔断

   配置规则路径命令如下：

   ```shell
   create /service=flowControlDemo/DegradeRule [{"grade":0,"resource":"/degrade","slowRatioThreshold":0.1,"timeWindow":10,"statIntervalMs":10000,"minRequestAmount":3,"count":100.0}]
   ```

5. **配置隔离仓规则**

   规则说明：

   | 规则配置项         | 说明                                                         |
   | ------------------ | ------------------------------------------------------------ |
   | grade              | 整形，熔断规则类型，0-基于响应时间，1-基于异常率， 2-基于异常数 |
   | resource           | 同流控，例如`/degrade`                                       |
   | maxConcurrentCalls | 最大并发数                                                   |
   | maxWaitDuration    | 最大等待时间，若线程超过`maxConcurrentCalls`，会尝试等待，若超出等待时间还未获取资源，则抛出隔离仓异常 |

   

   规则内容如下：

   ```json
   [{"resource":"/degrade","maxWaitDuration":1000, "maxConcurrentCalls": 2}]
   ```

   规则解释：针对`/degrade`接口，若并发数超过2，且新的请求等待1S后未拿到资源，则触发隔离仓异常

   配置规则路径命令如下：

   ```shell
   create /service=flowControlDemo/IsolateRule [{"resource":"/degrade","maxWaitDuration":1000, "maxConcurrentCalls": 2}]
   ```

   

### 6、验证规则是否生效

请求`/flow`, `/degrade`分别测试流控与熔断能力

当请求被限流时，响应`flow limited`

当请求被熔断时， 响应`Degraded and blocked`, 当经过timeWindow后才可再次尝试请求