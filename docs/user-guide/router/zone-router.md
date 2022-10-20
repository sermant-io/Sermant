# Zone Router

[简体中文](zone-router-zh.md) | [English](zone-router.md)

This document is used to introduce the usage of [zone router](../../../sermant-plugins/sermant-router).

Tag Router referring [Tag Router](document.md).

## Function

In the case that microservices are deployed in multiple AZs (available zone), priority is given to filtering instances of the same AZ for invocation, and jumping to different AZs when all instances of the same AZ are unavailable.

## Usage

- Modify The [Plugin Configuration File](../../../sermant-plugins/sermant-router/config/config.yaml) On Demand.

The file path is: `${agent_package_path}/agent/pluginPackage/service-router/config/config.yaml`. Please replace `${agent_package_path}` with the actual package path.

The configuration items are described as follows:

```yaml
router.plugin:
  # Whether to enable dubbo zone router
  enabled-dubbo-zone-router: false
  # Whether to enable spring cloud zone router(Reserved configuration, not supported at this time)
  enabled-spring-zone-router: false
  # Whether to enable sermant-springboot-registry zone router
  enabled-registry-zone-router: false
```

- Configure routing rules to take effect

Sermant backend provides api way to publish the configuration, you need to start the backend application before use, the configuration publishing interface is as follows:

**URL**

POST /publishConfig

**Request Body**

|Params|Mandatory or not|Param type|Description
|---|---|---|---|
|key|√|String|configuration key|
|group|√|String|Configuration group, which is used to configure subscriptions|
|content|√|String|Configuration text, that is, specific routing rules|

The key value needs to be sermant.plugin.router.

The group needs to be configured to application level, i.e. app=${yourApp}&&environment=${yourEnvironment}, app defaults to default, environment defaults to empty.

The content is the specific routing effective rule.

### Examples of tag routing rules and descriptions are as follows: 

```yaml
strategy: all # Effective Strategy: all(All effective)/none(None effective)/white(White list)/black(black list)
value: # Downstream service name, with multiple downstreams separated by commas. When the effective policy is white, the value is white list, when the effective policy is black, the value is black list.
```

**Note: When adding a new configuration, please remove the comment, otherwise it will cause the addition to fail.**

- Start the application

Add the following parameters as required at the start of the attached agent: 

```
-Dservice_meta_zone=${zone}
```

The parameters are described as follows: 

- ${zone} needs to be replaced with the az at the time of service registration。

## Result Verification

- Prerequisites [correctly packed Sermant](../../README.md)

- Registration center using Huawei CSE, download [Local-CSE](https://support.huaweicloud.com/devg-cse/cse_devg_0036.html), unzip and follow the documentation to start.

- Configuring Routing Effective Rules

Calling the interface `localhost:8900/publishConfig`, with the following request parameters:

```json
{
  "content": "strategy: all",
  "group": "app=default&&environment=",
  "key": "sermant.plugin.router"
}
```

- Compile [consumer demo](../../../sermant-integration-tests/dubbo-test/dubbo-2-7-integration-consumer) and [provider-demo](../../../sermant-integration-tests/dubbo-test/dubbo-2-7-integration-provider)

```shell
mvn clean package
```

- Start the consumer(zone is sz)

```shell
java -Drouter_plugin_enabled_dubbo_zone_router=true -Dservice_meta_zone=sz -Dservicecomb_service_enableDubboRegister=true -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar dubbo-integration-consumer.jar
```

- Start the provider(zone is sz)

```shell
java -Drouter_plugin_enabled_dubbo_zone_router=true -Dservice_meta_zone=sz -Dservicecomb_service_enableDubboRegister=true -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar dubbo-integration-provider.jar
```

- Start the provider(zone is gz)

```shell
java -Drouter_plugin_enabled_dubbo_zone_router=true -Dservice_meta_zone=gz -Dservicecomb_service_enableDubboRegister=true -Dserver.port=28022 -Ddubbo.protocol.port=28822 -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar dubbo-integration-provider.jar
```

${path} needs to be replaced with the actual Sermant installation path.

- Testing

After starting the above 3 applications and configuring the routing effective rules correctly, when accessing<http://127.0.0.1:28020/consumer/getZone?exit=false>through the http client tool, we can be find that the request is routed to the producer with zone sz. When the producer with zone sz is disabled, the request is routed to the producer with zone gz.

[Back to README of **Sermant** ](../../README.md)