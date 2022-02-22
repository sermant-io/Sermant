

# Register Migration - Dubbo

本文主要介绍 [注册中心插件](../../../sermant-plugins/sermant-register-center) 基于Dubbo框架注册中心的迁移能力

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

### 修改[配置文件](../../../sermant-plugins/sermant-register-center/config/config.yaml)

配置说明见[注册中心插件文档](./document.md#修改配置文件)

基于以上配置，**新增迁移配置**，配置内容如下：

```yaml
servicecomb.service:
  openMigration: true #是否开启迁移功能 若进行注册中心迁移，则需将该值设置为true
```

### 启动Service Center

Service Center启动流程详见[官网](https://github.com/apache/servicecomb-service-center)

### 进行双注册迁移模拟

（1）首先不带agent启动应用，例如有provider与consumer两个实例，启动后确保应用已成功注册到原注册中心且可正常访问；

（2）启动一个新的provider，附加以下JVM参数，带agent一起启动

```shell
java -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=appName
```

其中path需要替换为Sermant实际打包路径，x.x.x需要替换为Sermant实际版本号，appName为agent的启动参数，与注册参数无关。

（3）启动成功后，新的provider实例会同时注册到Service Center与原注册中心，且consumer可以成功访问

（4）关闭旧的provider， 再按照（2）的方式启动新的consumer实例，同样确认新和旧的consumer都可以访问到provider，再停止旧的consumer即可

（5）最后再停止旧的注册中心

[返回**Sermant**说明文档](../../README.md)