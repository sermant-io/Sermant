# Graceful Online/Offline

[简体中文](document-zh.md) | [English](document.md)

The document is used to introduce the usage of graceful online/offline,  it is currently integrated in the [Service Registration Plugin](../../../sermant-plugins/sermant-service-registry), but can be used independently.

## Functions

Operations such as restarting, upgrading, and capacity expansion of applications cannot be avoided. During these operations, the following problems may occur:

- Due to heavy traffic, the instance is accessed by a large amount of traffic during initialization. As a result, requests may be blocked or even broken down. For example, lazy loading.
- When an instance goes offline, the upstream application cannot be notified in real time due to it's refresh mechanism which need refresh instances at schedule time. As a result, traffic is lost or incorrect.

To solve the preceding problems, graceful online/offline is developed. To solve the preceding problems, the plug-in provides the **warm up** and **graceful offline** capabilities to protect against the preceding problems.

**Warm up**, as the name suggests, uses a small amount of traffic to access the instance first, and gradually increases the traffic based on time to ensure that the newly started instance can successfully transition.

**Graceful offline**,  The plugin quickly updates the upstream cache based on the **real-time notification** + **cache update mechanism**. In addition, traffic statistics are collected to ensure that the instances that are about to go offline can process traffic as much as possible, preventing traffic loss to the greatest extent.

## Support Versions

Currently, the graceful online/offline capability **supports only SpringCloud applications**. Ensure that the SpringCloud version is `Edgware.SR2` or later.

Regitry Center Support：Zookeeper、Consul、Naocs、Eureka、Service Center

**Notice**：The graceful online/offline capability is developed based on the default load balancing capability of SpringCloud. If you have implemented the custom load balancing capability, this capability is no longer applicable.

## Usage

### Enabling Graceful Online and Offline

To use the graceful Online and Offline capability, you need to enable the capability. For details, see the [configuration file](../../../sermant-plugins/sermant-service-registry/config/config.yaml). If you have packed the Sermant or download Release package, the configuration file path is `${agent path}/agent/pluginPackge/service-registry/config/config.yaml` 

```yaml
grace.rule:
  enableSpring: true # SpringCloud graceful online/offline switch
  startDelayTime: 0  # Graceful online/offline start delay, unit is seconds
  enableWarmUp: true # Whether to enable warm up
  warmUpTime: 120    # Warm up time unit is seconds
  enableGraceShutdown: true # Whether to enable graceful offline
  shutdownWaitTime: 30  # The maximum waiting time before traffic detection is disabled. Unit: s. This parameter takes effect only after enabledGraceShutdown is enabled.
  enableOfflineNotify: true # Whether to enable proactive offline notification.
  httpServerPort: 16688 # Enable the http server port for proactive offline notification.
  upstreamAddressMaxSize: 500 # Default size of the cache upstream address
  upstreamAddressExpiredTime: 60 # Expiration time of the cached upstream address
```

In addition to the preceding configuration file mode, you can also configure environment variables or system parameters. For example, you can set the environment variable `-Dgrace.rule.enableSpring=true to enable` for configuration 'enableSpring'.

### Use Graceful Online and Offline

#### Virtual Machine Scenario

Based your OS, start application with agent, in addition configure  configuration which to enable graceful online/offline.

```shell
# windows
java -Dgrace.rule.enableSpring=true -Dgrace.rule.enableWarmUp=true -Dgrace.rule.enableGraceShutdown=true -Dgrace.rule.enableOfflineNotify=true -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=default -jar xxx.jar

# mac, linux
java -Dgrace.rule.enableSpring=true -Dgrace.rule.enableWarmUp=true -Dgrace.rule.enableGraceShutdown=true -Dgrace.rule.enableOfflineNotify=true -javaagent:${path}/sermant-agent-x.x.x/agent/sermant-agent.jar=appName=default -jar xxx.jar
```

Description of relevant identifiers:

- ${path}: path indicates the actual agent path. Please replace it with the actual path.
- sermant-agent-x.x.x： x.x.x need be replaced  with the actual Sermant version, for example, 1.0.0.
- xxx.jar: is your app package. Please replace it with your app package name.

#### Container Scenario

In container scenarios, you can quickly start using the `Sermant-Injector`. For details, see [Container-based Deployment](../injector.md).

