# SpringBoot Registry

[简体中文](document-zh.md) | [English](document.md)

本篇文章主要介绍[SpringBoot注册插件](../../../sermant-plugins/sermant-springboot-registry)以及使用方法。

## 功能

该插件为纯SpringBoot应用提供服务注册发现能力，方便用户在不修改代码的前提下快速接入注册中心，同时提供服务超时重试的能力，实现服务调用的高可用。

插件的生效基于Url解析， 根据发起客户端调用Url解析下游服务，并根据负载均衡选择优选实例，动态替换Url， 完成服务调用。

目前Url支持的格式：http://www.domain.com/serviceName/apiPath

其中`www.domain.com`为实际调用的域名，`serviceName`为下游的服务名，`apiPath`则为下游请求接口路径。



## 版本支持

注册中心支持： Zookeeper 3.4.x及以上

客户端支持：

- HttpClient: 4.x
- HttpAsyncClient: 4.1.4
- OkhttpClient: 2.x, 3.x, 4.x
- Feign(springcloud-openfeign-core): 2.1.x, 3.0.x
- RestTemplate(Spring-web): 5.1.x, 5.3.x

框架支持：SpringBoot 1.5.10.Release及以上

## 使用说明

### 配置动态配置中心（可选）

用户可基于配置文件配置或者采用环境变量方式配置。

（1）基于配置文件配置

