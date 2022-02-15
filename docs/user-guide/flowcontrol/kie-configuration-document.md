# 基于KIE配置流控规则

本文档主要帮助读者如何基于**KIE配置中心**配置限流降级规则



## 1、环境准备

- 搭建[KIE](https://github.com/apache/servicecomb-kie)服务端
- 准备[DEMO](../../../sermant-plugins/sermant-flowcontrol/flowcontrol-demos/flowcontrol-demo)应用

## 2、下载源码并打包插件

参考文档[基于ZK配置流控规则](./zk-configuration-document.md#2下载源码并打包插件)

## 3、配置插件

**3.1 配置`Sermant`配置中心类型与地址**，配置文件路径`${agent路径}/config/config.properties`

```properties
dynamicconfig.timeout_value=30000
dynamicconfig.default_group=sermant
# Zookeeper配置中心地址
dynamicconfig.zookeeper_uri=zookeeper://127.0.0.1:2181
# 配置中心类型 此处使用KIE配置中心
# ZOO_KEEPER: zookeeper配置中心
# KIE: kie配置中心
dynamicconfig.dynamic_config_type=KIE
# 多个地址使用逗号隔开，例如http://127.0.0.1:30110,http://127.0.0.1:30111
dynamicconfig.kie_url=http://127.0.0.1:30110
```

**3.2 配置流控插件**

修改`${agent路径}/pluginPackge/flowcontrol/config/config.yaml`

```yaml
flow.control.plugin:
  flowFramework: SENTINEL
  useAgentConfigCenter: ${plugin.flowcontrol.use.agent.config_center:true} # 使用agent配置中心
```



## 4、打包并启动应用

参考文档[基于ZK配置流控规则](./zk-configuration-document.md#4打包并启动应用)

## 5、配置规则

通过`POST`请求地址http://127.0.0.1:30110/v1/default/kie/kv 创建规则

其他接口请参考[KIE API文档](https://github.com/apache/servicecomb-kie/blob/master/docs/api.yaml)

### 5.1 流控规则

```json
{
	"key": "FlowRule",
	"value": "[{\"count\":1.0,\"grade\":1,\"resource\":\"/flow\"}]",
	"labels": {
		"service": "flowControlDemo"
	},
	"status": "enabled"
}
```

### 5.2 熔断规则

```json
{
	"key": "DegradeRule",
	"value": "[{\"count\":1.0,\"grade\":0,\"resource\":\"/degrade\",\"slowRatioThreshold\":0.1,\"timeWindow\":10,\"statIntervalMs\": 10000, \"minRequestAmount\": 3}]",
	"labels": {
		"service": "flowControlDemo"
	},
	"status": "enabled"
}
```

### 5.3 隔离仓规则

```json
{
	"key": "IsolateRule",
	"value": "[{\"resource\":\"/degrade\",\"maxWaitDuration\":1000, \"maxConcurrentCalls\": 2}]",
	"labels": {
		"service": "flowControlDemo"
	},
	"status": "enabled"
}
```

### 5.4 规则说明

| 配置项         | 说明                                                         |
| -------------- | ------------------------------------------------------------ |
| key            | 配置的规则名称，固定值，流控-`FlowRule`   熔断-`DegradeRule` |
| value          | 配置的规则列表，json格式，其中规则解释参考文档[基于ZK配置流控规则](./zk-configuration-document.md#5配置规则) |
| labels.service | 指定的服务名，与JVM变量`project.name`关联                    |
| status         | 启用-`enabled` 禁用-`disabled`, 默认禁用                     |

## 6、验证规则是否生效

参考文档[基于ZK配置流控规则](./zk-configuration-document.md#6验证规则是否生效)
