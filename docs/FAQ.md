# Frequently Asked Questions
## Sermant 框架功能

### 启动参数appName是什么参数?

- `appName`表示宿主应用的名称，多个实例`appName`可以相同，`实例id`不同。

### Sermant 提供哪些方面的服务治理插件?

- Sermant有着很强的扩展性，除了框架本身提供的服务治理插件([限流降级功能介绍](user-guide/flowcontrol/flowcontrol.md)，[服务注册功能介绍](user-guide/registry/document.md)等)之外， 开发者可以根据业务需求去开发插件包括(数据收集，链路等)。

## 限流降级 插件

### 关于业务场景的apiPath是如何定义的?

- `apiPath`指需要作用的接口，针对不同框架定义会有所不同，当前支持http与dubbo协议请求：
  - `http协议`： 指请求的路径，例如存在接口http://localhost:8080/test, 则其`apiPath`为`/test`；
  - `dubbo协议`：由`"请求接口：接口版本.方法"`组成，如果无接口版本或者版本为0.0.0，则`apiPath`为`"请求接口.方法"`。

### 如何确定配置规则生效

- 首先需在配置中心上正确配置相关业务场景与治理策略，配置后可观察agent日志，一般在jar包启动路径的logs文件夹下，查看sermant-x.log文件， 搜索`has been`或者配置的键名， 若搜索到的日志与当前时间匹配，则说明规则已生效。

### 熔断策略未生效的可能原因

- 熔断生效有一定的前提，通常熔断从两个指标来判定：
  - `异常比例`：即接口请求发生异常时所占比例，在规定时间内发生异常的比例大于配置的即会触发熔断；
  - `慢调用比例`：即接口请求发生慢调用所占比例，设置熔断策略时需设置慢调用的阈值，例如100ms，则必须接口调用耗时超出100ms且超过配置的慢调用比例才可触发；
- 因此针对以上两项指标，首先排查应用接口是否满足以上其中一个条件，且规则时间内调用超过最小调用数（minimumNumberOfCalls配置）才可触发。

### 隔离仓规则未生效的可能原因

- 隔离仓规则需满足以下条件：
  - `调用满足并发数`（maxConcurrentCalls配置）要求，例如配置的阈值为2，则确保并发数需大于2；
  - `最大等待时间`（maxWaitDuration配置），即在达到最大并发数时，线程等待最大时间，超过该时间未拿到许可便会触发；
- 因此在实际测试时，若模拟该规则，建议确保业务接口耗时大于最大等待时间，并且并发数大于配置值。

### 重试规则未生效的可能原因

- 确保下游应用抛出的异常或者状态码符合重试策略要求，例如默认dubbo会检测下游是否抛出RpcException，Spring应用则可配置指定状态码检测。

## 服务注册 插件

### 报错 - No such extension org.apache.dubbo.registry.RegistryFactory by name sc

如下图所示：

  ![No such extension org.apache.dubbo.registry.RegistryFactory by name sc](./binary-docs/registry-faq-1.png)

该错误原因有以下3种：
- 宿主没有带agent启动。因为原生dubbo并不支持往sc注册，所以如果没带agent启动且配置的注册地址的协议为sc时，就会产生以上报错。
- 核心配置文件（${agent_package_path}/agent/config/config.properties）配置问题。仔细观察启动日志，会发现伴有类似以下的错误：

  ![核心配置文件错误](./binary-docs/registry-faq-2.png)

原因是核心配置文件中，配置中心类型（dynamic.config.dynamic_config_type）的值（需要为KIE/ZOOKEEPER）配置错误，从而导致宿主应用无法加载agent，最后导致No such extension org.apache.dubbo.registry.RegistryFactory by name sc的报错。 
- 核心配置文件（${agent_package_path}/agent/config/config.properties）配置问题。仔细观察启动日志，会发现伴有类似以下错误：

  ![核心配置文件错误](./binary-docs/registry-faq-3.png)

原因是核心配置文件中，配置中心地址（dynamic.config.server_address）配置错误或者配置中心没有启动或者网络不通，从而导致宿主应用无法加载agent，最后导致No such extension org.apache.dubbo.registry.RegistryFactory by name sc的报错。

### 报错 - sermant - /sermant/master/v1/register error

如下图所示：

  ![register error](./binary-docs/registry-faq-4.png)

- 原因是backend未启动或者配置地址不正确，请启动backend或正确配置地址。backend相关信息请见[backend文档](./user-guide/backend.md)。
- 注：该错误不会影响注册插件的流程，但会有相关报错。

### 报错 - Connection reset

如下图所示：

  ![Connection reset](./binary-docs/registry-faq-5.png)

- 请检查插件配置（${agent_package_path}/agent/pluginPackage/service-registry/config/config.yaml）中，注册中心地址（servicecomb.service.address）是否正确，协议是否正确（http/https）。

### 报错 - https protocol is not supported

如下图所示：

  ![https protocol is not supported](./binary-docs/registry-faq-6.png)

- 需要在插件配置（${agent_package_path}/agent/pluginPackage/service-registry/config/config.yaml）中，开启ssl（servicecomb.service.sslEnabled）。


### 报错 - No such extension org.apache.dubbo.metadata.report.MetadataReportFactory by name sc

如下图所示：

  ![No such extension org.apache.dubbo.metadata.report.MetadataReportFactory by name sc](./binary-docs/registry-faq-7.png)

请检查dubbo应用的注册配置，protocol是否存在且不为sc，如下所示：

- 例如dubbo/provider.xml

```xml
<dubbo:registry address="sc://127.0.0.1:30100" protocol="nacos"/>
```

- 例如application.yml（或application.properties），以application.yml为例
```yml
dubbo:
  registry:
    protocol: nacos
    address: sc://127.0.0.1:30100
```

如果protocol存在且不为sc，请把protocol的值设置成sc，或者删除protocol配置。

### 报错 - No registry config found or it's not a valid config

如下图所示：

  ![No registry config found or it's not a valid config](./binary-docs/registry-faq-8.png)

需要设置dubbo本身注册中心地址的配置，请参考[服务注册插件文档](./user-guide/registry/document.md#按需修改插件配置文件)中，关于**新开发**dubbo应用的说明。

### 插件配置中，enableSpringRegister/enableDubboRegister与openMigration之间的关系是什么？

enableSpringRegister/enableDubboRegister与openMigration之间的关系如下表所示：

|enableSpringRegister/enableDubboRegister|openMigration|作用|
|---|---|---|
|true|true|开启spring cloud/dubbo迁移功能|
|true|false|开启spring cloud/dubbo sc注册|
|false|true|关闭注册插件|
|false|false|关闭注册插件|

[返回**QuickStart**说明文档](./QuickStart.md)

[返回**Sermant**说明文档](./README.md)