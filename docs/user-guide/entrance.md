# 入口模块介绍

本文档主要介绍[**Sermant入口模块**](../../sermant-agentcore/sermant-agentcore-premain)。

## 组成部分

`source`文件夹下：

- `common`目录，存放公共内容。
  - `BootArgsBuilder`类，用于加载启动配置。
  - `PathDeclarer`类，用于声明相关资源的路径。
- `exception`目录，存放自定义异常。
- `AgentPremain`类，为`JavaAgent`入口，详见[使用方式](#使用方式)。

`resources`文件夹下：

- `config/bootstrap.properties`文件，为启动配置文件，将被[BootArgsBuilder](../../sermant-agentcore/sermant-agentcore-premain/src/main/java/com/huawei/sermant/premain/common/BootArgsBuilder.java)类加载出来，连同启动入参共同构成启动参数*Map*。

## 使用方式

**入口模块**打包输产出*JavaAgent*入口包`sermant-agent.jar`，在执行*Java*命令时，可以添加如下参数带起**Sermant**：

```shell
-javaagent:sermant-agent.jar[=${options}]
```

其中`${options}`为启动入参，会作为`premain`方法的入参`agentArgs`传入。

```java
public static void premain(String agentArgs, Instrumentation inst);
```

参数`agentArgs`的格式要求形如`key=value,key=value[(,key=value)...]`，以`','`分割键值对，以`'='`分割键值，形成`Map`结构。

其中需要注意的是，`agentArgs`中必须包含一个`key`为`appName`的键值对，因此启动时添加的*Java*命令参数准确来说应该如下：

```shell
-javaagent:sermant-agent.jar=appName=${appName}[,${otherOptions}]
```

其中`${appName}`表示应用名称，`${otherOptions}`表示其他参数。

更多*JavaAgent*相关内容可以参见官方文档对[java.lang.instrument](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html)包的详解

## 功能解析

**入口模块**作为**Sermant**的入口，起到的是将程序带入**核心功能**的作用，入口类[AgentPremain](../../sermant-agentcore/sermant-agentcore-premain/src/main/java/com/huawei/sermant/premain/AgentPremain.java)所做的工作包括：

- 防止**Sermant**被宿主多次带起（*JavaAgent*命令中添加了复数的**Sermant**入口包）。
- 加载**核心功能**包，这里使用系统类加载器加载。
- 将`agentArgs`和启动配置[bootstrap.properties](../../sermant-agentcore/sermant-agentcore-premain/src/main/resources/config/bootstrap.properties)封装为启动参数`Map`。
- 调用**核心功能**入口[AgentCoreEntrance](../sermant-agentcore-core/src/main/java/com/huawei/sermant/core/AgentCoreEntrance.java)带起**核心功能**。
- 注意，带起**核心功能**时需要将**路径相关参数**传递进去，这里将这些**路径相关参数**封装到启动参数`Map`中。这也说明**Sermant AgentCore Premain**需要做到对目录、文件位置的把控。

## 启动参数

**启动参数**指的是入参`agentArgs`和启动配置`bootstrap.properties`封装起来的配置`Map`，优先取前者的值。**启动参数**中固定内容如下：

|入参键|启动配置键|启动参数键|含义|默认值|不为空|备注|
|:-|:-|:-|:-|:-:|:-|:-|
|appName|app.name|appName|应用名称|/|是|入参中必须存在|
|instanceName|instance.name|instanceName|实例名称|default|是|/|
|appType|app.type|appType|应用类型|0|是|/|
|env|env|env|/|/|否|/|
|envTag|env.tag|envTag|/|/|否|/|
|business|business|business|/|/|否|/|
|subBusiness|sub.business|subBusiness|/|/|否|/|
|envSecret|env.secret|envSecret|/|/|否|/|
|accessKey|access.key|access.key|/|/|否|/|
|secretKey|secret.key|secret.key|/|/|否|/|
|masterAddress|master.address|master.address|/|/|否|/|
|/|/|agentPath|入口包目录|入口包目录|是|无需配置|
|/|/|bootPath|/|/|是|已废弃|
|/|/|pluginsPath|/|/|是|已废弃|
|/|/|sermant.config.file|统一配置文件|统一配置文件|是|无需配置|
|/|/|sermant.plugin.setting.file|插件设定文件|插件设定文件|是|无需配置|
|/|/|sermant.plugin.package.dir|插件包目录|插件包目录|是|无需配置|
|/|/|sermant.log.setting.file|日志配置文件|日志配置文件|是|无需配置|

入参`agentArgs`中可以为**启动参数**配置更多地值，他们可能会在[统一配置系统](agentcore.md#统一配置系统)中使用到。

## 相关文档

|文档名称|
|:-|
|[核心模块介绍](agentcore.md)|
|[后端模块介绍](backend.md)|

[返回**Sermant**说明文档](../README.md)
