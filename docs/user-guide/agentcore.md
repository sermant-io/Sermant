# 核心模块介绍

本文档主要介绍[**Sermant核心模块**](../../sermant-agentcore/sermant-agentcore-core)，该模块提供处理字节码增强、统一配置、核心服务、插件管理等能力重要内核。

- [核心包版本](#核心包版本)
- [目录结构](#目录结构)
- [字节码增强](#字节码增强)
- [统一配置系统](#统一配置系统)
  - [统一配置管理类](#统一配置管理类)
  - [统一配置类](#统一配置类)
  - [properties策略详解](#properties策略详解)
  - [yaml策略详解](#yaml策略详解)
  - [插件设定配置](#插件设定配置)
- [核心服务系统](#核心服务系统)
  - [核心服务管理类](#核心服务管理类)
  - [核心服务类](#核心服务类)
- [插件管理系统](#插件管理系统)
  - [插件管理类](#插件管理类)
  - [插件类加载器](#插件类加载器)
  - [插件配置系统](#插件配置系统)
  - [插件服务系统](#插件服务系统)
- [LubanAgent](#LubanAgent)
- [相关文档](#相关文档)

本文更多地只是简单介绍[**Sermant核心模块**](../../sermant-agentcore/sermant-agentcore-core)中各个目录的意义，仅做抛砖引玉的作用，如果开发者想要更为细致的了解代码中业务逻辑，请移步至相关的目录或类查看。

## 核心包版本

核心包的版本，属于核心包的内禀属性，因此我们将版本的定义封装到`manifest`文件中，作为`jar`包的元信息存在。核心包版本信息封装于`manifest`文件的`Sermant-Version`参数中，默认取值为`project.version`。在代码中，可以通过以下方法获取核心包版本：
```java
String version = BootArgsIndexer.getCoreVersion();
```

如果需要修改核心包的版本，可以直接修改`project.version`的值。

## 目录结构

[**Sermant核心模块**](../../sermant-agentcore/sermant-agentcore-core)的代码包含以下目录结构：

- `agent`目录，存放[字节码增强](#字节码增强)相关代码。
- `common`目录，存放一些公共的代码。
- `config`目录，存放[统一配置系统](#统一配置系统)相关代码。
- `exception`目录，存放自定义异常。
- `lubanops`目录，存放`luban`旧代码，主要含`bootstrap`模块和`core`模块。
- `plugin`目录，存放[插件管理系统](#插件管理系统)相关代码。
- `service`目录，存放[核心服务系统](#核心服务系统)相关代码。
- `util`目录，存放公用工具类。
- `AgentCoreEntrance`类，系[**Sermant核心模块**](../../sermant-agentcore/sermant-agentcore-core)的入口，调用`run`方法、传入**启动参数**和*Instrumentation*对象带起。

[**Sermant核心模块**](../../sermant-agentcore/sermant-agentcore-core)中还包含以下资源：

- `config`目录，配置文件目录。
  - `agent/plugins.yaml`文件，默认*Profile*的插件设置。
  - `all/plugins.yaml`文件，*all Profile*的插件设置。
  - `example/plugins.yaml`文件，*example Profile*的插件设置。
  - `config.properties`文件，统一配置文件。
- `META-INF/services`目录，*SPI*配置文件目录。
  - `com.huawei.sermant.core.config.common.BaseConfig`文件，用于声明统一配置类。
  - `com.huawei.sermant.core.config.strategy.LoadConfigStrategy`文件，用于声明配置的加载策略。
  - `com.huawei.sermant.core.service.BaseService`文件，用于声明核心服务实现。

## 字节码增强

**Sermant**的**字节码增强**代码见于[agent](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent)目录。

**Sermant**基于`byte-buddy`字节码增强框架做字节码增强，主要采用[**byte-buddy委派**](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/transformer/DelegateTransformer.java)的方式进行，对于原生类增强的场景，则使用[**Advice模板类**](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/template)配合[**byte-buddy advice**](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/transformer/BootstrapTransformer.java)技术进行增强。

`agent`目录下主要包含以下内容：

- `common`目录，存放字节码增强相关的一些公共内容。
- `definition`目录，存放**增强定义接口**，是插件开发者需要关注的内容。
- `enhancer`目录，存放**委派增强器**。
- `interceptor`目录，存放**拦截器接口**、**拦截器链**相关内容和**拦截器加载器**，其中**拦截器接口**是插件开发者需要关注的内容。
- `matcher`目录，存放**被增强类的匹配器**，是插件开发者需要关注的内容。
- `template`目录，存放**Advice模板类**。
- `transformer`目录，字节码转换器，包括委派转换器[DelegateTransformer](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/transformer/DelegateTransformer.java)和*Advice*转换器[BootstrapTransformer](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/transformer/BootstrapTransformer.java)，他们由通用转换器[CommonTransformer](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/transformer/CommonTransformer.java)统一调度。
- `ByteBuddyAgentBuilder`类，字节码增强的入口。

### 增强定义

插件开发者在编写**增强定义**时，实现[EnhanceDefinition](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/definition/EnhanceDefinition.java)接口的`enhanceClass`方法和`getMethodInterceptPoints`方法即可，详情可参见[插件代码开发手册中增强定义一节](../dev-guide/dev_plugin_code.md#增强定义)。

注意不要忘记添加`EnhanceDefinition`的*SPI*配置文件。

### 拦截器

插件开发者在编写**拦截器**时，需要依据被增强方法的类型，实现[interceptor](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/interceptor)目录的不同接口即可：

- 增强静态方法时，需要实现[StaticMethodInterceptor](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/interceptor/StaticMethodInterceptor.java)
- 增强构造函数时，需要实现[ConstructorInterceptor](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/interceptor/ConstructorInterceptor.java)
- 增强实例方法时，需要实现[InstanceMethodInterceptor](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/agent/interceptor/InstanceMethodInterceptor.java)

具体如何怎么做，可以参见[插件代码开发手册中拦截器一节](../dev-guide/dev_plugin_code.md#拦截器)。

## 统一配置系统

**Sermant**的**统一配置系统**代码见于[config](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/config)目录。

`config`目录下包含以下内容：

- `common`目录，存放公用内容。
  - `BaseConfig`类，**统一配置类**通用接口。
  - `ConfigFieldKey`注解，用于为**统一配置类**的字段器别名。
  - `ConfigTypeKey`注解，用于为**统一配置类**起别名。
- `strategy`目录，存放加载配置策略的相关内容。
  - `LoadConfigStrategy`接口，为配置加载策略接口，为加载不同格式的配置文件提供规范方法。
  - `LoadPropertiesStrategy`类，用于加载`properties`格式配置文件的策略，该策略主要用于加载统一配置文件`config.properties`。
  - `LoadYamlStrategy`类，用于加载`yaml`格式配置文件的策略，该策略主要用于加载插件设置和插件配置，详见于[插件配置系统](#插件配置系统)。
- `utils`目录，存放一些统一配置系统使用到的工具类。
- `ConfigManager`类，统一配置管理类，提供加载和获取统一配置的方法。

### 统一配置管理类

**统一配置管理类**`ConfigManager`中，使用者可以通过`getConfig`方法获取**统一配置类**实例：
```java
ConfigExample config = ConfigManager.getConfig(ConfigExample.class);
```

### 统一配置类

**统一配置系统**是一个加载**静态配置**为**Java Pojo**的管理系统，因此，**统一配置类**必须是一个实现[BaseConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/config/common/BaseConfig.java)接口的**Java Pojo**。这些**统一配置类**的具体要求由`LoadPropertiesStrategy`而定，详见[properties策略详解](#properties策略详解)。至于插件相关的[插件配置](#插件配置系统)，则与`LoadYamlStrategy`的要求有关，详见[yaml策略详解](#yaml策略详解)。

**统一配置类**是一个**Java Pojo**，他的`getter`方法和`setter`方法可以直接使用`lombok`的`Data`注解、`Getter`注解和`Setter`注解生成。

注意，编写完**统一配置类**之后，不要忘记添加[BaseConfig](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/config/common/BaseConfig.java)接口的*SPI*配置文件：

- 在资源目录`resources`下添加`META-INF/services`文件夹。
- 在`META-INF/services`中添加`com.huawei.sermant.core.config.common.BaseConfig`配置文件。
- 在上述文件中，以换行为分隔，键入插件包中所有的**统一配置类**。

### properties策略详解

`LoadPropertiesStrategy`加载策略用于对*properties*格式的配置文件进行加载，现在主要用于加载统一配置文件`config.properties`。

`LoadPropertiesStrategy`的思路简单来说就是，以**统一配置类**的全限定名或别名为前缀，以**统一配置类**的属性名或别名为后缀，拼接成*properties*格式的键，获取相应值之后，转化为对应属性的类型并赋值。

假设有以下**统一配置类**：
```java
package com.huawei.example;
public class ConfigExample implements BaseConfig {
  private String string;
  private int intField;
  // getter and setter
}
```

则对应的配置可能形如：
```properties
# 全限定名.属性名=属性值
com.huawei.example.ConfigExample.string=value
com.huawei.example.ConfigExample.intField=123456
```

#### 属性类型

`LoadPropertiesStrategy`支持的属性类型包括：

- 布尔、数值类的基础类型及包装类型
- 字符串类型
- 枚举类型
- 上述类型构成的数组
- 前三种类型构成的*List*
- 前三种类型构成的*Map*

其中`数组`和`List`都是将字符串用`','`分割后，再将各部分转换成相应的类型，即他们的配置形如：
```properties
# 数组配置
com.huawei.example.ConfigExample.stringArr=value1,value2,value3
# List列表配置
com.huawei.example.ConfigExample.intList=100,200,300
```

而`Map`的解析方式，则是通过`','`分割键值对，然后通过`':'`分割键值，配置形如：
```properties
# Map字典配置
com.huawei.example.ConfigExample.string2IntMap=key1:value1,key2:value2,key3:value3
```

需要注意的是，`LoadPropertiesStrategy`不支持复杂类型属性。

#### 起别名

`LoadPropertiesStrategy`支持使用`ConfigTypeKey`注解和`ConfigFieldKey`注解为全限定名和属性名起别名，假定上述`ConfigExample`类修改如下：
```java
@ConfigTypeKey("config.example")
public class ConfigExample implements BaseConfig {
  @ConfigFieldKey("stringField")
  private String string;
  private int intField;
  // getter and setter
}
```

则对应的配置如：
```properties
# 全限定名别名.属性名别名=属性值
config.example.stringField=value
config.example.intField=123456
```

#### 值内省

`LoadPropertiesStrategy`中的属性值支持内省，可以使用`${}`去映射当前配置、系统变量、启动参数等元素。比如`ConfigExample`的配置可以设置为：
```properties
# appName映射启动参数，user.home映射系统变量，com.huawei.example.ConfigExample.intField映射当前配置内容
com.huawei.example.ConfigExample.string=value, ${appName:test}, ${user.home}, ${com.huawei.example.ConfigExample.intField}
com.huawei.example.ConfigExample.intField=123456
```

以`${appName:test}`为例，`appName`为内省的检索键，`test`则是内省失败后的默认取值。内省的检索优先级如下：

- 启动参数(入参和启动配置)
- 当前配置文件(即`config.properties`)
- 环境变量
- 系统变量
- 默认值(即`':'`后内容)

启动参数中包含的内容可参见[入口模块介绍](entrance.md#启动参数)

#### 特殊键值对

`LoadPropertiesStrategy`支持优先使用启动参数中的键值对，启动参数中不存在时，才会使用配置文件中的配置。

假定有以下配置类：
```java
@ConfigTypeKey("env")
public class ConfigExample implements BaseConfig {
  private String tag;
  private String secret;
  // getter and setter
}
```

则`tag`和`secret`两个属性优先使用启动配置`bootstrap.config`中的`env.tag`值和`env.secret`值。

### yaml策略详解

`LoadYamlStrategy`加载策略用于对*yaml*格式的配置文件进行加载，现在主要用于加载插件设定`plugins.yaml`和插件配置`config.yaml`。鉴于插件设定较为简单，后面我们仅对**插件配置类**做介绍。

**插件配置类**和**统一配置类**一样，是个**Java Pojo**，只不过后者实现`BaseConfig`接口，前者实现`PluginConfig`接口或继承`AliaConfig`抽象类，详情可查阅[插件代码开发手册的插件配置一节](../dev-guide/dev_plugin_code.md#插件配置)，我们这里用`PluginConfig`接口举例。

假设有以下**插件配置类**：
```java
package com.huawei.example;
public class ConfigExample implements PluginConfig {
  private String string;
  private int intField;
  // getter and setter
}
```

则对应的配置可能形如：
```yaml
com.huawei.example.ConfigExample:
  string: value
  intField: 123456
```

#### 属性类型

`LoadYamlStrategy`支持的属性类型包括：

- 布尔、数值类的基础类型及包装类型
- 字符串类型
- 枚举类型
- 复杂对象类型
- 上述类型构成的数组
- 前四种类型构成的*List*
- 前四种类型构成的*Map*

#### 起别名

`LoadYamlStrategy`支持使用`ConfigTypeKey`注解和`ConfigFieldKey`注解为全限定名和属性名起别名，假定上述`ConfigExample`类修改如下：
```java
@ConfigTypeKey("config.example")
public class ConfigExample implements PluginConfig {
  @ConfigFieldKey("stringField")
  private String string;
  private int intField;
  // getter and setter
}
```

则对应的配置如：
```yaml
config.example:
  stringField: value
  intField: 123456
```

需要注意的是，对于数组、List和Map中涉及的复杂对象，不支持`ConfigFieldKey`修正属性名。换言之，`ConfigFieldKey`仅对**插件配置类**的属性，及其复杂对象类型属性的属性有效。

#### 值内省

`LoadYamlStrategy`中的属性值支持内省，可以使用`${}`去映射当前集合、系统变量、启动参数等元素。比如`ConfigExample`的配置可以设置为：
```yaml
com.huawei.example.ConfigExample:
  string: value, ${appName:test}, ${user.home}, ${intField}
  intField: 123456
```

以`${appName:test}`为例，`appName`为内省的检索键，`test`则是内省失败后的默认取值。内省的检索优先级如下：

- 启动参数(入参和启动配置)
- 当前集合(如案例中的`ConfigExample`类)
- 环境变量
- 系统变量
- 默认值(即`':'`后内容)

需要注意的是，`LoadPropertiesStrategy`可以映射到整个配置文件，`LoadYamlStrategy`由于配置格式的限制，只能映射当前的`Map`或**复杂对象**。

如果映射当前集合(**复杂对象**)的**公共属性**时，如果**公共属性**使用了`ConfigFieldKey`做别名修正，那么能否正确映射和属性定义顺序有关，比如下面的**插件配置类**：
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

如果`field1`和`field3`需要使用`field2`则对应的配置如：
```yaml
config.example:
  field1: value1, ${stringField}
  stringField: value2
  field3: value3, ${field2}
```

基于上述情况，建议开发者不要对**公共属性**起别名修正。如果实在需要其别名，那么建议将这些**公共属性**统一放在**插件配置类**的开头或结尾。

启动参数中包含的内容可参见[入口模块说明](../sermant-agentcore-premain/README.md#启动参数)。

#### 特殊键值对

`LoadYamlStrategy`不支持使用启动参数中的键值对对**插件配置类**的属性赋值。

### 插件设定配置

**插件设定配置**即`plugins.yaml`文件，在[**Sermant核心模块**](../../sermant-agentcore/sermant-agentcore-core)中存在三个这样的文件：

- [agent/plugins.yaml](../../sermant-agentcore/sermant-agentcore-core/src/main/resources/config/agent/plugins.yaml): 默认编译场景下的**插件设定配置**，不含示例工程。
- [all/plugins.yaml](../../sermant-agentcore/sermant-agentcore-core/src/main/resources/config/all/plugins.yaml): 执行-Pall参数打包时的**插件设定配置**，较`agent`多了示例工程
- [example/plugins.yaml](../../sermant-agentcore/sermant-agentcore-core/src/main/resources/config/example/plugins.yaml): 执行-Pexample参数打包时的**插件设定配置**，仅含示例工程

`plugins.yaml`中，配置了**Sermant**启动后需要加载的插件目录，形如：
```yaml
plugins:
  - plugin1
  - plugin2
  - plugin3
```

这些配置的插件目录将对应到`pluginPackage`目录下的内容。

## 核心服务系统

**Sermant**的**核心服务系统**代码见于[service](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service)目录。

`service`目录下中主要包括：

- [BaseService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/BaseService.java): [**核心服务类**](#核心服务类)
- [ServiceManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/ServiceManager.java): [**核心服务管理类**](#核心服务类)
- 核心服务实现目录

### 核心服务管理类

**核心服务管理类**`ServiceManager`中，使用者可以通过`getService`方法获取**核心服务类**实例：
```java
ServiceExample service = ServiceManager.getService(ServiceExample.class);
```

### 核心服务类

**核心服务系统**是一个将实现[BaseService](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/BaseService.java)的**核心服务类**加载、管理的系统，其核心就是实现**核心服务类**。

我们假定有一个叫`example`的服务，为其编写如下接口：
```java
public interface ServiceExample extends BaseService {
  void foo();
}
```

这样就定义了带有`foo`行为的`example`服务接口，他可能有如下实现：
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

接下来，把`ServiceExampleImpl`添加到`BaseService`的*SPI*配置中即可使用：

- 在资源目录`resources`下添加`META-INF/services`文件夹。
- 在`META-INF/services`中添加`com.huawei.sermant.core.service.BaseService`配置文件。
- 在上述文件中，以换行为分隔，键入插件包中所有的**核心服务类**实现(`ServiceExampleImpl`)。

这样就能通过`ServiceManager`的`getService`方法获取到**核心服务类**实例了。

## 插件管理系统

**Sermant**的**插件管理系统**代码见于[plugin](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin)目录。

`plugin`目录下主要包括：

- [classloader/PluginClassLoader](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin/classloader/PluginClassLoader.java)类，即[插件类加载器](#插件类加载器)。
- [config](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin/config)目录，里面存放着[插件配置系统](#插件配置系统)相关代码。
- [service](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin/service)目录，里面存放着[插件服务系统](#插件服务系统)相关代码。
- [PluginManager](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/plugin/PluginManager.java)类，即[插件管理类](#插件管理类)

### 插件管理类

在**插件管理类**类中，主要对插件设定文件`plugins.yaml`中配置的插件目录进行遍历，对每个插件目录来说：

- 加载其所有**插件包**至系统类加载器`AppClassLoader`。
- 自定义[插件类加载器](#插件类加载器)加载所有**插件服务包**。
- 加载所有相关的[插件配置](#插件配置系统)。
- 加载所有相关的[插件服务](#插件服务系统)。

### 插件类加载器

**插件类加载器**即`PluginClassLoader`类。`PluginClassLoader`是一个特殊的`URLClassLoader`，他将持有单个功能的所有插件服务包的*URL*。`PluginClassLoader`破坏了双亲委派机制，在加载*Class*的时候，优先使用自己的*Class*，在调用父类原生的加载方法，具体执行逻辑如下：

- 尝试获取自身已加载过的*Class*。
- 尝试加载自身持有*URL*的*Class*，并将其缓存。
- 无法从自身获取*Class*时，再调用父类原生的加载方法。

### 插件配置系统

**插件配置系统**是[**统一配置系统**](#统一配置系统)的特例，主要用于读取插件配置文件`config.yaml`，因此遵循[yaml格式加载策略](#yaml策略详解)的规则，这里不做赘述。

更多**插件配置系统**相关内容，可以参见[插件代码开发手册的插件配置一节](../dev-guide/dev_plugin_code.md#插件配置)。

### 插件服务系统

**插件服务系统**是[**核心服务系统**](#核心服务系统)的特例，主要用于加载插件服务`PluginService`，因此他遵循**核心服务系统**的规则，这里不做赘述。

更多**插件服务系统**相关内容，可以参见[插件代码开发手册的插件服务一节](../dev-guide/dev_plugin_code.md#插件服务)。

## LubanAgent

**LubanAgent**指的是`lubanops`目录下的代码，其中主要包含消息发送、心跳、链路追踪等功能的实现。

## 相关文档

|文档名称|
|:-|
|[入口模块介绍](entrance.md)|
|[后端模块介绍](backend.md)|

[返回**Sermant**说明文档](../README.md)
