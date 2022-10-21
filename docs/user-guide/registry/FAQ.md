# Service Registration FAQ

[简体中文](FAQ-zh.md) | [English](FAQ.md)

This document describes frequently asked questions (FAQs) about using the [service registration plugin](../../../sermant-plugins/sermant-service-registry).

## Exception: No Such Extension org.apache.dubbo.registry.RegistryFactory by name sc

As shown in the following figure:

![No such extension org.apache.dubbo.registry.RegistryFactory by name sc](../../binary-docs/registry-faq-1.png)

The possible causes are as follows:

1.1 The application is not started with the agent. The native dubbo does not support registration with the SC. Therefore, if the application started without agent and the protocol of the registration address is SC, the preceding error is reported.

1.2 The core configuration file (${agent_package_path}/agent/config/config.properties) is incorrectly configured. Check the startup log carefully and you will find an error similar to the following:

![核心配置文件错误](../../binary-docs/registry-faq-2.png)

- The value of the configuration center type (dynamic.config.dynamic_config_type) in the core configuration file is incorrect. As a result, the host application cannot load the agent and the No such extension org.apache.dubbo.registry.RegistryFactory by name sc reports an error.

1.3 The core configuration file (${agent_package_path}/agent/config/config.properties) is incorrectly configured. Check the startup log carefully. The error information similar to the following is displayed:

![核心配置文件错误](../../binary-docs/registry-faq-3.png)

- The configuration center address (dynamic.config.server_address) is incorrectly configured in the core configuration file, the configuration center is not started, or the network is disconnected. As a result, the host application fails to load the agent, and the No such extension org.apache.dubbo.registry.RegistryFactory by name sc reports an error.

## Exception: /sermant/master/v1/register error

As shown in the following figure:

![register error](../../binary-docs/registry-faq-4.png)

The cause is that the backend is not started or the configured address is incorrect. Start the backend or configure the address correctly. For details about the backend, see the [backend document](../backend.md).

Note: This error does not affect the plugin registration process, but related errors may be reported.

## Exception: Connection reset

As shown in the following figure:

![Connection reset](../../binary-docs/registry-faq-5.png)

Check whether the registration center address (servicecomb.service.address) and protocol (HTTP/HTTPS) in the plugin configuration (${agent_package_path}/agent/pluginPackage/service-registry/config/config.yaml) are correct.

## Exception: https protocol is not supported

As shown in the following figure:

![https protocol is not supported](../../binary-docs/registry-faq-6.png)

You need to enable ssl (servicecomb.service.sslEnabled) in the plugin configuration (${agent_package_path}/agent/pluginPackage/service-registry/config/config.yaml).

## Exception: No such extension org.apache.dubbo.metadata.report.MetadataReportFactory by name sc

As shown in the following figure:

![No such extension org.apache.dubbo.metadata.report.MetadataReportFactory by name sc](../../binary-docs/registry-faq-7.png)

Check the registration configuration of the dubbo application. Check whether protocol exists and is not sc.

- Example for dubbo/provider.xml

```xml
<dubbo:registry address="sc://127.0.0.1:30100" protocol="nacos"/>
```

- For example, application.yml (or application.properties). The following uses application.yml as an example.
```yml
dubbo:
  registry:
    protocol: nacos
    address: sc://127.0.0.1:30100
```

If protocol exists and is not set to sc, set protocol to sc or delete the protocol configuration.

## Exception: No registry config found or it's not a valid config

As shown in the following figure:

![No registry config found or it's not a valid config](../../binary-docs/registry-faq-8.png)

For details about how to set the address of the DUBBO registration center, see the description of newly developed DUBBO applications in the [service registration plugin](./document.md#Modify-the-plugin-configuration-file-on-demand) document.

## What Is The Relationship Between Plugin Configuration, enableSpringRegister/enableDubboRegister, And openMigration?

The following table describes the relationship between enableSpringRegister/enableDubboRegister and openMigration.

|enableSpringRegister/enableDubboRegister|openMigration|effect|
|---|---|---|
|true|true|Enabling the Spring Cloud/Dubbo Migration Function|
|true|false|Enable Spring cloud/Dubbo With SC Registration|
|false|true|Disabling the Registration Plugin|
|false|false|Disabling the Registration Plugin|



[Back to **Service Registration**](./document.md)

[Back to README of **Sermant** ](../../README.md)