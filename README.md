# JavaMesh

## 概述

JavaMesh是一个基于ByteBuddy字节码技术开发的javaagent框架;框架当前提供了流量控制，流量录制插件;基于JavaMesh,只需实现少量的接口即可快速开发自己需要实现的agent功能;框架提供了基于netty的统一消息发送模块;只需部署netty-server服务，即可实现心跳或数据的传输,同时支持自定义消息类型。

## 模块说明

 - javamesh-agentcore: 核心功能
   - javamesh-agentcore/javamesh-agentcore-core: 核心模块
   - javamesh-agentcore/javamesh-agentcore-premain: 启动入口模块
 - javamesh-backend: 消息发送模块服务端
 - javamesh-package: 打包模块  
 - javamesh-prepare: 初始化模块，现用作清理临时文件
 - javamesh-samples: 样品模块，内含各种功能的插件及其附加件
   - javamesh-samples/javamesh-example: 插件示例
   - javamesh-samples/javamesh-flowcontrol: 流控功能
   - javamesh-samples/javamesh-server-monitor: 服务监控功能
   - javamesh-samples/javamesh-flowrecord: 流量录制回放功能

## 打包流程说明

JavaMesh的打包流程大致分为以下步骤：

- prepare: 清理临时文件夹并拷贝外部插件、后端和前端
- agent: 编译、打包核心功能和插件
- example: 编译、打包核心功能和示例模块(默认不开启)
- ext: 编译、打包插件附带的后端、前端和其他附加件
- package: 将以上的打包结果归档为产品包
- all: 执行以上全部步骤(比默认情况多打示例模块)

## 产品目录说明

正常打包结束后，将在工程目录下生成一个javamesh-agent-x.x.x文件夹，以及javamesh-agent-x.x.x.tar文件，前者是打包的临时目录，后者为产品包，前者即为后者的解压内容。

- javamesh-agent-x.x.x/agent: javamesh-agent的客户端
  - javamesh-agent-x.x.x/agent/javamesh-agent.jar: javamesh-agent的agent包
  - javamesh-agent-x.x.x/agent/apm.config: javamesh-agent的配置文件
  - javamesh-agent-x.x.x/agent/core: javamesh-agent的核心实现包存放目录
  - javamesh-agent-x.x.x/agent/plugins: javamesh-agent的插件包存放目录
- javamesh-agent-x.x.x/server: javamesh-agent和各个插件的相应后端存放目录
- javamesh-agent-x.x.x/webapp: 各个后端对应的前端目录

## 示例说明

### [示例插件](javamesh-samples/javamesh-example/demo-plugin)

示例插件中将展示以下内容：

- 如何编写一个增强定义[EnhanceDefinition](javamesh-agentcore/javamesh-agentcore-core/src/main/java/com/huawei/apm/core/definition/EnhanceDefinition.java)
  - 如何定位到一个被注解修饰的类[DemoAnnotationDefinition](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/definition/DemoAnnotationDefinition.java)
  - 如何通过名称定位到一个类[DemoNameDefinition](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/definition/DemoNameDefinition.java)
  - 如何定位到一个超类的子类[DemoSuperTypeDefinition](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/definition/DemoSuperTypeDefinition.java)
- 如何编写一个拦截器[Interceptor](javamesh-agentcore/javamesh-agentcore-core/src/main/java/com/huawei/apm/core/interceptors/Interceptor.java)
  - 如何编写一个构造函数的拦截器[DemoConstInterceptor](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoConstInterceptor.java)
  - 如何编写一个示例方法的拦截器[DemoInstInterceptor](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoInstInterceptor.java)
  - 如何编写一个静态方法的拦截器[DemoStaticInterceptor](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoStaticInterceptor.java)
