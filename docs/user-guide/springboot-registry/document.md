# SpringBoot Registry

[简体中文](document-zh.md) | [English](document.md)

This article describes how to [SpringBoot registry plugin]((../../../sermant-plugins/sermant-springboot-registry)) and how to use it.

## Ability

This plugin provides service registration and discovery abilities for pure SpringBoot applications. Users can quickly access the registration center without modifying code. In addition, the plugin also provides the service timeout retry ability, achieving high availability of service invocation.

The plugin takes effect based on URL resolution. The plugin parses downstream services based on the URL invoking by the client, selects a preferred instance based on load balancer, and dynamically replaces the URL to complete service invoking.

Currently, URL formats are supported.：http://www.domain.com/serviceName/apiPath

Just like the URL above, ` www.domain.com` indicates the domain name, `serviceName` indicates the downstream service name, and `apiPath` indicates the downstream request interface path.

## Version Supports

Reistry Center Support： Zookeeper 3.4.x and above

Client Supports：

- HttpClient: 4.x
- HttpAsyncClient: 4.1.4
- OkhttpClient: 2.x, 3.x, 4.x
- Feign(springcloud-openfeign-core): 2.1.x, 3.0.x
- RestTemplate(Spring-web): 5.1.x, 5.3.x

Application Framework Supports：SpringBoot 1.5.10.Release and above

## Usage

### (Optional) Configuring the Dynamic Configuration Center

You can configure it based on the configuration file or by using environment variables.

（1）Configuration based on the configuration file

