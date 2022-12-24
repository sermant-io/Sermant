<div align="center">
<p></p><p></p>
<p>
    <img  src="docs/binary-docs/sermant-logo.png" width="50%" syt height="50%">
</p>
<h1>基于Java Agent的无代理服务网格解决方案</h1>

[简体中文](README-zh.md) | [English](README.md) 

[![Gitter](https://badges.gitter.im/SermantUsers/community.svg)](https://gitter.im/SermantUsers/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![CI/IT Tests](https://github.com/huaweicloud/Sermant/workflows/Java%20CI%20with%20Maven/badge.svg?branch=develop)](https://github.com/huaweicloud/Sermant/actions?query=workflow:Java%20CI%20with%20Maven%20event:push%20branch:develop)
[![codecov](https://codecov.io/gh/huaweicloud/Sermant/develop/graph/badge.svg)](https://codecov.io/gh/huaweicloud/Sermant)

</div>

## Sermant

**Sermant**(也称之为Java-mesh)是基于Java Agent无代理的服务网格技术。其利用JavaAgent为宿主应用程序提供增强的服务治理功能，以解决大规模微服务体系结构中的服务治理问题。

Sermant的愿景还包括构建插件开发生态系统，以帮助开发人员更容易地开发服务治理功能，同时不干扰业务代码。Sermant架构描述如下。

![pic](docs/binary-docs/sermant-product-arch.png)

根据上图，Sermant中Java Agent包含两层功能。

- 框架核心层。核心层提供Sermant的基本框架功能，以简化插件开发。该层的功能包括心跳、数据传输、动态配置等。
- 插件服务层。插件为宿主应用提供实际的治理服务。开发者可以直接利用框架核心服务开发简单插件，也可以开发插件自身的复杂服务治理功能来开发复杂插件。

Sermant中的Java Agent广泛采用类隔离技术，以消除框架代码、插件代码和宿主应用程序代码之间的类加载冲突。

使用Sermant的微服务架构具有以下三个组件，如下图所示。

![pic](docs/binary-docs/sermant-rt-arch.png)

- Sermant Java Agent：动态地为宿主应用程序提供服务治理能力。
- Sermant Backend：为Java Agent的上传数据提供连接和预处理服务。
- Dynamic configuration center：通过动态更新监听的Java Agent的配置来提供指令。Sermant项目不直接提供动态配置中心。这些项目目前支持servicecomb-kie等。

## 快速开始

### 下载或编译

点击[此处](https://github.com/huaweicloud/Sermant/releases)下载**Sermant**二进制包。如果您想自己编译项目，请遵循以下步骤。

执行*maven*命令来打包**Sermant**项目的 [demo module](sermant-example)。

```shell
mvn clean package -Dmaven.test.skip -Pexample
```

### 启动Sermant

准备和启动zookeeper，启动 **Sermant** demo 应用：

```shell
# Run under Linux
java -cp sermant-example/demo-application/target/demo-application.jar \
  -javaagent:sermant-agent-x.x.x/agent/sermant-agent.jar=appName=test \
  com.huawei.example.demo.DemoApplication
```

```shell
# Run under Windows
java -cp sermant-example\demo-application\target\demo-application.jar ^
  -javaagent:sermant-agent-x.x.x\agent\sermant-agent.jar=appName=test ^
  com.huawei.example.demo.DemoApplication
```

检查**Sermant**的运行状态。在本例中，打开浏览器并导航到URL“http://localhost:8900".

![pic](docs/binary-docs/backend_sermant_info.png)

## 更多文档

请参阅 [Sermant文档](https://sermant.io/zh/document/)

## License

Sermant 采用 [Apache 2.0 License.](/LICENSE)

## 贡献指南

请阅读[贡献指南](https://sermant.io/zh/document/CONTRIBUTING.html)以了解如何贡献项目。

## 声明

- [Apache/Servicecomb-java-chassis](https://github.com/apache/servicecomb-java-chassis)：Sermant引用了Apache Servicecomb项目中的服务治理算法。
- [Apache/Servicecomb-kie](https://github.com/apache/servicecomb-kie): Sermant使用servicecomb-kie作为默认的动态配置中心。
- [Apache/SkyWalking](https://skywalking.apache.org/): 本项目中的插件架构参考了Apache Skywalking。Sermant中的部分框架代码是基于Apache Skywalking项目构建的。
- [Alibaba/Sentinel](https://github.com/alibaba/Sentinel): Sermant的流量控制插件是基于阿里巴巴Sentinel项目构建的。

## 联系我们

* [Gitter](https://gitter.im/SermantUsers/community)：Sermant社区的聊天室。
* 微信交流群：请先申请Sermant小二为好友，通过后会拉您进群，申请时请备注公司+职务，谢谢。

![pic](docs/binary-docs/contact-wechat.png)
