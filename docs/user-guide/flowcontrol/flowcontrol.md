# FlowControl

本文档主要介绍[流控插件](../../../sermant-plugins/sermant-flowcontrol)以及该插件的使用方法



## 功能

流控插件基于[Alibaba Sentinel](https://github.com/alibaba/Sentinel)与[resilience4j](https://github.com/resilience4j)框架，以"流量"切入点，实现"无侵入式"流量控制；当前支持**流控**、**熔断**与**隔离仓**能力，并且支持配置中心动态配置规则，实时生效。

- **流控**：对指定接口限制1S秒内通过的QPS，当1S内流量超过指定阈值，将触发流控，限制请求流量。
- **熔断**：对指定接口配置熔断策略，可从单位统计时间窗口内的错误率或者慢请求率进行统计，当请求错误率或者慢请求率达到指定比例阈值，即触发熔断，在时间窗口重置前，隔离所有请求。
- **隔离仓**：针对大规模并发流量，对并发流量进行控制，避免瞬时并发流量过大导致服务崩溃。
- **重试：**当服务遇到非致命的错误时，可以通过重试的方式避免服务的最终失败。特别注意的是，该能力仅基于[resilience4j](https://github.com/resilience4j)框架才可生效。



## 使用说明

### 环境准备

**（1）部署ServiceCenter环境与Kie环境**

**（2）打包编译Sermant Agent**

​	参考[Sermant源码编译](../../../QuickStart.md#源码编译)

### 配置agent

**（1）修改服务注册信息**

找到[config.properties]()文件，修改如下配置

```properties
# 服务app名称， 其中sermant.agent.service.meta.application为环境变量名，可通过环境变量指定
service.meta.application=${sermant.agent.service.meta.application:default}
# 注册服务版本
service.meta.version=${sermant.agent.service.meta.version:1.0.0}
# ServiceComb命名空间
service.meta.project=${sermant.agent.service.meta.project:default}
# 环境
service.meta.environment=${sermant.agent.service.meta.environment:development}
```

**（2）配置注册插件**

修改配置文件`${javaagent路径}/config/register-center/config/config.yaml`， 修正servicecomb注册中心地址

```yaml
servicecomb.service:
  address: http://127.0.0.1:30100 #注册中心地址
```

**（3）配置流控插件**

修改配置文件`${javaagent路径}/config/flowcontrol/config/config.yaml`

```yaml
flow.control.plugin:
  useCseRule: ${plugin.flowcontrol.adapte.cse:true} # 是否开启ServiceComb适配
  useAgentConfigCenter: ${plugin.flowcontrol.use.agent.config_center:true} # 使用agent配置中心
```

### 部署应用

执行以下命令启动应用

```shell
# 其中agent路径指打包后的路径
# serviceName值应用名
# applicationName即对应app名称
# environment即对应环境名称
# xxx.jar值打包后应用jar包
java -javaagent:${agent路径}/sermant-agent.jar=appName=${serviceName} -Dsermant.agent.service.meta.application=${applicationName} -Dsermant.agent.service.meta.environment=${environment}  -jar xxx.jar
```

### 验证应用部署

登录[Service Center](localhost:30103)后台, 查看应用是否正确注册

### 配置流控规则

如何发布规则请参考[配置中心API]()

限流规则各参数配置项配置说明如下：

| 配置参数 | 说明                                                 |
| -------- | ---------------------------------------------------- |
| key      | 配置键                                               |
| group    | 配置的标签组                                         |
| content  | 配置内容，即具体的规则配置，其格式均为**`yaml`**格式 |

> 其中**group**的配置格式为k1=v1, 多个值使用"&"分隔，例如k1=v1&k2=v2, 代表该key绑定的标签组

**以下配置以`app=region-A`,` serviceName=flowControlDemo`, `environment=testing`举例**

- #### 流量标记配置示例

  ```json
  {
      "key":"servicecomb.matchGroup.scene",
      "group":"app=region-A&service=flowControlDemo&environment=testing",
      "content":"alias: scene\nmatches:\n- apiPath:\n    exact: /flow\n  headers: {}\n  method:\n  - POST\n  name: rule1\n"
  }
  ```

  

  **content配置项说明**

  ```yaml
  alias: flowcontrol  # 别名
  matches:            # 匹配器集合，可配置多个
  - apiPath:          # 匹配的api路径， 支持各种比较方式，相等(exact)、包含(contains)等
      exact: /degrade # 具体匹配路径
    headers: {}       # 请求头
    method:           # 支持方法类型
    - GET
    name: degrade     # 可选，配置名
  ```

  

  **规则解释:**

  - 请求路径为`/degrade`且方法类型为`GET`即匹配成功
  - 针对app为region-A，服务名为flowControlDemo且环境为testing的服务实例生效

  

  > 详细配置项可参考[ServiceComb开发文档](http://servicecomb.gitee.io/servicecomb-java-chassis-doc/java-chassis/zh_CN/references-handlers/governance.html#_2)流量标记部分
  >
  >   
  >
  > **注意事项：**
  >
  > - 流控配置首先需配置业务场景，再配置与业务场景绑定的流控规则
  > - `key`必须以`servicecomb.matchGroup.`为前置，`scene`则为业务名称

    

- #### **流控规则配置示例**

  ```json
  {
      "key":"servicecomb.rateLimiting.scene",
      "group":"app=region-A&service=flowControlDemo&environment=testing",
      "content":"limitRefreshPeriod: \"1000\"\nname: flow\nrate: \"2\"\n"
  }
  ```

  

  **流控配置项说明：**

  |       配置项       |                             说明                             |
  | :----------------: | :----------------------------------------------------------: |
  | limitRefreshPeriod | 单位统计时间，单位毫秒,  若需配置秒则可增加单位`S`， 例如`10S` |
  |        rate        |              单位统计时间所能通过的**请求个数**              |

  

  **规则解释：**

  - 针对app为region-A，服务名为flowControlDemo且环境为testing的服务实例生效

  - 1秒内超过2个请求，即触发流控效果

       

  > **注意事项：**
  >
  > `key`必须以`servicecomb.rateLimiting.`为前置，`scene`则为业务名称，确保与流量标记的业务场景名称一致

- #### **熔断规则配置示例**

  ```json
  {
      "key":"servicecomb.circuitBreaker.scene",
      "group":"app=region-A&service=flowControlDemo&environment=testing",
      "content":"failureRateThreshold: 90\nminimumNumberOfCalls: 3\nname: degrade\nslidingWindowSize: 10S\nslidingWindowType: time\nslowCallDurationThreshold: \"1\"\nslowCallRateThreshold: 80\n"
  }
  ```

  

  **熔断配置项说明：**

  |          配置项           |                             说明                             |
  | :-----------------------: | :----------------------------------------------------------: |
  |   failureRateThreshold    |                     熔断所需达到的错误率                     |
  |   minimumNumberOfCalls    |                    滑动窗口内的最小请求数                    |
  |           name            |                     配置项名称，可选参数                     |
  |     slidingWindowSize     | 滑动统计窗口大小，支持毫秒与秒，例如`1000`为1000毫秒, `10S`代表10秒 |
  |     slidingWindowType     |                滑动窗口类型，目前只支持`time`                |
  | slowCallDurationThreshold |                慢请求阈值，单位同滑动窗口配置                |
  |   slowCallRateThreshold   |         慢请求占比，当慢调用请求数达到该比例触发通断         |

  

  **规则解释:** 

  - 针对app为region-A，服务名为flowControlDemo且环境为testing的服务实例生效
  - 10秒内，若请求个数超过3个，且错误率超过90%或者慢请求占比超过80%则触发熔断

  

  > **注意事项：**
  >
  > `key`必须以`servicecomb.circuitBreaker.`为前置，`scene`则为业务名称，确保与流量标记的业务场景名称一致

- #### 隔离仓规则配置示例

  ```json
  {
      "key":"servicecomb.bulkhead.scene",
      "group":"app=region-A&service=flowControlDemo&environment=testing",
      "content":"maxConcurrentCalls: \"5\"\nmaxWaitDuration: \"10S\"\nname: \"隔离仓\"\n"
  }
  ```

  **隔离仓配置项说明：**

  |       配置项       |                             说明                             |
  | :----------------: | :----------------------------------------------------------: |
  | maxConcurrentCalls |                          最大并发数                          |
  |  maxWaitDuration   | 最大等待时间，若线程超过`maxConcurrentCalls`，会尝试等待，若超出等待时间还未获取资源，则抛出隔离仓异常 |
  |        name        |                        可选，配置名称                        |

  **规则解释:** 

  - 针对app为region-A，服务名为flowControlDemo且环境为testing的服务实例生效
  - 若最大并发数超过5，且新的请求等待10S，还未获取资源，则触发隔离仓异常

  

  > **注意事项：**
  >
  > `key`必须以`servicecomb.bulkhead.`为前置，`scene`则为业务名称，确保与流量标记的业务场景名称一致

## 快速开始

### 1、编译打包

通过[此处](https://github.com/huaweicloud/Sermant/releases)下载agent源码包, 并下载[Demo应用](../../../sermant-plugins/sermant-flowcontrol/flowcontrol-demos/flowcontrol-demo)

执行以下maven命令对agent进行打包

```shell
mvn clean package -Dmaven.test.skip -Pagent
```

执行以下maven命令对Demo应用执行打包

```shell
mvn clean package
```

### 2、启动应用

```shell
java -javaagent:${agent路径}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=FlowControlDemo -Dsermant.agent.service.meta.application=region-A -Dsermant.agent.service.meta.environment=testing -Dspring.application.name=FlowControlDemo -jar FlowControlDemo.jar
```

### 3、配置规则

参考[配置流控规则](#配置流控规则)配置**流量标记**与**流控规则**

**流量标记:**

```json
{
    "key":"servicecomb.matchGroup.sceneFlow",
    "group":"app=sc&service=flowControlDemo&environment=testing",
    "content":"alias: scene\nmatches:\n- apiPath:\n    exact: /flow\n  headers: {}\n  method:\n  - POST\n  name: flow\n"
}
```

**流控规则：**

```json
{
    "key":"servicecomb.rateLimiting.scene",
    "group":"app=region-A&service=flowControlDemo&environment=testing",
    "content":"limitRefreshPeriod: \"2S\"\nname: flow\nrate: \"4\"\n"
}
```

### 4、验证结果

多次请求`localhost:12000/flow`, 若在2秒内请求数超过4个时返回`flow limited`，则触发流控成功



## FAQ

#### 如何确定拦截的资源名是什么？

答：Spring与Dubbo的资源定义名不同：

**Spring**应用通过相对路径获取资源名，例如`localhost:8080/flow`,则定义的资源名为`/flow`。

**Dubbo**应用则是通过**请求的接口:接口版本.请求方法**拼凑，例如版本为`1.0.0`的请求接口`com.huawei.demo.TestService.hello`， 则拿到的最终定义的资源名为`com.huawei.demo.TestService:1.0.0.hello`

#### 启动时为什么会报HttpHostConnectException异常

答：出现该异常的原因是未启动`Sermant`后台服务`sermant-backhend`, 找到启动类`com.huawei.apm.backend.NettyServerApplication`启动后台服务，并重启应用即可。

## 其他

如果读者**希望不使用配置中心API且不去适配ServiceComb**，可参考下面的文档

- [基于KIE配置中心配置限流规则](./kie-configuration-document.md)
- [基于ZOOKEEPER配置中心配置限流规则](zk-configuration-document.md)



[返回**Sermant**说明文档](../../README.md)
