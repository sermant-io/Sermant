# Registry Migration - Dubbo

[简体中文](dubbo-registry-migiration-zh.md) | [English](dubbo-registry-migiration.md)

This document describes the migration capability of the [service registration plugin](../../../sermant-plugins/sermant-service-registry) based on the Dubbo framework registration center.

For details about SpringCloud migration, see [SpringCloud Registration Center Migration](spring-cloud-registry-migiration.md).

## Function

Provides the capability of quickly migrating the registration center to the [Service Center](https://github.com/apache/servicecomb-service-center) based on the dual-registration mode without interrupting online services business. The following registration centers are supported:

| Registration Center | Support |
| --------- | -------- |
| Nacos     | ✅        |
| Zookeeper | ✅        |

## Support Versions

Dubbo 2.6.x, 2.7.x

**Schematic diagram of migration**

![agent注册中心迁移-迁移示意图](../../binary-docs/sermant-register-migration-en.png)

## Usage

### Modify [Plugin Configuration File](../../../sermant-plugins/sermant-service-registry/config/config.yaml)

The file path is `${agent_package_path}/agent/pluginPackage/service-registry/config/config.yaml`. Please replace `${agent_package_path}` with the actual package path.

Set servicecomb.service.openMigration and servicecomb.service.enableDubboRegister to true as follows:

```yaml
servicecomb.service:
  openMigration: true #Enable the migration function
  enableDubboRegister: true #Enabling Dubbo plugin registration
```

For details, see the [service registration plugin document](./document.md#Modify-the-plugin-configuration-file-on-demand).

**Notices**: If the migration function is enabled, you do not need to change the address of the original registration center. Otherwise, the registration will not be performed at the same time with two registration centers (original registration center + SC).

### Startup Service Center

For details about the Service Center startup process, see the [official website](https://github.com/apache/servicecomb-service-center).

## Verification

- Notice：In this example, the original registration center is Nacos.
- Prerequisites: [The Sermant is packaged correctly](../../README.md).
- Start Service Center. For details about how to download, use, and start Service Center, see the [official website](https://github.com/apache/servicecomb-service-center).
- Start the Nacos. For details about how to download, use, and start the Nacos, see the [official website](https://nacos.io/zh-cn/docs/quick-start.html).
- Compile [demo application](../../../sermant-plugins/sermant-service-registry/demo-registry/demo-registry-dubbo)

```shell
mvn clean package
```

- Start up dual registration consumer

```shell
# windows
java -Dservicecomb.service.openMigration=true -Dservicecomb.service.enableDubboRegister=true -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=dubbo-consumer -jar dubbo-consumer.jar

# mac, linux
java -Dservicecomb.service.openMigration=true -Dservicecomb.service.enableDubboRegister=true -javaagent:${path}/sermant-agent-x.x.x/agent/sermant-agent.jar=appName=dubbo-consumer -jar dubbo-consumer.jar
```

Notices: To facilitate the test, the dubbo migration function is enabled in -Dservicecomb.service.openMigration=true -Dservicecomb.service.enableDubboRegister=true mode. If the dubbo migration function is enabled in other modes, you do not need to add this parameter.

- Start the original producer (registered with the Nacos).

```shell
# windows
java -jar dubbo-provider.jar

# mac, linux
java -jar dubbo-provider.jar
```

- Starting a New Producer (Registering to the SC)

```shell
# windows
java -Dservicecomb.service.enableDubboRegister=true -Dserver.port=48021 -Ddubbo.protocol.port=48821 -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=dubbo-provider -jar dubbo-provider.jar

# mac, linux
java -Dservicecomb.service.enableDubboRegister=true -Dserver.port=48021 -Ddubbo.protocol.port=48821 -javaagent:${path}/sermant-agent-x.x.x/agent/sermant-agent.jar=appName=dubbo-provider -jar dubbo-provider.jar
```

Notice: To facilitate the test, the Dubbo registration function is enabled in -Dservicecomb.service.enableDubboRegister=true mode. If the Dubbo registration function is enabled in other modes, you do not need to add this parameter. In addition, to solve the port conflict problem when two providers are started on the same server, you need to add the -Dserver.port=48021 -Ddubbo.protocol.port=48821 parameter. If the two providers are on different servers, you do not need to add this parameter.

Please replace `${path}` with the Sermant project path, replace x.x.x with the actual Sermant version number, and appName with the application name in the agent startup parameter, which is irrelevant to the registration parameter. The directory for running the command must be the directory where the JAR package of the demo application is located.

Startup parameters can referring to the [entry module](../entrance.md#Startup-Parameters).

- Test

After the preceding three applications are started, log in to the [Service Center](http://127.0.0.1:30103/) background and [Nacos background](http://127.0.0.1:8848/nacos/index.html#/serviceManagement) to view the consumer and provider applications. Access the application interface http://localhost:28020/test for multiple times and check whether the interface returns a normal response. If the interface successfully returns a response and the producer instances of the two registration centers can be accessed, indiactes that the registration and migration are successful.

[Back to **Service Registration**](./document.md)

[Back to README of **Sermant** ](../../README.md)