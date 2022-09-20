# Heartbeat Service

[简体中文](service_heartbeat-zh.md) | [English](service_heartbeat.md)

This document focuses on [Heartbeat Service](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/heartbeat) of the **sermant-agentcore-core**.

## Function Orientation

The **Heartbeat Service** is a service that periodically sends heartbeat messages from **sermant-agentcore-core** to the **Backend Module** to inform the current `Sermant` that the application is alive. The **Heartbeat Service** mainly targets all plugins. For each `main` of plugin, **Heartbeat Service** will customize a heartbeat message for it and send it regularly. These customized data will be used in the subsequent flow of the **Backend Module**.

## Implementation

**Heartbeat Service** is built based on the **sermant-agentcore-core** for `informing survival` and `customizing data`.

Details of **Sermant-agentcore-core** could be found [introduction to sermant-agentcore-core](../user-guide/agentcore.md#Core-Service-System).

The common usage of heartbeat system is as follows:

```java
// create netty client
NettyClient nettyClient = ClientManager.getNettyClientFactory().getNettyClient(
    AgentConfigManager.getNettyServerIp(), 
    Integer.parseInt(AgentConfigManager.getNettyServerPort()));
// create heartbeat message
HeartbeatMessage message = new HeartbeatMessage();
// customize additional data in heartbeat message
message.registerInformation("${key}", "${value}");
// build message
String msg = message.generateCurrentMessage();
// Serialization, and send through the Netty client with the data type annotated as heartbeat
nettyClient.sendData(msg.getBytes(Charset.forName("UTF-8")), Message.ServiceData.DataType.SERVICE_HEARTBEAT);
```

The `HeartbeatMessage` contains the following data by default:

- `hostname`: hostname of the sending client
- `ip`：ip of the sending client
- `app`：application name (`appName` at startup)
- `appType`：application type, (`appType` at startup)
- `heartbeatVersion`：time of last heartbeat
- `lastHeartbeat`：time of last heartbeat
- `version`：version of sermant-agentcore-core

### Initialize Heartbeat of Plugin 

The **Heartbeat Service** gets the name and version of the plugin from the **Plugin Manager** when it is initialized:
```java
// key is the name of plugin, value is the version of plugin
Map<String, String> pluginVersionMap = PluginManager.getPluginVersionMap();
```

Every once in a while, a heartbeat message is built for each `main` of plugin module and sent using heartbeat system. The interval is configured in the core configuration file `config.properties`:
```properties
# unit：ms
heartbeat.interval=3000
```

Besides the information that heartbeat system sends regularly, two additional parameters will be added to the heartbeat message:

- `pluginName`：the name of plugin
- `pluginVersion`：the version of plugin

### Custom Data

The **Heartbeat Service** allows plugin developers to customize the plugin's heartbeat message with additional dynamic parameters:
```java
final HeartbeatService service = ServiceManager.getService(HeartbeatService.class);
service.setExtInfo(new ExtInfoProvider() {
  @Override
  public Map<String, String> getExtInfo() {
    // do something
  }
});
```

When the `setExtInfo` method is executed, it will find the `jar` package of plugin or plugin service through the custom `ExtInfoProvider` implementation and get the value of `sermant-plugin-name ` of its `manifest` file. Once the plugin name is obtained, the **Heartbeat Service** can correctly bind dynamic parameters to the corresponding heartbeat message.

The `ExtInfoProvider` interface defines `getExtInfo` method, through which the **Heartbeat Service** will dynamically fetch additional parameters each time a heartbeat message is built.

## How to Use

In general, the **Heartbeat Service** is a no-concern for plugin developers. Customizing data for heartbeat messages is only necessary if the plugin `backend` needs to fetch specific data from the Kafka heartbeat topic of the **Backend Module**. When plugins need to customize data for their heartbeat messages, usually you can develop a [custom plugin service](dev_plugin_code.md#Plugin-Service) by [adding custom data](#Custom-Data) in the `start` method.

You can refer to [DemoHeartBeatService](../../sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/service/DemoHeartBeatService.java) for your development.

[Back to README of **Sermant** ](../README.md)