- 如何编写一个插件服务[DemoService](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/service/DemoService.java)
- 如何在插件端使用日志功能[DemoLoggerInterceptor](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoLoggerInterceptor.java)
- 如何在插件端使用统一配置[DemoConfigInterceptor](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoConfigInterceptor.java)
- 如何在插件端使用心跳功能[DemoHeartBeatService](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/service/DemoHeartBeatService.java)
- 如何在插件端使用链路监控功能[DemoTraceInterceptor](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/interceptor/DemoTraceInterceptor.java)
- 如何在插件端使用配置中心功能[DemoConfigServerService](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/service/DemoConfigServerService.java)

### [示例插件拦截的应用](javamesh-samples/javamesh-example/demo-application)

示例插件拦截的应用为[示例插件](javamesh-samples/javamesh-example/demo-plugin)中的示例功能提供了各种被拦截点。

## 快速开始

### 环境安装

- [jdk](https://www.oracle.com/java/technologies/downloads/)
- [maven](https://maven.apache.org/download.cgi)
- [idea](https://www.jetbrains.com/idea/)

### 编译出包

- 下载`JavaMesh`源码,用`idea`打开
- 在`File | Settings | Build, Execution, Deployment | Build Tools | Maven`中配置`maven`信息
- 在`idea`中执行`mvn clean package`
- 编译结果文件:`JavaMesh\javamesh-agent-x.x.x.tar`

### 运行

#### 终端

- 打包[示例插件拦截的应用](javamesh-samples/javamesh-example/demo-application)

- 执行以下命令启动示例应用
```bat
java -javaagent:${JavaMesh}\javamesh-agent-x.x.x\agent\javamesh-agent.jar=appName=${appName} ^
    -cp .\demo-application-1.0.0.jar ^
    com.huawei.example.demo.DemoApplication
```
其中`${JavaMesh}`是框架项目路径,`${appName}`为应用名称,可任意取值

#### IDEA

- IDEA挂载JavaMesh,需在应用`Run Configuration -> VM options`
  加入`-javaagent:${JavaMesh}\javamesh-agent-x.x.x\agent\javamesh-agent.jar=appName=${appName}`
  即可,其中`${JavaMesh}`是框架项目路径,`${appName}`为应用名称。
- 运行[应用](javamesh-samples/javamesh-example/demo-application/src/main/java/com/huawei/example/demo/DemoApplication.java)

## 插件开发
框架采用SPI机制进行插件的加载，插件的开发需要在resources/META-INF/service创建相应的文件(文件名与实现接口的全限定名一致)
### [增强类接口](javamesh-agentcore/javamesh-agentcore-core/src/main/java/com/huawei/apm/core/definition/EnhanceDefinition.java)
该接口定义了两个方法：`ClassMatcher enhanceClass()`和`MethodInterceptPoint[] getMethodInterceptPoints()`：  
`ClassMatcher enhanceClass()`用来获取需要增强的目标类，支持单个和多个类，注解，也可以通过前缀匹配需要增强的类；  
`MethodInterceptPoint[] getMethodInterceptPoints()`用来获取封装了待增强目标方法和其拦截器的MethodInterceptPoint(对应的拦截器接口说明在下面详细说明)，支持返回多个不同类型的拦截器。
- [spi文件示例](javamesh-samples/javamesh-example/demo-plugin/src/main/resources/META-INF/services/com.huawei.apm.bootstrap.definition.EnhanceDefinition)  
  文件名为接口类文件的全限定名；  
  文件内容为实现了该接口的类的全限定名；    
  文件位置按照spi的机制应放到模块`resources/META-INF/services`。
- [实现示例](javamesh-samples/javamesh-example/demo-plugin/src/main/java/com/huawei/example/demo/definition/DemoAnnotationDefinition.java)
```java
public class DemoAnnotationDefinition implements EnhanceDefinition {
    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.annotationWith("com.huawei.example.demo.DemoAnnotation");
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newStaticMethodInterceptPoint(
                        "com.huawei.example.demo.interceptor.DemoStaticInterceptor",
                        ElementMatchers.<MethodDescription>named("staticFunc")
                )
          };
    }
}
```
在示例代码中增强了`com.huawei.example.demo.DemoAnnotation`注解修饰的类，拦截器的类为`com.huawei.example.demo.interceptor.DemoStaticInterceptor`，实现了静态方法拦截接口(这部分在下面详细说明)，拦截的方法为`staticFunc`方法。
### [拦截器接口](javamesh-agentcore/javamesh-agentcore-core/src/main/java/com/huawei/apm/core/interceptors/Interceptor.java)
该部分接口的实现不需要通过spi机制加载；  
拦截器接口的实现类用在增强类接口的`getMethodInterceptPoints()`方法中；
根据方法的不同扩展出了三种拦截器接口，分别是静态方法拦截器`StaticMethodInterceptor`，实例方法拦截器`InstanceMethodInterceptor`,构造方法拦截器`ConstructorInterceptor`。
- [静态拦截器](javamesh-agentcore/javamesh-agentcore-core/src/main/java/com/huawei/apm/core/interceptors/StaticMethodInterceptor.java)  
  该拦截器接口中有三个方法：`before`, `after`, `onThrow`。  
  `before`在拦截方法执行前前运行；`after`在拦截方法执行结束后运行；`onThrow`用于异常处理。
```java
public class DemoStaticInterceptor implements StaticMethodInterceptor {
    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        System.out.println(clazz.getSimpleName() + ": [DemoStaticInterceptor]-before");
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
        System.out.println(clazz.getSimpleName() + ": [DemoStaticInterceptor]-after");
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {
        System.out.println(clazz.getSimpleName() + ": [DemoStaticInterceptor]-onThrow");
    }
}
```
- [示例拦截器](javamesh-agentcore/javamesh-agentcore-core/src/main/java/com/huawei/apm/core/interceptors/InstanceMethodInterceptor.java)  
  该拦截器接口中有三个方法：`before`, `after`, `onThrow`。  
  `before`在拦截方法执行前前运行；`after`在拦截方法执行结束后运行；`onThrow`为异常处理。
- [构造拦截器](javamesh-agentcore/javamesh-agentcore-core/src/main/java/com/huawei/apm/core/interceptors/ConstructorInterceptor.java)
- 该拦截器接口中有一个方法：`onConstruct`。
### [插件配置接口](javamesh-agentcore/javamesh-agentcore-core/src/main/java/com/huawei/apm/core/config/BaseConfig.java)
插件配置接口实现类中写入插件运行过程中需要的配置信息。
- [spi文件示例](javamesh-samples/javamesh-example/demo-plugin/src/main/resources/META-INF/services/com.huawei.apm.bootstrap.config.BaseConfig)  
  文件名为接口类文件的全限定名；  
  文件内容为实现了该接口的类的全限定名；    
  文件位置按照spi的机制应放到模块`resources/META-INF/services`。
```java
@ConfigTypeKey("demo.test")
public class DemoConfig implements BaseConfig {
    @ConfigFieldKey("str2DoubleMap") 
    private Map<String, Double> map = Collections.emptyMap();
}
```
### [插件初始化接口](javamesh-agentcore/javamesh-agentcore-core/src/main/java/com/huawei/apm/core/boot/PluginService.java)
- [spi文件示例](javamesh-samples/javamesh-example/demo-plugin/src/main/resources/META-INF/services/com.huawei.apm.bootstrap.boot.PluginService)  
  文件名为接口类文件的全限定名；  
  文件内容为实现了该接口的类的全限定名；    
  文件位置按照spi的机制应放到模块`resources/META-INF/services`。  

插件初始化接口用户初始化插件，比如插件的心跳等定时任务的启动。  
该接口有两个方法：`init()`用于启动插件初始化，`stop()`用于停止插件。
下面给出插件通过扩展框架线条功能的初始化示例：
```java
public class DemoService implements PluginService {
    @Override
    public void init() {
        System.out.println("[DemoService]-init");
    }

    @Override
    public void stop() {
        System.out.println("[DemoService]-stop");
    }
}
```
