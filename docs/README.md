# Introduction to Sermant Development and Usage

[简体中文](README-zh.md) | [English](README.md) 

Sermant is a bytecode enhancement technology based on Java Agent. It uses Java Agent to enhance the host application in a non-intrusive way to solve the microservice governance problem of Java applications. The original intention of Sermant is to establish a solution ecosystem for micro-service governance that is non-intrusive to the development state, reduce the difficulty of service governance development and use, and achieve the effect of simplified development and plug-and-play by means of abstract interface, function integration and plugin isolation.

This document describes how to develop and use Sermant in detail.

## Runtime Environment

- [HuaweiJDK 1.8](https://gitee.com/openeuler/bishengjdk-8) / [OpenJDK 1.8](https://github.com/openjdk/jdk) / [OracleJDK 1.8](https://www.oracle.com/java/technologies/downloads/)
- [Apache Maven 3](https://maven.apache.org/download.cgi)

## Project Modules

**Sermant** contains the following modules:

- [sermant-agentcore](../sermant-agentcore): *Java Agent* related content
  - [sermant-agentcore-core](../sermant-agentcore/sermant-agentcore-core): Core functional module of the framework
  - [sermant-agentcore-premain](../sermant-agentcore/sermant-agentcore-premain): *Java Agent* entry module
  - [sermant-agentcore-config](../sermant-agentcore/sermant-agentcore-config): Configuration module of the framework
- [sermant-backend](../sermant-backend): Server side of message sending module 
- [sermant-package](../sermant-package): Packaging module
- [sermant-plugins](../sermant-plugins):  Root module of plugins, contains a variety of functional plugins and related add-ons
- [sermant-injector](../sermant-injector): Admission webhook module for deployment of sermant-agent via containers 

## Packaging Steps

The packaging process of **Sermant** is roughly divided into the following steps:

- *agent*: Compile or package core function and plugins
- *example*: Compile or package core function and demo modules(disabled by default)
- *backend*: Compile or package **Sermant** backend module
- *ext*: Compile and package the backend, frontend, and other add-ons that come with plugins
- *package*: Archive the above packaging results as a product package
- *release*: Publish built artifacts to Maven Central Repository
- *all*: Perform all the preceding steps (disabled by default)

Execute the following *maven* command  package the **Sermant** project by default：

```shell
mvn clean package -Dmaven.test.skip
```

After the command is executed, a folder such as `sermant-agent-x.x.x` and a compressed file such as `sermant-agent-x.x.x.tar.gz` will be generated in the project directory. The latter is the product package of **sermant** and the former is the decompressed content of the product package.

## Product Directory

- *agent*: Java Agent related product
  - *config*: Configuration file directory
    - *bootstrap.properties*: Startup configuration
    - *config.properties*: Core function configuration
    - *plugins.yaml*: Plugin configuration, which config the plugin functionality that needs to be loaded
  - *core/sermant-agentcore-core-x.x.x.jar*: Core package of **Sermant** framework
  - *pluginPackage*: Plugin package directory, where plugins are classified by feature name
    - *xxx*: Any plugin functionality
      - *config/config.yaml*: Plugin configuration file
      - *plugin*: Plugin package directory
      - *service*: plugin service's package directory
  - *sermant-agent.jar*: Java Agent entry package
- *server*: Server directory, which contains server with **Sermant**, plugin server and client

## Containerized Deployment
In K8S environment, Sermant supports quickly deployment by using Sermant-Injector module to automatically mount Sermant-Agent package for host application. To know more about deploying Sermant-Injector and host applications, you can refer to [Containerized Deployment Guide](user-guide/injector.md).

## Plugin Development

How to develop a new plugin module？You can refer to the [Plugin Module Development Guide](dev-guide/dev_plugin_module.md) for details on adding plugins, plugin services, and add-ons.

How to write a plugin can be found in the [Plugin Code Development Guide](dev-guide/dev_plugin_code.md), which covers most of the possible scenarios in developing a plugin.

## Other Related Documents

|Document Name|Type of Document|
|---|---|
|[Third Party Copyright Declaration](dev-guide/third_party_copyright.md)|Development Guide|
|[Version Management Guide](dev-guide/version_manage.md)|Development Guide|
|[Plugin Module Development Guide](dev-guide/dev_plugin_module.md)|Development Guide|
|[Plugin Code Development Guide](dev-guide/dev_plugin_code.md)|Development Guide|
|[Plugin without Source Code Development Guide](dev-guide/dev_plugin_introduce.md)|Development Guide|
|[Introduction to Dynamic Configuration Service](dev-guide/service_dynamicconfig.md)|Development Guide|
|[Introduction to Heartbeat Service](dev-guide/service_heartbeat.md)|Development Guide|
|[Introduction to Gateway Service](dev-guide/service_send.md)|Development Guide|
|[Introduction to Core Modules](user-guide/agentcore.md)|User Guide|
|[Introduction to Sermant Entrance Module](user-guide/entrance.md)|User Guide|
|[Introduction to Backend Module](user-guide/backend.md)|User Guide|
|[Containerized Deployment Guide](user-guide/injector.md) |User Guide|
|[Introduction to Flow Control and Degradation](user-guide/flowcontrol/flowcontrol.md)|User Guide|
|[Introduction to Loadbalancer](user-guide/loadbalancer/document.md)|User Guide|
|[Introduction to Dynamic Configuration](user-guide/dynamic-config/document.md)|User Guide|
|[Introduction to Service Registration](user-guide/registry/document.md)|User Guide|
|[Introduction to Tag Router](user-guide/router/document.md)|User Guide|
|[Introduction to Graceful online/offline](user-guide/graceful/document.md)|User Guide|
|[Introduction to Server Monitor](user-guide/server-monitor/document.md)|User Guide|
|[Introduction to Plugin for Threadlocal](user-guide/threadlocal/document.md)|User Guide|
|[Summary List of Current Plugin Features](user-guide/feature-list.md)|User Guide|
|[FAQ](./FAQ.md)|User Guide|

