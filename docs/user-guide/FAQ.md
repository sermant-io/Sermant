# Sermant 框架功能

## 启动参数appName是什么参数?

- `appName`表示宿主应用的名称，多个实例`appName`可以相同，`实例id`不同。

## Sermant提供哪些方面的服务治理插件?

- Sermant有着很强的扩展性，除了框架本身提供的服务治理插件([限流降级功能介绍](../user-guide/flowcontrol/flowcontrol.md)，[服务注册功能介绍](../user-guide/registry/document.md)等)之外， 开发者可以根据业务需求去开发插件包括(数据收集，链路等)。
