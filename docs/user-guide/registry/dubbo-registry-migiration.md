# Registry Migration - Dubbo

本文主要介绍[服务注册插件](../../../sermant-plugins/sermant-service-registry)基于Dubbo框架注册中心的迁移能力。

SpringCloud迁移见[SpringCloud注册中心迁移](spring-cloud-registry-migiration.md)

## 功能

提供代码无侵入方式，基于双注册的模式让线上应用在线上业务不停机的前提下将注册中心快速迁移到[Service Center](https://github.com/apache/servicecomb-service-center)的能力。支持注册中心如下：

| 注册中心   | 是否支持 |
| --------- | -------- |
| Nacos     | ✅        |
| Zookeeper | ✅        |

**支持版本**

Dubbo 2.6.x, 2.7.x

**搬迁示意图**

![agent注册中心迁移-迁移示意图](../../binary-docs/sermant-register-migration.png)

## 使用说明

### 修改[插件配置文件](../../../sermant-plugins/sermant-service-registry/config/config.yaml)

文件路径为：${agent_package_path}/agent/pluginPackage/service-registry/config/config.yaml，其中${agent_package_path}需要替换为实际的打包路径。

将servicecomb.service.openMigration与servicecomb.service.enableDubboRegister的值设置为true，如下所示：

```yaml
servicecomb.service:
  openMigration: true #开启迁移功能
  enableDubboRegister: true #开启dubbo插件注册
```

详细配置说明见[服务注册插件文档](./document.md#按需修改插件配置文件)

**注意：如果开启了迁移功能，则无需修改原有的注册中心地址，否则将不会同时向2个注册中心（原注册中心+sc）进行注册。**

### 启动Service Center

Service Center启动流程详见[官网](https://github.com/apache/servicecomb-service-center)

## 结果验证

- 说明：此处以原注册中心为Nacos进行举例。

- 前提条件[正确打包Sermant](../../README.md)。

- 启动Service Center，下载、使用说明和启动流程详见[官网](https://github.com/apache/servicecomb-service-center)。

- 启动Nacos，下载、使用说明和启动流程详见[官网](https://nacos.io/zh-cn/docs/quick-start.html)。

- 编译[demo应用](../../../sermant-plugins/sermant-service-registry/demo-registry/demo-registry-dubbo)。

```shell
mvn clean package
```

- 启动双注册消费者

```shell
# windows
java -Dservicecomb.service.openMigration=true -Dservicecomb.service.enableDubboRegister=true -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=dubbo-consumer -jar dubbo-consumer.jar

# mac, linux
java -Dservicecomb.service.openMigration=true -Dservicecomb.service.enableDubboRegister=true -javaagent:${path}/sermant-agent-x.x.x/agent/sermant-agent.jar=appName=dubbo-consumer -jar dubbo-consumer.jar
```

注：为了便于测试，这里使用了-Dservicecomb.service.openMigration=true -Dservicecomb.service.enableDubboRegister=true的方式打开了dubbo迁移功能，如果使用了其它的方式打开了dubbo迁移开关，则无需添加该参数。

- 启动原生产者（注册到Nacos中）

```shell
# windows
java -jar dubbo-provider.jar

# mac, linux
java -jar dubbo-provider.jar
```

- 启动新生产者（注册到SC中）

```shell
# windows
java -Dservicecomb.service.enableDubboRegister=true -Dserver.port=48021 -Ddubbo.protocol.port=48821 -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=dubbo-provider -jar dubbo-provider.jar

# mac, linux
java -Dservicecomb.service.enableDubboRegister=true -Dserver.port=48021 -Ddubbo.protocol.port=48821 -javaagent:${path}/sermant-agent-x.x.x/agent/sermant-agent.jar=appName=dubbo-provider -jar dubbo-provider.jar
```

注：为了便于测试，这里使用了-Dservicecomb.service.enableDubboRegister=true的方式打开了dubbo注册开关，如果使用了其它的方式打开了dubbo注册开关，则无需添加该参数；另外为了解决同一台服务器启动2个provider遇到的端口冲突问题，需要增加-Dserver.port=48021 -Ddubbo.protocol.port=48821参数，如果测试时2个provider在不同的服务器，则无需添加该参数。

其中${path}需要替换为Sermant工程路径，x.x.x需要替换为Sermant实际版本号，appName为agent启动参数中的应用名，与注册参数无关，执行命令的目录需要为demo应用的jar包所在的目录。

启动参数的具体意义见[入口模块](../entrance.md#启动参数)。

- 测试

当启动以上3个应用后，登录[Service Center](http://127.0.0.1:30103/)后台和[Nacos](http://127.0.0.1:8848/nacos/index.html#/serviceManagement)后台，均可查看到consumer和provider应用，并且多次访问应用接口<http://localhost:28020/test>，确认接口是否正常返回，若接口成功返回并可访问2个注册中心的生产者实例，则说明注册并迁移成功。

[返回**服务注册插件**说明文档](./document.md)