You need to find the configuration file [config.properties]((https://github.com/huaweicloud/Sermant/blob/develop/sermant-agentcore/sermant-agentcore-config/config/config.properties)) and modify the following configuration:

```properties
# Configuration Center Address
dynamic.config.serverAddress=127.0.0.1:2181
# Configuration Center Type, Currently, support ZOOKEEPER and KIE
dynamic.config.dynamicConfigType=ZOOKEEPER
```

（2）Configuration based on Environment

You can add environment variables during application startup, for example, `-Ddynamic.config.serverAddress=127.0.0.1:2181 -Ddynamic.config.dynamicConfigType=ZOOKEEPER`.

### (Optional) Configuring the Plugin

After the sermant is packaged, you can find the configuration file of the plugin in `${agent path}/agent/pluginPackage/springboot-registry/config/config.yaml`. The configuration is as follows:

```yaml
sermant.springboot.registry:
  realmName: www.domain.com   # Replace the domain name with the domain name of your request address.

sermant.springboot.registry.lb:
  lbType: RoundRobin               # Load balancer type. The options are RoundRobin, Random, WeightedResponseTime, and BestAvailable.
  registryAddress: 127.0.0.1:2181  # Registration Center Address (Mandatory)
  instanceCacheExpireTime: 0       # Instance expiration time, in seconds. If the value is less than or equal to 0, the instance never expires.
  instanceRefreshInterval: 0       # Instance refresh time, in seconds. The value must be less than the value of instanceCacheExpireTime.
  refreshTimerInterval: 5          # Interval for checking whether the instance has expired. If the value is greater than the value of instanceRefreshInterval, the value is set to instanceRefreshInterval.
```

Ensure that the values of` realName` and `registryAddress` are correct. Otherwise, the plugin does not take effect.

In addition to the preceding configurations, the following configurations are optional. You can configure the configurations by using environment variables.

| Configuration                   | Desc                                                         | Default           |
| ------------------------------- | ------------------------------------------------------------ | ----------------- |
| connectionTimeoutMs             | Timeout interval for connecting to the ZooKeeper.            | 2000ms            |
| readTimeoutMs                   | Read timeout interval for connecting to the ZooKeeper.       | 10000ms           |
| retryIntervalMs                 | ZooKeeper connection retry interval                          | 3000ms            |
| zkBasePath                      | Path of the node registered when the ZooKeeper as the registration center. | /sermant/services |
| registryCenterType              | Registration center type. Currently, only ZooKeeper is supported. | Zookeeper         |
| maxRetry                        | Maximum number of retries when a call timeout occurs         | 3 times           |
| retryWaitMs                     | Retry Wait Time                                              | 1000ms            |
| enableSocketConnectTimeoutRetry | Whether `SocketTimeoutException: connect timed out` to retry | true              |
| enableSocketReadTimeoutRetry    | Whether `SocketTimeoutException: read timed out` to retry    | true              |
| enableTimeoutExRetry            | Indicates whether to retry when a `TimeoutException` occurs. | true              |
| specificExceptionsForRetry      | Extra retry exception                                        | emptyList         |
| statsCacheExpireTime            | Indicates the cache duration of statistics indicators, in minutes. | 60Min             |
| instanceStatTimeWindowMs        | Indicates the time window for counter statistics, in milliseconds. | 600000ms          |

### (Mandatory) Configuring a Gray Policy

To make the plugin take effect, you need to configure a gray policy for the plugin.

The gray policy is used to determine whether a request needs to be proxyed based on the specified service name and replace the URL. Currently, the gray policy includes the following three types:

- all，all service URLs are replaced by proxy, regardless of downstream service names.
- none, contrary to the above
- white， In trustlist mode, the specified service collection proxy requests.

The configuration format is as follows:

```yaml
# Gray Policy Type
strategy: all
# Trustlist service set. This parameter is valid only when strategy is set to white
value: service-b
```

#### **Gray Policy Delivery**

##### Backend-Based Delivery

How to configure and deliver a gray policy? You need to configure and deliver the gray policy based on the delivery interface of the backend. The backend provides the following interfaces:

URL `/publishConfig`

The request parameters are as follows:：

| Configurations | Desc                                                         |
| -------------- | ------------------------------------------------------------ |
| key            | Config key                                                   |
| group          | Config Group                                                 |
| content        | Configuration content, that is, the specific rule configuration, is in `YAML` format. |

> The format of group is k1=v1, and multiple values are separated by ampersands (&). For example, k1=v1&k2=v2, indicating the label group bound to the key.

The default scenario plugin subscribes the groups `app=default&environment=`和`service=yourApplicationName&app=default&environment=`

`yourApplicationName` is the `spring.application.name` of your current app. To modify a subscription group, you can set the following environment variables:

- `-Dservice.meta.application`, Specifies the tag of an app.
- `-Dservice.meta.environment`, Specifies the label of the environment.

The preceding figure shows the configuration process. The configuration process is complete after the service is started with the sermant message.

##### Based On The Configuration Center

You can also directly deliver configurations based on the configuration center client. The following uses ZooKeeper as an example.

（1）Login the ZooKeeper Client.

```shell
# windows
double click zkCli.cmd
# linux
sh zkCli.sh -server localhost:2181
```

（2）Create Group Path

```shell
create /app=default&environment= ""
```

（3）Create configuration node named `sermant.plugin.registry` 

```shell
create /app=default&environment=/sermant.plugin.registry "strategy: all"
```

## Quick Use

The following uses the demo as an example to describe how to use the plugin ability.

### Environment Preparation

- JDK1.8 and above
- Maven
- Downloaded [demo souce code](https://github.com/huaweicloud/Sermant-examples/tree/springboor-registry-demo/registry-demo/springboot-registry-demo)
- package sermant

### Package Demo

Run the following command to pack the package:

```shell
mvn clean package
```

You can get two JAR packages, service-a.jar and service-b.jar, and the calling relationship is a->b.

### Start the Demo Application.

Run the following command to start application a:

```shell
java --javaagent:${agent path}\sermant-agent-1.0.0\agent\sermant-agent.jar=appName=default -jar -Dserver.port=8989 service-a.jar
```

Run the following command to start application b:

```shell
java --javaagent:${agent path}\sermant-agent-1.0.0\agent\sermant-agent.jar=appName=default -jar -Dserver.port=9999 service-b.jar
```

### Delivering a Gray Policy

[Deliver the gray policy](#(Mandatory) Configuring-a-Gray-Policy) by referring to. Deliver the following configurations:

```json
{
    "key":"sermant.plugin.registry",
    "group":"app=default&environment=",
    "content":"strategy: all"
}
```

All downstream service requests will handled by plugin.

### Validation

Invoke the` localhost:8989/httpClientGet` interface to check whether the interface is successfully returned. If yes, the plugin has taken effect.

[[Back to README of **Sermant** ](../../README.md)]
