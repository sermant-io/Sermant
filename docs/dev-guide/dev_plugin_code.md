# Plugin Code Development Guide

[简体中文](dev_plugin_code-zh.md) | [English](dev_plugin_code.md) 

This document focuses on **Sermant**'s [example module](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template), which describes some common scenarios during plug-in development.

- [Components](#Components)
- [Plugin Module](#Plugin-Module)
  - [Enhancement Definition](#Enhancement-Definition)
    - [Enhancement for Native Class](#Enhancement-for-Native-Class)
  - [Interceptor](#Interceptor)
  - [Plugin Configuration](#Plugin-Configuration)
  - [Plugin Service](#Plugin-Service)
    - [Simple Plugin Service](#Simple-Plugin-Service)
    - [Complex Plugin Service](#Complex-Plugin-Service)
    - [Log](#Log)
    - [Heartbeat](#Heartbeat)
    - [Trace Tracking](#Trace-Tracking)
    - [Dynamic Configuration](#Dynamic-Configuration)
- [Plugin Service Module](#Plugin-Service-Module)

## Components

According to the [Plugin Module Development Guide](dev_plugin_module.md), a `main` plugin module contains the following five things:

- The `plugin` module, which is mainly used to declare enhancements to the host application.
- The `service` module, which provides the service implementation for the plugin package.
- The `backend` module (server), which receives the data from plugin.
- The `frontend` module (webapp), which is used to display the server-side data
- The `other` module (special add-on), which is usually used for debugging.

Considering that the latter three have great changes with the actual business scenarios, they are given a high degree of development freedom, and they are only limited by the module directory and the output directory. For this reason, the `example module` will not be a reference case for them. The `example module` contains the following modules:

- [demo-plugin](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin): the example for plugin
- [demo-service](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-service): the example for plugin service
- [demo-application](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/demo-application): the example for host application

## Plugin Module

The example plugin module `demo-plugin` is mainly used to show plugin developers some scenarios that may be encountered in the development process of plugins and some functions that may be used.

Before we start, it's important to agree that in `plugin` modules, developers can only use the *Java* native API and the [**Sermant** core module](../../sermant-agentcore/sermant-agentcore-core) and cannot rely on or use any third-party dependencies other than `byte-buddy` and `slf4j`. If the business needs to use other third-party dependencies, you can only define the interface in the `plugin` module and write the implementation in the `service` module. Refer to the [Plugin Module Development Guide](dev_plugin_module.md#Add-Plugin-Module) for more information.

### Enhancement Definition

The core capability of **Sermant** is to make non-intrusive bytecode enhancements to the host application, and these enhancement rules are base on plugins. In the `main` module of each **Sermant** plugin, enhancement definitions can be defined to enhance the bytecode of specific methods of the host application to achieve certain functionality. So how the `main` module tells the **Sermant** which classes to augment is an important topic.

The Enhancement definition of plugins requires the implementation of [PluginDeclarer](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/declarer/PluginDeclarer.java) interface, which contains two methods:

- `getClassMatcher` is used to get the matcher [ClassMatcher](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/matcher/ClassMatcher.java) of the enhanced class.
- `getInterceptDeclarers` is used to obtain the method of interceptor point of the enhanced class, as well as the interceptors embedded in it, encapsulated in the method interceptor point [InterceptDeclarer](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/declarer/InterceptDeclarer.java).
- `getSuperTpeDecarers` gets the plugin's superclass declaration [SuperTypeDeclarer](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/declarer/SuperTypeDeclarer.java).

The matcher [ClassMatcher](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/matcher/ClassMatcher.java), which provides two types of matchers in the core module:

[ClassTypeMatcher](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/matcher/ClassTypeMatcher.java)(the matcher for class name)

- The most common way to do this is purely by name matching, which is obtained by:
  ```java
  ClassMatcher.nameEquals("${class reference}");
  ```
  Where `${class reference}` is the full qualified class name of the enhanced class.


- Matching multiple classes by name, which is the plural version of `nameEquals`, can be obtained with the following method:
  ```java
  ClassMatcher.nameContains("${class reference array}");
  ```
  Where `${class reference array}` is a full qualified class name mutable array of the enhanced class.

[ClassFuzzyMatcher](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/matcher/ClassFuzzyMatcher.java)（the fuzzy matcher of class name）

- The enhanced class is located by the prefix of full qualified class name, which can be obtained by:
  ```java
  ClassMatcher.namePrefixedWith("${prefix}");
  ```
  Where `${prefix}` is the prefix of full qualified class name.


- The enhanced class is located by the suffix of full qualified class name, which can be retrieved as follows:
  ```java
  ClassMatcher.nameSuffixedWith("${suffix}")
  ```
  Where `${suffix}` is the suffix of full qualified class name.


- The enhanced class is located by the infix of full qualified class name, which can be obtained as follows:
  ```java
  ClassMatcher.nameinfixedWith("${infix}")
  ```
  Where `${infix}` is the infix of full qualified class name.


- The enhanced class is located by matching the full qualified class name with a regular expression, which can be obtained as follows:
  ```java
  ClassMatcher.nameMatches("${pattern}")
  ```
  Where `${pattern}` is a regular expression.


- The enhanced class is located by annotation decorated, which can be obtained as follows:

  ```java
  ClassMatcher.isAnnotationWith("${annotation reference array}");
  ```
  Where `${annotation reference array}` is a full qualified class name array of annotations.


- Locating a subclass by means of a superclass can be obtained using the following methods:
  ```java
  ClassMatcher.isExtendedFrom("${super class array}");
  ```
  Where `${super class array}` is a superclass mutable array. Due to Java inheritance rules, the array can only have one `class`, the rest must be `interface`.


- A matching logical operation, which is true when all matchers do not match:
  ```java
  ClassMatcher.not("${class matcher array}")
  ```
  Where `${class matcher array}` is a variable-length array of matcher.


- A matching logical operation, which is true if the matcher matches all of them:
  ```java
  ClassMatcher.and("${class matcher array}")
  ```
  Where `${class matcher array}` is a variable-length array of matcher.


- A matching logical operation, which is true if one of the matcher matches:
  ```java
  ClassMatcher.or("${class matcher array}")
  ```
  Where `${class matcher array}` is a variable-length array of matcher.

For method interception points [MethodMatcher](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/matcher/MethodMatcher.java), we provide a variety of matching methods:

- Match any method:
  ```java
  MethodMatcher.any();
  ```
- Match by method name:
  ```java
  MethodMatcher.nameEquals("${method name}");
  ```
  Where `${method name}` is the method name.


- Match static method:
  ```java
  MethodMatcher.isStaticMethod();
  ```
- Match constructor:
  ```java
  MethodMatcher.isConstructor();
  ```
- Match multiple methods:
  ```java
  MethodMatcher.nameContains("${method name array}");
  ```
  Where `${method name array}` is an array of method names.


- Match by prefix of method name:
  ```java
  MethodMatcher.namePrefixedWith("${method name prefix}");
  ```
  Where `${method name prefix}` is the prefix of method name .


- Match by suffix of method name:
  ```java
  MethodMatcher.nameSuffixedWith("${method name suffix}");
  ```
  Where `${method name suffix}` is the suffix of method name.


- Match by infix of method name：
  ```java
  MethodMatcher.nameinfixedWith("${method name infix}");
  ```
  Where `${method name infix}` is the infix of method name.


- Match by regular expression:
  ```java
  MethodMatcher.nameMatches("${pattern}");
  ```
  Where `${pattern}` is a regular expression.


- Matches by annotation:
  ```java
  MethodMatcher.isAnnotatedWith("${annotations array}");
  ```
  Where `${annotations array}` is the annotation set.


- Match by specified number of arguments:
  ```java
  MethodMatcher.paramCountEquals("${param count}");
  ```
  Where `${param count}` is the number of input parameters.


- Match by the specific type of argument:
  ```java
  MethodMatcher.paramTypeEquals("${param type array}");
  ```
  Where `${param type array}` is the set of input types.


- Match by return type
  ```java
  MethodMatcher.resultTypeEquals("${result type}");
  ```
  Where `${result type}` is the return type.


- Logical operation, where the result is true if the method matcher set does not match at all.
  ```java
  MethodMatcher.not("${element matcher array}");
  ```
  Where `${element matcher array}` is the set of method matcher.


- Logical operation, where the result is true if the method matcher set fully matches.
  ```java
  MethodMatcher.and("${element matcher array}");
  ```
  Where `${element matcher array}` is the set of method matcher.


- Logical operation, where the result is true if method matcher set matches one of the result.
  ```java
  MethodMatcher.or("${element matcher array}");
  ```
  Where `${element matcher array}` is the set of method matcher.

For more methods matching method, refer to [byte-buddy](https://javadoc.io/doc/net.bytebuddy/byte-buddy/latest/net/bytebuddy/matcher/ElementMatchers.html) methods with a generic type of `MethodDescription` .

Don't forget to add the *SPI* configuration file for the `PluginDeclarer` interface:

- Add `META-INF/services` folder under `resources`.
- Add `com.huaweicloud.sermant.core.plugin.agent.declarer.PluginDeclarer` under `META-INF/services` .
- In the above file, type all the enhancements definitions of `PluginDeclarer` in the plugin package separated with LF.

The `example module` of **Sermant** contains the following example implementation of the `PluginDeclarer` interface:

- [DemoAnnotationDeclarer](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/declarer/DemoAnnotationDeclarer.java): Locate enhanced class by annotation in the decorated class.
- [DemoNameDeclarer](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/declarer/DemoNameDeclarer.java): Locate enhanced class by name.
- [DemoSuperTypeDeclarer](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/declarer/DemoSuperTypeDeclarer.java): Locate enhanced class by superclass.
- [DemoBootstrapDeclarer](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/declarer/DemoBootstrapDeclarer.java): Enhancement definition for the boot classloader, refer to [Enhancement for Native Class](#Enhancement-for-Native-Class) for details.
- [DemoTraceDeclarer](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/declarer/DemoTraceDeclarer.java): Enhancement definition for the usage of trace tracking, refer to [Trace Tracking](#Trace-Tracking) for details.

When the plugin developers write the plugin enhancement definition, you can use the above examples as a reference to develop the enhancement definition that meets their own needs.

#### Enhancement for Native Class

For *Java* native classes such as `java.lang.Thread`, which are loaded by the `BootStrapClassLoader`, there are two main difficulties in enhancing them:

- Native classes are loaded by the BootStrapClassLoader, so if we want to enhance them, we need to overwrite the enhanced bytecode back into the BootStrapClassLoader. Considering that the enhanced embedded code is mostly written in **interceptors**, this content is mainly loaded by the system class loader `AppClassLoader`, which is not accessible by BootStrapClassLoader. Since [**Advice template class**](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/template) invokes the interceptors by reflection, it is allowed to write interceptors without restrictions for enhancement of native classes.

- Due to *Java* redefining *Class* restrictions, we can't modify the metadata of these native classes, so we can't use byte-buddy delegation to enhance them (by adding delegation properties and static code blocks). Fortunately, we can use [**Advice template class**](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/template) with [**byte-buddy advice**](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/transformer/BootstrapTransformer.java) technology to enhance. That's what **core module** is doing. 

Combined with the above, there is no difference between native classes and regular classes in terms of enhancement definition and writing interceptors. However, it is desirable for plugin developers to minimize enhancement to native classes for three reasons:

- Enhancements to native classes tend to be divergent. And enhancements to them are likely to impact other plugins or host functionality.
- The native class enhancement logic will use reflection to invoke interceptor methods in the system classloader. Due to the *Java* redefinition of the *Class*, every time an enhanced method is called, the reflection logic is processed, which significantly limits the *TPS* of the method.
- The enhancements to the native classes involved using the **Advice template class** to generate a dynamic interceptor class. For each enhanced native class method, one will be generated dynamically and they will be loaded by the system classloader. If native classes are enhanced without restriction, loading dynamic classes can also become a significant burden during startup.

In summary, [**Sermant** core module](../../sermant-agentcore/sermant-agentcore-core) provides the ability to augment *Java* native classes. However, it is not recommended to enhance them without restrictions. If there are multiple enhancement points to choose from, you'd better choose enhancing regular classes.

In **Sermant** `example module`, [DemoBootstrapDeclarer](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/declarer/DemoBootstrapDeclarer.java) enhances `java.lang.Thread`. You can launch the example application [DemoApplication](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/demo-application/src/main/java/com/huawei/example/demo/DemoApplication.java) to see if `java.lang.Thread` is enhanced properly.

### Interceptor

In the development of the new version of the plugin, the distinction between static methods, constructors and instance methods is no longer made at the interceptor level, which reduces the complexity of plugin development.
For `MethodInterceptPoint`, there are three acquisition types: static method, constructor, and instance method. There are also three types of interceptors:

- [interceptor](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/interceptor/Interceptor.java): the interceptor interface, which contains three methods:
  - `before`, the preceding method, which is executed before the interception point. The ExecuteContext parameter is the context of the plugin execution, which encapsulates all the parameters required for the interceptor to operate. Through the skip method, the main process can be skipped and the final method result can be set. Note that the main process cannot be skipped when the constructor is enhanced .
  - `after`, the post-method, ends up in the post-method whether or not the intercepted method executes normally. Postmethods can override the return value of the intercepted method with their return value, so developers need to be careful not to return null easily here.
  - `onThrow`, an exception handling method that is triggered when the intercepted method executes an exception. Handling the exception here does not affect the normal throwing of the exception.

The `example module` of **Sermant** contains the following example interceptor:

- [DemoStaticInterceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoStaticInterceptor.java): an ordinary static method interceptor
- [DemoConstInterceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoConstInterceptor.java): an ordinary constructor interceptor
- [DemoMemberInterceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoMemberInterceptor.java): an ordinary instance method interceptor
- [DemoConfigInterceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoConfigInterceptor.java): an example interceptor for the plugin configuration acquisition, as described in the [Plugin Configuration](#Plugin-Configuration) section.
- [DemoServiceInterceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoServiceInterceptor.java): an example interceptor for the usage of plugin service, as described in the [Plugin Service](#Plugin-Service) section.

When writing custom interceptors, plugin developers can use the above example as a reference to develop interceptors that meet their own functional needs.

### Plugin Configuration

**Plugin Configuration** refers to the configuration system used in plugin packages and plugin service packages. It consists of three main parts:

- [PluginConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/config/PluginConfig.java) plugin configuration interface: All normal plugin configurations must implement this interface.

- [PluginConfigManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/config/PluginConfigManager.java) plugin configuration manager: provides a method to get the plugin configuration `PluginConfig` :
  
  ```java
  // ${plugin config class} is the plugin configuration class
  PluginConfigManager.getPluginConfig(${plugin config class});
  ```
  `PluginConfigManager` is the unified configuration manager [ConfigManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/config/ConfigManager.java), The plugin side can directly use the interface of the latter to obtain plugin configuration and unified configuration：
  
  ```java
  // ${base config class}is the plugin configuration class or unified configuration class
  ConfigManager.getConfig(${base config class});
  ```

From the **Sermant** `example module` plugin configuration file [config.yaml](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/config/config.yaml) can be seen that the configuration file is a *yaml file*. The `plugin` and `service` configurations of a `main` plugin module are encapsulated in a single `config.yaml`.

Instead of a traditional *YAML* format configuration file for a single *Java Pojo* object, here `config.yaml` can encapsulate multiple *Java Pojos*, which are distinguished by fully qualified names or aliases, forming a *Map-like* structure. The key `demo.test` corresponds to the [DemoConfig](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/config/DemoConfig.java) object in the **example plugin package** and `com.huawei.example.demo.config.DemoServiceConfig` key corresponds to [DemoServiceConfig](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-service/src/main/java/com/huawei/example/demo/config/DemoServiceConfig.java) object in the **example plugin service pack**.

Compared to `DemoServiceConfig`, [DemoConfig](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/config/DemoConfig.java) is decorated with the [ConfigTypeKey](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/config/common/ConfigTypeKey.java) annotation, so the `demo.test` alias is set. If it is not annotated with the `ConfigTypeKey` annotation, the full qualified class name is used as the index.

The `map` property in [DemoConfig](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/config/DemoConfig.java) is annotated with [ConfigFieldKey](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/config/common/ConfigFieldKey.java), changing its property name to `str2DemoSimplePojoMap`. However, it is important to note that the semantics of the **Java Pojo** using this annotation is invalid if it is wrapped by an array, a *List* or a *Map*. Therefore, for now, this annotation can only be used to modify the current **plugin configuration class** or directly used **Java Pojo**.

As [DemoConfig](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/config/DemoConfig.java)[DemoConfig](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/config/DemoConfig.java) shows, plugin configuration now supports data types include:

- Primitive and wrapper types for Boolean and numeric classes
- String
- Enum
- Complex object
- Array of the above types
- List of the first four types
- Map of the first four types

*YAML* format configuration files currently have a few other rules:

- For complex objects involved in arrays, lists, and maps, using `ConfigFieldKey` to fix property names is not supported.
- For strings in arrays, lists, and maps, there is no support for `${}` conversions, **plugin configuration class** string properties and string properties inside complex type properties are supported.
- Parameters are only used for string `${}` conversions. Direct setting property values using parameters is not supported.
- The field names of configuration classes are usually small camels.You can use [ConfigFieldKey](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/config/common/ConfigFieldKey.java) to define aliases for the transverse-line style. After the annotation is added, it can be parsed in *YAML* using either transverse-line or small camel style. Refer to the `intField` of [DemoConfig](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/config/DemoConfig.java).
- The priority of the configuration is: startup parameters > environment variables > system variables (-d parameter) > *YAML* file configuration
- The camel style and transverse line can be used to split words to look for matches when the configuration class properties fetch reference values according to the priority(startup parameters, environment variables, and system variables (-d parameter)) in effect. For example, for `intField` of [DemoConfig](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/config/DemoConfig.java), the key will be transformed into one of the following forms and searched in order:
  - demo.test.intField
  - demo_test_intField
  - demo-test-intField
  - DEMO.TEST.INTFIELD
  - DEMO_TEST_INTFIELD
  - DEMO-TEST-INTFIELD
  - demo.test.intfield
  - demo_test_intfield
  - demo-test-intfield
  - demo.test.int.field
  - demo_test_int_field
  - demo-test-int-field
  - DEMO.TEST.INT.FIELD
  - DEMO_TEST_INT_FIELD
  - DEMO-TEST-INT-FIELD
- If the key of the basic data type/array /map/list/set(which does not support complex objects) is not defined in the *YAML* configuration, the reference value will be obtained according to the priority of the configuration effect: startup parameters > environment variables > system variables (-d parameter). When retrieving reference values from the above data sources, note that:
  - The array /list/set should be configured in `YAML` string format. For example: DEMO_TEST_LIST_NAME=[elem1,elem2]
  - The map needs to be configured as a `YAML` string format. For example: DEMO_TEST_MAP_NAME={key1: value1, key2: value2}

Most of the possible configuration scenarios are covered in [DemoConfig](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/config/DemoConfig.java), plugin developers can reference and write plugin configuration classes that meet your business needs.

Finally, don't forget to add the *SPI* configuration file for the plugin configuration:

- Add `META-INF/services` folder under `resources`.
- Add `com.huaweicloud.sermant.core.plugin.config.PluginConfig` configuration file under `META-INF/services`.
- In the above file, type the `PluginConfig` implementation for all plugin configurations in the plugin package and separate them by LF.

### Plugin Service

**Plugin service** refers to the service system used in plugin package and plugin service package. It mainly consists of two parts:

- [PluginService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/service/PluginService.java), the plugin service interface.
- [PluginServiceManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/service/PluginServiceManager.java), the plugin service manager, which provides an interface to get `PluginService` :
  
  ```java
  // ${plugin service class} is the plugin service class
  PluginServiceManager.getPluginService(${plugin service class});
  ```
  `PluginServiceManager` is a particular case of [ServiceManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/ServiceManager.java). The latter interface can be used directly to access the core and plugin services:
  
  ```java
  // ${base service class}is the core service class or plugin service class
  ServiceManager.getService(${base service class});
  ```

  Learned form [Plugin Module Development Guide](dev_plugin_module.md#Add-Plugin-Module), plugins can be categorized as **simple plugins** and **complex plugins**, depending on the complexity of the service they define:
  
  - Services defined in **simple plugins** can only use *Java* native *APIs*, self-developed *APIs* (start with `com.huawei`) in [**Sermant** core module](../../sermant-agentcore/sermant-agentcore-core) , and *APIs* of `byte-buddy` and `slf4j`.
  - In addition to the above *API*, the services in **complex plugins** have the right to use other *APIs* that third parties rely on. These services need to be separated into the **plugin service interface** and the **plugin service implementation**: the former is written in the `plugin` module for the interceptor to invoke. The latter is written in the `service` module and loaded by a custom *ClassLoader* to achieve classloader-level dependency isolation.

#### Simple Plugin Service

As for **simple plugin service**, you can just implement [PluginService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/service/PluginService.java), such as [DemoSimpleService](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/service/DemoSimpleService.java). Based on the implementation of the `start` and `stop` methods, you can add other required methods such as the `activeFunc` method. Get an instance of the `DemoSimpleService` and invoke the `activeFunc` method with the following code:
```java
DemoSimpleService simpleService = PluginServiceManager.getPluginService(DemoSimpleService.class);
simpleService.activeFunc();
```

For **simple plugin service**, the only restriction is to use only the *Java* native *API*, [**Sermant** core module](../../sermant-agentcore/sermant-agentcore-core) self-developed *APIs* (starting with `com.huawei`) and *APIs* in `byte-buddy` and `slf4j`.

Plugin developers can refer to [DemoSimpleService](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/service/DemoSimpleService.java) to write **simple plugin services** for your business on demand.

Finally, don't forget to add the *SPI* configuration file for [PluginService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/service/PluginService.java):

- Add `META-INF/services` folder under `resources`.
- Add `com.huaweicloud.sermant.core.plugin.service.PluginService` configuration file under `META-INF/services`.
- In the above file, type the `PluginService` implementation for all plugin services in the plugin package and separate them by LF.

In particular, do not try to get instances of other **plugin services** in the `start` method of `PluginService`, since **plugin services** are still being initialized and it may not be possible to get these **plugin services** correctly.

#### Complex Plugin Service

There are only two differences between **complex plugin service** and **simple plugin service**:

- **Complex plugin services** write their interface in the `plugin` module and their implementation in the [Plugin Service Module](#Plugin-Service-Module), while **simple plugin services** do not need to write the interface and are implemented directly in the `plugin` module.
- The implementation of a **complex plugin service** can use any third-party dependency on demand.

[DemoComplexService](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/service/DemoComplexService.java) is a **complex plugin service** sample interface. Methods can be added on demand, such as the `activeFunc` method. [DemoComplexServiceImpl](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-service/src/main/java/com/huawei/example/demo/service/DemoComplexServiceImpl.java) is the corresponding implementation. We can invoke the `activeFunc` method with the following code:

```java
DemoComplexService complexService = PluginServiceManager.getPluginService(DemoComplexService.class);
complexService.activeFunc();
```

Adding *SPI* configuration and other considerations is no different than **simple plugin service**. Developers can refer the `DemoComplexService` interface and `DemoComplexServiceImpl` implementation to write **complex plugin services** that meet business requirements.

#### Log

Considering dependency isolation, [**Sermant** core module](../../sermant-agentcore/sermant-agentcore-core) provides the `plugin` and `service` to use only **jul** log. Get the **jul** log instance by:

```java

import com.huaweicloud.sermant.core.common.LoggerFactory;
Logger logger=LoggerFactory.getLogger();
```

Plugin developers who need to output log information can refer to [DemoLogger](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/common/DemoLogger.java) sample development.

#### Heartbeat

Heartbeat is one of the core services in [**Sermant** core module](../../sermant-agentcore/sermant-agentcore-core). An instance of heartbeatService is obtained by:
```java
HeartbeatService heartbeatService = ServiceManager.getService(HeartbeatService.class);
```

The heartbeat service starts execution when it is initialized, and periodically sends the name, version and other information of each plugin to the backend server. Currently, plugin heartbeats report information like:

- `hostname`: the hostname of the sending client
- `ip`: the ip of the sending client
- `app`: application name, as well as `appName` in startup parameters
- `appType`: application type，as well as `appType` in startup parameters
- `heartbeatVersion`: time of last heartbeat
- `lastHeartbeat`: time of last heartbeat
- `version`: the version of the core package, as well as the `sermant-version` value of the core package `manifest` file
- `pluginName`: plugin name
- `pluginVersion`：plugin version, which is the `Sermant-plugin-version` value of the `manifest` file in the plugin jar

If you want to add additional content to the data reported by the plugin, you can call the following API:
```java
// use ExtInfoProvider to add additional content
heartbeatService.setExtInfo(new ExtInfoProvider() {
  @Override
  public Map<String, String> getExtInfo() {
    // do something
  }
});
```

Plugin developers who need to add additional content to the packets sent by the heartbeat function can refer to [DemoHeartBeatService](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/service/DemoHeartBeatService.java) sample development.

For more information about heartbeat services, see [Heartbeat Service](service_heartbeat.md).

#### Trace Tracking

The **Trace Tracking** is an upper layer function established by message sending capability, which simply means embedding the following logic between the invoke chains of the host side:

- When sending data, the `TraceId` and `SpanId` required by the trace are inserted in the data packet. The former is the view of the whole trace in the distributed system, and the later represents the view inside the different services in the whole trace.
- When receiving data, it parses the trace-related content embedded in the data packet, forms a trace and submits it to the backend server, and gradually forms a invoke chain.

In the [DemoTraceService](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/demo-application/src/main/java/com/huawei/example/demo/service/DemoTraceService.java), the `counsumer` and `provider` methods mimic how the server receives data and handles sending it, while the packet is assumed to exist in a `ThreadLocal` until the next invocation to the `provider` method receives the data.

Based on the above example host application, we write enhancement definition [DemoTraceDeclarer](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/declarer/DemoTraceDeclarer.java) and enhance `provider` and `consumer` of `DemoTraceService` by [DemoTraceProviderInterceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoTraceProviderInterceptor.java) and [DemoTraceConsumerInterceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoTraceConsumerInterceptor.java) respectively

- For the sending `provider` method, the following enhancements are made：
  ```java
    private final TracingService tracingService = ServiceManager.getService(TracingService.class);
  
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        TracingRequest request =
            new TracingRequest(context.getRawCls().getName(), context.getMethod().getName());
        ExtractService<HashMap<String, String>> extractService = (tracingRequest, carrier) -> {
            tracingRequest.setTraceId(carrier.get(TracingHeader.TRACE_ID.getValue()));
            tracingRequest.setParentSpanId(carrier.get(TracingHeader.PARENT_SPAN_ID.getValue()));
            tracingRequest.setSpanIdPrefix(carrier.get(TracingHeader.SPAN_ID_PREFIX.getValue()));
        };
        tracingService.onProviderSpanStart(request, extractService, (HashMap<String, String>)context.getArguments()[0]);
        return context;
    }
  
    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        tracingService.onSpanFinally();
        return context;
    }
  
    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        tracingService.onSpanError(context.getThrowable());
        return context;
    }
  ```
  
- For the `consumer` method, the following enhancements are made：
  ```java
    TracingService tracingService = ServiceManager.getService(TracingService.class);
  
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }
  
    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        TracingRequest request =
            new TracingRequest(context.getRawCls().getName(), context.getMethod().getName());
        InjectService<HashMap<String, String>> injectService = (spanEvent, carrier) -> {
            carrier.put(TracingHeader.TRACE_ID.getValue(), spanEvent.getTraceId());
            carrier.put(TracingHeader.PARENT_SPAN_ID.getValue(), spanEvent.getSpanId());
            carrier.put(TracingHeader.SPAN_ID_PREFIX.getValue(), spanEvent.getNextSpanIdPrefix());
        };
        tracingService.onConsumerSpanStart(request, injectService, (HashMap<String, String>)context.getResult());
        tracingService.onSpanFinally();
        return context;
    }
  
    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        tracingService.onSpanError(context.getThrowable());
        return context;
    }
  ```
  If the plugin developers need to use the trace tracking function, refer to [DemoTraceNormalInterceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoTraceNormalInterceptor.java) for further development.


### Dynamic Configuration

Dynamic configuration is one of the core services in [**Sermant** core module](../../sermant-agentcore/sermant-agentcore-core). An instance is obtained by:
```java
DynamicConfigService service = ServiceManager.getService(DynamicConfigService.class);
```

Invoke the following method to register a listener:
```java
// ${group} is user group，${key} is the key listened. For zookeeper, the path is: / + ${group} + ${key}
// if ${group} do not exist，it will set the value by dynamicconfig.default_group in unified configuration
service.addConfigListener("${key}", "${group}", new DynamicConfigListener() {
  @Override
  public void process(ConfigChangedEvent event) {
    // do something
  }
});
```

Once the listener is registered, the `process` method will be triggered when the server creates, deletes, modifies, or adds child nodes.

Plugin developers who need to use dynamic configuration can refer to [DemoDynaConfService](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/service/DemoDynaConfService.java) sample development.

For more information on dynamic configuration service, refer to [Dynamic Configuration Service](service_dynamicconfig.md).


## Plugin Service Module

Compared to **plugin module**, **plugin service module** :

- can only write a **plugin configuration** and **plugin service interface** implementation, and cannot write **enhancement definition ** **interceptor** and **plugins service interface**.
- allow the freedom to add third-party dependencies as needed. When packaging, you need to provide a way to export dependencies, you can use the `shade` plugin or `assembly` plugin to export the dependency *jar* package, or you can directly use the `dependency` plugin to export the dependency package.

Note: Due to the limitations of the `byte-buddy` and `slf4j` packages, it is still recommended to use the `shade` plugin package directly. The relevant rules are defined in the [main module of plugins](../../sermant-plugins). You can directly import the plugin. Refer to [Plugin Module Development Guide](dev_plugin_module.md#Add-Plugin-Service-Module) for details.

The **plugin service module** usually involves writing [plugin configuration](#Plugin-Configuration) and [plugin service](#Plugin-Service), where **plugin service** mainly refers to the implementation of [complex plugin service](#Complex-Plugin-Service). 

[Back to README of **Sermant**](../README.md)