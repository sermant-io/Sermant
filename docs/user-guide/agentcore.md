# Sermant-agentcore-core

[简体中文](agentcore-zh.md) | [English](agentcore.md)

This document focuses on [**sermant-agentcore-core**](../../sermant-agentcore/sermant-agentcore-core), which provides important kernels for handling bytecode enhancements, unified configuration, core services, plugin management, etc.

- [Version of Agentcore-core](#Version-of-Sermant-agentcore-core)
- [Directory Structure](#Directory-Structure)
- [Bytecode Enhancement](#Bytecode-Enhancement)
- [Unified Configuration System](#Unified-Configuration-System)
  - [Unified Configuration Management Class](#Unified-Configuration-Management-Class)
  - [Unified Configuration Classes](#Unified-Configuration-Classes)
  - [Detailed Description for Properties Strategy](#Detailed-Description-for-Properties-Strategy)
  - [Detailed Description for Yaml Strategy](#Detailed-Description-for-Yaml-Strategy)
  - [Plugin Setup Configuration](#Plugin-Setup-Configuration)
- [Core Service System](#Core-Service-System)
  - [Core Service Management Class](#Core-Service-Management-Class)
  - [Core Service Class](#Core-Service-Class)
- [Plugin Management System](#Plugin-Management-System)
  - [Plugin Management Class](#Plugin-Management-Class)
  - [Plugin Classloader](#Plugin-Classloader)
  - [Plugin Configuration System](#Plugin-Configuration-System)
  - [Plugin Service System](#Plugin-Service-System)
- [Related Documents](#Related-Documents)

This article is just a brief introduction to [**sermant-agentcore-core**](../../sermant-agentcore/sermant-agentcore-core) in the meaning of each directory. If the developers want a more detailed understanding of the code's service logic, please move to the related directory or class.

## Version of Sermant-agentcore-core

The package version of sermant-agentcore-core (also called core package version) is an intrinsic property of the package, so we encapsulate the version definition in the `manifest` file as meta-information for the JAR package. Core package Version information is encapsulated in the `sermant-version` parameter of the`manifest` file, which defaults to `project.version`. In the code, you can get the core package version by:
```java
String version = BootArgsIndexer.getCoreVersion();
```

If you need to change the core package version, you can change the value of `project.version` directly.

## Directory Structure

[**Sermant-agentcore-core**](../../sermant-agentcore/sermant-agentcore-core) contains the following directories：

- `agent`, contains the deprecated code related to bytecode enhancement.
- `common`, contains the common code.
- `config`, contains the code related to [Unified Configuration System](#Unified-Configuration-System).
- `exception`, contains custom exceptions.
- `plugin`, contains the code related to [Plugin Management System](#Plugin-Management-System) and [Bytecode Enhancement](#Bytecode-Enhancement).
- `service`, contains the code related to [Core Service System](#Core-Service-System).
- `util`, contains the code of common utility classes.
- `AgentCoreEntrance`, is the entrance of [**Sermant-agentcore-core**](../../sermant-agentcore/sermant-agentcore-core)，which calls the `run` method and transfers **startup parameters** and *Instrumentation* object.

The following resources are also included in [**Sermant-agentcore-core**](../../sermant-agentcore/sermant-agentcore-core):

- `META-INF/services`, *SPI* configuration file directory.
  - `com.huaweicloud.sermant.core.config.common.BaseConfig`, declares unified configuration classes.
  - `com.huaweicloud.sermant.core.config.strategy.LoadConfigStrategy`, declares the configration loading policy.
  - `com.huaweicloud.sermant.core.service.BaseService`, declares implementations of core service.

## Bytecode Enhancement

The **Bytecode Enhancement** code for **Sermant** could be found in [agent](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent).

**Sermant** implements bytecode enhancements based on the `Byte-Buddy` framework. It mainly utilizes [**byte-buddy delegate**](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/transformer/AdviceTransformer.java) to enhance classes. For the scene of native class enhancement, [**Advice template classes**](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/template) with [**byte-buddy advice**](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/transformer/BootstrapTransformer.java) technology play a role.

The main content of `plugin/agent` directory show as following items：

- `declarer`, contains **enhancement definition interfaces** is something that plugin developers need to focus on.
- `interceptor`, contains the **interceptor interfaces**, which plugin developers need to focus on.
- `matcher`, contains the **matchers of enhanced classes**, which plugin developers need to focus on.
- `template`, contains the **Advice template class**.
- `transformer`, contains the **Bytecode Transformer**.
- `BufferedAgentBuilder`, entrance of bytecode enhancement.

### Enhancement Definition 

When coding **enhancement definitions**, plugin developers should implement `getClassMatcher` and `getInterceptDeclarers` of [PluginDeclarer](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/declarer/PluginDeclarer.java) , as detailed in the [Plugin Code Development Guide](../dev-guide/dev_plugin_code.md#Enhancement Definition).

 Don't forget to add the *SPI* configuration file for `PluginDeclarer`.

### Interceptor

When coding an **interceptor**, developers just need to implement `interceptor` of [interceptor](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/agent/interceptor) directory

For details, refer to [Plugin Code Development Guide](../dev-guide/dev_plugin_code.md#Interceptor).

## Unified Configuration System

The **Unified Configuration System** for **Sermant** can be found in [config](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/config) directory.

`config` contains following contents:

- `common`，contains common code.
  - `BaseConfig`, common interface of **Unified Configuration Classes** .
  - `ConfigFieldKey`，used for setting alias of fields for **Unified Configuration Classes**.
  - `ConfigTypeKey`, used for setting alias for **Unified Configuration Classes**.
- `strategy`, contains the code of configuration loading strategy.
  - `LoadConfigStrategy`, interface of the configuration loading strategy.
  - `LoadPropertiesStrategy`, strategy that used to load the `properties` format configuration file, which is mainly applied to load the Unified configuration file `config.properties`.
  - `LoadYamlStrategy`，, strategy that used to load the `yaml` format configuration file, which is mainly applied to load the plugin setup configuration and plugin configuration, detailed in [Plugin Configuration System](#Plugin-Configuration-System).
- `utils`，contains utility classes used by the Unified Configuration System.
- `ConfigManager`，Unified Configuration Management Class, which provides methods for loading and fetching unified configuration.

### Unified Configuration Management Class

In `ConfigManager`, which is called Unified Configuration Management Class, developers can get the instance of **Unified Configuration Classes** via the `getConfig` method:

```java
ConfigExample config = ConfigManager.getConfig(ConfigExample.class);
```

### Unified Configuration Classes

**Unified Configuration System** is a management system that loads **static configuration** as **Java Pojo**. Therefore, an **Unified Configuration Class** must be a **Java Pojo** that implement [BaseConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/config/common/BaseConfig.java) interface. The exact requirements for these **Unified Configuration Classes** are dictated by `LoadPropertiesStrategy`, as described in the [Detailed Description for Properties Strategy](#Detailed-Description-for-Properties-Strategy). As for plugin-related [plugin configuration](#Plugin-Configuration-System), this is related to the requirements of `LoadYamlStrategy`.See details in [Detailed Description for Yaml Strategy](#Detailed-Description-for-Yaml-Strategy).

An **Unified Configuration Class** is a **Java Pojo**, whose `getter`and `setter` methods can be directly generated using Lombok's `Data`, `getter`, and `setter` annotations.

Note that after coding the **Unified Configuration Class**, don't forget to add SPI configuration file of [BaseConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/config/common/BaseConfig.java) interface:

- Add `META-INF/services` folder under `resources`
- add configuration file `com.huaweicloud.sermant.core.config.common.BaseConfig` under `META-INF/services`.
- In the above file, type all the **Unified Configuration Classes** in the plugin package and split them with **LF**.

### Detailed Description for Properties Strategy 

The `LoadPropertiesStrategy` is used to load configuration files in the *properties* format, which is now mainly used to load the unified configuration file `config.properties`.

The idea of `LoadPropertiesStrategy` could be simply described as: take the full qualified class name or alias of the **Unified Configuration Class** as the prefix and the property name or alias of the **Unified Configuration Class** as the suffix, and concatenate into the key in the **properties** format, then convert to the type of the corresponding property and assign the value after getting the corresponding value. 

Suppose there is a following **Unified Configuration Class**:

```java
package com.huawei.example;
public class ConfigExample implements BaseConfig {
  private String string;
  private int intField;
  // getter and setter
}
```

The corresponding configuration might look like this:

```properties
# formant: full-qualified-class-name.propertyName=propertyValue
com.huawei.example.ConfigExample.string=value
com.huawei.example.ConfigExample.intField=123456
```

#### Property Type

The property types that `LoadPropertiesStrategy`supports includes：

- Primitive and wrapper types for Boolean and numeric classes
- String
- Enum
- Array that consist of the above types
- *List* that consist of the first three types
- *Map* that consist of the first three types

There are two ways to config `array` and `List`, one is to split the string with `,`:
```properties
# Array
com.huawei.example.ConfigExample.stringArr=value1,value2,value3
# List
com.huawei.example.ConfigExample.intList=100,200,300
```

Another way is to use index:

```properties
# Array
com.huawei.example.ConfigExample.stringArr[0]=value1
com.huawei.example.ConfigExample.stringArr[1]=value2
com.huawei.example.ConfigExample.stringArr[2]=value3
# List
com.huawei.example.ConfigExample.intList[0]=100
com.huawei.example.ConfigExample.intList[1]=200
com.huawei.example.ConfigExample.intList[2]=300
```

There are two ways to config `Map`, one is to split the key/value pairs with `,` and split the key and value with `':'` :

```properties
# Map
com.huawei.example.ConfigExample.string2IntMap=key1:value1,key2:value2,key3:value3
```

Another way is to add the key at the end of propertyName:

```properties
# Map
com.huawei.example.ConfigExample.string2IntMap.key1=value1
com.huawei.example.ConfigExample.string2IntMap.key2=value2
com.huawei.example.ConfigExample.string2IntMap.key3=value3
```

Note that `LoadPropertiesStrategy` does not support complex type properties.

#### Alias

`LoadPropertiesStrategy` supports setting alias for full qualified class name and property names using the `ConfigTypeKey` annotation and the `ConfigFieldKey` annotation. Assume the above `ConfigExample` class is modified as follows：

```java
@ConfigTypeKey("config.example")
public class ConfigExample implements BaseConfig {
  @ConfigFieldKey("stringField")
  private String string;
  private int intField;
  // getter and setter
}
```

Then the configuration file should be like this：
```properties
# alias for full-qualified-class-name.property=propertyValue
config.example.stringField=value
config.example.intField=123456
```

#### Value of Introspection

The property values in `LoadPropertiesStrategy'`support introspection. `${}` can be used to map the current configuration, system variables, startup parameters and other elements. For example, the `ConfigExample` configuration can be set to:

```properties
# appName linked to startup parameters ，user.home linked to system variable，com.huawei.example.ConfigExample.intField linked to current configuration
com.huawei.example.ConfigExample.string=value, ${appName:test}, ${user.home}, ${com.huawei.example.ConfigExample.intField}
com.huawei.example.ConfigExample.intField=123456
```

Take `${appName:test}` for example ，`appName` is the index key for introspection and `test` is the default value. The retrieval priority for introspection is as follows：

- Startup parameters
- Current configuration file(`config.properties`)
- Environment variable
- System variable
- Default value(content at the right of `':'`)

The contents contained in the startup parameters can be found in [Introduction to Entrance Module](entrance.md).

#### Special Key/Value pairs

`LoadPropertiesStrategy` allows you to use the key/value pairs in the startup parameter first. Otherwise it will use the config from the configuration file if the startup parameter does not exist.

Suppose there is a configuration class like this:
```java
@ConfigTypeKey("env")
public class ConfigExample implements BaseConfig {
  private String tag;
  private String secret;
  // getter and setter

```

The `tag` and `secret` properties take precedence over the `env.tag` and `env.secret` values in the `bootstrap.config`.

### Detailed Description for Yaml Strategy

The `LoadYamlStrategy` is used to load configuration files in the *YAML* format, which is currently mainly used to load plugin setup configuration `plugins.yaml` and plugin configuration `config.yaml`. Since plugin setup configuration is relatively simple, we will only cover the **Plugin Configuration Class**.

The **Plugin Configuration Class** is a **Java Pojo** just like the **Unified Configuration Class**, except that the latter implements the `BaseConfig` interface, while the former implements the `PluginConfig` interface. Refer to [Plugin Code Development Guide](../dev-guide/dev_plugin_code.md#Plugin-Configuration) for more information. We will take the `PluginConfig` interface as an example.

Suppose there is a following **Plugin Configuration Class** :
```java
package com.huawei.example;
public class ConfigExample implements PluginConfig {
  private String string;
  private int intField;
  // getter and setter
}
```

The corresponding configuration might look like this:

```yaml
com.huawei.example.ConfigExample:
  string: value
  intField: 123456
```

#### Property Type

The property types that `LoadYamlStrategy`supports includes：

- Primitive and wrapper types for Boolean and numeric classes
- String
- Enum
- Complex Object
- Array that consist of the above types
- *List* that consist of the first four types
- *Map* that consist of the first four types

#### Alias

`LoadYamlStrategy` supports setting alias for full qualified class name and property names using the `ConfigTypeKey` annotation and the `ConfigFieldKey` annotation. Assume the above `ConfigExample` class is modified as follows：

```java
@ConfigTypeKey("config.example")
public class ConfigExample implements PluginConfig {
  @ConfigFieldKey("stringField")
  private String string;
  private int intField;
  // getter and setter
}
```

Then the configuration file should be like this：：
```yaml
config.example:
  stringField: value
  intField: 123456
```

Note that fixing property names by `ConfigFieldKey` is not supported for complex objects involved in arrays, lists, and maps. In other words, `ConfigFieldKey` only applies to properties of the **Plugin Configuration Class** and its complex object type properties.

#### Value of Introspection

The property values in `LoadYamlStrategy'`support introspection. `${}` can be used to map the current configuration, system variables, startup parameters and other elements. For example, the `ConfigExample` configuration can be set to:

```yaml
com.huawei.example.ConfigExample:
  string: value, ${appName:test}, ${user.home}, ${intField}
  intField: 123456
```

Take `${appName:test}` for example ，`appName` is the index key for introspection and `test` is the default value. The retrieval priority for introspection is as follows:

- Startup parameters
- Current configuration file
- Environment variable
- System variable
- Default value(content at the right of `':'`)

Note that `LoadPropertiesStrategy` can map to the entire configuration file, and `LoadYamlStrategy` can only map to the current `Map` or **complex object** due to the configuration format.

while mapping the **public properties** of the current collection (**complex objects**) and the **public properties** are aliasing with `ConfigFieldKey`, the correct mapping will depend on the order in which the properties are defined, like **Plugin Configuration Classes** below：
```java
@ConfigTypeKey("config.example")
public class ConfigExample implements PluginConfig {
  private String field1;
  @ConfigFieldKey("stringField")
  private String field2;
  private String field3;
  // getter and setter
}
```

If `field1` and `field3` need to use `field2`, the corresponding configuration would look like this:
```yaml
config.example:
  field1: value1, ${stringField}
  stringField: value2
  field3: value3, ${field2}
```

Based on the above, it is recommended that developers do not set aliases for **public properties**. If you really need an alias, it is recommended to place these **public properties** at the beginning or end of the **Plugin Configuration Class**. 

The contents contained in the startup parameters can be found in [Introduction to Entrance Module](entrance.md#Startup-Parameters).

#### Special Key/Value pairs

`LoadYamlStrategy` does not support assigning properties to the **Plugin Configuration Class** using key/value pairs in the startup parameter.

### Plugin Setup Configuration

**Plugin Setup Configuration** is `plugins.yaml`. There are three such files in [**sermant-agentcore-core**](../../sermant-agentcore/sermant-agentcore-core)：

- [plugins.yaml](../../sermant-agentcore/sermant-agentcore-config/config/plugins.yaml): **Plugin Setup Configuration** for the default build scenario, without the example project.
- [all/plugins.yaml](../../sermant-agentcore/sermant-agentcore-config/config/all/plugins.yaml): **Plugin Setup Configuration ** when executing the -Pall to package, with extra example project than `agent`.
- [example/plugins.yaml](../../sermant-agentcore/sermant-agentcore-config/config/example/plugins.yaml): **Plugin Setup Configuration** when executing the -Pexample to package, including only the example project.

In `plugins.yaml`, the plugins is configured to be loaded when **Sermant** starts, like this:

```yaml
plugins:
  - plugin1
  - plugin2
  - plugin3
```

These configured plugins correspond to the contents of the `pluginPackage` directory.

In addition, developers can use `profile` to define the plugins that need to be loaded for different scenarios. Such as:

```yaml
profiles:
  scene1:
    - plugin1
    - plugin2
  scene2:
    - plugin3
    - plugin4
  scene3:
    - plugin5
    - plugin6
profile: scene1, scene2
```

In `Profiles` you can customize the plugin loading configuration for different scenarios, and in' profiles' you can configure which scenarios will take effect after the application launches.

## Core Service System

The **Core Service System** code for **Sermant** could be found in [service](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service).

`service`includes：

- [BaseService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/BaseService.java): [**Core Service Class**](#Core-Service-Class)
- [ServiceManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/ServiceManager.java): [**Core Service Management Class**](#Core-Service-Management-Class)
- Core service implementation directory

### Core Service Management Class

In the **Core Service Management Class** `ServiceManager`, developers can obtain the **core service class** instance via the `getService` method:

```java
ServiceExample service = ServiceManager.getService(ServiceExample.class);
```

### Core Service Class

The **Core Service System** is a system that loads and manages the services that implement the [BaseService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/BaseService.java) **Core Service Class**. Its core is the implementation of **Core Service Class**.：

Suppose there is a service called example and write an interface for this service:

```java
public interface ServiceExample extends BaseService {
  void foo();
}
```

This defines the `Example` service interface with the `foo` method, which might be implemented as follows:
```java
public class ServiceExampleImpl implements ServiceExample {
  @Override
  public void start() {
    // initialize
  }

  @Override
  public void stop() {
    // shut down
  }

  @Override
  public void foo() {
    // do something
  }
}
```

Next, add `ServiceExampleImpl` to the *SPI* configuration of `BaseService`:

- Add `META-INF/services` folder under `resources`.
- add configuration file `com.huaweicloud.sermant.core.service.BaseService` under `META-INF/services`.
- In the above file, type all the **Core Service Classes**(`ServiceExampleImpl`) in the plugin package and split them with **LF**.

In this way you can fetch the instances of **Core Service Classes** via the `getService` method of the `ServiceManager`.

## Plugin Management System

The **Plugin Management system** code for **Sermant** can be found in [plugin](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin).

`plugin` includes:

- [classloader/PluginClassLoader](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/classloader/PluginClassLoader.java), describes in [Plugin Classloader](#Plugin-Classloader).
- [config](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/config), contains the code related to [Plugin Configuration System](#Plugin-Configuration-System).
- [service](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/service), contains the code related to [Plugin Service System](#Plugin-Service-System).
- [PluginManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/PluginManager.java)，describes in [Plugin Management Class](#Plugin-Management-Class).

### Plugin Management Class

In the **Plugin Management Class**, we mainly traverse the plugin directories configured in the plugin configuration file `plugins.yaml`. For each plugin directory:

- Load all its **plugin packages** into the system class loader `AppClassLoader`.
- Custom [Plugin Classloader](#Plugin-Classloader) loads all plugin packages.
- Load all related [Plugin Configuration](#Plugin-Configuration-System).
- Load all related [Plugin Service](#Plugin-Service-System).

### Plugin Classloader

**Plugin Classloader** is exactly `PluginClassLoader`. `PluginClassLoader` is a special `URLClassLoader`. It will hold all the *URL* of plugin service packages for a single feature. `PluginClassLoader` breaks the parent delegation mechanism. When loading a *Class*, it preferentially uses its own *Class*, and then calls the native loading method of its parent class if it can't find：

- Try to get the *Class* that it has already loaded.
- Try to load the *Class* that itself holds the *URL* and cache it.
- When it can't get the *Class* from itself, it will call the native loading method of the parent class.

### Plugin Configuration System

**Plugin Configuration System** is a special case of [**Unified Configuration System**](#Unified-Configuration-System). It is mainly used to read the plugin configuration file `config.yaml`, so it follows the rules of the [YAML format loading strategy](#Detailed-Description-for-Yaml-Strategy).

For more information on the **Plugin Configuration System**, refer to the [Plugin Code Development Guide](../dev-guide/dev_plugin_code.md#Plugin-Configuration).

### Plugin Service System

**Plugin Service System** is a special case of [**Core Service System**](#Core-Service-System), which is mainly used to load the PluginService `PluginService`. So it follows the rules of **Core Service System**.

For more information on the **Plugin Service System**, refer to the [Plugin Code Development Guide](../dev-guide/dev_plugin_code.md#Plugin-Service).

## Related Documents

|Documents|
|:-|
|[Introduction to Entrance Module](entrance.md)|
|[Introduction to Backend Module](backend.md)|

[Back to README of **Sermant** ](../README.md).

