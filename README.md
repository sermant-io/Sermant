<img src="docs/binary-docs/sermant-logo.png" width="25%" syt height="25%" />

### A proxyless service mesh solution based on Javaagent

[![Gitter](https://badges.gitter.im/SermantUsers/community.svg)](https://gitter.im/SermantUsers/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![CI/IT Tests](https://github.com/huaweicloud/Sermant/workflows/Java%20CI%20with%20Maven/badge.svg?branch=develop)](https://github.com/huaweicloud/Sermant/actions?query=workflow:Java%20CI%20with%20Maven%20event:push%20branch:develop)
## What is Sermant

**Sermant** (also known as Java-mesh) is a proxyless **ser**vice **m**esh technology based on J**a**vaAge**nt** . It leverages the [JavaAgent](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html) to instrument the host application with enhanced service governance function, in order to solve the service governance problem, in the massive micro-service architecture.

Sermant's purpose also includes building a plugin-development ecosystem to help developers develop the service governance function more easily while not interfering the business code. The Sermant architecture is depicted as follows.

![pic](docs/binary-docs/sermant-product-arch.png)

As described above, Sermant's Javaagent has two layers of functions.

- Framework core layer. The core layer provides Sermant's basic framework capability, in order to ease the plugin development. The function of this layer includes heart beat, data transmit, dynamic configuration, etc.
- Plugin service layer. The plugin provides actual governance service for the application. The developer can either develop simple plugin by directly leveraging framework core service, or can develop complex plugin by developing plugin's own complex service-governance function.

Sermant's Javaagent widely adopts class isolation technology in order to eliminate the class load conflicts between framework code, plugin code, and application code.

A microservice architecture using Sermant has the following three components, which is depicted in the following diagram.

![pic](docs/binary-docs/sermant-rt-arch.png)

- Sermant Javaagent: dynamically instrument the application for the service governance capability.
- Sermant Backend: provide the connection and the pre-processing service for the Javaagents' all uploaded-data.
- Dynamic configuration center: Providing the instructions by dynamically update the config to the listening Javaagent. Dynamic configuration center is not directly provided by Sermant project. The projects currently support servicecomb-kie, etc.


## Quick start

### Download or compile

Click [here](https://github.com/huaweicloud/Sermant/releases) to download **Sermant** binary package. If you will to compile the project yourself, please follow the following steps.

Execute *maven* command to package the **Sermant** project's [demo module](sermant-plugins/sermant-example).

```shell
mvn clean package -Dmaven.test.skip -Pexample
```

### Start Sermant

Start **Sermant** backend, **Prepare zookeeper**.

```shell
# Run under Linux
java -jar sermant-agent-x.x.x/server/sermant/sermant-backend-x.x.x.jar
```

```shell
# Run under Windows
java -jar sermant-agent-x.x.x\server\sermant\sermant-backend-x.x.x.jar
```

Start **Sermant** demo project: 

```shell
# Run under Linux
java -cp sermant-plugins/sermant-example/demo-application/target/demo-application.jar \
  -javaagent:sermant-agent-x.x.x/agent/sermant-agent.jar=appName=test \
  com.huawei.example.demo.DemoApplication
```

```shell
# Run under Windows
java -cp sermant-plugins\sermant-example\demo-application\target\demo-application.jar ^
  -javaagent:sermant-agent-x.x.x\agent\sermant-agent.jar=appName=test ^
  com.huawei.example.demo.DemoApplication
```
Check running status of Sermant. In this example, open the browser and navigate to the URL "http://localhost:8900".

![pic](docs/binary-docs/backend_sermant_info.png)

#### Please refer to the  [Service Register & Discovery Plugin QuickStart](docs/QuickStart.md) for details.

## More documents to follow

Please refer to the  [Development Guide](docs/README.md)

## License

Sermant adopts [Apache 2.0 License.](/LICENSE)

## How to contribute

Please read  [Contribute Guide](CONTRIBUTING.md) to refer how to jion the contribution.

## Declaration

- [Apache/Servicecomb-java-chassis](https://github.com/apache/servicecomb-java-chassis): Sermant refer the service governance algorithm from Apache Servicecomb project.
- [Apache/Servicecomb-kie](https://github.com/apache/servicecomb-kie): Sermant uses servicecomb-kie as the default dynamic configuration center.
- [Apache/SkyWalking](https://skywalking.apache.org/): The plugin architecture in this project is refered to Apache Skywalking. Part of the framework code in Sermant is built based on Apache Skywalking project as well.
- [Alibaba/Sentinel](https://github.com/alibaba/Sentinel): Sermant's flow-control plugin is built based on Alibaba Sentinel project. 

