# 区域路由

[简体中文](zone-router-zh.md) | [English](zone-router.md)

本文档主要介绍[区域路由插件](../../../sermant-plugins/sermant-router)的使用方法。

标签路由见[标签路由](document-zh.md)。

## 功能

在微服务部署在多个AZ(available zone)的情况下，优先筛选同AZ实例进行调用，同AZ的实例都不可用时，跳到不同AZ。

## 使用说明

- 按需修改[插件配置文件](../../../sermant-plugins/sermant-router/config/config.yaml)

文件路径为：`${agent_package_path}/agent/pluginPackage/service-router/config/config.yaml`，其中`${agent_package_path}`需要替换为实际的打包路径。

配置项说明如下:

```yaml
router.plugin:
  # 是否开启dubbo区域路由
  enabled-dubbo-zone-router: false
  # 是否开启spring cloud区域路由（预留配置，暂不支持）
  enabled-spring-zone-router: false
  # 是否开启注册插件（sermant-springboot-registry）区域路由
  enabled-registry-zone-router: false
```

- 配置路由生效规则

Sermant backend提供api的方式发布配置, 使用前需启动backend后台应用，配置发布接口如下：

**URL**

POST /publishConfig

**请求Body**

|参数|是否必填|参数类型|描述
|---|---|---|---|
|key|√|String|配置的key|
|group|√|String|配置的组|
|content|√|String|配置文本|

其中key值为sermant.plugin.router。

group需要配置为应用级别，即app=${yourApp}&&environment=${yourEnvironment}，其中app默认为default，environment默认为空。

content为具体的路由规则。

### 生效规则示例及说明如下：

```yaml
strategy: all # 生效策略：all（全部生效）/none（全不生效）/white（白名单）/black（黑名单）
value: # 下游服务名，多个下游用逗号分隔。当生效策略为white时，value为白名单，当生效策略为black时，value为黑名单
```

**注意：新增配置时，请去掉注释，否则会导致新增失败。**

- 启动应用

在附带agent启动时，按需加上以下参数：

```
-Dservice_meta_zone=${zone}
```

参数说明如下：

- ${zone}需替换为服务注册时的az。

## 结果验证

- 前提条件[正确打包Sermant](../../README.md)

- 注册中心使用华为CSE，下载[Local-CSE](https://support.huaweicloud.com/devg-cse/cse_devg_0036.html) ，解压后按照文档说明进行启动

- 配置路由生效规则

调用接口`localhost:8900/publishConfig`, 请求参数如下:

```json
{
   "content": "strategy: all", 
   "group": "app=default&&environment=", 
   "key": "sermant.plugin.router"
}
```

- 编译[消费者应用](../../../sermant-integration-tests/dubbo-test/dubbo-2-7-integration-consumer)与[生产者应用](../../../sermant-integration-tests/dubbo-test/dubbo-2-7-integration-provider)

```shell
mvn clean package
```

- 启动消费者（zone为sz）

```shell
java -Drouter_plugin_enabled_dubbo_zone_router=true -Dservice_meta_zone=sz -Dservicecomb_service_enableDubboRegister=true -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar dubbo-integration-consumer.jar
```

- 启动生产者（zone为sz）

```shell
java -Drouter_plugin_enabled_dubbo_zone_router=true -Dservice_meta_zone=sz -Dservicecomb_service_enableDubboRegister=true -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar dubbo-integration-provider.jar
```

- 启动生产者（zone为gz）

```shell
java -Drouter_plugin_enabled_dubbo_zone_router=true -Dservice_meta_zone=gz -Dservicecomb_service_enableDubboRegister=true -Dserver.port=28022 -Ddubbo.protocol.port=28822 -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar dubbo-integration-provider.jar
```

其中path需要替换为Sermant实际安装路径。

- 测试

当启动以上3个应用并正确配置路由生效规则后，通过http客户端工具访问<http://127.0.0.1:28020/consumer/getZone?exit=false>，可以发现，请求会路由到zone为sz的生产者中。当停掉zone为sz的生产者后，请求会路由到zone为gz的生产者中

[返回**Sermant**说明文档](../../README.md)