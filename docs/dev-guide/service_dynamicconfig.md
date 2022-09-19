# Dynamic Configuration Service

[简体中文](service_dynamicconfig-zh.md) | [English](service_dynamicconfig.md)

This document is about the [Dynamic Configuration Service](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig) in **sermant-agentcore**.

- [Function Orientation](#Function-Orientation)
- [API](#API)
- [Implementation for ZooKeeper](#Implementation-for-ZooKeeper)
- [Implementation  for Kie](#Implementation-for-Kie)
- [Wrapper](#Wrapper)
- [How to Use](#How-to-Use)

## Function Orientation

**Dynamic Configuration Service** is a service that allows developers to dynamically pull configuration from servers, acting as a [Unified Configuration System](../user-guide/agentcore.md#Unified-Configuration-System). Its core purpose is to solve the problem that the configuration provided by the latter can not be changed.

## API

The functionality `API` of **Dynamic Configuration Service** is provided by the abstract class [DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/DynamicConfigService.java) , which implements three interfaces, as seen in [api](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api) directory, The concrete interface is as follows:：

|Interface|Method|Explanation|
|:-|:-|:-|
|[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyService.java)|String getConfig(String)|Get the configured value for a key (default group).|
|[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyService.java)|boolean publishConfig(String, String)|Set value for a key (default group) .|
|[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyService.java)|boolean removeConfig(String)|Remove a configured value for a key (default group).|
|[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyService.java)|List\<String> listKeys()|Get all keys (default group).|
|[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyService.java)|boolean addConfigListener(String, [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java))|Add a listener for a key (default group).|
|[KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyService.java)|boolean removeConfigListener(String)|Remove a listener for a key (default group).|
|[KeyGroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyGroupService.java)|String getConfig(String, String)|Get the configured value for a key in the group.|
|[KeyGroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyGroupService.java)|boolean publishConfig(String, String, String)|Set value for a key in the group.|
|[KeyGroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyGroupService.java)|boolean removeConfig(String, String)|Remove the configured value for a key in the group.|
|[KeyGroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyGroupService.java)|boolean addConfigListener(String, String, [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java))|Add a listener for a key in the group.|
|[KeyGroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyGroupService.java)|boolean removeConfigListener(String, String)|Remove a listener for a key in the group.|
|[GroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/GroupService.java)|List\<String> listKeysFromGroup(String)|Get all keys in the group.|
|[GroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/GroupService.java)|boolean addGroupListener(String, [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java))|Add listeners for all keys in the group.|
|[GroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/GroupService.java)|boolean removeGroupListener(String)|Remove listeners for all keys in the group.|
|[DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/DynamicConfigService.java)|boolean addConfigListener(String, [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java), boolean)|Add a listener for a key(default group). Whether to trigger the initialization event depends on the input parameters|
|[DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/DynamicConfigService.java)|boolean addConfigListener(String, String, [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java), boolean)|Add a listener for a key in the group. Whether to trigger the initialization event depends on the input parameters.|
|[DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/DynamicConfigService.java)|boolean addGroupListener(String, [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java), boolean)|Add listeners for all keys in the group. Whether to trigger the initialization event depends on the input parameters.|

Above all, two concepts need to be clear:

- `Key`, a single reference to a dynamical configuration key
- `Group`, a dynamical configuration set of groups, often used to distinguish between users

As you can see, the above `API` is mainly divided into data adding, deleting, querying and modifying operations, and add/remove operations of the listener's [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java). The latter event callback is a crucial part of the functionality of the **Dynamic Configuration Service**, which is the main feature of the plugin using **Dynamic Configuration Service**.

Also, in the [KeyService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyService.java) interface, all the `API` defined are `API` without `Group`. They will actually use the default `Group` and be fixed to `API` of [KeyGroupService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/api/KeyGroupService.java) in [DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/DynamicConfigService.java). The default `Group` can be modified via `dynamic.config.default_group` in the **unified configuration file** `config.properties`.

Finally, besides the above service interfaces, there are a few other interfaces, configurations, or entities that developers need to pay attention to:

- Static configuration [DynamicConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/config/DynamicConfig.java) for **Dynamic Configuration Service**, which involves the following configuration:
  |Type|Property|Key in Unified Configuration File|Explanation|
  |:-|:-|:-|:-|
  |int|timeoutValue|dynamic.config.timeout_value|Timeout for server connection, unit: ms|
  |String|defaultGroup|dynamic.config.default_group|Default group|
  |String|serverAddress|dynamic.config.server_address|Server address, must be of the form: {@code host:port[(,host:port)...]}|
  |[DynamicConfigServiceType](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigServiceType.java)|serviceType|dynamic.config.dynamic_config_type|Service implementation type, take NOP, ZOOKEEPER, KIE|
  
- **Dynamic configuration service implementation type**[DynamicConfigServiceType](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigServiceType.java), contains:
  
  |Enum|Explanation|
  |:-|:-|
  |ZOOKEEPER|ZooKeeper|
  |KIE|ServiceComb Kie|
  |NOP|No implementation|
  
- **Dynamic configuration listener** [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java), which contains the following interface methods:
  
  |Method|Explanation|
  |:-|:-|
  |void process([DynamicConfigEvent](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigEvent.java))|Callback interface for handling change events of configuration|
  
- **Change events of dynamic configuration** [DynamicConfigEvent](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigEvent.java), whose member properties are as follows:
  
  |Type|Property|Explanation|
  |:-|:-|:-|
  |String|key|Key of configuration|
  |String|group|Group of configuration|
  |String|content|Content of configuration|
  |[DynamicConfigEventType](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigEventType.java)|changeType|Type of configuration change event|
  
- **Type of change events of dynamic configuration** [DynamicConfigEventType](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigEventType.java), which contains following four kinds:
  
  |Enum|Explanation|
  |:-|:-|
  |INIT|Initial response when adding a listener|
  |CREATE|Event of adding new configuration|
  |MODIFY|Event of modifying configuration|
  |DELETE|Event of deleting configuration|

## Implementation for ZooKeeper

For `ZooKeeper` servers, the dynamic configuration is the value of the ZooKeeper node. The `Key` and `Group` should be used as elements to build the **node path**. Since `Group` contains user-specific information, it should be the prefix string for the **node path** so that the `Key` value exists as the second half:
```txt
/${group}/${key} -> ${value}
```

As for [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java), we convert it to a `Watcher` of `ZooKeeper`.

The implementation of `Zookeeper` could be found in [zookeeper](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/zookeeper). It mainly contains [ZooKeeperDynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/zookeeper/ZooKeeperDynamicConfigService.java) and [ZooKeeperBufferedClient](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/zookeeper/ZooKeeperBufferedClient.java).

- [ZooKeeperDynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/zookeeper/ZooKeeperDynamicConfigService.java) is an implementation of [DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/DynamicConfigService.java) for `ZooKeeper`, whose main duty is to complete the following parameter conversions:
  
  - `Key` and `Group` -> `ZooKeeper` node path
  - [DynamicConfigListener](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/common/DynamicConfigListener.java) -> `Watcher` of `ZooKeeper`。
  
  After they are parsed, [ZooKeeperBufferedClient](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/zookeeper/ZooKeeperBufferedClient.java) will do the business operation.
- [ZooKeeperBufferedClient](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/zookeeper/ZooKeeperBufferedClient.java), its main function is to wrap the native `ZooKeeper` client to provide higher-level `API`:
  
  |Method|Explanation|
  |:-|:-|
  |boolean ifNodeExist(String)|Check whether the node exists.|
  |String getNode(String)|Query node information.|
  |boolean createParent(String)|Create the parent node of a node.|
  |boolean updateNode(String, String)|Update the content of a node. If the node does not exist, it will be automatically created.|
  |boolean removeNode(String)|Remove a node|
  |List\<String> listAllNodes(String)|Query the path set of all descendant nodes under a node|
  |boolean addDataLoopWatch(String, Watcher, BreakHandler)|Add a temporary data watcher for a loop. This watcher will be re-registered after triggering until it receives a watcher remove event <p> Note that when other watchers on the same node are accurately removed, the watcher will choose to abandon the loop registration because it cannot identify whether it has been removed.|
  |boolean addPersistentRecursiveWatches(String, Watcher)|Add a watches for persistent recursion, valid for descendant nodes|
  |boolean removeDataWatches(String)|Remove data watchers|
  |boolean removeAllWatches(String)|Remove all watchers under a node, including descendant nodes|
  |void close()|Close `ZooKeeper` client|

## Implementation for Kie

For the `Kie` service, the so-called dynamic configuration is the value of the `Kie'` configuration. `Kie` queries the associated configuration based on the label. `Key` and `Group` are the elements of the associated configuration. `Key` is the name of the configured Key, and `Group` is the label of the associated Key. Each `Key` can be configured with one or more labels. The format is usually as follows:

```properties
{
	"key": "keyName",                # key
	"value": "value",                # value
	"labels": {
		"service": "serviceName"     #label，support one or more
	},
	"status": "enabled"
}
```

Compared with `Zookeeper`, `Kie` is more focused on `Group` and its value transfer format is different. The value transfer format of `Kie` is as follows:

```properties
groupKey1=groupValue1[&groupKey2=groupValue2...]
```

> `groupKey` is the key of label, `groupValue` is the value of label. Multiple labels are spliced by `&`. `Group` could be  generated by [LabelGroupUtils#createLabelGroup](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/utils/LabelGroupUtils.java).
>
> **NOTE：**
>
> ​	If the input `Group` is not in the above format, the label `Group=input Group` will be added by default.

The implementation of `Kie` could be found in [kie](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/kie), which contains [KieDynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/kie/KieDynamicConfigService.java), [LabelGroupUtils](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/utils/LabelGroupUtils.java) and [SubscriberManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/kie/listener/SubscriberManager.java):

- [KieDynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/kie/KieDynamicConfigService.java) is an implementation of [DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/DynamicConfigService.java) for `Kie`. Its main duty is to wrap the subscription API of [SubscriberManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/kie/listener/SubscriberManager.java) and the `Key` management of `Group`.

- [LabelGroupUtils](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/utils/LabelGroupUtils.java) is used for conversion of `Group`, which contains following APIs：

  | Method                      | Explanation                                         |
  | --------------------------- | --------------------------------------------------- |
  | createLabelGroup（Map）     | Create labels and transfer multiple labels in KV    |
  | getLabelCondition（String） | Converts the `Group` to the condition for a request |
  | isLabelGroup（String）      | Check whether it is a label of `Kie`                |

- The main responsibility of [SubscriberManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/kie/listener/SubscriberManager.java) is to manage all the `Group` subscribers and to provide data update notifications. It will establish a connection request task with `Kie` according to the subscribed Group, namely the Label Group, and dynamically monitor data update changes. This class contains the following APIs:

  | Method                                                       | Explanation                                                  |
  | ------------------------------------------------------------ | ------------------------------------------------------------ |
  | boolean addGroupListener(String, DynamicConfigListener, boolean) | And a listener for a label group , which is managed by [SubscriberManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/kie/listener/SubscriberManager.java). Listening task will be established and the ability to notify the first subscription is provided. |
  | boolean removeGroupListener(String, DynamicConfigListener)   | Remove a label group listener.                               |
  | boolean publishConfig(String, String, String)                | Publish configuration of Kie                                 |

## Wrapper

You can learn from [Sermant-agentcore-core](../user-guide/agentcore.md#Plugin-Service-System) that **Plugin Service System** is based on `SPI`.

The corresponding ` SPI ` implementation of [DynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/DynamicConfigService.java) is [BufferedDynamicConfigService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/BufferedDynamicConfigService.java). The latter is the wrapper of all the methods of the former. It reads [DynamicConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/dynamicconfig/config/DynamicConfig.java) configuration at startup, selects concrete service implementation and delegates all `API` to these concrete service implementations via `dynamic.config. dynamic_config_type`.

## How to Use

**Dynamic Configuration Service** is mainly used in plugin interceptors or plugin services. The specific way can be found [Plugin Code Development Guide](dev_plugin_code.md). The important thing is the construction of `Key` and `Group`. For details, see the requirements for these two values in the previous implementations.

[Back to README of **Sermant** ](../README.md)
