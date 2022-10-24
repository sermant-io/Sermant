# Summary List of Current Plugin Features

[简体中文](feature-list-zh.md) | [English](feature-list.md)

|Feature|Status|Microservice Framework Supported|Configuration Center Supported|Registration Center Supported|Notice|
|:-:|:-:|:-----|:--|:--|:--|
|[FlowControl](flowcontrol/flowcontrol.md)|Experimental Stage|SpringBoot 1.2.x - 2.6.x <br> SpringWebMvc 4.1.3.RELEASE - 5.3.x<br>Dubbo 2.6.x-2.7.x|servicecomb-kie<br>ZooKeeper|N/A|-|
|[Service Registration](registry/document.md)|GA|SpringBoot 1.5.x - 2.6.2 <br> SpringCloud Edgware.SR2 - 2021.0.0<br>Dubbo 2.6.x-2.7.x|N/A|servicecomb-service-center|-|
|[Service Dual Registration and Migration](registry/spring-cloud-registry-migiration.md)|Experimental Stage|SpringBoot 1.5.x - 2.6.2 <br/> SpringCloud Edgware.SR2 - 2021.0.0<br>Dubbo 2.6.x-2.7.x|N/A|**Target Registration Center** supported：servicecomb-service-center<br/>**Original Registration Center of SpringCloud** supported：Eureka、Nacos、Zookeeper、Consul<br/>**Original Registration Center of Dubbo** supported：Nacos、Zookeeper|-|
|[Loadbalancer](loadbalancer/document.md)|In Development|SpringBoot 1.5.x - 2.6.2 <br/> SpringCloud Edgware.SR2 - 2021.0.0|servicecomb-kie<br/>ZooKeeper|N/A|Different versions support different load balancing policies. For details, refer to [Policies Supported in Loadbalance](loadbalancer/document.md#The-Strategy-Loadbalacne-Support)<br/> Gateway applications are not supported at present.|
|[Tag Router](router/document.md)|Experimental Stage|SpringBoot 1.5.x - 2.6.2 <br/>SpringCloud Edgware.SR2 - 2021.0.0<br/>Dubbo 2.6.x-2.7.x|servicecomb-kie|servicecomb-service-center|Asynchronous invocation is not supported. <br />Hybrid frameworks (SpringCloud or Dubbo) are not supported for routing|
|[Zone Router](router/zone-router.md)|Experimental Stage|Dubbo 2.6.x-2.7.x|servicecomb-kie <br> ZooKeeper|servicecomb-service-center <br> ZooKeeper|-|
|[Graceful Online/Offline](graceful/document.md)|Experimental Stage|SpringBoot 1.5.x - 2.6.2 <br/> SpringCloud 
Edgware.SR2 - 2021.0.0|servicecomb-kie<br/>ZooKeeper|N/A|This function is implemented based on the default loadbalance of SpringCloud. If user-defined loadbalance is implemented, this function won't work.|
|[Dynamic Configuration](dynamic-config/document.md)|In Development|SpringBoot 1.5.x - 2.6.2<br>spring-cloud-starter-alibaba-nacos-config 1.5.0.RELEASE+<br>spring-cloud-starter-zookeeper-config 1.2.0.RELEASE+|servicecomb-kie<br/>ZooKeeper|-||
|[Server Monitor](server-monitor/document.md)|In Development|ALL|N/A|N/A|-|
|[Service Registration and Discovery](springboot-registry/document.md)|Experimental Stage |SpringBoot 1.5.10.Release+|servicecomb-kie<br/>ZooKeeper|Zookeeper 3.4.x+|-|