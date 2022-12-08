# Plugin Module Development Guide

[简体中文](dev_plugin_module-zh.md) | [English](dev_plugin_module.md) 

This document focuses on **Sermant** 's [root module of plugins](../../sermant-plugins) and describes how to add a plugin and its services and add-ons to the `root module of plugins`.

- [Development Environment](#Development-Environment)
- [Components](#Components)
- [Packaging](#Packaging)
- [Add Main Module of Plugin](#Add-Main-Module-of-Plugin)
  - [Plugin Name and Version](#Plugin-Name-and Version)
- [Plugin Development Process](#Plugin Development Process)
  - [Before Development](#Before-Development)
  - [Add Plugin Module](#Add-Plugin-Module)
  - [Add Plugin Service Module](#Add-Plugin-Service-Module)
  - [Add Configuration](#Add-Configuration)
  - [Example Project](#Example-Project)
  - [Attentions](#Attentions)
  - [Packaging for Plugin](#Packaging-for-Plugin)
- [Add-on Development Process](#Add-on-Development-Process)

## Development Environment

- [HuaweiJDK 1.8](https://gitee.com/openeuler/bishengjdk-8) / [OpenJDK 1.8](https://github.com/openjdk/jdk) / [OracleJDK 1.8](https://www.oracle.com/java/technologies/downloads/)
- [Apache Maven 3](https://maven.apache.org/download.cgi)

## Components

The `root module of plugins` of **Sermant** is divided into sub-modules based on plugin functionality known as `main module of plugin`, which could contain the following types of sub-modules:

- The `plugin` module, which is mainly used to declare enhancements to the host application.
- The `service` module, which provides the service implementation for the plugin package.
- The `server` module, which receives the plugin data.
- The `frontend` module, which is used to display the server-side data.
- The `other` module, special add-on, usually used for debugging.

## Packaging

There six steps of packaging in current [Sermant](../../pom.xml), including `agent`, `test`, and `release`. among which the ones related to [sermant-plugins](../../sermant-plugins/pom.xml) are as follows:

- `agent`: Package all the stable version `plugin module` and `service module`,and export them to the `agent/pluginPackage/${feature name}` directory; package all the stable version add-ons, including `backend module(server)`, `frontend module(webApp)` and `other`. And`backend module (server)` and `frontend module (WebApp)` will be exported to the product's `server/${feature name}` directory. `Other` modules are generally add-ons for debugging purposes and have no packaging requirements.
- `test`: Package all `plugin module`, `service module` and add-ons in Sermant.
- `release` : Package the stable version modules in Sermant which should be published to maven central repository.

## Add Main Module of Plugin

- Add the `main` module to the [pom.xml of sermant-plugins](../../sermant-plugins/pom.xml). According to the contents of the `main` module, add this module in specific `profile` of `pom.xml` .
  - This module must be added to `profile` whose `id` is `test`.
- Add the following label to the module's `pom.xml`：
  ```xml
  <packaging>pom</packaging>
  ```
  ```xml
  <properties>
    <sermant.basedir>${pom.basedir}/../../..</sermant.basedir>
    <package.plugin.name>${plugin name}</package.plugin.name>
  </properties>
  ```
  - Add the new `main` module to [default plugin setup file](../../sermant-agentcore/sermant-agentcore-config/config/plugins.yaml) and [all plugin setup file](../../sermant-agentcore/sermant-agentcore-config/config/test/plugins.yaml) to complete registration.

The sub-module development process of the `main` module of plugin is described in the following chapter:

- Refer to [Plugin Development Process](#Plugin-Development-Process) for detailed information of `plugin` and `service` development process.
- Refer to [Add-on Development Process](#Add-on-Development-Process) for detailed information of `backend(server)` and `frontend(webapp)` development process.
- The `other` module is only involved in debugging and is not involved in development flow constraints.

### Plugin Name and Version

The name and version of the plugin are intrinsic properties of the `plugin` and `service`, so we encapsulate them in the `manifest` file as meta-information for the `jar` package:

- The **plugin name** is encapsulated in the `Sermant-Name-Version` parameter of the `manifest` file, set by the `package.plugin.name` parameter of the `pom` file, which defaults to the **plugin name** meaning.
- The **plugin version** is encapsulated in the `Sermant-Plugin-Version` parameter of the `manifest` file, which defaults to `project.version` and can be changed via the `package.plugin.version` parameter in the `pom` file.
```xml
<properties>
  <package.plugin.name>${plugin name}</package.plugin.name>
  <package.plugin.version>${plugin version}</package.plugin.version>
</properties>
```

During the loading of the plugin package, the name and version of the `plugin` and `service` modules are checked and an exception is thrown if the following conditions are not met:

- For all `plugin` modules, since third-party dependencies other than `byte-buddy` and `slf4j`are not allowed, full qualified class name in the `byte-buddy` and `slf4j` packages will be modified through the `shade` plugin. Therefore, when loading the `plugin` directory (the `plugin` directory inside the `main` module directory), an exception will be thrown if a third-party package without the **plugin name** and **plugin version** is checked out.
- For all `plugin` and `service` modules, an exception will be thrown if the **plugin name** does not match the name configured in the **plugin setup file**.
- For all `plugin` and `service` modules, an exception will be thrown if there are multiple different **plugin versions**.

For plugin developers, it is not recommended to change the default design without special needs. Just keep plugins free of third-party dependencies other than `byte-buddy` and `slf4j`. Keep the principle that plugins contains interfaces, and services contains implementations.

## Plugin Development Process

In this section, we will go through the process of developing plugins. The modules involved are `plugin` and `service`.

### Before Development

The position of `plugin` is to define the enhanced logic of the host application. Considering the problem of dependency conflicts, the enhanced bytecode cannot involve the use of third-party dependencies other than `byte-buddy` and `slf4j`, which needs to be discussed in two cases:

- For simple plugins, where the plugin service is written to use only the self-developed functionality of the core package, and does not involve complex functionality that depends on other third-party dependencies, then only the `plugin` module is needed. The `plugin` will be loaded by the system class loader `AppClassLoader`.
- For some complex plugins, if you need to rely on the complex functions of other third parties, you need to design the service interface in the `plugin` module, and write the `service` module to implement it. The `plugin` module is still loaded by the system class loader `AppClassLoader`, while the `service` module is preferentially loaded by the custom class loader [PluginClassLoader](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/classloader/PluginClassLoader.java) to achieve classloader-level dependency isolation.

As for the third party dependencies in the core module, the full qualified class name of code will be modified by [shade plugin](https://github.com/apache/maven-shade-plugin) as well as self-developed code. It can't be used in `plugin` modules unless you do the same thing when plugins are packaged.

Also, for the `byte-buddy` package, since the method matcher is not extracted in the **core module**, the plugin still needs to use them, so the `plugin` is required to fix the full qualified class name of the `byte-buddy` package.

As for the `slf4j` package, due to classloader isolation during the initialization of the **complex plugin service**, the third-party `slf4j` interface used the **core module** configuration but could not be linked to the **core module** implementation, so the full qualified class name of `slf4j` had to be fixed to fit.

That's why plugin and service packages have to fix the full qualified class name of `byte-buddy` and `slf4j`, and why plugin can use these two dependencies.

### Add Plugin Module

Combined with the steps introduced in the [Packaging](# Packaging), there are three `profiles` related to plugin development: `agent`, `test` and `release`. `agent` is used to publish release package in github. `release` is used to publish modules to maven central repository. `test` is used for developing code and testing. If you need to add a `plugin` submodule to the `main` module:

- Add the `module` to the following `profile` in the `pom.xml` file of the `main` module:
  ```xml
  <profiles>
    <profile>
      <id>agent</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <modules>
        <module>${plugin module name}</module>
      </modules>
    </profile>
    <profile>
      <id>test</id>
      <modules>
        <module>${plugin module name}</module>
      </modules>
    </profile>
    <profile>
      <id>release</id>
      <modules>
        <module>${plugin module name}</module>
      </modules>
    </profile>
  </profiles>
  ```
  
- Add the following parameters to the `plugin` submodule:
  ```xml
  <properties>
   <package.plugin.type>plugin</package.plugin.type>
  </properties>
  ```
  
- Add the core package dependency to the `plugin` submodule:
  ```xml
  <dependencies>
    <dependency>
      <groupId>com.huaweicloud.sermant</groupId>
      <artifactId>sermant-agentcore-core</artifactId>
      <scope>provided</scope>
    </dependency>
  </dependencies>
  ```
  Note that third party dependencies other than `byte-buddy` and `slf4j` cannot be added or used for `plugin`, unless their full qualified class name are corrected using the `shade` plugin (not recommended)!
  
- Add the `shade` plugin to the `plugin` submodule to fix the full qualified class name of `byte-buddy` and `slf4j`, for reasons detailed in [Before Development](# Before-Development) :
  ```xml
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
  ```

### Add Plugin Service Module

Similar to the `plugin` module, here's how to add a `service` module:

- Add the `module` to the following `profile` in the `pom.xml` file of the `main` module:
  ```xml
  <profiles>
    <profile>
      <id>agent</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <modules>
        <module>${plugin service module name}</module>
      </modules>
    </profile>
    <profile>
      <id>test</id>
      <modules>
        <module>${plugin service module name}</module>
      </modules>
    </profile>
    <profile>
      <id>release</id>
      <modules>
        <module>${plugin service module name}</module>
      </modules>
    </profile>
  </profiles>
  ```
  
- Add the following parameters to the `service` submodule:
  ```xml
  <properties>
    <package.plugin.type>service</package.plugin.type>
  </properties>
  ```
  
- Add the core package and related plugin dependency to the `service` submodule:
  ```xml
  <dependencies>
    <dependency>
      <groupId>com.huaweicloud.sermant</groupId>
      <artifactId>sermant-agentcore-core</artifactId>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>com.huaweicloud.sermant</groupId>
      <artifactId>${plugin module name}</artifactId>
      <version>${plugin module version}</version>
      <scope>provided</scope>
    </dependency>
  </dependencies>
  ```
  
- Add third-party dependencies to the `service` module

  - Add directly

    The `service` module allows you to add third-party dependencies on demand, directly in `pom.xml` via ` <scope>compile</scope>`. The third-party dependencies introduced by this method are loaded by the PluginClassloader of each plugin independently. There is a class isolation between the plugin and the host application or between the plugins 

  - Add public third-party dependencies indirectly by importing`sermant-common`

    Sermant provides a CommonClassLoader mechanism, and plugins can extract common dependencies into `sermant-common` on demand to share common dependencies between plugins.

    `service` module can import `sermant-common` via `<scope>provided</scope>`, where third-party dependencies are loaded by the CommonClassLoader and there is no class isolation between plugins.

    If the `service` module imports `sermant-common` and needs to use a different version of the third-party dependency from the one imported in `sermant-common`, it needs to import the specific dependency via`<scope>compile</scope>` in this module. Third-party dependencies imported by this approach are loaded by the PluginClassLoader of the current plugin, isolated from the CommonClassLoader.

- Add the `shade` plugin to the `service` submodule:
  
  ```xml
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
  ```
  It is not recommended to use other package plugins. Refer to [Before Development](# Before-Development) for details.

### Add Configuration

If plugin configuration `PluginConfig` is needed in the `plugin` and `service` module, you need to add the plugin configuration file:

- Add a `config` folder to the home directory of `main` module.
- Add a `config.yaml` file in the `config` folder. This is the configuration file of the plugin.
- Add the following configuration to any submodule of `main` module (usually the first submodule) to execute the resource copy:
  ```xml
  <properties>
    <config.skip.flag>false</config.skip.flag>
  </properties>
  ```
  If the submodule configured with the above parameters is not in the `main` module directory of the plugin, you also need to set the following parameters based on the relative path of the module to the `config` directory:
  ```xml
  <properties>
    <config.source.dir>../config</config.source.dir> <!-- relative path of config directory -->
  </properties>
  ```

### Example Project

The [Sermant-examples](https://github.com/huaweicloud/Sermant-examples) is a demo project. The `sermant-template` module is a sample module that covers most of the scenarios you might encounter when developing a plugin. This section explains the contents of this module to help you get started with developing plugin features as soon as possible.

- The example for enhancement definition, which can be found in [Declarer](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/declarer):
  - [DemoAnnotationDeclarer](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/declarer/DemoAnnotationDeclarer.java) shows how to locate the enhanced class by annotations that decorates the class.
  - [DemoNameDeclarer](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/declarer/DemoNameDeclarer.java) shows how to locate enhanced classes by name.
  - [DemoSuperTypeDeclarer](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/declarer/DemoSuperTypeDeclarer.java) shows how to locate enhanced classes via superclasses.
  - In all three above, you can see how to declare interceptors to enhance constructors, static functions, and instance functions.
  - Remember to add the [spi file](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/resources/META-INF/services/com.huaweicloud.sermant.core.plugin.agent.declarer.PluginDeclarer) for [EnhanceDeclarer](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/agent/declarer/PluginDeclarer.java).
  
- The example for interceptors, which can be found in [interceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor):
  - [DemoConstInterceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoConstInterceptor.java) shows how to write an interceptor to enhance a constructor.
  - [DemoStaticInterceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoStaticInterceptor.java) shows how to write an interceptor to enhance a static function.
  - [DemoMemberInterceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoMemberInterceptor.java) shows how to write an interceptor to enhance an instance function.
  
- The example for log system. [DemoLogger](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/common/DemoLogger.java) shows how to get the log class in the plugin.

- The example for adding additional parameters via the heartbeat function, as shown by the [DemoHeartBeatService](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/service/DemoHeartBeatService.java) service, which is typically a custom service to bring in additional parameters.

- The example for trace tracking usage. As [DemoTraceNormalInterceptor](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoTraceNormalInterceptor.java) shows, the trace tracking function is applied to the interceptor, which enhances the method invocation process of the host application, captures its relevant data information and reports it.

- The example for enhancing native class. As [DemoBootstrapDeclarer](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/Java/com/huawei/example/demo/declarer/DemoBootstrapDeclarer.java) shows, it  is the same as the general way of enhancement. However, considering that modifying native classes is a very dangerous and risk-spreading operation, it is not recommended to enhance native classes.

- The example for plugin configuration : plugin configuration is a specialization of the unified configuration system and follows the rules of the unified configuration system. [config.yaml](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/config/config.yaml) is the configuration file of this sample project , which contains the information of [DemoConfig](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/config/DemoConfig.java) and [DemoServiceConfig](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-service/src/main/java/com/huawei/example/demo/config/DemoServiceConfig.java). As you can see from the definition and invoke of the configuration:
  - There can only be one configuration file per feature, (the `config.yaml` file).
  
  - The package configuration class needs to implement [PluginConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/config/PluginConfig.java) interface whether or not it has an alias for interceptor.
  
  - It is necessary to add the [spi file](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/resources/META-INF/services/com.huaweicloud.sermant.core.plugin.config.PluginConfig) for [PluginConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/config/PluginConfig.java).
  
  - The property type of configuration classes can be boolean, number, string, enum, complex object, as well as their array, list, and dictionary type.
  
  - Plain strings and string fields of complex objects support `${key:default}` style mappings with the following precedence: 
    ```
    javaagent startup parameters > Other properties in this class > environment variables > system variables> default value
    ```
    However, strings wrapped in array, list, and dictionary type will not execute this mapping.
    
  -  It is supported to use of [ConfigTypeKey](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/config/common/ConfigTypeKey.java) annotations to modify full qualified class name in configuration classes , but make sure that all full qualified class name of unified configuration classes (whether modified or not) are not repeatable.
  
  - It is supported to use of [ConfigFieldKey](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/config/common/ConfigFieldKey.java) to modify the name of field for configuration class and its complex object. However, properties of complex objects wrapped in array, list, and dictionary type do not support modification.
  
  - The object of configuration class can be retrieved with the following code:
    ```java
    PluginConfigManager.getPluginConfig(PluginConfigType.class)
    ```
  
- The example for plugin service. Plugin services are specializations of the core service system that follow the rules of the core service system.
  - As [DemoSimpleService](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/service/DemoSimpleService.java) shows, it is a simple plugin service written in the `plugin` module. Because of the positioning of a simple plugin service, it can only use the native Java API and the self-developed API in the core package, and cannot use any third-party API (whether the core package is imported or not).
  - [DemoComplexService](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/service/DemoComplexService.java) is the complex service interface defined in the example `plugin` module, which will be implemented in the `service` module.
  - [DemoComplexServiceImpl](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-service/src/main/java/com/huawei/example/demo/service/DemoComplexServiceImpl.java) is the implementation of [DemoComplexService](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/java/com/huawei/example/demo/service/DemoComplexService.java). It's written in a `service` module, which is a complex plugin service that can use other third-party dependencies (not used in the example).
  - Both simple plugin services and complex plugin service interfaces need to inherit (implement) [PluginService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/service/PluginService.java).
  - It is necessary to add the [spi file](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/template/template-plugin/src/main/resources/META-INF/services/com.huaweicloud.sermant.core.plugin.service.PluginService) for [PluginService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/plugin/service/PluginService.java)
  - The object of service can be retrieved with the following code:
    ```java
    ServiceManager.getService(ServiceType.class)
    ```

For more details on developing plugins, refer to the [Plugin Code Development Guide](dev_plugin_code-zh.md).

### Attentions

In this section, we'll list some of the most error-prone areas of plugin development:

`plugin` cannot rely on or use third-party dependencies other than `byte-buddy` and `slf4j `. If the service functionality is complex and must use other third-party dependencies, they can be extracted as `service` or isolated with `shade` plugin (not recommended).

- If there are multiple `service` modules in the same `main` module, they cannot have dependency conflicts.
- There can be only one configuration file for plugins: `config.yaml`.
- In `config.yaml`, complex objects wrapped in arrays, lists, and dictionaries will not support [ConfigFieldKey](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/config/common/ConfigFieldKey.java) to modify names. Wrapped strings also do not support the `${XXX}` mapping.
- The full qualified class name of the corresponding configuration class in the configuration file cannot be repeated. This rule is valid for all unified configurations globally.
- If the `plugin` and `service` modules are not defined in the `main` module directory of the plugin, you need to pay attention to the output jar package and the configuration path is correct.
- The interceptor type needs to be the same as [MethodInterceptPoint](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/agent/definition/MethodInterceptPoint.java). Be careful not to make mistakes.

### Packaging for Plugin

- The output of the `plugin` product is exported to the `agent /pluginPackage/${feature name}/plugin` directory of the entire product, which is packaged directly with the `shade` plugin by adding the following label:
  ```xml
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
  ```
  
- The `service` product outputs to the `agent/pluginPackage/${feature name}/service` directory of the entire product:
  - Using the `shade` plugin in preference, add the following tags:
    ```xml
    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-shade-plugin</artifactId>
        </plugin>
      </plugins>
    </build>
    ```
    
  - If you make sure the `service` module doesn't have `byte-buddy` and `slf4j` dependencies, you can also:
    - Choose the `Assembly` plugin to pack age dependencies.
    - Or use the `jar` plugin and `dependency` plugin to add the relevant third-party dependencies to the `agent/pluginPackage/${feature name}/service` directory, along with the `service` package without dependencies.
  
  In addition, if there are multiple `service` modules, and they have common dependencies (except `byte-buddy ` and `slf4j`), you can choose to mix `shade` and `dependency` plugins to exclude these common dependencies from the dependency list of `shade` plugin. And export it using the `dependency` plugin.

## Add-on Development Process

This section will describe the development process of two add-ons: `backend module (server)` and `frontend module (webapp)`. Since these two parts are relatively independent parts of the `function`, there are not many development restrictions.

Combined with the steps covered in the [Packaging](# Packaging), there are two `profiles` related to add-on development: `agent` and `test`. It's not recommended adding add-ons to `release`. If you need to add a `backend module (server)` or `frontend module (webapp)` submodule to the `main` plugin module:

- Add the `module` to the following `profile` in the `pom.xml` file of the `main` plugin module：
  ```xml
  <profiles>
    <profile>
      <id>agent</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <modules>
        <module>${backend module name(if exists)}</module>
        <module>${frontend module name(if exists)}</module>
        <module>${other module name(if exists)}</module>
      </modules>
    </profile>
    <profile>
      <id>test</id>
      <modules>
        <module>${backend module name(if exists)}</module>
        <module>${frontend module name(if exists)}</module>
        <module>${other module name(if exists)}</module>
      </modules>
    </profile>
  </profiles>
  ```
- Export ` backend module (server) ` and ` frontend module (webapp) ` to `${package.server.output.dir} `. Common package plugin as follows:
  ```xml
  <!-- spring package plugin -->
  <plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <version>${version of spring-boot package plugin}</version>
    <configuration>
      <mainClass>${main class}</mainClass>
      <outputDirectory>${package.server.output.dir}</outputDirectory>
    </configuration>
    <executions>
      <execution>
        <goals>
          <goal>repackage</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
  
  <!-- shade plugin -->
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>${version of shade plugin}</version>
    <configuration>
      <outputDirectory>${package.server.output.dir}</outputDirectory>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
  </plugin>
  
  <!-- assembly plugin -->
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>${version of assembly plugin}</version>
    <configuration>
      <outputDirectory>${package.server.output.dir}</outputDirectory>
      <descriptorRefs>
        <descriptorRef>jar-with-dependencies</descriptorRef>
      </descriptorRefs>
    </configuration>
    <executions>
      <execution>
        <id>make-assembly</id>
        <phase>package</phase>
        <goals>
          <goal>single</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
  ```
- How the `backend module (server)` submodule and the `frontend module (webapp)` submodule are developed depends on the actual situation. The only requirement is to export  them to the `server/${function name}` directory of the product, and provide scripts or auxiliary text to start them, which can help users start and close quickly.

[Back to README of **Sermant**](../README-zh.md)
