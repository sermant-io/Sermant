# Java-mesh

**Java-mesh**是一个开源的非侵入式增强框架。

## 产品定位

**Java-mesh**基于[ByteBuddy](https://github.com/raphw/byte-buddy)字节码增强技术，使用[JavaAgent](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html)技术对宿主应用进行非侵入式增强。**Java-mesh**的涉及初衷是建立一个非侵入式增强的生态，其核心诉求在于降低非侵入式增强的难度，通过抽象接口、功能整合、插件剥离等手段，达到简化开发、功能即插即用的效果。

## 运行环境

- [HuaweiJDK 1.8](https://gitee.com/openeuler/bishengjdk-8) / [OpenJDK 1.8](https://github.com/openjdk/jdk) / [OracleJDK 1.8](https://www.oracle.com/java/technologies/downloads/)
- [Apache Maven 3](https://maven.apache.org/download.cgi)

## 模块说明

**Java-mesh**包含以下模块：

- [javamesh-agentcore](../javamesh-agentcore): *JavaAgent*相关内容
  - [javamesh-agentcore-core](../javamesh-agentcore/javamesh-agentcore-core): 核心功能模块
  - [javamesh-agentcore-premain](../javamesh-agentcore/javamesh-agentcore-premain): *JavaAgent*入口模块
- [javamesh-backend](../javamesh-backend): 消息发送模块服务端
- [javamesh-package](../javamesh-package): 打包模块  
- [javamesh-samples](../javamesh-samples): 样本模块，内含各种功能的插件及相关附加件

## 打包流程

**Java-mesh**的打包流程大致分为以下步骤：

- *agent*: 编译、打包核心功能和插件
- *example*: 编译、打包核心功能和示例模块(默认不开启)
- *backend*: 编译、打包**Java-mesh**后端模块
- *ext*: 编译、打包插件附带的后端、前端和其他附加件
- *package*: 将以上的打包结果归档为产品包
- *all*: 执行以上全部步骤(默认不开启)

执行以下*maven*命令，对**Java-mesh**工程进行默认打包：

```shell
mvn clean package -Dmaven.test.skip
```

命令执行完毕后，工程目录下将生成一个形如`javamesh-agent-x.x.x`的文件夹和形如`javamesh-agent-x.x.x.tar`的压缩文件，后者为**Java-mesh**的产品包，前者则为产品包解压后的内容。

## 产品目录说明

- *agent*: JavaAgent相关内容
  - *config*: 配置文件目录
    - *bootstrap.properties*: 启动配置
    - *config.properties*: 核心功能配置
    - *plugins.yaml*: 插件配置，配置着需要被加载的插件功能
  - *core/javamesh-agentcore-core-x.x.x.jar*: **Java-mesh**的核心功能包
  - *pluginPackage*: 插件包目录，插件按功能名称分类
    - *xxx*: 任意插件功能
      - *config/config.yaml*: 插件配置文件
      - *plugin*: 插件包目录
      - *service*: 插件服务包目录
  - *javamesh-agent.jar*: JavaAgent入口包
- *server*: 服务器目录，含**Java-mesh**的服务端，插件的服务端和客户端

## 插件开发

如何新增一个插件可以参考[插件模块开发手册](dev-guide/dev_sample.md)，其中涉及添加插件、插件服务及附加件的详细流程。

如何编写一个插件的内容可以参考[插件代码开发手册](dev-guide/dev_plugin.md)，其中涉及大部分开发插件过程中可能遇到场景。

## 相关文档

|文档名称|文档类型|
|:-|:-|
|[插件模块开发手册](dev-guide/dev_sample.md)|开发手册|
|[插件代码开发手册](dev-guide/dev_plugin.md)|开发手册|
|[动态配置服务介绍](dev-guide/service_dynamicconfig.md)|开发手册|
|[心跳服务介绍](dev-guide/service_heartbeat.md)|开发手册|
|[网关服务介绍](dev-guide/service_send.md)|开发手册|
|[核心模块介绍](user-guide/agentcore.md)|使用手册|
|[入口模块介绍](user-guide/entrance.md)|使用手册|
|[后端模块介绍](user-guide/backend.md)|使用手册|
|[流控功能介绍](user-guide/flowcontrol/document.md)|使用手册|
|[流量录制回放功能介绍](user-guide/flowrecord/document.md)|使用手册|
|[全链路压测功能介绍](user-guide/hercules/document.md)|使用手册|
|[online-stresstest功能介绍](user-guide/online-stresstest/document.md)|使用手册|
|[灰度发布功能介绍](user-guide/route/document.md)|使用手册|
|[服务监控功能介绍](user-guide/server-monitor/document.md)|使用手册|
|[threadlocal功能介绍](user-guide/threadlocal/document.md)|使用手册|

[返回**Java-mesh**项目首页](../README.md)
