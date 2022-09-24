# FlowControl

[简体中文](flowcontrol-zh.md) | [English](flowcontrol.md)

This document is used to introduce the usage of [Flow Control Plugin](../../../sermant-plugins/sermant-flowcontrol)

## Functions

The flow control plugin is based on the [resilience4j]((https://github.com/resilience4j)) framework and implements non-intrusive flow control based on the "traffic" entry point. Currently, **Traffic Limiting, Circuit Breaker, Bulkhead, Error Injection, and Retry** are supported. In addition, rules can be dynamically configured in the configuration center and take effect in real time.

- **Traffic Limiting**：The number of QPS that pass through a specified interface within 1s is limited. When the traffic within 1s exceeds the specified threshold, flow control is triggered to limit the requested traffic.
- **Circuit Breaker**：Configure a circuit breaker policy for a specified interface to collect statistics on the error rate or slow request rate in a specified time window. When the error rate or slow request rate reaches a specified threshold, the circuit breaker is triggered. Before the time window is reset, all requests are isolated.
- **Bulkhead**：Controls concurrent traffic for a large number of concurrent traffic to prevent service breakdown caused by excessive instantaneous concurrent traffic.
- **Retry**：If a service encounters a non-fatal error, you can retry the service to prevent the service failure.
- **Error Injection:**  An error injection policy is configured for a specified service when the service is running. Before the client accesses the target service, the error injection policy is used. This policy is mainly used to reduce the access load of the target service and can be used as a measure of downgrading the target service.

## Usage

### Environment Preparation

**（1）Deploying the ServiceCenter and Kie environments**

**（2）Package and compile Sermant Agent**

The way that compile Sermant Agent referring [this article](../../QuickStart.md#Get-Compiled-Products)

Compile by referring to the Sermant source code.

### Configure Agent

**（1）Modify Service Registration Information**

Find the `${javaagent path}/agent/config/config.properties`  and modify the content comply following configuration:

```properties
# application name, you can keep default
service.meta.application=default
# service version
service.meta.version=1.0.0
# serviceComb namespace, keep default
service.meta.project=default
# environment, it only support development/testing/production
service.meta.environment=development
# custom label, it use to subscribe the config of config center
service.meta.customLabel=public
service.meta.customLabelValue=default
```

**Notice**：The preceding configurations can be specified using environment variables. The corresponding key value is the environment variable key. For example, the service app name can be specified by `-Dservice.meta.application=application`. All keys in other configuration files can be configured using this method.

##### **（2）Modify the Configuration Center**

Modify the configuration file ${javaagent path}/config/config.properties and modify the configuration center type and address as follows:

```properties
# IP address of the configuration center. Set this parameter based on the IP address of the configuration center.
dynamic.config.server_address=127.0.0.1:30110
# Configuration center type. The options are KIE and ZOOKEEPER.
dynamic.config.dynamic_config_type=KIE
```

**（3）Configure the Flow Control Plugin**

Modify the Configuration File`${javaagent path}/pluginPackage/flowcontrol/config/config.yaml`

```yaml
flow.control.plugin:
  useCseRule: true 
```

If adaptation is enabled, the plugin subscribes to the configuration center based on the application configuration, service configuration, and customized tag configuration.

> If useCseRule is set to false, the flow control plugin configures subscription based on the service name of the current instance. For example, if spring.application.name is set to flowControlDemo, the flow control plugin receives configuration based on the service=flowControlDemo tag during actual subscription.

### Deploying Applications

Run the following command to start the application:

```shell
# agent path indicates the package path.
# serviceName indicates the name of your service
# applicationName indicates the app name
# environment indicates the environment of your service, support testing/producation/developing
# xxx.jar indicates the application packages of your service
java -javaagent:${agent path}/sermant-agent.jar=appName=${serviceName} -Dservice.meta.application=${applicationName} -Dservice.meta.environment=${environment}  -jar xxx.jar
```

### Verify Application Deployment

Login to the [Service Center](localhost:30103) background and check whether the application is correctly registered.

### Flow Control Rules Specification

Traffic governance uses traffic marking and flow control rules to control specified traffic. Traffic marking refers to request information, such as the interface path, interface method type, request header, and downstream service name. Whether a flow control rule takes effect depends on the traffic flag. A flow control rule takes effect only when the traffic flag matches the request. The mapping between traffic marks and specific rules depends on the service scenario name. Generally, a specified prefix must be configured for traffic marks and traffic control rules. For example, the key of traffic marks must be prefixed with `servicecomb.MatchGroup`. The traffic limiting rule is prefixed with `servicecomb.rateLimiting`. The following is an example:

The traffic marking configuration key：`servicecomb.MatchGroup.flow`

The key for configuring the traffic limiting rule：`servicecomb.rateLimiting.flow`

In the preceding information, `flow `is the service scenario name. The traffic limiting rule takes effect only when the two service scenario names are the same and the request matches a traffic flag.

The following describes the related configurations:

- **Traffic Marking**

  ```yaml
  matches:            # Matcher set. Multiple matchers can be configured.
  - apiPath:          # Matched API path. Various comparison modes are supported, such as exact and contain.
      exact: /degrade # Specific Matching Path
    headers:          # Request header
      key: 
        exact: value  # Request header value. The value is key=value. The comparison method is the same as that of apiPath.
    method:           # Supported Method Types
    - GET
    name: degrade     # Configuration name, which is optional.
  ```

  **what traffic marking above can match  :**

  - If the request path is `/degrade`, the method type is `GET`, and the request header contains `key=value`, the matching is successful.

  

  > For details about the configuration items, see the traffic marking section in the [ServiceComb development document](http://servicecomb.gitee.io/servicecomb-java-chassis-doc/java-chassis/zh_CN/references-handlers/governance.html#_2).

  **Traffic marking request path (apiPath) configuration description**

  The request path for traffic marking varies according to the request protocol configuration. Currently, HTTP (Spring) and RPC (Dubbo) protocols are used. The following describes how to configure the two request protocols:

  - **Http protocol**

    This protocol performs matching based on the request path. For example, if the request path is `localhost:8080/test/flow`, the actual path is `/test/flow`. Therefore, if you need to set a matching rule, you need to configure the matching rule based on the path.

    It should be noted that if the contextPath configured by the user is valid only after the contextPath prefix is added.

  - **Rpc protocol(Dubbo)**

    The protocol invoking needs to be based on an interface+method. For example, if the requested interface is `com.demo.test`, and the method is `flow`, a corresponding request path is `com.demo.test.flow`. Specially, if a user configures an interface version, for example, a specified version is `1.0.0`, The request path is `com.demo.test:1.0.0.flow`. In addition, set the request method to `POST`. The RPC protocol supports only POST.

- **Traffic Limiting**

  | Configuration      | Description                                                  |
  | ------------------ | ------------------------------------------------------------ |
  | limitRefreshPeriod | Unit of statistics time, in milliseconds. If you need to set this parameter, the unit can be set to `S`, for example, `10s`. |
  | rate               | Number of requests that can be processed in the unit of statistical time. |

- **Circuit Breaker**

  | Configuration             | Description                                                  |
  | ------------------------- | ------------------------------------------------------------ |
  | failureRateThreshold      | Error rate required for fuse                                 |
  | minimumNumberOfCalls      | Minimum number of requests in the sliding window. The fuse condition is determined only when the minimum number of requests is exceeded. |
  | name                      | Specifies the name of a configuration item. This parameter is optional. |
  | slidingWindowSize         | Size of the sliding statistics window. The value can be milliseconds or seconds. For example, 1000 indicates 1000 milliseconds, and 10s indicates 10 seconds. |
  | slidingWindowType         | Sliding window type. Currently, `time` and `count` are supported. The former is based on the time window and the latter is based on the number of requests. |
  | slowCallDurationThreshold | Slow request threshold. The unit is the same as that of the sliding window. |
  | slowCallRateThreshold     | Percentage of slow invoking requests. When the number of slow invoking requests reaches this percentage, connectivity is triggered. |
  | waitDurationInOpenState   | Recovery time after a circuit breaker. The default value is `60s`. |

- **Bulkhead**

  | Configuration      | Description                                                  |
  | ------------------ | ------------------------------------------------------------ |
  | maxConcurrentCalls | Maximum number of concurrent calls                           |
  | maxWaitDuration    | Maximum waiting time. If the thread exceeds maxConcurrentCalls, the thread attempts to wait. If the thread does not obtain resources after the waiting time expires, an isolation warehouse exception is thrown. |
  | name               | name of configuration, which is optional.                    |

- **Retry**

  | Configuration         | Description                                                  |
  | --------------------- | ------------------------------------------------------------ |
  | waitDuration          | Retry wait time. The default value is milliseconds. The unit is second, for example, 2s. |
  | retryStrategy         | Retry policy. Currently, two retry policies are supported: fixed interval (FixedInterval) and exponential increase interval (RandomBackoff). |
  | maxAttempts           | Maximum number of retries                                    |
  | retryOnResponseStatus | HTTP status code. Currently, only HTTP requests are supported. For dubbo requests, you can configure the exception type to determine whether to retry. The default value is RpcException. |

- **Error Injection**

  | Configuration | Description                                                  |
  | ------------- | ------------------------------------------------------------ |
  | type          | Error injection type. Currently, `abort (request response)` and `delay (request delay)` are supported. |
  | percentage    | Error Injection triggering probability                       |
  | fallbackType  | Return type of the request invoking. This parameter is valid only when `type is set to abort`. Currently, two types are supported, `ReturnNull`: empty content is directly returned and the status code is 200. `ThrowException`: The error code is returned based on the specified error code.` |
  | errorCode     | Specifies the returned error code. The default value is 500. This parameter is valid only `when type is abort and fallbackType is ThrowException`. |
  | forceClosed   | Indicates whether to forcibly disable the error injection capability. If this parameter is set to true, error injection does not take effect. The default value is false. |

### Configuring Flow Control Rule

#### Configuring Flow Control Rules Based On The Configuration File

If your application is not a SpringBoot application, this method is not applicable.

When an application is started, the flow control plugin attempts to read the flow control rules and corresponding traffic flags from the configuration source loaded by SpringBoot. You need to configure the flow control rules before starting the application. The following is a configuration example. The example configuration is based on the `application.yml` file.

```yaml
servicecomb:
  matchGroup:
    demo-fault-null: |
      matches:
        - apiPath:
            exact: "/flow"
    demo-retry: |
      matches:
        - apiPath:
            prefix: "/retry"
          serviceName: rest-provider
          method:
          - GET
    demo-rateLimiting: |
      matches:
        - apiPath:
            exact: "/flow"
    demo-circuitBreaker-exception: |
      matches:
        - apiPath:
            exact: "/exceptionBreaker"
    demo-bulkhead: |
      matches:
        - apiPath:
            exact: "/flowcontrol/bulkhead"
  rateLimiting:
    demo-rateLimiting: |
      rate: 1
  retry:
    demo-retry: |
      maxAttempts: 3
      retryOnResponseStatus:
      - 500
  circuitBreaker:
    demo-circuitBreaker-exception: |
      failureRateThreshold: 44
      minimumNumberOfCalls: 2
      name: circuit breaker
      slidingWindowSize: 10000
      slidingWindowType: time
      waitDurationInOpenState: 5s
  bulkhead:
    demo-bulkhead: |
      maxConcurrentCalls: 1
      maxWaitDuration: 10
  faultInjection:
    demo-fault-null: |
      type: abort
      percentage: 100
      fallbackType: ReturnNull
      forceClosed: false
```

The preceding configurations are used to configure the supported flow control rules. Change the configuration items based on the site requirements.。

#### Configuring Interface Advertisement Rules Based on Sermant Backend

The backend service provides the function of publishing configurations through the /publishConfig request interface. The request parameters are as follows:

| Configuration | Description                                                  |
| ------------- | ------------------------------------------------------------ |
| key           | Configuration key                                            |
| group         | Configured tag group                                         |
| content       | Configuration content, that is, specific rule configuration, is in YAML format. |

> The format of group is k1=v1, and multiple values are separated by ampersands (&). For example, k1=v1&k2=v2, indicating the label group bound to the key.

**The following uses `app=region-A,serviceName=flowControlDemo, environment=testing` as an example.**

- #### The Example Configuration For Traffic Marking Rule

  ```json
  {
      "key":"servicecomb.matchGroup.scene",
      "group":"app=region-A&service=flowControlDemo&environment=testing",
      "content":"alias: scene\nmatches:\n- apiPath:\n    exact: /flow\n  headers: {}\n  method:\n  - POST\n  name: rule1\n"
  }
  ```


**Rule Interpretation:**

- If the request path is `/flow` and the method type is `GET`, the matching is successful.
- This parameter takes effect for the service instance whose app is `region-A`, service name is `flowControlDemo`, and environment is `testing`.



> **Notices：**
>
> - To configure flow control, you need to configure the service scenario and then configure the flow control rule bound to the service scenario.
> - The `key` must be preceded by `servicecomb.matchGroup`. and the scene indicates the service name.

- #### The Example Configuration For Traffic Limiting Rule

  ```json
  {
      "key":"servicecomb.rateLimiting.scene",
      "group":"app=region-A&service=flowControlDemo&environment=testing",
      "content":"limitRefreshPeriod: \"1000\"\nname: flow\nrate: \"2\"\n"
  }
  ```
  **Rule Interpretation：**

    - This parameter takes effect for the service instance whose app is region-A, service name is flowControlDemo, and environment is testing.
    - If more than two requests are received within one second, flow control is triggered.

  

  > **Notices：**
  >
  > The `key` must be preceded by `servicecomb.rateLimiting`. and scene indicates the service name. Ensure that the value is the same as the service scenario name of the traffic tag.

- #### The Example Configuration For Circuit Breaker Rule

  ```json
  {
      "key":"servicecomb.circuitBreaker.scene",
      "group":"app=region-A&service=flowControlDemo&environment=testing",
      "content":"failureRateThreshold: 90\nminimumNumberOfCalls: 3\nname: degrade\nslidingWindowSize: 10S\nslidingWindowType: time\nslowCallDurationThreshold: \"1\"\nslowCallRateThreshold: 80\nwaitDurationInOpenState: 10s"
  }
  ```
  **Rule Interpretation:**

    - This parameter takes effect for the service instance whose app is region-A, service name is flowControlDemo, and environment is testing.
    - If the number of interface or flow requests exceeds three within 10 seconds and the error rate exceeds 90% or the percentage of slow requests exceeds 80%, the circuit breaker is triggered.

  

  > **Notices：**
  >
  > The key must be preceded by servicecomb.circuitBreaker. and scene indicates the service name. Ensure that the value is the same as the service scenario name of the traffic tag.

- #### The Example Configuration For Bulkhead Rule

  ```json
  {
      "key":"servicecomb.bulkhead.scene",
      "group":"app=region-A&service=flowControlDemo&environment=testing",
      "content":"maxConcurrentCalls: \"5\"\nmaxWaitDuration: \"10S\"\nname: \"bulkhead\"\n"
  }
  ```

  **Rule Interpretation:**

    - This parameter takes effect for the service instance whose app is region-A, service name is flowControlDemo, and environment is testing.
    - For an interface `/flow`, if the maximum number of concurrent requests exceeds 5 and a new request waits for 10 seconds and resources are not obtained, an exception occurs in the isolation repository.

  

  > **Notices：**
  >
  > The key must be preceded by servicecomb.bulkhead. and scene indicates the service name. Ensure that the value is the same as the service scenario name of the traffic tag.

- #### The Example Configuration For Retry Rule

  ```json
  {
      "key":"servicecomb.retry.scene",
      "group":"app=region-A&service=flowControlDemo&environment=testing",
      "content":"waitDuration: \"2000\"\nretryStrategy: FixedInterval\nmaxAttempts: 2\nretryOnResponseStatus:\n- 500"
  }
  ```

  **Rule Interpretation：**

    - This parameter takes effect for the service instance whose app is region-A, service name is flowControlDemo, and environment is testing.
    - For an interface `/flow`, when the 500 exception is thrown, the request is retried until the retry succeeds or the maximum number of retry times is reached.

  

  > **Notices**：
  >
  > The key must be preceded by servicecomb.retry. and scene indicates the service name. Ensure that the value is the same as the service scenario name of the traffic tag.

  **Notices**: For Dubbo retry, the flow control plugin will replace the original ClusterInvoker. The request logic is invoked by the ClusterInvoker implemented by the flow control plugin, and the original ClusterInvoker will become invalid (for example: Default FailoverClusterInvoker for dubbo. If you need to use original ClusterInvoker, you can add the environment variable `-Dflow.control.plugin.useOriginInvoker=true`. However, this method may cause a small performance loss.

- #### The Example Configuration For Error Injection Rule

  ```json
  {
      "key":"servicecomb.faultInjection.scene",
      "group":"app=region-A&service=flowControlDemo&environment=testing",
      "content":"type: abort\npercentage: 100\nfallbackType: ReturnNull\nforceClosed: false\nerrorCode: 503"
  }
  ```

  **Rule Interpretation：**

  - This parameter takes effect for the service instance whose app is region-A, service name is flowControlDemo, and environment is testing.
  - When the interface /flow is requested, 100% returns null.

  

  > **Notices**：
  >
  > The key must be preceded by servicecomb.faultInjection. and scene indicates the service name. Ensure that the value is the same as the service scenario name of the traffic tag.

## Quick Start

### 1、Compile And Package

Download the [sermant release package](https://github.com/huaweicloud/Sermant/releases) and [demo source code](../../../sermant-plugins/sermant-flowcontrol/flowcontrol-demos/flowcontrol-demo).

Run the following maven command to package the demo application:

```shell
mvn clean package
```

### 2、Start Application

```shell
java -javaagent:${agent path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=FlowControlDemo -Dservice.meta.application=region-A -Dservice.meta.environment=testing -Dspring.application.name=FlowControlDemo -jar FlowControlDemo.jar
```

### 3、Configure The Rule

Configure traffic marking and traffic limiting rules by referring to [Configuring Flow Control Rules](#Configuring-Flow-Control-Rule).

**Traffic Marking:**

```json
{
  "key": "servicecomb.matchGroup.sceneFlow",
  "group": "app=sc&service=flowControlDemo&environment=testing",
  "content": "alias: scene\nmatches:\n- apiPath:\n    exact: /flow\n  headers: {}\n  method:\n  - POST\n  name: flow\n"
}
```

**Traffic Limiting Rule：**

```json
{
  "key": "servicecomb.rateLimiting.scene",
  "group": "app=region-A&service=flowControlDemo&environment=testing",
  "content": "limitRefreshPeriod: \"2S\"\nname: flow\nrate: \"4\"\n"
}
```

### 4、Verify Result

Request `localhost:12000/flow` for multiple times. If `rate limited` is returned when the number of requests exceeds 4 within 2 seconds, flow control is triggered successfully.

## Others

If you encounter any problems, refer to the [FAQ document](./FAQ.md).



[Back to README of **Sermant** ](../../README.md)
