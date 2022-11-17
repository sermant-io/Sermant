# Loadbalancer

[简体中文](document-zh.md) | [English](document.md)

This document is used to introduce the usage of [loadbalancer](../../../sermant-plugins/sermant-loadbalancer)

## Functions:

Based on the configuration in the configuration center, the loadbalance rules of the host application can be dynamically modified without intrusion.

## The Strategy Loadbalacne Support

| Framework                   | Strategy                             | Configuration value/Loadbalance Strategy       | version support                                              |
| --------------------------- | ------------------------------------ | ---------------------------------------------- | ------------------------------------------------------------ |
| dubbo                       | Random (Dubbo default)               | Random / RANDOM                                | 2.6.x, 2.7.x                                                 |
| dubbo                       | RoundRobin                           | RoundRobin / ROUNDROBIN                        | 2.6.x, 2.7.x                                                 |
| dubbo                       | leastActive                          | leastActive / LEASTACTIVE                      | 2.6.x, 2.7.x                                                 |
| dubbo                       | Consistent hash                      | consistentHash / CONSISTENTHASH                | 2.6.x, 2.7.x                                                 |
| dubbo                       | Minimum response time                | shortestResponse / SHORTESTRESPONSE            | 2.7.7+                                                       |
| spring-cloud-netflix-ribbon | Area weight (default value)          | zoneAvoidance / ZONE_AVOIDANCE                 | ZONE_AVOIDANCEspring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x |
| spring-cloud-netflix-ribbon | Random                               | Random / RANDOM                                | spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x |
| spring-cloud-netflix-ribbon | RoundRobin                           | RoundRobin / ROUND_ROBIN                       | spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x |
| spring-cloud-netflix-ribbon | retry                                | retry / RETRY                                  | spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x |
| spring-cloud-netflix-ribbon | bestAvailable                        | bestAvailable / BEST_AVAILABLE                 | spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x |
| spring-cloud-netflix-ribbon | availabilityFiltering                | availabilityFiltering / AVAILABILITY_FILTERING | spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x |
| spring-cloud-netflix-ribbon | Response Time Weighting (Deprecated) | ResponseTimeWeighted / RESPONSE_TIME_WEIGHTED  | spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x |
| spring-cloud-netflix-ribbon | Response time weighting              | weightedResponseTime / WEIGHTED_RESPONSE_TIME  | spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x |
| spring-cloud-loadbalancer   | RoundRobin(default)                  | RoundRobin / ROUND_ROBIN                       | spring cloud Hoxton.SR10+, spring cloud 2020.0.x, spring cloud 2021.0.x |
| spring-cloud-loadbalancer   | Random                               | Random / RANDOM                                | spring cloud Hoxton.SR10+, spring cloud 2020.0.x, spring cloud 2021.0.x |

## How to Configure

Load balancing is dynamically configured based on the configuration center. To use this capability, you need to configure the corresponding load balancing policy in the configuration center. The loadbalance plugin uses **traffic marking + loadbalance rules**. To configure a rule, you **need to configure both of them**. The following describes the two configurations:

### Traffic Marking

Traffic marking is used to mark the services that take effect. If the service is empty, the traffic marking is applied to all microservices. The configuration example is as follows:

**Configuring key:**  `servicecomb.matchGroup.testLb`

> description of key: 
>
> `servicecomb.matchGroup. `：Fixed prefix of traffic marking. Traffic marking must be configured for all keys.
>
> `testLb`：Service scenario name. The corresponding load balancing rule must be configured with the same service scenario name.

**configure content:**

```yaml
alias: loadbalancer-rule
matches:
- serviceName: zk-rest-provider  # downstream service name
```

Example rule description: serviceName indicates the name of the downstream service, that is, the loadbalance rule to be applied to the requested microservice zk-rest-provider. If the serviceName configuration item is not set, the configuration item applies to all microservices. Note that only the serviceName configuration item needs to be configured in this configuration. Other formats remain unchanged.

> Priority: If multiple load balancing rules are configured, the plugin preferentially matches the load balancing rule with the service name configured. Otherwise, the plugin uses the load balancing rule with no service name configured.

### Loadbalance Rule

