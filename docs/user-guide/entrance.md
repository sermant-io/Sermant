# Entrance of Sermant

[简体中文](entrance-zh.md) | [English](entrance.md)

This document is about [**Entrance of Sermant**](../../sermant-agentcore/sermant-agentcore-premain).

## Components

`source` contains：

- `common`, contains the common code.
  - `BootArgsBuilder`, is used to load the configuration at startup.
  - `PathDeclarer`, defines the locations of resources in the sermant-agentcore.
- `exception`, contains custom exceptions.
- `AgentPremain`，the entrance of `JavaAgent`. Refer to [How to Use](#How to Use).

`resources` contains：

- `config/bootstrap.properties`，the bootstrap configuration file, loaded by [BootArgsBuilder](../../sermant-agentcore/sermant-agentcore-premain/src/main/java/com/huawei/sermant/premain/common/BootArgsBuilder.java), which forms the startup parameter *Map* together with the launch input parameter.

## **How to Use**

The **Entrance of Sermant** packages the *Java Agent* entrance package `sermant-agent.jar`. When you execute *Java* command, you can add the following parameters to bring up **Sermant** :

```shell
-javaagent:sermant-agent.jar[=${options}]
```

Where `${options}` is the launch input parameter, which will be transferred as the parameter `agentArgs` to the `premain` method.

```java
public static void premain(String agentArgs, Instrumentation inst);
```

The format of the `agentArgs` parameter should look like `key1=value1,key2=value2[(,keyN=valueN)...]`, which splits key/value pairs with `','`, and splits between key and value with `'='` 'to form a `Map` structure.

Note that `agentArgs` must contain a key/value pair whose `key` is `appName`. So the *Java* command argument added at startup should look exactly like this:

```shell
-javaagent:sermant-agent.jar=appName=${appName}[,${otherOptions}]
```

Where `${appName}` represents the appName and `${otherOptions}` represents other parameters.

More *Java Agent* related content can be found in the official documentation for [java.lang.instrument](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html).

## Function Explanation

The **Entrance of Sermant** serves as the entry point to the **Sermant**, which brings the application to the **core functionality**. The functions of entry class [AgentPremain](../../sermant-agentcore/sermant-agentcore-premain/src/main/java/com/huawei/sermant/premain/AgentPremain.java) include:

- Prevent **Sermant** from being fetched more than once by the host application (*Java Agent* command is added more than one **Sermant** entrance package).
- Load the packages of **core functionality**, using the system class loader.
- Encapsulate `agentArgs` and the bootstrap configuration [bootstrap.properties](../../sermant-agentcore/sermant-agentcore-config/config/bootstrap.properties)as startup parameter `Map`.
- Invoke the entrance of **core functionality** [AgentCoreEntrance](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/AgentCoreEntrance.java) to bring up the core functionality.
- Note that **path-related parameters** are transferred to bring the **core functionality**. These **path-related parameters** are encapsulated in the startup parameters `Map`. This also means that **Sermant AgentCore Premain** needs to be able to control the directory and file location.

## Startup Parameters

**Startup Parameters** refer to the configuration `Map` encapsulated by the input parameter `agentArgs` and the bootstrap configuration `bootstrap.properties`, taking precedence to fetch the value from the former. The fixed contents of **startup parameters** are as follows:

|Key in Input Parameters|Key in Bootstrap Configuration|Key in Startup Parameters|Explanation|Default Value|NotNull|Notes|
|:-|:-|:-|:-|:-:|:-|:-|
|appName|app.name|appName|The name of host application|/|True|Must present in startup parameters|
|instanceName|instance.name|instanceName|The name of the specific instance|default|True|/|
|appType|app.type|appType|The type of host application|0|True|/|
|env|env|env|/|/|False|/|
|envTag|env.tag|envTag|/|/|False|/|
|business|business|business|/|/|False|/|
|subBusiness|sub.business|subBusiness|/|/|False|/|
|envSecret|env.secret|envSecret|/|/|False|/|
|accessKey|access.key|access.key|/|/|False|/|
|secretKey|secret.key|secret.key|/|/|False|/|
|masterAddress|master.address|master.address|/|/|False|/|
|/|/|agentPath|The directory of Entrance package|The directory of Entrance package|True|No need to configure|
|/|/|bootPath|/|/|True|Deprecated|
|/|/|pluginsPath|/|/|True|Deprecated|
|/|/|sermant.config.file|Unified Configuration File|Unified Configuration File|True|No need to configure|
|/|/|sermant.plugin.setting.file|Plugin Setup Configuration|Plugin Setup Configuration|True|No need to configure|
|/|/|sermant.plugin.package.dir|The directory of plugin package|The directory of plugin package|True|No need to configure|
|/|/|sermant.log.setting.file|The directory of log configuration file|The directory of log configuration file|True|No need to configure|

The `agentArgs` parameter allows you to configure more values for **startup parameters**, which may be used in the [Unified Configuration System](agentcore.md#Unified-Configuration-System).

## Related Documents

|Document|
|:-|
|[Introduction to Sermant-agentcore-core](agentcore.md)|
|[Introduction to Backend Module](backend.md)|

[Back to README of **Sermant** ](../README.md)
