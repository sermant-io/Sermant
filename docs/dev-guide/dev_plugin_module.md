# 插件模块开发手册

本文档主要针对**Sermant**的[插件根模块](../../sermant-plugins)，介绍如何在`插件根模块`中添加一个插件及其服务和附加件。

- [开发环境](#开发环境)
- [组成分类](#组成分类)
- [打包流程](#打包流程)
- [添加插件主模块](#添加插件主模块)
  - [插件名称和版本](#插件名称和版本)
- [插件开发流程](#插件开发流程)
  - [插件开发前言](#插件开发前言)
  - [添加插件模块](#添加插件模块)
  - [添加插件服务模块](#添加插件服务模块)
  - [添加配置](#添加配置)
  - [示例工程解读](#示例工程解读)
  - [注意事项](#注意事项)
  - [插件打包流程](#插件打包流程)
- [附加件开发流程](#附加件开发流程)

## 开发环境

- [HuaweiJDK 1.8](https://gitee.com/openeuler/bishengjdk-8) / [OpenJDK 1.8](https://github.com/openjdk/jdk) / [OracleJDK 1.8](https://www.oracle.com/java/technologies/downloads/)
- [Apache Maven 3](https://maven.apache.org/download.cgi)

## 组成分类

**Sermant**的`插件根模块`按照插件功能划分子模块，即`插件主模块(main)`，其中又可以包含以下类型的子模块：

- `插件模块(plugin)`，该模块主要用于声明对宿主应用的增强逻辑
- `服务模块(service)`，用于为插件包提供服务实现
- `后端模块(server)`，用于接收插件数据的服务端
- `前端模块(webapp)`，用于对服务端数据作前端展示
- `其他模块(other)`，特殊附加件，一般用作调试

## 打包流程

目前[Sermant](../../pom.xml)的打包流程中，包含`agent`、`ext`、`example`、`package`和`all`
6个步骤，其中与[sermant-plugins](../../sermant-plugins/pom.xml)相关的步骤如下：

- `agent`: 对除示例功能[sermant-example](../../sermant-plugins/sermant-example)外所有`插件模块(plugin)`和`服务模块(service)`进行打包，他们将输出到产品`agent/pluginPackage/${功能名称}`目录。
- `ext`: 对所有附加件进行打包，包括`后端模块(server)`、`前端模块(webapp)`和`其他模块(other)`，其中`后端模块(server)`和`前端模块(webapp)`将输出到产品的`server/${功能名称}`目录，`其他模块(other)`一般为调试用的附加件，没有打包要求。
- `example`: 对示例功能[sermant-example](../../sermant-plugins/sermant-example)进行打包。
- `all`: 对上述的所有内容进行打包。

## 添加插件主模块

- 添加`插件主模块(main)`，依据该`插件主模块(main)`中涉及的内容，在[sermant-plugins的pom文件](../../sermant-plugins/pom.xml)中的特定`profile`中添加相应模块：
  - 必须在`id`为`all`的`profile`中添加该模块。
  - 如果该模块包含`插件模块(plugin)`，那么需要在`id`为`agent`的`profile`中添加该模块。
  - 如果该模块包含其他内容，则需要在`id`为`ext`的`profile`中添加该模块。
- 在该模块的`pom.xml`中添加以下标签：
  ```xml
  <packaging>pom</packaging>
  ```
  ```xml
  <properties>
    <sermant.basedir>${pom.basedir}/../../..</sermant.basedir>
    <package.plugin.name>${插件名称}</package.plugin.name>
  </properties>
  ```
  - 在[默认插件设置文件](../../sermant-agentcore/sermant-agentcore-core/src/main/resources/config/agent/plugins.yaml)和[全数插件设置文件](../../sermant-agentcore/sermant-agentcore-core/src/main/resources/config/all/plugins.yaml)中添加新增的`插件主模块(main)`，完成注册。

`插件主模块(main)`的子模块开发流程参见一下章节：
- `插件模块(plugin)`和`服务模块(service)`开发流程参见[插件开发流程](#插件开发流程)
- `后端模块(server)`和`前端模块(webapp)`开发流程参见[附加件开发流程](#附加件开发流程)
- `其他模块(other)`由于只参与调试，不涉及开发流程限定

### 插件名称和版本

插件的名称和版本，属于`插件模块(plugin)`和`服务模块(service)`的内禀属性，因此我们将他们封装到`manifest`文件中，作为`jar`包的元信息存在：

- **插件名称**封装于`manifest`文件的`Sermant-Name-Version`参数中，通过`pom`文件中的`package.plugin.name`参数设定，默认含义为**插件名称**。
- **插件版本**封装于`manifest`文件的`Sermant-Plugin-Version`参数中，默认取值为`project.version`，可通过`pom`文件中的`package.plugin.version`参数修改。
```xml
<properties>
  <package.plugin.name>${插件名称}</package.plugin.name>
  <package.plugin.version>${插件版本}</package.plugin.version>
</properties>
```

在插件包被加载的过程中，将对`插件模块(plugin)`和`服务模块(service)`的名称和版本进行校验，如果不满足以下条件的话，将抛出异常：

- 对于所有`插件模块(plugin)`来说，由于不允许包含`byte-buddy`和`slf4j`以外的第三方依赖，考虑到`byte-buddy`和`slf4j`包将通过`shade`插件修改全限定名，因此，在加载`插件模块(plugin)`存放目录(`插件主模块(main)`目录下的`plugin`目录)时，如果检出不含**插件名称**和**插件版本**的第三方包，将抛出异常。
- 对于所有`插件模块(plugin)`和`服务模块(service)`来说，如果其中设置的**插件名称**与**插件设定文件**中配置的名称不一致，将抛出异常。
- 对于所有`插件模块(plugin)`和`服务模块(service)`来说，如果其中存在多个不同的**插件版本**，将抛出异常。

对于插件开发者来说，如果没有特殊的需要，不建议修改默认的设计，保持`插件模块(plugin)`中不使用`byte-buddy`和`slf4j`以外的第三方依赖、`插件模块(plugin)`写接口、`服务模块(service)`写实现等原则就好。

## 插件开发流程

本节将介绍插件开发的相关流程，其中涉及的模块为`插件模块(plugin)`和`服务模块(service)`。

### 插件开发前言

`插件模块(plugin)`的定位是定义宿主应用的增强逻辑，考虑到依赖冲突的问题，其增强的字节码中不能涉及对`byte-buddy`和`slf4j`以外的第三方依赖的使用，这时就需要分两种情况讨论：

- 对于简单的插件，其中编写的插件服务只会使用核心包中的自研功能，不涉及需要依赖其他第三方依赖的复杂功能时，那么只需要开发`插件模块(plugin)`即可。`插件模块(plugin)`将被系统类加载器`AppClassLoader`加载。
- 对于一些复杂的插件，如果需要依赖其他第三方依赖的复杂功能时，就需要在`插件模块(plugin)`设计服务接口，并编写`服务模块(service)`予以实现。其中`插件模块(plugin)`仍被系统类加载器`AppClassLoader`加载，而`服务模块(service)`则会优先被自定义类加载器[PluginClassLoader](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin/classloader/PluginClassLoader.java)加载，以此达成类加载级别的依赖隔离。

至于**核心模块**中的第三方依赖，他们在打包过程中会随自研代码一起，被[shade插件](https://github.com/apache/maven-shade-plugin)修正全限定名。除非`插件模块(plugin)`打包时做同样的全限定名的操作，不然是无法在`插件模块(plugin)`使用的。

另外，对于`byte-buddy`包来说，由于方法匹配器没有在**核心模块**中提取，`插件模块(plugin)`仍需沿用他们，因此对`插件模块(plugin)`才有修正`byte-buddy`包全限定名的要求。

至于`slf4j`包，考虑到在**复杂插件服务**初始化期间，由于类加载器隔离的问题，导致第三方的`slf4j`接口使用了**核心功能模块**的配置却无法关联到**核心功能模块**的实现，因此只能修正`slf4j`的全限定名以适配。

以上就是`插件模块(plugin)`和`服务模块(service)`打包时必须修正`byte-buddy`和`slf4j`的全限定名的原因，也是`插件模块(plugin)`可以使用这两个依赖的原因。

### 添加插件模块

结合[打包流程](#打包流程)中介绍的步骤，与插件开发相关的步骤有`agent`和`all`两个`profile`。如果需要为`插件主模块(main)`添加`插件模块(plugin)`子模块：

- 在`插件主模块(main)`的`pom.xml`文件的以下`profile`中添加`module`：
  ```xml
  <profiles>
    <profile>
      <id>agent</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <modules>
        <module>${插件模块名}</module>
      </modules>
    </profile>
    <profile>
      <id>all</id>
      <modules>
        <module>${插件模块名}</module>
      </modules>
    </profile>
  </profiles>
  ```
- 为`插件模块(plugin)`子模块添加以下参数：
  ```xml
  <properties>
    <package.plugin.type>plugin</package.plugin.type>
  </properties>
  ```
- 为`插件模块(plugin)`子模块添加核心包依赖：
  ```xml
  <dependencies>
    <dependency>
      <groupId>com.huawei.sermant</groupId>
      <artifactId>sermant-agentcore-core</artifactId>
      <scope>provided</scope>
    </dependency>
  </dependencies>
  ```
  注意，不能为`插件模块(plugin)`添加或使用`byte-buddy`和`slf4j`以外的第三方依赖，除非使用`shade`插件将他们的全限定名修正(不建议)！
- 为`插件模块(plugin)`子模块添加`shade`插件，以修正`byte-buddy`和`slf4j`的全限定名，原因详见[插件开发前言](#插件开发前言)：
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

### 添加插件服务模块

和`插件模块(plugin)`相似，添加`服务模块(service)`步骤如下：

- 在`插件主模块(main)`的`pom.xml`文件的以下`profile`中添加`module`：
  ```xml
  <profiles>
    <profile>
      <id>agent</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <modules>
        <module>${插件服务模块名}</module>
      </modules>
    </profile>
    <profile>
      <id>all</id>
      <modules>
        <module>${插件服务模块名}</module>
      </modules>
    </profile>
  </profiles>
  ```
- 为`服务模块(service)`子模块添加以下参数：
  ```xml
  <properties>
    <package.plugin.type>service</package.plugin.type>
  </properties>
  ```
- 为`服务模块(service)`子模块添加核心包和相关插件包的依赖：
  ```xml
  <dependencies>
    <dependency>
      <groupId>com.huawei.sermant</groupId>
      <artifactId>sermant-agentcore-core</artifactId>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>com.huawei.sermant</groupId>
      <artifactId>${插件模块名}</artifactId>
      <version>${插件模块版本}</version>
      <scope>provided</scope>
    </dependency>
  </dependencies>
  ```
  `服务模块(service)`中允许按需添加第三方依赖！
- 为`服务模块(service)`子模块添加`shade`插件打包：
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
  这里不建议使用其他打包插件，详见[插件开发前言](#插件开发前言)。

### 添加配置

如果`插件模块(plugin)`和`服务模块(service)`中需要使用插件配置`PluginConfig`，那么需要添加插件的配置文件：

- 在`插件主模块(main)`的主目录下添加`config`文件夹。
- 在`config`文件夹中添加一个`config.yaml`文件，这就是插件的配置文件。
- 在`插件主模块(main)`的任意子模块(一般取第一个子模块)中添加以下配置以启动资源拷贝：
  ```xml
  <properties>
    <config.skip.flag>false</config.skip.flag>
  </properties>
  ```
  如果配置上述参数的子模块不在`插件主模块(main)`的目录下，还需要根据该模块与`config`目录的相对位置设置以下参数：
  ```xml
  <properties>
    <config.source.dir>../config</config.source.dir> <!-- 调整该值为config目录的相对位置 -->
  </properties>
  ```

### 示例工程解读

`sermant-example`模块是一个示例功能模块，其中涉及了大部分开发插件时可能碰到的场景，本节将会对该模块的内容进行解读，以帮助读者能尽快上手开发插件功能。

- 增强定义示例，见于[definition](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/definition)包：
  - [DemoAnnotationDefinition](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/definition/DemoAnnotationDefinition.java)展示如何通过修饰类的注解定位到被增强的类。
  - [DemoNameDefinition](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/definition/DemoNameDefinition.java)展示如何通过名称定位到被增强的类。
  - [DemoSuperTypeDefinition](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/definition/DemoSuperTypeDefinition.java)展示如何通过超类定位到被增强的类。
  - 上述三者都可以看出如何声明用于增强构造函数、静态函数和实例函数的拦截器。
  - 需要添加[EnhanceDefinition](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/definition/EnhanceDefinition.java)的[spi配置文件](../../sermant-plugins/sermant-example/demo-plugin/src/main/resources/META-INF/services/com.huawei.sermant.core.agent.definition.EnhanceDefinition)。
- 拦截器示例，见于[interceptor](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/interceptor)包：
  - [DemoConstInterceptor](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoConstInterceptor.java)展示了如何编写一个用于增强构造函数的拦截器。
  - [DemoStaticInterceptor](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoStaticInterceptor.java)展示了如何编写一个用于增强静态函数的拦截器。
  - [DemoInstInterceptor](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoInstInterceptor.java)展示了如何编写一个用于增强实例函数的拦截器。
- 日志系统使用示例，如[DemoLogger](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/common/DemoLogger.java)展示了如何在插件中获取日志类。
- 通过心跳功能添加额外参数示例，如[DemoHeartBeatService](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/service/DemoHeartBeatService.java)服务所示，通常使用自定义服务的方式将额外参数带入。
- 链路功能使用示例，如[DemoTraceInterceptor](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoTraceInterceptor.java)所示，通常链路功能应用于拦截器，对宿主应用的方法调用过程进行增强，捕获其相关的数据信息并上报。
- 增强原生类示例，如[DemoBootstrapDefinition](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/definition/DemoBootstrapDefinition.java)所示，与普通类的增强方式无异。但是，考虑到修改原生类是非常危险的且风险扩散的操作，不建议对原生类进行增强。
- 插件配置示例：插件配置是统一配置系统的特化，遵循统一配置系统的规则。[config.yaml](../../sermant-plugins/sermant-example/config/config.yaml)是示例工程的配置文件，其中包含[DemoConfig](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/config/DemoConfig.java)和[DemoServiceConfig](../../sermant-plugins/sermant-example/demo-service/src/main/java/com/huawei/example/demo/config/DemoServiceConfig.java)两个配置类对应的配置信息，从配置的定义和调用可以看到：
  - 每个功能的配置文件仅能有1个，即`config.yaml`文件。
  - 插件包的配置类如果有拦截器别名的设定，可以继承[AliaConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin/config/AliaConfig.java)类，其他情况都实现[PluginConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin/config/PluginConfig.java)接口。
  - 无论继承哪个类，都需要添加[PluginConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin/config/PluginConfig.java)的[spi配置文件](../../sermant-plugins/sermant-example/demo-plugin/src/main/resources/META-INF/services/com.huawei.sermant.core.plugin.config.PluginConfig)。
  - 配置类的属性类型可以是布尔、数字、字符串、枚举、复杂对象，以及他们的数组、列表和字典类型。
  - 普通字符串和复杂对象的字符串字段支持`${key:default}`风格的映射，优先级如下：
    ```
    javaagent启动参数 > 当前类的其他属性 > 环境变量 > 系统变量 > 默认值
    ```
    但是，被数组、列表和字典类型包装的字符串将不会做上述映射。
  - 配置类支持使用[ConfigTypeKey](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/config/common/ConfigTypeKey.java)注解修改限定名，但是需要确保，所有统一配置的限定名(无论是否修改)不可重复。
  - 配置类及其复杂对象的属性支持使用[ConfigFieldKey](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/config/common/ConfigFieldKey.java)注解修改字段的名称。但是，被数组、列表和字典类型包装的复杂对象的属性则不支持修改。
  - 通过以下代码可以获取配置对象：
    ```java
    PluginConfigManager.getPluginConfig(PluginConfigType.class)
    ```
- 插件服务示例：插件服务是核心服务系统的特化，遵循核心服务系统的规则。
  - 如[DemoSimpleService](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/service/DemoSimpleService.java)所示，是一个简单的插件服务，他编写于`插件模块(plugin)`中。鉴于简单的插件服务的定位，他只能使用java原生api以及核心包中自研的api，不能使用任何第三方api(无论核心包是否引入)。
  - [DemoComplexService](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/service/DemoComplexService.java)接口是示例`插件模块(plugin)`中定义复杂服务接口，该接口将会在`服务模块(service)`中实现。
  - [DemoComplexServiceImpl](../../sermant-plugins/sermant-example/demo-service/src/main/java/com/huawei/example/demo/service/DemoComplexServiceImpl.java)是[DemoComplexService](../../sermant-plugins/sermant-example/demo-plugin/src/main/java/com/huawei/example/demo/service/DemoComplexService.java)接口的实现，他编写于`服务模块(service)`中，属于复杂的插件服务，可以按需使用其他第三方依赖(示例中未使用)。
  - 简单的插件服务和复杂的插件服务接口都需要继承(实现)[PluginService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin/service/PluginService.java)接口。
  - 需要添加[PluginService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin/service/PluginService.java)的[spi配置文件](../../sermant-plugins/sermant-example/demo-plugin/src/main/resources/META-INF/services/com.huawei.sermant.core.plugin.service.PluginService)。
  - 通过以下代码可以获取服务对象：
    ```java
    ServiceManager.getService(ServiceType.class)
    ```

更多详细的插件开发相关内容，参见[插件代码开发手册](dev_plugin_code.md)。

### 注意事项

本节将列举一些开发插件过程中容易出现错误的点：

- `插件模块(plugin)`不能依赖或使用`byte-buddy`和`slf4j`以外第三方依赖，如果服务功能较为复杂，必须使用其他第三方依赖，则可以将他们提取为`服务模块(service)`，或者用`shade`插件隔离(不建议)。
- 同个`插件主模块(main)`中，如果存在多个`服务模块(service)`，他们不能存在依赖冲突的问题。
- 继承[AliaConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin/config/AliaConfig.java)的插件配置类对应的spi配置文件依然是[PluginConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin/config/PluginConfig.java)。
- 插件的配置文件有且只能有一个，即`config.yaml`。
- `config.yaml`中，被数组、列表和字典包装的复杂对象将不支持[ConfigFieldKey](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/config/common/ConfigFieldKey.java)属性别名，被包装的字符串也将不支持`${xxx}`映射。
- 配置文件中对应配置类的限定名不能重复，该规则针对全局所有统一配置有效。
- `插件模块(plugin)`和`服务模块(service)`如果不是定义在`插件主模块(main)`的目录下，需要留意输出的jar包和配置路径是否正确。
- 拦截器的类型需要和[MethodInterceptPoint](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/definition/MethodInterceptPoint.java)的对应方法保持一致，注意别写错。

### 插件打包流程

- `插件模块(plugin)`的产品输出到整个产品的`agent/pluginPackage/${功能名称}/plugin`目录，直接使用`shade`插件打包，添加如下标签即可：
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
- `服务模块(service)`的产品输出到整个产品的`agent/pluginPackage/${功能名称}/service`目录：
  - 优先选择使用`shade`插件，添加以下标签：
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
  - 如果确认`服务模块(service)`不含`byte-buddy`和`slf4j`依赖，也可以：
    - 选择使用`assembly`插件打带依赖包。
    - 或者直接使用`jar`插件和`dependency`插件将相关的第三方依赖连同不带依赖的`服务模块(service)`包一同打到`agent/pluginPackage/${功能名称}/service`目录下。
  
  另外，如果存在多个`服务模块(service)`，且他们存在公共依赖(`byte-buddy`和`slf4j`除外)，可以选择混用`shade`插件和`dependency`插件，将这些公共依赖排除出`shade`插件的依赖列表，并使用`dependency`插件导出。

## 附加件开发流程

本节将介绍`后端模块(server)`和`前端模块(webapp)`两种附加件的开发流程，由于这两部分是`功能function`中相对独立的内容，因此没有太多开发上的限制。

结合[打包流程](#打包流程)中介绍的步骤，与附加件开发相关的步骤有`ext`和`all`两个`profile`。如果需要为`插件主模块(main)`添加`后端模块(server)`或`前端模块(webapp)`子模块：

- 在`插件主模块(main)`的`pom.xml`文件的以下`profile`中添加`module`：
  ```xml
  <profiles>
    <profile>
      <id>ext</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <modules>
        <module>${后端模块名(如果有)}</module>
        <module>${前端模块名(如果有)}</module>
        <module>${其他模块名(如果有)}</module>
      </modules>
    </profile>
    <profile>
      <id>all</id>
      <modules>
        <module>${后端模块名(如果有)}</module>
        <module>${前端模块名(如果有)}</module>
        <module>${其他模块名(如果有)}</module>
      </modules>
    </profile>
  </profiles>
  ```
- 将`后端模块(server)`子模块和`前端模块(webapp)`子模块的输出调整至`${package.server.output.dir}`，常见打包插件如下：
  ```xml
  <!-- spring打包插件打包 -->
  <plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <version>${spring-boot打包插件版本}</version>
    <configuration>
      <mainClass>${main函数入口}</mainClass>
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

  <!-- shade插件打包 -->
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>${shade插件版本}</version>
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

  <!-- assembly插件打包 -->
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>${assembly插件版本}</version>
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
- `后端模块(server)`子模块和`前端模块(webapp)`子模块如何开发依实际情况而定，仅有的要求，就是输出到产品的`server/${功能名称}`目录，同时提供启动他们的脚本或辅助性文本，能够帮助使用者快速启动和关闭即可。

[返回**Sermant**说明文档](../README.md)
