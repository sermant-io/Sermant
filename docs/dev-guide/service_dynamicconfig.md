# 动态配置服务介绍

本文档主要介绍**核心模块**的[动态配置服务](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig)。

- [功能定位](#功能定位)
- [Api解析](#Api解析)
- [ZooKeeper实现](#ZooKeeper实现)
- [Kie实现](#Kie实现)
- [实现包装](#实现包装)
- [使用方式](#使用方式)

## 功能定位

**动态配置服务**是一个允许使用者动态从服务器拉取配置的服务，它作为[统一配置系统](../user-guide/agentcore.md#统一配置系统)的动态补充，其核心述求在于解决后者提供的配置不可改变的问题。

## Api解析

**动态配置服务**的服务功能`API`由[DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/DynamicConfigService.java)抽象类提供，其实现三个接口，见于[api](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api)目录中，具体接口如下所示：

|接口|方法|解析|
|:-|:-|:-|
|[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyService.java)|String getConfig(String)|获取某个键的配置值(默认组)|
|[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyService.java)|boolean publishConfig(String, String)|设置某个键的配置值(默认组)|
|[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyService.java)|boolean removeConfig(String)|移除某个键的配置值(默认组)|
|[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyService.java)|List\<String> listKeys()|获取所有键(默认组)|
|[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyService.java)|boolean addConfigListener(String, [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java))|为某个键添加监听器(默认组)|
|[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyService.java)|boolean removeConfigListener(String)|移除某个键的监听器(默认组)|
|[KeyGroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyGroupService.java)|String getConfig(String, String)|获取组下某个键的配置值|
|[KeyGroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyGroupService.java)|boolean publishConfig(String, String, String)|设置组下某个键的配置值|
|[KeyGroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyGroupService.java)|boolean removeConfig(String, String)|移除组下某个键的配置值|
|[KeyGroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyGroupService.java)|boolean addConfigListener(String, String, [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java))|为组下某个键添加监听器|
|[KeyGroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyGroupService.java)|boolean removeConfigListener(String, String)|移除组下某个键的监听器|
|[GroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/GroupService.java)|List\<String> listKeysFromGroup(String)|获取组中所有键|
|[GroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/GroupService.java)|boolean addGroupListener(String, [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java))|为组下所有的键添加监听器|
|[GroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/GroupService.java)|boolean removeGroupListener(String)|移除组下所有键的监听器|
|[DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/DynamicConfigService.java)|boolean addConfigListener(String, [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java), boolean)|为某个键添加监听器(默认组)，根据入参决定是否触发初始化事件|
|[DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/DynamicConfigService.java)|boolean addConfigListener(String, String, [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java), boolean)|为组下某个键添加监听器，根据入参决定是否触发初始化事件|
|[DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/DynamicConfigService.java)|boolean addGroupListener(String, [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java), boolean)|为组下所有的键添加监听器，根据入参决定是否触发初始化事件|

以上，需要明确两个概念：

- `Key`，单指某个动态配置的键
- `Group`，指一系列动态配置的分组，通常用于区分使用者

通过观察可以发现，以上的`API`主要分为数据的增删查改操作，以及监听器的[DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java)增删操作，其中后者的事件回调是**动态配置服务**得以实现功能中至关重要的一环，也是插件中使用**动态配置服务**的主要功能。

另外，在[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyService.java)接口中定义的所有`API`都是不带`Group`的`API`，它们在[DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/DynamicConfigService.java)中其实都会使用默认`Group`修正为[KeyGroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/api/KeyGroupService.java)的`API`，这点需要注意。默认`Group`可以通过**统一配置文件**`config.properties`的`dynamic.config.default_group`修改。

最后，除了以上的服务接口以外，使用者还需要关注一些其他接口、配置或实体：

- **动态配置服务**的静态配置[DynamicConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/config/DynamicConfig.java)，其中涉及一下配置：
  |类型|属性|统一配置值|解析|
  |:-|:-|:-|:-|
  |int|timeoutValue|dynamic.config.timeout_value|服务器连接超时时间，单位：ms|
  |String|defaultGroup|dynamic.config.default_group|默认分组|
  |String|serverAddress|dynamic.config.server_address|服务器地址，必须形如：{@code host:port[(,host:port)...]}|
  |[DynamicConfigServiceType](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigServiceType.java)|serviceType|dynamic.config.dynamic_config_type|服务实现类型，取NOP、ZOOKEEPER、KIE|
- **动态配置服务**实现类型[DynamicConfigServiceType](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigServiceType.java)，含一下几种类型：
  |枚举值|解析|
  |:-|:-|
  |ZOOKEEPER|ZooKeeper实现|
  |KIE|ServiceComb Kie实现|
  |NOP|无实现|
- 动态配置监听器[DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java)，其中包含的接口方法如下：
  |方法|解析|
  |:-|:-|
  |void process([DynamicConfigChangeEvent](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigChangeEvent.java))|处理配置改变事件的回调接口|
- 动态配置改变事件[DynamicConfigChangeEvent](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigChangeEvent.java)，其成员属性如下：
  |类型|属性|解析|
  |:-|:-|:-|
  |String|key|配置键|
  |String|group|配置分组|
  |String|content|配置信息|
  |[DynamicConfigEventType](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigEventType.java)|changeType|事件类型|
- 动态配置改变事件类型[DynamicConfigEventType](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigEventType.java)，含以下四种：
  |枚举值|解析|
  |:-|:-|
  |INIT|添加监听器时的初始化响应|
  |CREATE|配置新增事件|
  |MODIFY|配置信息修改事件|
  |DELETE|配置删除事件|

## ZooKeeper实现

对于`ZooKeeper`服务器来说，所谓的动态配置就是`ZooKeeper`节点的值，至于`Key`和`Group`应当作为构建**节点路径**的元素。考虑到`Group`包含区别使用者的信息，应当作为**节点路径**的前缀，这样`Key`值则作为后半部分存在：
```txt
/${group}/${key} -> ${value}
```

至于监听器[DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java)，则需要转换为`ZooKeeper`的`Watcher`。

`ZooKeeper`实现见于[zookeeper](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/zookeeper)包，主要包含[ZooKeeperDynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/zookeeper/ZooKeeperDynamicConfigService.java)和[ZooKeeperBufferedClient](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/zookeeper/ZooKeeperBufferedClient.java)两个类：

- [ZooKeeperDynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/zookeeper/ZooKeeperDynamicConfigService.java)是[DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/DynamicConfigService.java)的`ZooKeeper`实现类，主要职责是完成上述的参数转换：
  - `Key`和`Group` -> `ZooKeeper`节点路径
  - [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java) -> `ZooKeeper`的`Watcher`。

  将他们解析完毕之后，交由[ZooKeeperBufferedClient](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/zookeeper/ZooKeeperBufferedClient.java)做业务操作。
- [ZooKeeperBufferedClient](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/zookeeper/ZooKeeperBufferedClient.java)，其主要功能是对原生`ZooKeeper`客户端进行包装，封装其原生的功能，提供更为高阶的`API`：
  |方法|解析|
  |:-|:-|
  |boolean ifNodeExist(String)|判断节点是否存在|
  |String getNode(String)|查询节点内容|
  |boolean createParent(String)|创建节点的前置节点|
  |boolean updateNode(String, String)|更新节点内容，不存在时自动创建|
  |boolean removeNode(String)|移除节点|
  |List\<String> listAllNodes(String)|查询节点下所有子孙节点的路径集合|
  |boolean addDataLoopWatch(String, Watcher, BreakHandler)|添加循环的临时数据监听器，该监听器将在触发后重新注册，直到接收到移除监听器事件<p>注意，当同一节点的其他监听器被精准移除时，由于该监听器无法鉴别到底是不是移除自身，因此会选择放弃循环注册|
  |boolean addPersistentRecursiveWatches(String, Watcher)|添加持久递归的监听器，对子孙节点有效|
  |boolean removeDataWatches(String)|移除数据监听器|
  |boolean removeAllWatches(String)|移除节点下所有的监听器，含子孙节点|
  |void close()|关闭`ZooKeeper`客户端|

## Kie实现

对于`Kie`服务来说，所谓动态配置就是`Kie`配置的键值，`Kie`是基于标签去查询关联配置， 至于`Key`与`Group`则是关联配置的元素。`Key`即配置的键的名称，而`Group`则是关联`Key`的标签， 每一个`Key`都可配置一个或者多个标签，其格式往往如下:

```properties
{
	"key": "keyName",                # 配置键
	"value": "value",                # 配置值
	"labels": {
		"service": "serviceName"     #标签，kv形式，支持一个或者多个
	},
	"status": "enabled"
}
```

相对于`Zookeeper`, `Kie`更专注于`Group`, 其传值格式也有所不同，`Kie`的传值格式如下:

```properties
groupKey1=groupValue1[&groupKey2=groupVaue2...]
```

> 其中`groupKey`为标签键， `groupValue`则为标签值，多个标签使用`&`拼接；生成`Group`可通过[LabelGroupUtils#createLabelGroup](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/utils/LabelGroupUtils.java)生成
>
> **特别说明：**
>
> ​	若传入的`Group`非以上格式，则会默认添加标签`GROUP=传入Group`

`Kie`的实现见于包[kie](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/kie), 主要包含[KieDynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/kie/KieDynamicConfigService.java)、[LabelGroupUtils](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/utils/LabelGroupUtils.java)与[SubscriberManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/kie/listener/SubscriberManager.java)三个类：

- [KieDynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/kie/KieDynamicConfigService.java)是[DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/DynamicConfigService.java)的`Kie`实现类， 主要职责是封装[SubscriberManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/kie/listener/SubscriberManager.java)的订阅API以及`Group`的`Key`管理

- [LabelGroupUtils](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/utils/LabelGroupUtils.java)则是负责`Group`转换，主要包含以下API：

  | 方法                        | 解析                             |
  | --------------------------- | -------------------------------- |
  | createLabelGroup（Map）     | 创建标签，多个标签使用KV形式传入 |
  | getLabelCondition（String） | 将Group转换为请求的条件          |
  | isLabelGroup（String）      | 判断是否为Kie的标签              |

- [SubscriberManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/kie/listener/SubscriberManager.java)主要职责是管理`Group`的所有订阅者以及进行数据更新通知；其会根据订阅的Group，即标签组，与`Kie`建立连接请求任务，动态监听数据更新变化；该类主要包含以下API：

  | 方法                                                         | 解析                                                         |
  | ------------------------------------------------------------ | ------------------------------------------------------------ |
  | boolean addGroupListener(String, DynamicConfigListener, boolean) | 订阅标签监听，由[SubscriberManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/kie/listener/SubscriberManager.java)管理，建立监听任务，并提供首次订阅通知能力 |
  | boolean removeGroupListener(String, DynamicConfigListener)   | 移除标签监听                                                 |
  | boolean publishConfig(String, String, String)                | 发布Kie配置                                                  |

## 实现包装

从[核心模块介绍](../user-guide/agentcore.md#插件服务系统)可知，**插件服务系统**是基于`SPI`实现的，而[DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/DynamicConfigService.java)对应的`SPI`实现配置为[BufferedDynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/BufferedDynamicConfigService.java)，后者为前者所有具体实现类的包装。它在初始化的过程中读取[DynamicConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/dynamicconfig/config/DynamicConfig.java)配置，通过`dynamic.config.dynamic_config_type`配置选择具体的服务实现，并将所有`API`委派给这些具体的服务实现完成。

## 使用方式

**动态配置服务**主要应用于插件的拦截器或插件服务中，具体使用方式可以参见[插件代码开发手册的动态配置功能一节](dev_plugin_code.md#动态配置功能)，使用过程中较为关键的当属`Key`和`Group`的构建，具体内容可参见前文中几种实现中对这两个值的要求，这里不做赘述。

[返回**Sermant**说明文档](../README.md)
