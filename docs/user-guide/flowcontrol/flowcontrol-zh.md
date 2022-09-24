# FlowControl

[简体中文](flowcontrol-zh.md) | [English](flowcontrol.md)

本文档主要介绍[流控插件](../../../sermant-plugins/sermant-flowcontrol)以及该插件的使用方法

## 功能

流控插件基于[resilience4j](https://github.com/resilience4j)框架，以"流量"切入点，实现"无侵入式"流量控制；当前支持**限流**、**熔断**、**隔离仓**、**错误注入**与**重试**能力，并且支持配置中心动态配置规则，实时生效。

- **限流**：对指定接口限制1S秒内通过的QPS，当1S内流量超过指定阈值，将触发流控，限制请求流量。
- **熔断**：对指定接口配置熔断策略，可从单位统计时间窗口内的错误率或者慢请求率进行统计，当请求错误率或者慢请求率达到指定比例阈值，即触发熔断，在时间窗口重置前，隔离所有请求。
- **隔离仓**：针对大规模并发流量，对并发流量进行控制，避免瞬时并发流量过大导致服务崩溃。
- **重试**：当服务遇到非致命的错误时，可以通过重试的方式避免服务的最终失败。
- **错误注入:**  指在服务运行时，给指定服务配置错误注入策略，在客户端访问目标服务前，以指定策略模式返回。该策略多用于减少目标服务的访问负载，可作为降级的一种措施。

## 使用说明

### 环境准备

**（1）部署ServiceCenter环境与Kie环境**

**（2）打包编译Sermant Agent**

​ 参考[Sermant源码编译](../../QuickStart-zh.md#源码编译)

### 配置agent

**（1）修改服务注册信息**

找到`${javaagent path}/agent/config/config.properties`，修改如下配置

```properties
# 服务app名称
service.meta.application=default
# 服务版本
service.meta.version=1.0.0
# serviceComb 命名空间
service.meta.project=default
# 环境
service.meta.environment=development
# 自定义标签，按需配置，用于后续的配置订阅
service.meta.customLabel=public
service.meta.customLabelValue=default
```

**提示**：以上配置均可通过环境变量指定，对应的键值即为环境变量键，例如服务app名称可由`-Dservice.meta.application=application`指定, 其他配置文件的所有键均可采用该方式配置。

##### **（2）修改配置中心**

修改配置文件`${javaagent路径}/config/config.properties`, 修改配置中心类型与地址，如下位置：

```properties
# 配置中心地址， 根据配置中心地址配置
dynamic.config.server_address=127.0.0.1:30110
# 配置中心类型， 支持KIE与ZOOKEEPER
dynamic.config.dynamic_config_type=KIE
```

**（3）配置流控插件**

修改配置文件`${javaagent路径}/pluginPackage/flowcontrol/config/config.yaml`

```yaml
flow.control.plugin:
  useCseRule: true # 是否开启ServiceComb适配
```

开启适配后，插件将根据应用配置，服务配置以及自定义标签订阅配置中心配置

> 此处若设置useCseRule为false，流控插件将基于当前实例的服务名进行配置订阅，例如：用户配置的spring.application.name=flowControlDemo, 则在实际订阅时会根据标签service=flowControlDemo接收配置。

### 部署应用

执行以下命令启动应用

```shell
# 其中agent路径指打包后的路径
# serviceName值应用名
# applicationName即对应app名称
# environment即对应环境名称
# xxx.jar值打包后应用jar包
java -javaagent:${agent路径}/sermant-agent.jar=appName=${serviceName} -Dservice.meta.application=${applicationName} -Dservice.meta.environment=${environment}  -jar xxx.jar
```

### 验证应用部署

登录[Service Center](localhost:30103)后台, 查看应用是否正确注册

### 流控规则介绍

流量治理采用流量标记+流控规则的方式对指定的流量进行流控，所谓流量标记，通俗讲为请求信息，例如接口路径、接口方法类型、请求头以及下游服务名。流控规则的是否生效取决与流量标记，仅当流量标记与请求相匹配，流控规则才会实际生效。而如何将流量标记对应上具体规则，则取决于业务场景名，通常流量标记与流控规则配置均要配置指定前缀，例如流量标记的键需以`servicecomb.MatchGroup`为前缀, 而限流规则则以`servicecomb.rateLimiting`为前缀，以一个具体的例子：

流量标记配置键为：`servicecomb.MatchGroup.flow`

限流规则配置键为：`servicecomb.rateLimiting.flow`

如上，`flow`即为业务场景名，仅当两者业务场景名称一致，当请求匹配上流量标记时，限流规则才会生效

下面依次介绍相关配置：

- **流量标记**

  ```yaml
  matches:            # 匹配器集合，可配置多个
  - apiPath:          # 匹配的api路径， 支持各种比较方式，相等(exact)、包含(contains)等
      exact: /degrade # 具体匹配路径
    headers:          # 请求头
      key: 
        exact: value  # 请求头值，此处为key=value, 比较方式同apiPath
    method:           # 支持方法类型
    - GET
    name: degrade     # 可选，配置名
  ```

  **流量标记解释:**

  - 请求路径为`/degrade`且方法类型为`GET`, 同时满足要求请求头包含`key=value`即匹配成功

    详细配置项可参考[ServiceComb开发文档](http://servicecomb.gitee.io/servicecomb-java-chassis-doc/java-chassis/zh_CN/references-handlers/governance.html#_2)流量标记部分

  **流量标记请求路径（apiPath）配置说明**

  流量标记的请求路径会因不同的请求协议配置而存在差异，当前主要为Http（Spring）与Rpc（Dubbo）协议，下面分别说明两种请求协议的配置方式：

  - Http协议

    该协议依据请求路径进行匹配，例如请求路径为localhost:8080/test/flow, 则实际拿到的路径为`/test/flow`，因此若需设置匹配规则，需依据该路径进行配置。

    值得注意的是，如果用户配置的contextPath, 则需要加上contextPath前缀才可生效。

  - Rpc协议（Dubbo）

    该协议调用需要基于接口+方法，例如请求的接口为com.demo.test, 其方法为flow， 则对应的请求路径为`com.demo.test.flow`, 特别的，如果用户有配置接口的版本，例如指定的version为1.0.0， 则请求路径为`com.demo.test:1.0.0.flow`。同时需要配置请求方法为`POST`, RPC协议仅支持POST类型。

- **限流**

  | 配置项             | 说明                                                         |
  | ------------------ | ------------------------------------------------------------ |
  | limitRefreshPeriod | 单位统计时间，单位毫秒, 若需配置秒则可增加单位`S`， 例如`10S` |
  | rate               | 单位统计时间所能通过的**请求个数**                           |

- **熔断**

  | 配置项                    | 说明                                                         |
  | ------------------------- | ------------------------------------------------------------ |
  | failureRateThreshold      | 熔断所需达到的错误率                                         |
  | minimumNumberOfCalls      | 滑动窗口内的最小请求数， 超过最小请求数才开始判断熔断条件    |
  | name                      | 配置项名称，可选参数                                         |
  | slidingWindowSize         | 滑动统计窗口大小，支持毫秒与秒，例如`1000`为1000毫秒, `10S`代表10秒 |
  | slidingWindowType         | 滑动窗口类型，目前支持`time`与`count`两种类型，前者基于时间窗口统计，后者基于请求次数 |
  | slowCallDurationThreshold | 慢请求阈值，单位同滑动窗口配置                               |
  | slowCallRateThreshold     | 慢请求占比，当慢调用请求数达到该比例触发通断                 |
  | waitDurationInOpenState   | 熔断后恢复时间，默认`60S`                                    |

- **隔离**

  | 配置项             | 说明                                                         |
  | ------------------ | ------------------------------------------------------------ |
  | maxConcurrentCalls | 最大并发数                                                   |
  | maxWaitDuration    | 最大等待时间，若线程超过`maxConcurrentCalls`，会尝试等待，若超出等待时间还未获取资源，则抛出隔离仓异常 |
  | name               | 可选，配置名称                                               |

- **重试**

  | 配置项                | 说明                                                         |
  | --------------------- | ------------------------------------------------------------ |
  | waitDuration          | 重试等待时间，默认毫秒；支持秒单位，例如2S                   |
  | retryStrategy         | 重试策略，当前支持两种重试策略：固定时间间隔（FixedInterval）， 指数增长间隔(RandomBackoff) |
  | maxAttempts           | 最大重试次数                                                 |
  | retryOnResponseStatus | HTTP状态码，当前仅支持HTTP请求；针对dubbo请求，可通过配置异常类型确定是否需要重试，默认为RpcException |

- **错误注入**

  | 配置项       | 说明                                                         |
  | ------------ | ------------------------------------------------------------ |
  | type         | 错误注入类型, 目前支持abort(请求直接返回)与delay（请求延时） |
  | percentage   | 错误注入触发概率                                             |
  | fallbackType | 请求调用返回类型，仅`type=abort`生效。当前支持两种`ReturnNull`:直接返回空内容，状态码200；`ThrowException`: 按照指定错误码返回，关联配置`errorCode` |
  | errorCode    | 指定错误码返回, 默认500, 仅在`type=abort`且`fallbackType=ThrowException`生效 |
  | forceClosed  | 是否强制关闭错误注入能力, 当为true时，错误注入将不会生效。默认false |

### 配置流控规则

#### 基于配置文件配置流控规则

如果您的应用**非**SpringBoot应用， 该方式将不适用。

流控插件在应用启动时，会尝试的从SpringBoot加载的配置源读取流控规则以及对应的流量标记，用户需要在启动之前进行配置。如下为配置示例, 示例配置直接基于`application.yml`进行配置

```yaml
servicecomb:
  matchGroup:
    demo-fault-null: |
      matches:
        - apiPath:
            exact: "/flow"
    demo-retry: |
      matches:
        - apiPath:
            prefix: "/retry"
          serviceName: rest-provider
          method:
          - GET
    demo-rateLimiting: |
      matches:
        - apiPath:
            exact: "/flow"
    demo-circuitBreaker-exception: |
      matches:
        - apiPath:
            exact: "/exceptionBreaker"
    demo-bulkhead: |
      matches:
        - apiPath:
            exact: "/flowcontrol/bulkhead"
  rateLimiting:
    demo-rateLimiting: |
      rate: 1
  retry:
    demo-retry: |
      maxAttempts: 3
      retryOnResponseStatus:
      - 500
  circuitBreaker:
    demo-circuitBreaker-exception: |
      failureRateThreshold: 44
      minimumNumberOfCalls: 2
      name: 熔断
      slidingWindowSize: 10000
      slidingWindowType: time
      waitDurationInOpenState: 5s
  bulkhead:
    demo-bulkhead: |
      maxConcurrentCalls: 1
      maxWaitDuration: 10
  faultInjection:
    demo-fault-null: |
      type: abort
      percentage: 100
      fallbackType: ReturnNull
      forceClosed: false
```

以上配置，配置当前支持的流控规则，具体配置项请根据实际需要进行变更。

#### 基于sermant-backend统一配置接口发布规则

[backend]()服务提供配置发布功能, 通过请求接口`/publishConfig`发布配置，请求参数如下：

| 配置参数 | 说明                                             |
| -------- | ------------------------------------------------ |
| key      | 配置键                                           |
| group    | 配置的标签组                                     |
| content  | 配置内容，即具体的规则配置，其格式均为`YAML`格式 |

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


**规则解释:**

- 请求路径为`/flow`且方法类型为`GET`即匹配成功
- 针对app为region-A，服务名为flowControlDemo且环境为testing的服务实例生效



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
      "content":"failureRateThreshold: 90\nminimumNumberOfCalls: 3\nname: degrade\nslidingWindowSize: 10S\nslidingWindowType: time\nslowCallDurationThreshold: \"1\"\nslowCallRateThreshold: 80\nwaitDurationInOpenState: 10s"
  }
  ```
  **规则解释:**

    - 针对app为region-A，服务名为flowControlDemo且环境为testing的服务实例生效
    - 10秒内，若请求接口`/flow`次数超过3次，且错误率超过90%或者慢请求占比超过80%则触发熔断

  

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

  **规则解释:**

    - 针对app为region-A，服务名为flowControlDemo且环境为testing的服务实例生效
    - 针对接口`/flow`, 若最大并发数超过5，且新的请求等待10S，还未获取资源，则触发隔离仓异常

  

  > **注意事项：**
  >
  > `key`必须以`servicecomb.bulkhead.`为前置，`scene`则为业务名称，确保与流量标记的业务场景名称一致

- #### 重试规则配置示例

  ```json
  {
      "key":"servicecomb.retry.scene",
      "group":"app=region-A&service=flowControlDemo&environment=testing",
      "content":"waitDuration: \"2000\"\nretryStrategy: FixedInterval\nmaxAttempts: 2\nretryOnResponseStatus:\n- 500"
  }
  ```

  **规则解释：**

    - 针对app为region-A，服务名为flowControlDemo且环境为testing的服务实例生效
    - 针对接口`/flow`, 当请求抛出500异常时进行重试，直到重试成功或者达到最大重试次数

  

  > **注意事项：**
  >
  > `key`必须以`servicecomb.retry.`为前置，`scene`则为业务名称，确保与流量标记的业务场景名称一致

  **特别说明**：对于Dubbo重试， 流控插件采用替代原生的ClusterInvoker方式实现, 请求逻辑将采用流控插件自身实现的ClusterInvoker进行调用，而原宿主选择的将会失效（例如：dubbo默认的FailoverClusterInvoker）。如果此处仍需使用原来的ClusterInvoker，可通过添加环境变量`-Dflow.control.plugin.useOriginInvoker=true`实现， 但该方式可能存在少量的性能损失。

- #### 错误注入规则示例

  ```json
  {
      "key":"servicecomb.faultInjection.scene",
      "group":"app=region-A&service=flowControlDemo&environment=testing",
      "content":"type: abort\npercentage: 100\nfallbackType: ReturnNull\nforceClosed: false\nerrorCode: 503"
  }
  ```

  **规则解释：**

  - 针对app为region-A，服务名为flowControlDemo且环境为testing的服务实例生效
  - 当请求接口`/flow`时，100%将返回空

  

  > **注意事项：**
  >
  > `key`必须以`servicecomb.faultInjection.`为前置，`scene`则为业务名称，确保与流量标记的业务场景名称一致

## 快速开始

### 1、编译打包

通过[此处](https://github.com/huaweicloud/Sermant/releases)下载sermant软件包, 并下载[Demo源码](../../../sermant-plugins/sermant-flowcontrol/flowcontrol-demos/flowcontrol-demo)

执行以下maven命令对Demo应用执行打包

```shell
mvn clean package
```

### 2、启动应用

```shell
java -javaagent:${agent路径}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=FlowControlDemo -Dservice.meta.application=region-A -Dservice.meta.environment=testing -Dspring.application.name=FlowControlDemo -jar FlowControlDemo.jar
```

### 3、配置规则

参考[配置流控规则](#配置流控规则)配置**流量标记**与**流控规则**

**流量标记:**

```json
{
  "key": "servicecomb.matchGroup.sceneFlow",
  "group": "app=sc&service=flowControlDemo&environment=testing",
  "content": "alias: scene\nmatches:\n- apiPath:\n    exact: /flow\n  headers: {}\n  method:\n  - POST\n  name: flow\n"
}
```

**流控规则：**

```json
{
  "key": "servicecomb.rateLimiting.scene",
  "group": "app=region-A&service=flowControlDemo&environment=testing",
  "content": "limitRefreshPeriod: \"2S\"\nname: flow\nrate: \"4\"\n"
}
```

### 4、验证结果

多次请求`localhost:12000/flow`, 若在2秒内请求数超过4个时返回`rate limited`，则触发流控成功

## 其他

若使用过程中遇到问题请先参考[FAQ文档](./FAQ-zh.md)



[返回**Sermant**说明文档](../../README-zh.md)
