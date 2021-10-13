# JavaMesh

## 概述

JavaMesh是一个基于ByteBuddy字节码技术开发的javaagent框架;框架当前提供了流量控制，流量录制插件;基于JavaMesh,只需实现少量的接口即可快速开发自己需要实现的agent功能;框架提供了基于netty的统一消息发送模块;只需部署netty-server服务，即可实现心跳或数据的传输,同时支持自定义消息类型。

## 模块说明

bootstrap: 公共模块  
IntegratedService: 消息发送模块服务端  
core: 核心模块  
integration:  消息发送模块客户端  
packaging: 打包模块  
premain: 启动入口模块

## [示例插件](example/demo-plugin)

- 示例插件中拦截了org.springframework.boot.autoconfigure.SpringBootApplication注解

## [示例插件拦截的应用](example/demo-application)

## 快速开始

### 环境安装

- [jdk](https://www.oracle.com/java/technologies/downloads/)
- [maven](https://maven.apache.org/download.cgi)
- [idea](https://www.jetbrains.com/idea/)

### 编译出包

- 下载`JavaMesh`源码,用`idea`打开
- 在`File | Settings | Build, Execution, Deployment | Build Tools | Maven`中配置`maven`信息
- 在`idea`中执行`mvn clean package`
- 编译结果文件:`JavaMesh\lubanops-apm-javaagent-packaging\target\apm-javaagent-2.0.5.tar`

### 运行

#### 终端

- 打包[示例插件拦截的应用](example/demo-application)
-

执行`java -javaagent:${JavaMesh}\lubanops-apm-javaagent-packaging\target\apm-javaagent-2.0.5\apm-javaagent\apm-javaagent.jar=appName=demo -jar .\DemoApplication-0.0.1 -SNAPSHOT.jar`
,`${JavaMesh}`是框架项目路径

#### IDEA

- IDEA挂载JavaMesh,需在应用`Run Configuration -> VM options`
  加入`-javaagent:${JavaMesh}\lubanops-apm-javaagent-packaging\target\apm-javaagent-2.0.5\apm-javaagent\apm-javaagent.jar=appName=Demo`
  即可,其中`${JavaMesh}`是框架项目路径。
- 运行[应用](example/demo-application/src/main/java/com/lubanops/demo/DemoApplication.java)

## 插件开发
框架采用SPI机制进行插件的加载，插件的开发需要在resources/META-INF/service创建相应的文件(文件名与实现接口的全限定名一致)
### [增强类接口](lubanops-apm-javaagent-bootstrap/src/main/java/com/huawei/apm/bootstrap/definition/EnhanceDefinition.java)
- [示例](example/demo-plugin/src/main/resources/META-INF/services/com.huawei.apm.bootstrap.definition.EnhanceDefinition)
- 获取待增强的目标类(enhanceClass)支持单个类名，多个类名，注解，前缀匹配需要增强的类
- 获取封装了待增强目标方法和其拦截器的(MethodInterceptPoint)接口中，匹配增强方法支持单个方法名，多个方法名，前缀，后缀，包含等匹配方法
### [拦截器接口](lubanops-apm-javaagent-bootstrap/src/main/java/com/huawei/apm/bootstrap/interceptors/Interceptor.java)
- [静态拦截器](lubanops-apm-javaagent-bootstrap/src/main/java/com/huawei/apm/bootstrap/interceptors/StaticMethodInterceptor.java)
- [示例拦截器](lubanops-apm-javaagent-bootstrap/src/main/java/com/huawei/apm/bootstrap/interceptors/InstanceMethodInterceptor.java)
- [构造拦截器](lubanops-apm-javaagent-bootstrap/src/main/java/com/huawei/apm/bootstrap/interceptors/ConstructorInterceptor.java)
### [插件配置接口](lubanops-apm-javaagent-bootstrap/src/main/java/com/huawei/apm/bootstrap/config/BaseConfig.java)
- [示例](example/demo-plugin/src/main/resources/META-INF/services/com.huawei.apm.bootstrap.config.BaseConfig)
### [插件初始化接口](lubanops-apm-javaagent-bootstrap/src/main/java/com/huawei/apm/bootstrap/boot/PluginService.java)
- [示例](example/demo-plugin/src/main/resources/META-INF/services/com.huawei.apm.bootstrap.boot.PluginService)