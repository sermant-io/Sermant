# Register-Center

本文主要介绍[注册中心插件](../../../sermant-plugins/sermant-register-center)以及该插件的使用方法

## 功能

注册中心插件提供代码无侵入，让线上应用注册中心快速转接入[Service Center](https://github.com/apache/servicecomb-service-center)的能力，支持Dubbo与SpringCloud框架。
对于SpringCloud框架而言，可让原本注册于Eureka，Nacos，Zookeeper等主流注册中心的微服务，无侵入地注册到Service Center上。
对于Dubbo框架而言，只要引入基础dubbo依赖包，即可无侵入地注册到Service Center。

## 使用说明

### 修改[核心配置文件](../../../sermant-agentcore/sermant-agentcore-core/config/config.properties)

文件路径为：${agent_package_path}/agent/config/config.properties，其中${agent_package_path}需要替换为实际的打包路径。

配置项说明如下:

```properties
#应用名
service.meta.application=${sermant.agent.service.meta.application:default}
#版本号
service.meta.version=${sermant.agent.service.meta.version:1.0.0}
#命名空间
service.meta.project=${sermant.agent.service.meta.project:default}
#环境
service.meta.environment=${sermant.agent.service.meta.environment:development}
```

### 修改[插件配置文件](../../../sermant-plugins/sermant-register-center/config/config.yaml)

文件路径为：${agent_package_path}/agent/pluginPackage/register-center/config/config.yaml，其中${agent_package_path}需要替换为实际的打包路径。

配置项说明如下:

```yaml
servicecomb.service:
  address: http://127.0.0.1:30100 #注册中心地址，多个注册中心地址使用逗号分隔
  heartbeatInterval: 15 #服务实例心跳发送间隔（单位：秒）
  openMigration: false #是否开启迁移功能
  enableSpringRegister: false #是否开启spring插件注册能力，spring cloud框架需开启，dubbo框架需关闭
  sslEnabled: false # 是否开启ssl
```

- 对于dubbo应用，在**没有开启迁移功能**的前提下还需要修改dubbo本身注册中心地址的配置。这个配置项一般在dubbo应用的配置文件中，比如“dubbo/provider.xml”文件中：

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

**注意：如果开启了迁移功能，则无需修改原有的注册中心地址，否则将不会同时向2个注册中心（原注册中心+sc）进行注册。**

## 结果验证

- 前提条件[正确打包Sermant](../../README.md)

- 启动Service Center，下载、使用说明和启动流程详见[官网](https://github.com/apache/servicecomb-service-center)

- 编译[demo应用](../../../sermant-plugins/sermant-register-center/demo-register/demo-register-dubbo)

```shell
mvn clean package
```

- 启动消费者

```shell
# windows
java -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=dubbo-consumer -jar dubbo-consumer.jar

# mac, linux
java -javaagent:${path}/sermant-agent-x.x.x/agent/sermant-agent.jar=appName=dubbo-consumer -jar dubbo-consumer.jar
```

- 启动生产者

```shell
# windows
java -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=dubbo-provider -jar dubbo-provider.jar

# mac, linux
java -javaagent:${path}/sermant-agent-x.x.x/agent/sermant-agent.jar=appName=dubbo-provider -jar dubbo-provider.jar
```

其中${path}需要替换为Sermant工程路径，x.x.x需要替换为Sermant实际版本号，appName为agent启动参数中的应用名，与注册参数无关，执行命令的目录需要为demo应用的jar包目录。

启动参数的具体意义见[入口模块](../entrance.md#启动参数)。

- 测试

当启动以上2个应用后，登录[Service Center](http://127.0.0.1:30103/)后台，查看相关服务实例是否已注册，并且访问应用接口<http://localhost:28020/test>，确认接口是否正常返回，若接口成功返回，则说明注册成功。

## 配置说明

**核心配置文件**与**插件配置文件**均支持环境变量、java -D参数配置），如下所示：

```properties
service.meta.application=${sermant.agent.service.meta.application:default}
```

以上配置代表优选读取环境变量或-D参数中sermant.agent.service.meta.application的值作为应用名，如果环境变量或-D参数中找不到这个值，则把default作为应用名。

## 更多文档

- [SpringCloud注册中心迁移](./spring-cloud-register-migiration.md)

- [Dubbo注册中心迁移](./dubbo-register-migiration.md)

[返回**Sermant**说明文档](../../README.md)