Load balancing rules must be configured for applications. Load balancing policies depend on the existing load balancing policies of the host. That is, load balancing policies can be configured only when the host supports the load balancing policies. For details about the supported load balancing policies, see the [The Strategy Loadbalacne Support](#The-Strategy-Loadbalacne-Support).

**configure key：**`servicecomb.loadbalance.testLb`

> configuration item description: 
>
> `servicecomb.loadbalance. `：Fixed prefix configured for the loadbalancer rule. Loadbalancer rules must be configured for all keys.
>
> `testLb`：Service scenario name, which takes effect only when it is the same as the service scenario of the traffic flag.

**configure content:**

```yaml
rule: Random
```

Example configuration item description: Configure a random load balancing rule. For details about the configuration values, see the [The Strategy Loadbalacne Support](#The-Strategy-Loadbalacne-Support).

> Check the framework version of the host application and determine the supported load balancing strategy.

### Publish Loadbalance Rule

The Sermant Backend provides APIs for releasing configurations. Before using the API, you need to start the backend application. The following describes the configuration publishing interface:

**URL**

POST /publishConfig

**Request Body**

| Params  | Mandatory or not | Param type | Description                                                  |
| ------- | ---------------- | ---------- | ------------------------------------------------------------ |
| key     | √                | String     | configuration key                                            |
| group   | √                | String     | Configuration group, which is used to configure subscriptions |
| content | √                | String     | Configuration text, that is, specific rules                  |

In the preceding table, the key and content correspond to the key and content of the traffic tag and load balancing rule. The group refers to the tag of the specified service. The group is configured in the tag pair mode. For example, app=default&environment=development indicates publishing configurations for microservices that subscribe to this tag.

The loadbalance plugin has three subscription tags by default.：

- custom tag：By default, the tag `public=defaul`t is subscribed to. You can also modify the customized tag by setting environment variables and add the following parameters to the startup parameter: `-Dservice.meta.customLabel=public -Dservice.meta.customLabelValue=default`
- microservice tag:  By default, the `app=default&service=${yourServiceName}&environment= tag is subscribed. ${yourServiceName}` indicates the microservice name and environment is empty by default. You can also use environment variables to change the startup parameters. Add the following parameters to the startup parameters: `-Dservice.meta.application=default -Dservice.meta.environment=${yourEnvironment}`, corresponds to app and envrionment, and the service name is dynamically obtained.
- application tag：By default, the tag `app=default&environment=` is subscribed. The environment variable configuration method is the same as that of the microservice tag.

### Version Notices

- In versions earlier than spring cloud 20200.0.x, the core component used by spring cloud load balancers is spring-cloud-netflix-ribbon by default. (The host application can use the spring-cloud-loadbalancer component by excluding ribbon-related components.) From spring cloud 20200.0.x, the core component of load balancing is spring-cloud-loadbalancer.

- In versions earlier than spring cloud Hoxton.SR10, the load balancing policy of spring-cloud-loadbalancer can only be round robin (ROUND_ROBIN). Therefore, the plugin does not support modifying the load balancing policy of spring-cloud-loadbalancer components earlier than Hoxton.SR10. For versions earlier than spring cloud Hoxton.SR10, you are advised to use the spring-cloud-netflix-ribbon component for load balancing.

## Verify Result

1. Prerequisites: [sermant has been downloaded](https://github.com/huaweicloud/Sermant/releases), [the demo source code is downloaded](https://github.com/huaweicloud/Sermant-examples/tree/main/sermant-template/demo-register), and [the ZooKeeper is downloaded](https://zookeeper.apache.org/releases.html#download).

2. start zookeeper

3. start backend, referring to the [backend module introduction](../backend.md)

4. compile and package demo application

   ```
   mvn clean package
   ```

   

5. publish traffic marking rule

   Invoke the interface `localhost:8900/publishConfig`, request body just follow below:

   ```json
   {
       "content": "alias: loadbalancer-rule\nmatches:\n- serviceName: zk-rest-provider", 
       "group": {
           "app": "default", 
           "environment": "", 
           "service": "zk-rest-consumer"
       }, 
       "key": "servicecomb.matchGroup.testLb"
   }
   ```

   

6. publish loadbalance rule (Random as a example).

   Invoke the interface`localhost:8900/publishConfig`, request body just follow below:

   ```json
   {
       "content": "rule: Random", 
       "group": {
           "app": "default", 
           "environment": "", 
           "service": "zk-rest-consumer"
       }, 
       "key": "servicecomb.loadbalance.testLb"
   }
   ```

   

7. Starting a provider (two instances)

   ```shell
   java -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=default -Dserver.port=${port} -jar zk-rest-provider.jar
   ```

   Replace `path` with the **actual packaging path** of the Sermant and port with the producer port. In this example, you need to start two instances. Configure different ports for the two instances.

8. Start a consumer (one instance).

   ```shell
   java -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=default -Dserver.port=8005 -jar zk-rest-consumer.jar
   ```

9. testing

   After the preceding steps are complete, access the localhost:8005/hello interface and check whether the random load balancing rule (by default, RoundRobin) takes effect based on the returned port.

[Back to README of **Sermant** ](../../README.md)
