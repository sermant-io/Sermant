<div align="center">
<p></p><p></p>
<p>
    <img  src="docs/binary-docs/sermant-logo.png" width="50%" syt height="50%">
</p>
<h1>A Proxyless Service Mesh Solution Based on Java Agent</h1>

[简体中文](README-zh.md) | [English](README.md) 

[![Gitter](https://badges.gitter.im/SermantUsers/community.svg)](https://gitter.im/SermantUsers/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![CI/IT Tests](https://github.com/huaweicloud/Sermant/workflows/Java%20CI%20with%20Maven/badge.svg?branch=develop)](https://github.com/huaweicloud/Sermant/actions?query=workflow:Java%20CI%20with%20Maven%20event:push%20branch:develop)
[![codecov](https://codecov.io/gh/huaweicloud/Sermant/develop/graph/badge.svg)](https://codecov.io/gh/huaweicloud/Sermant)
</div>

## What is Sermant?

**Sermant** (also known as Java-mesh) is a proxyless **ser**vice **m**esh technology based on J**a**va Age**nt** . It leverages the [Java Agent](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html) to instrument the host application with enhanced service governance function, in order to solve the service governance problem, in the massive micro-service architecture.

Sermant's purpose also includes building a plugin-development ecosystem to help developers develop the service governance function more easily while not interfering the business code. The Sermant architecture is depicted as follows.

![pic](docs/binary-docs/sermant-product-arch.png)

As described above, Sermant's Java Agent has two layers of functions.

- Framework core layer. The core layer provides Sermant's basic framework capability, in order to ease the plugin development. The function of this layer includes heart beat, data transmit, dynamic configuration, etc.
- Plugin service layer. The plugin provides actual governance service for the application. The developer can either develop simple plugin by directly leveraging framework core service, or can develop complex plugin by developing plugin's own complex service-governance function.

Sermant's Java Agent widely adopts class isolation technology in order to eliminate the class load conflicts between framework code, plugin code, and application code.

A microservice architecture using Sermant has the following three components, which is depicted in the following diagram.

![pic](docs/binary-docs/sermant-rt-arch.png)

- Sermant Java Agent: dynamically instrument the application for the service governance capability.
- Sermant Backend: provide the connection and the pre-processing service for the Java Agents' all uploaded-data.
- Dynamic configuration center: Providing the instructions by dynamically update the config to the listening Java Agent. Dynamic configuration center is not directly provided by Sermant project. The projects currently support servicecomb-kie, etc.


## Quick Start

Below is a simple demo that guides new users to use Sermant in just 4 steps.

### Preparation

- [Download](https://github.com/huaweicloud/Sermant/releases) Sermant package
- [Download](https://github.com/huaweicloud/Sermant-examples/tree/main/flowcontrol-demo/spring-cloud-demo/spring-provider) demo application
- [Download](https://zookeeper.apache.org/releases#download) and start zookeeper

### Compile demo application

Execute the following command in the `${path}/Sermant-examples/flowcontrol-demo/spring-cloud-demo/spring-provider/` directory:

```shell
# windows linux mac
mvn clean package
```

After successful packaging，GET `spring-provider.jar` in `${path}/Sermant-examples/flowcontrol-demo/spring-cloud-demo/spring-provider/target`

> Note: path is the path where the demo application is downloaded

### Modify the Sermant configuration

Modify the `agent.config.serviceBlackList` configuration in the `${path}/sermant-agent-x.x.x/agent/config/config.properties` file to be empty, as shown below:

```properties
agent.config.serviceBlackList=
```

> Note: path is the path where the Sermant package is downloaded

### Start Backend

Execute the following command in the `${path}/sermant-agent-x.x.x/server/sermant` directory:

```shell
java -jar sermant-backend-lite.jar
```

> Note: path is the path where the Sermant package is downloaded

### Start demo application

Execute the following command in the `${path}/Sermant-examples/sermant-template/demo-application/target`directory：

```shell
# linux mac
java -javaagent:${path}/sermant-agent-x.x.x/agent/sermant-agent.jar -jar spring-provider.jar

# windows
java -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar -jar spring-provider.jar
```

> Note: path is the path where the Sermant package is downloaded

### Verification

Check running status of Sermant. In this example, open the browser and navigate to the URL `http://localhost:8900`.

![pic](docs/binary-docs/backend_sermant_info.png)

## More Documents to Follow

Please refer to the  [Sermant Document](https://sermant.io/en/document/)

## License

Sermant adopts [Apache 2.0 License.](/LICENSE)

## How to Contribute

Please read  [Contribute Guide](https://sermant.io/en/document/CONTRIBUTING.html) to refer how to join the contribution.

## Declaration

- [Apache/Servicecomb-java-chassis](https://github.com/apache/servicecomb-java-chassis): Sermant refer the service governance algorithm from Apache Servicecomb project.
- [Apache/Servicecomb-kie](https://github.com/apache/servicecomb-kie): Sermant uses servicecomb-kie as the default dynamic configuration center.
- [Apache/SkyWalking](https://skywalking.apache.org/): The plugin architecture in this project is refered to Apache Skywalking. Part of the framework code in Sermant is built based on Apache Skywalking project as well.
- [Alibaba/Sentinel](https://github.com/alibaba/Sentinel): Sermant's flow-control plugin is built based on Alibaba Sentinel project. 

## Contact Us

* [Gitter](https://gitter.im/SermantUsers/community): Sermant's chat room for community messaging, collaboration and discovery.
* WeChat Group: Please apply for Sermant Xiao Er as a friend first, and will pull you into the group after passing, please note the company + position when applying, thank you.

![sermant](docs/binary-docs/contact-wechat.png)

