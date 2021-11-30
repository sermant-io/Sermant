<img src="docs/binary-docs/java-mesh-logo.png" width="30%" syt height="530%" />

### 一种基于 Javaagent 技术的 Service Mesh 解决方案
[![Gitter](https://badges.gitter.im/JavaMeshUsers/community.svg)](https://gitter.im/JavaMeshUsers/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![CI/IT Tests](https://github.com/huaweicloud/java-mesh/workflows/Java%20CI%20with%20Maven/badge.svg?branch=develop)](https://github.com/huaweicloud/java-mesh/actions?query=workflow:Java%20CI%20with%20Maven%20event:push%20branch:develop)
## Java-mesh 是什么

**Java-mesh** 基于Java的字节码增强技术，通过 [JavaAgent](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html) 对宿主应用进行非侵入式增强，以解决Java应用的微服务治理问题。**JavaMesh**的初衷是建立一个面向微服务治理的对开发态无侵入的解决方案生态，降低服务治理开发和使用的难度，通过抽象接口、功能整合、插件隔离等手段，达到简化开发、功能即插即用的效果。其产品架构图如下图所示。

![pic](docs/binary-docs/java-mesh-product-arch.png)

如上图所示，Java-mesh 的 Javaagent 主要由两部分组成。

- 核心服务框架层，提供 Java-mesh 基本的框架服务能力，以方便服务治理插件开发者开发插件。主要功能包括 动态配置服务，消息发送服务，心跳服务，等。
- 服务治理插件层，以插件方式提供服务治理能力。插件开发者需要开发 既可以基于框架服务和 Java 字节码增强功能开发出各类服务治理功能，也可以在插件中自行实行核心的治理能力，以满足特定服务治理的场景需求。

Java-mesh 的 Javaagent 广泛采用类隔离技术，以保证服务治理层、框架服务层、以及用户的业务应用互相不干扰，杜绝Java类冲突问题。其技术原理如下图所示。

在使用 Java-mesh 的微服务架构下，和 Java-mesh 架构相关的组件主要有三个，相关架构图如下图所示：

![pic](docs/binary-docs/java-mesh-rt-arch.png)

- Java-mesh Javaagent: 动态对业务应用进行字节码增强，以满足服务治理场景需求。
- Java-mesh Backend：对 Javaagent 提供长连接端口，处理各类心跳、数据业务信息，并可以以消息方式转发给相关服务后台。
- 动态配置中心：通过动态配置Javaagent进行指令下发，以控制Javaagent的服务治理行为。动态配置中心在 java-mesh 中不提供单独实现，目前支持开源软件如 servicecomb-kie, zookeeper, 等。


## 示例工程快速开始

### 下载或编译

可通过[这里](https://github.com/huaweicloud/java-mesh/releases)下载**JavaMesh**的产品包。如果希望自行编译，请参考以下步骤。

执行以下*maven*命令，对**JavaMesh**工程的[示例模块](javamesh-plugins/javamesh-example)进行打包：

```shell
mvn clean package -Dmaven.test.skip -Pexample
```

执行以下*maven*命令，对**JavaMesh**工程的[后端模块](javamesh-plugins/javamesh-backend)进行打包：

```shell
mvn clean package -Dmaven.test.skip -Pbackend
```

### 启动Java-mesh

启动**JavaMesh**后端：

```shell
# Linux下执行
java -jar javamesh-agent-x.x.x/server/javamesh/javamesh-backend-x.x.x.jar
```

```bat
:: Windows下执行
java -jar javamesh-agent-x.x.x\server\javamesh\javamesh-backend-x.x.x.jar
```

运行**JavaMesh**示例工程：

```shell
# Linux下执行
java -cp javamesh-plugins/javamesh-example/demo-application/target/demo-application.jar \
  -javaagent:javamesh-agent-x.x.x/agent/javamesh-agent.jar=appName=test \
  com.huawei.example.demo.DemoApplication
```

```bat
:: Windows下执行
java -cp ..\javamesh-plugins\javamesh-example\demo-application\target\demo-application.jar ^
  -javaagent:javamesh-agent-x.x.x\agent\javamesh-agent.jar=appName=test ^
  com.huawei.example.demo.DemoApplication
```


## 其他更多文档参考

请参考 [开发文档](docs/README.md)

## 许可证

Java-mesh 采用 [Apache 2.0 License.](/LICENSE) 许可证。


## 如何贡献

请阅读 [贡献指南](CONTRIBUTING.md) 来参考如何加入 Java-mesh 社区进行贡献。

## 其他相关项目

- [apache/servicecomb-java-chassis](https://github.com/apache/servicecomb-java-chassis): Java-mesh相关限流降级、灰度发布的算法实现参考了servicecomb的开源实现。
- [apache/servicecomb-kie](https://github.com/apache/servicecomb-kie): Java-mesh 支持采用 servicecomb-kie 来作为后端的分布式动态配置中心。

