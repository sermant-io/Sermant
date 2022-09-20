# Flow Control FAQs

[简体中文](FAQ-zh.md) | [English](FAQ.md)

This document describes the common problems encountered when the flow control plug-in is used.

## How Is The APIPath Defined In The Service Scenario?

- `apiPath` indicates the interface to be used. The definition varies according to the framework. Currently, HTTP and Dubbo requests are supported:
    - `http procotol`： Indicates the request path. For example, if the interface http://localhost:8080/test exists, the apiPath of the interface is `/test`.
    - `dubbo procotol`：Request interface: `interface:version. Method`. If there is no interface version or the version is 0.0.0, the value of apiPath is Request `interface.Method`.

## How Do I Determine Whether a Configuration Rule Takes Effect?

- Configure service scenarios and governance policies correctly in the configuration center. After the configuration, you can view the agent logs. Generally, in the logs folder in the startup path of the JAR package, view the sermant-x.log file and search for `has been` or the configured key name. If the found logs match the current time, the rule has taken effect.

## Possible Causes For The Failure Of The Circuit Breaker Policy To Take Effect

- A circuit breaker takes effect only after the following conditions are met:
    - `Error Rate`：The percentage of error interface requests. If the percentage of error interface requests within a specified period is greater than the configured value, the circuit breaker is triggered.
    - `Slow Invoking Ratio`：The percentage of slow invoking requests. When setting the circuit breaker policy, you need to set the slow invoking threshold. For example, if the time required for invoking the interface exceeds 100 ms and exceeds the configured slow invoking ratio, the circuit breaker can be triggered only when the time required for invoking the interface exceeds 100 ms.
- Therefore, for the preceding two indicators, check whether the application interface meets either of the preceding conditions and whether the number of invoking requests within the rule period exceeds the minimum number specified by minimumNumberOfCalls.

## Possible Reasons Why The Quarantine Rule Does Not Take Effect

- The quarantine rules must meet the following conditions:
    - `The number of concurrent calls meets the requirement` (configured by maxConcurrentCalls). For example, if the threshold is set to 2, ensure that the number of concurrent calls is greater than 2.
    - `Maximum waiting time` (configured by maxWaitDuration), that is, the maximum waiting time of a thread when the number of concurrent connections reaches the maximum. If no permission is obtained after the maximum waiting time expires, the thread is triggered.
- Therefore, you are advised to ensure that the service interface duration is greater than the maximum waiting time and the number of concurrent requests is greater than the configured value.

## Possible Causes For The Retry Rule Does Not Take Effect

- Ensure that exceptions or status codes thrown by downstream applications meet the retry policy requirements. For example, by default, dubbo checks whether the downstream applications throw RpcException, and a specified status code can be configured for Spring applications.

## Possible Causes Of The HttpHostConnectException Error Reported During Startup

- The cause is that the Sermant background service sermant-backhend is not started. Find the startup class com.huawei.apm.backend.NettyServerApplication to start the background service and restart the application.



[Back to README of **Sermant** ](../../README.md)