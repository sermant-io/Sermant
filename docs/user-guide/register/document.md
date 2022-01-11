# Register-Center

本文主要介绍[注册中心插件](../../../sermant-plugins/sermant-register-center)以及该插件的使用方法

## 功能

注册中心插件提供代码无侵入，让线上应用注册中心快速转接入[Service Center](https://github.com/apache/servicecomb-service-center)的能力，支持Dubbo与SpringCloud框架。
对于SpringCloud框架而言，可让原本注册于Eureka，Nacos，Zookeeper等主流注册中心的微服务，无侵入地注册到Service Center上。
对于Dubbo框架而言，只要引入基础dubbo依赖包，即可无侵入地注册到Service Center。

## 使用说明

### 修改[配置文件](../../../sermant-plugins/sermant-register-center/config/config.yaml)

配置项说明如下:

```yaml
servicecomb.service:
  project: default #命名空间
  application: default #应用名
  version: 0.0.0 #版本号
  environment: development #环境
  address: http://127.0.0.1:30100 #注册中心地址，多个注册中心地址使用逗号隔开
  heartbeatInterval: 15 #服务实例心跳发送间隔（单位：秒）
```

- 对于dubbo应用，还需要修改dubbo本身注册中心地址的配置。这个配置项一般在spring的配置文件中，比如“dubbo/provider.xml”文件中：

```xml
<dubbo:registry address="sc://127.0.0.1:30100"/>
```

也可能在application.yml（或application.properties）中，以application.yml为例：

```yml
dubbo:
  registry:
    address: sc://127.0.0.1:30100
```

需要强调的是，这个配置项的地址信息**不会使用**，只使用了协议名称sc（即ip地址不重要，只需要**sc://** 开头即可）。

### 启动Service Center

Service Center启动流程详见[官网](https://github.com/apache/servicecomb-service-center)，然后启动应用

```shell
java -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=appName
```

其中path需要替换为Sermant实际打包路径，x.x.x需要替换为Sermant实际版本号，appName为agent的启动参数，与注册参数无关。

### 验证

登录[Service Center](http://127.0.0.1:30103/)后台，查看相关服务实例是否已注册，并且访问应用接口，确认接口是否正常返回，若接口成功返回，则说明注册成功。



## 更多文档

- [SpringCloud注册中心迁移](./spring-cloud-register-migiration.md)



[返回**Sermant**说明文档](../../README.md)