您需找到配置文件[config.properties](https://github.com/huaweicloud/Sermant/blob/develop/sermant-agentcore/sermant-agentcore-config/config/config.properties), 修改如下配置：

```properties
# 配置中心地址
dynamic.config.serverAddress=127.0.0.1:2181
# 配置中心类型， 目前支持ZOOKEEPER与KIE
dynamic.config.dynamicConfigType=ZOOKEEPER
```

（2）基于环境变量配置

您可在应用启动时，增加环境变量进行指定，例如：`-Ddynamic.config.serverAddress=127.0.0.1:2181 -Ddynamic.config.dynamicConfigType=ZOOKEEPER` 便可达到上述配置文件的效果。

### 配置插件（可选）

在sermant打包后，您可在路径`${agent path}/agent/pluginPackage/springboot-registry/config/config.yaml`找到该插件的配置文件， 配置如下所示：

```yaml
sermant.springboot.registry:
  realmName: www.domain.com   # 该域名需替换为您的请求地址的域名!

sermant.springboot.registry.lb:
  lbType: RoundRobin               # 负载均衡类型, 当前支持轮询(RoundRobin)、随机(Random)、响应时间权重(WeightedResponseTime)、最低并发数(BestAvailable)
  registryAddress: 127.0.0.1:2181  # 注册中心地址(必填)
  instanceCacheExpireTime: 0       # 实例过期时间, 单位秒, 若<=0则永不过期
  instanceRefreshInterval: 0       # 实例刷新时间, 单位秒, 必须小于instanceCacheExpireTime
  refreshTimerInterval: 5          # 实例定时检查间隔, 判断实例是否过期, 若其大于instanceRefreshInterval, 则值设置为instanceRefreshInterval


```

如上配置， **请注意务必确保配置`realName`与`registryAddress`填写正确**， 否则插件不会生效！

除以上用户需要注意的配置外，如下为可选配置， 用户可采用环境变量的方式进行配置

| 参数名                          | 描述                                                         | 默认值            |
| ------------------------------- | ------------------------------------------------------------ | ----------------- |
| connectionTimeoutMs             | 连接ZK的超时时间                                             | 2000ms            |
| readTimeoutMs                   | 连接ZK的响应超时时间                                         | 10000ms           |
| retryIntervalMs                 | 连接ZK的重试间隔                                             | 3000ms            |
| zkBasePath                      | ZK作为注册中心时注册的节点路径                               | /sermant/services |
| registryCenterType              | 注册中心类型, 目前仅支持Zookeeper                            | Zookeeper         |
| maxRetry                        | 当调用发生超时时，最大重试次数                               | 3次               |
| retryWaitMs                     | 重试等待时间                                                 | 1000ms            |
| enableSocketConnectTimeoutRetry | 是否在发生`SocketTimeoutException: connect timed out`进行重试 | true              |
| enableSocketReadTimeoutRetry    | 是否在发生`SocketTimeoutException: read timed out`进行重试   | true              |
| enableTimeoutExRetry            | 是否在发生`TimeoutException`进行重试                         | true              |
| specificExceptionsForRetry      | 额外的重试异常                                               | 空                |
| statsCacheExpireTime            | 统计指标的缓存时间，单位分钟                                 | 60Min             |
| instanceStatTimeWindowMs        | 指标统计时间窗口， 单位毫秒                                  | 600000ms          |

### 配置灰度策略（必须）

若使插件生效，还需为插件配置灰度策略，首先说明下灰度策略。

灰度策略意在根据指定服务名判断需不需要为请求进行代理，替换url地址，当前灰度策略包含如下三种：

- all， 即全量，不区分下游服务名，所有的服务url均代理替换
- none, 与上相反
- white， 白名单模式， 指定的服务集合才会代理请求。

其配置格式如下:

```yaml
# 灰度策略类型
strategy: all
# 白名单服务集合， 仅当strategy配置为white时生效
value: service-b
```



#### **灰度策略下发**

##### 基于backend下发

那如何配置下发灰度策略， 这里需要基于backend的下发接口进行配置下发，backend提供如下接口：

URL `/publishConfig`

请求参数如下：

| 配置参数 | 说明                                             |
| -------- | ------------------------------------------------ |
| key      | 配置键                                           |
| group    | 配置的标签组                                     |
| content  | 配置内容，即具体的规则配置，其格式均为`YAML`格式 |

> 其中**group**的配置格式为k1=v1, 多个值使用"&"分隔，例如k1=v1&k2=v2, 代表该key绑定的标签组

默认插件会订阅分组`app=default&environment=`和`service=yourApplicationName&app=default&environment=`

其中`yourApplicationName`为您当前的app的`spring.application.name`。若您想修改订阅分组， 您可设置如下环境变量进行指定：

- `-Dservice.meta.application`, 可指定app的标签
- `-Dservice.meta.environment`, 可指定environment的标签

以上，为配置流程，随后携带sermant启动即可完成。

##### 基于配置中心

当然此处您也可以直接基于配置中心客户端直接配置下发， 以zookeeper为例：

（1）登录zookeeper 客户端

```shell
# windows
双击 zkCli.cmd
# linux
sh zkCli.sh -server localhost:2181
```

（2）创建组路径

```shell
create /app=default&environment= ""
```

（3）创建该具体配置节点路径（`sermant.plugin.registry`）

```shell
create /app=default&environment=/sermant.plugin.registry "strategy: all"
```



## 快速使用

下面以demo为例，演示如何使用该插件能力

### 环境准备

- JDK1.8及以上
- Maven
- 完成下载[demo源码](https://github.com/huaweicloud/Sermant-examples/tree/springboor-registry-demo/registry-demo/springboot-registry-demo)
- 完成编译打包sermant

### 编译打包demo应用

执行如下命令进行打包:

```shell
mvn clean package
```

您可得到两个jar包，service-a.jar与service-b.jar, 调用关系为a->b

### 启动demo应用

参考如下命令启动a应用

```shell
java --javaagent:${agent path}\sermant-agent-1.0.0\agent\sermant-agent.jar=appName=default -jar -Dserver.port=8989 service-a.jar
```

参考如下命令启动b应用

```shell
java --javaagent:${agent path}\sermant-agent-1.0.0\agent\sermant-agent.jar=appName=default -jar -Dserver.port=9999 service-b.jar
```

### 下发灰度策略

参考使用[配置灰度策略](#配置灰度策略（必须）)的灰度策略进行下发， 下发如下配置

```json
{
    "key":"sermant.plugin.registry",
    "group":"app=default&environment=",
    "content":"strategy: all"
}
```

以上即对所有下游服务请求均受理。

### 验证

调用接口`localhost:8989/httpClientGet`, 判断接口是否成功返回， 若成功返回则说明插件已成功生效。

[返回**Sermant**说明文档](../../README-zh.md)