In addition, you need to modify the [deployment orchestration file](../injector.md#Deployment) by adding environment variables for graceful login and logout. The modified orchestration file is as follows:

```yaml
apiVersion: v1
kind: Deployment
metadata:
  name: demo-test
  labels:
    app: demo-test
spec:
  replicas: 1
  selector:
    app: demo-test
    matchLabels:
      app: demo-test
  template:
    metadata:
      labels:
        app: demo-test
        sermant-injection: enabled
    spec:
      containers:
      - name: image
        # Please replace it with own image
        image: image:1.0.0
        ports: 
        - containerPort: 8080
        env:
        - name: "grace_rule_enableSpring"
          value: "true"
        - name: "grace.rule.enableWarmUp"
          value: "true"
        - name: "grace_rule_enableGraceShutdown"
          value: "true"
        - name: "grace.rule.enableOfflineNotify"
          value: "true"
  ports:
    - port: 443
      targetPort: 8443
```

Then start application according to the containerization process .

## Quick Start

The following uses a simple demo to demonstrate how to verify graceful online/offline in a VM scenario.

1. Environment Preparation

   （1）Download the JDK and configure the JDK environment. The JDK version must be 1.8 or later. Download the Maven and configure the Maven environment.

   （2）Download the latest Sermant release package. Click [here](https://github.com/huaweicloud/Sermant/releases) to download it.

   （3）Download the [Demo Source Code](https://github.com/huaweicloud/Sermant/tree/develop/sermant-example/demo-grace/spring-grace-nacos-demo)

   （4）Compile Demo

   ​	Run the following command to package the demo:

   ​	`mvn clean package`

   （5）Download nacos and start

   （6）Download and start the ZooKeeper ( the sermant configuration center).

2. Deploy

   We will deploy one consumer instance, two provider instances, and one data instance, as shown in the following figure:

   `consumer  ----------->  provider(two instances)  ------------->  data`

   The graceful online/offline are enabled for the consumer, the warm up and graceful offline are enabled for one provider instance, and only the graceful offline are enabled for the other provider instance.

   （1）Startup data

   ```shell
   java -Dspring.cloud.nacos.discovery.server-addr=localhost:8848 -jar nacos-rest-data-2.2.0.RELEASE.jar
   ```

   （2）Start the first provider instance (port is 8880, with the **warm up  disabled**).

   ```shell
   # windows
   java -Dgrace.rule.enableSpring=true -Dgrace.rule.enableWarmUp=false -Dgrace.rule.enableGraceShutdown=true -Dgrace.rule.enableOfflineNotify=true -Dgrace.rule.httpServerPort=16688 -Dspring.cloud.nacos.discovery.server-addr=localhost:8848 -Dserver.port=8880 -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=default -jar nacos-rest-provider-2.2.0.RELEASE.jar
   
   # mac, linux
   java -Dgrace.rule.enableSpring=true -Dgrace.rule.enableWarmUp=false -Dgrace.rule.enableGraceShutdown=true -Dgrace.rule.enableOfflineNotify=true -Dgrace.rule.httpServerPort=16688 -Dspring.cloud.nacos.discovery.server-addr=localhost:8848 -Dserver.port=8880 -javaagent:${path}/sermant-agent-x.x.x/agent/sermant-agent.jar=appName=default -jar nacos-rest-provider-2.2.0.RELEASE.jar
   ```

   （3）Start the second provider instance (port is 8890,  with the **warm up  enabled**).

   ```shell
   # windows
   java -Dgrace.rule.enableSpring=true -Dgrace.rule.enableWarmUp=true -Dgrace.rule.enableGraceShutdown=true -Dgrace.rule.enableOfflineNotify=true -Dgrace.rule.httpServerPort=16689 -Dspring.cloud.nacos.discovery.server-addr=localhost:8848 -Dserver.port=8890 -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=default -jar nacos-rest-provider-2.2.0.RELEASE.jar
   
   # mac, linux
   java -Dgrace.rule.enableSpring=true -Dgrace.rule.enableWarmUp=true -Dgrace.rule.enableGraceShutdown=true -Dgrace.rule.enableOfflineNotify=true -Dgrace.rule.httpServerPort=16689 -Dspring.cloud.nacos.discovery.server-addr=localhost:8848 -Dserver.port=8890 -javaagent:${path}/sermant-agent-x.x.x/agent/sermant-agent.jar=appName=default -jar nacos-rest-provider-2.2.0.RELEASE.jar
   ```

   （4）Startup consumer

   ```shell
   # windows
   java -Dgrace.rule.enableSpring=true -Dgrace.rule.enableWarmUp=true -Dgrace.rule.enableGraceShutdown=true -Dgrace.rule.enableOfflineNotify=true -Dgrace.rule.httpServerPort=16690 -Dspring.cloud.nacos.discovery.server-addr=localhost:8848 -Dserver.port=8800 -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=default -jar nacos-rest-consumer-2.2.0.RELEASE.jar
   
   # mac, linux
   java -Dgrace.rule.enableSpring=true -Dgrace.rule.enableWarmUp=true -Dgrace.rule.enableGraceShutdown=true -Dgrace.rule.enableOfflineNotify=true -Dgrace.rule.httpServerPort=16690 -Dspring.cloud.nacos.discovery.server-addr=localhost:8848 -Dserver.port=8800 -javaagent:${path}/sermant-agent-x.x.x/agent/sermant-agent.jar=appName=default -jar nacos-rest-consumer-2.2.0.RELEASE.jar
   ```

3. Verify warm up

   Request the `localhost:8800/graceHot` interface and check whether the preheating takes effect based on the IP address and port number returned by the interface. If the response contains port 8880 in the majority during the warm up period (120s by default), and the traffic is averaged over time, the warm up has taken effect.

4. Verify graceful offline

   Continuously request the `localhost:8800/graceDownOpen` interface. Make one of the provider instances offline. Check whether occurs error in the request. If no error occurs, the graceful offline  verification is successful.

[Back to README of **Sermant** ](../../README.md)