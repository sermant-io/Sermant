# 心跳服务介绍

[简体中文](service_heartbeat-zh.md) | [English](service_heartbeat.md)

本文档主要介绍**核心模块**的[心跳服务](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/heartbeat)。

## 功能定位

**心跳服务**指的是**核心模块**定期向**后端模块**发送心跳包的服务，起到告知当前`Sermant`应用存活的作用。**心跳服务**主要针对所有插件，对每个`插件主模块(main)`，**心跳服务**都会为其定制一个心跳数据包并定期发送，这些定制化数据将在**后端模块**的后续流程中发挥作用。

## 实现方式

**心跳服务**以**核心服务系统**为基础开发的一套用于`告知存活`和`定制数据`的服务。

**核心服务系统**相关内容详见[核心模块介绍](../user-guide/agentcore-zh.md#核心服务系统)，这里不做赘述。

心跳系统的常见使用方式如下：
```java
// 创建netty客户端
NettyClient nettyClient = ClientManager.getNettyClientFactory().getNettyClient(
    AgentConfigManager.getNettyServerIp(), 
    Integer.parseInt(AgentConfigManager.getNettyServerPort()));
// 创建心跳数据包，其中包含一些默认数据，下面会介绍
HeartbeatMessage message = new HeartbeatMessage();
// 定制心跳数据包中的其他数据
message.registerInformation("${key}", "${value}");
// 构建心跳数据包信息
String msg = message.generateCurrentMessage();
// 序列化心跳数据包，并通过netty客户端发送，数据类型标注为心跳
nettyClient.sendData(msg.getBytes(Charset.forName("UTF-8")), Message.ServiceData.DataType.SERVICE_HEARTBEAT);
```

其中`HeartbeatMessage`中默认包含的数据如下：

- `hostname`：发送客户端的主机名
- `ip`：发送客户端的IP地址
- `app`：应用名称，即启动参数中的`appName`
- `appType`：应用类型，即启动参数中的`appType`
- `heartbeatVersion`：上一次心跳发送时间
- `lastHeartbeat`：上一次心跳发送时间
- `version`：核心包的版本

### 初始化插件心跳

**心跳服务**在初始化的时候，将从**插件管理器**中获取插件的名称和版本号：
```java
// 键为插件名称，值为插件版本
Map<String, String> pluginVersionMap = PluginManager.getPluginVersionMap();
```

每隔一段时间，就会对每个`插件主模块(main)`构建一个心跳数据包并使用心跳系统发送，间隔时间配置于核心配置文件`config.properties`中：
```properties
# 单位：ms
heartbeat.interval=3000
```

发送的心跳数据包中，除了上述心跳系统固定发送的信息外，将额外增加两个参数：

- `pluginName`：插件名称
- `pluginVersion`：插件版本号

### 添加定制数据

**心跳服务**允许插件开发者定制化地为插件的心跳数据包添加额外动态参数：
```java
final HeartbeatService service = ServiceManager.getService(HeartbeatService.class);
service.setExtInfo(new ExtInfoProvider() {
  @Override
  public Map<String, String> getExtInfo() {
    // do something
  }
});
```

`setExtInfo`方法在执行的时候，将通过自定义`ExtInfoProvider`实现找到所在的插件或插件服务`jar`包，获取其`manifest`文件`Sermant-Plugin-Name`的值，即插件名称。获取到插件名称之后，**心跳服务**才能正确地将动态参数绑定到相应的心跳数据包中。

`ExtInfoProvider`接口定义了`getExtInfo`方法，**心跳服务**每次构建心跳数据包时，将通过该方法获取动态获取额外参数。

## 使用方式

一般情况下，对于插件开发者来说，**心跳服务**是无需关心的内容。只有当插件`后端模块(backend)`需要从**后端模块**的kafka心跳主题中捞特定数据时，才有必要为心跳数据包定制数据。插件为其心跳数据包定制数据时，通常可以[自定义插件服务](dev_plugin_code-zh.md#插件服务)，在`start`方法中[添加定制数据](#添加定制数据)即可。

可以参考示例工程的[DemoHeartBeatService](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/service/DemoHeartBeatService.java)进行开发。

[返回**Sermant**说明文档](../README-zh.md)