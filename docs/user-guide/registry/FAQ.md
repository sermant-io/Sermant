## 服务注册常见问题

本文主要介绍[服务注册插件](../../../sermant-plugins/sermant-service-registry)在使用时遇到的常见问题。

**1.常见报错 - No such extension org.apache.dubbo.registry.RegistryFactory by name sc**

如下图所示：

![No such extension org.apache.dubbo.registry.RegistryFactory by name sc](../../binary-docs/registry-faq-1.png)

该错误原因有以下3种：

1.1 宿主没有带agent启动。因为原生dubbo并不支持往sc注册，所以如果没带agent启动且配置的注册地址的协议为sc时，就会产生以上报错。

1.2 核心配置文件（${agent_package_path}/agent/config/config.properties）配置问题。仔细观察启动日志，会发现伴有类似以下的错误：

![核心配置文件错误](../../binary-docs/registry-faq-2.png)

- 原因是核心配置文件中，配置中心类型（dynamic.config.dynamic_config_type）的值（需要为KIE/ZOOKEEPER）配置错误，从而导致宿主应用无法加载agent，最后导致No such extension org.apache.dubbo.registry.RegistryFactory by name sc的报错。

1.3 核心配置文件（${agent_package_path}/agent/config/config.properties）配置问题。仔细观察启动日志，会发现伴有类似以下错误：

![核心配置文件错误](../../binary-docs/registry-faq-3.png)

- 原因是核心配置文件中，配置中心地址（dynamic.config.server_address）配置错误或者配置中心没有启动或者网络不通，从而导致宿主应用无法加载agent，最后导致No such extension org.apache.dubbo.registry.RegistryFactory by name sc的报错。

**2.常见报错 - sermant - /sermant/master/v1/register error**

如下图所示：

![register error](../../binary-docs/registry-faq-4.png)

原因是backend未启动或者配置地址不正确，请启动backend或正确配置地址。backend相关信息请见[backend文档](../backend.md)。

注：该错误不会影响注册插件的流程，但会有相关报错。

**3.常见报错 - Connection reset**

如下图所示：

![Connection reset](../../binary-docs/registry-faq-5.png)

请检查插件配置（${agent_package_path}/agent/pluginPackage/service-registry/config/config.yaml）中，注册中心地址（servicecomb.service.address）是否正确，协议是否正确（http/https）。

**4.常见报错 - https protocol is not supported**

如下图所示：

![https protocol is not supported](../../binary-docs/registry-faq-6.png)

需要在插件配置（${agent_package_path}/agent/pluginPackage/service-registry/config/config.yaml）中，开启ssl（servicecomb.service.sslEnabled）。