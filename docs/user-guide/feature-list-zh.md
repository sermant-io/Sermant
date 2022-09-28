# 当前功能列表

[简体中文](feature-list-zh.md) | [English](feature-list.md)

|功能名称|状态|微服务框架组件支持列表|配置中心支持列表|注册中心支持列表|注意事项|
|:-:|:-:|:-----|:--|:--|:--|
|[限流降级](flowcontrol/flowcontrol-zh.md)|实验|SpringBoot 1.2.x - 2.6.x <br> SpringWebMvc 4.1.3.RELEASE - 5.3.x<br>Dubbo 2.6.x-2.7.x|servicecomb-kie<br>ZooKeeper|N/A|-|
|[服务注册](registry/document-zh.md)|GA|SpringBoot 1.5.x - 2.6.2 <br> SpringCloud Edgware.SR2 - 2021.0.0<br>Dubbo 2.6.x-2.7.x|N/A|servicecomb-service-center|-|
|[服务双注册迁移](registry/spring-cloud-registry-migiration-zh.md)|实验|SpringBoot 1.5.x - 2.6.2 <br/> SpringCloud Edgware.SR2 - 2021.0.0<br>Dubbo 2.6.x-2.7.x|N/A|**目标注册中心**支持：servicecomb-service-center<br/>**SpringCloud原注册中心**支持：Eureka、Nacos、Zookeeper、Consul<br/>**Dubbo原注册中心**支持：Nacos、Zookeeper|-|
|[负载均衡](loadbalancer/document-zh.md)|开发中|SpringBoot 1.5.x - 2.6.2 <br/> SpringCloud Edgware.SR2 - 2021.0.0|servicecomb-kie<br/>ZooKeeper|N/A|不同的版本所支持的负载均衡策略不同，具体请参考[负载均衡策略支持](loadbalancer/document.md#负载均衡策略支持一览)<br/>当前暂不支持网关应用|
|[标签路由](router/document-zh.md)|实验|SpringBoot 1.5.x - 2.6.2 <br/>SpringCloud Edgware.SR2 - 2021.0.0<br/>Dubbo 2.6.x-2.7.x|servicecomb-kie|servicecomb-service-center|不支持异步调用<br>不支持混合框架（Dubbo调SpringCloud或者SpringCloud调Dubbo）做路由|
|[优雅上下线](graceful/document-zh.md)|实验|SpringBoot 1.5.x - 2.6.2 <br/> SpringCloud Edgware.SR2 - 2021.0.0|servicecomb-kie<br/>ZooKeeper|N/A|该功能基于SpringCloud默认负载均衡实现，若实现自定义负载均衡，该能力将失效|
|[动态配置](dynamic-config/document-zh.md)|开发中|SpringBoot 1.5.x - 2.6.2<br>spring-cloud-starter-alibaba-nacos-config 1.5.0.RELEASE+<br>spring-cloud-starter-zookeeper-config 1.2.0.RELEASE+|servicecomb-kie<br/>ZooKeeper|-||
|[服务监控](server-monitor/document-zh.md)|开发中|ALL|N/A|N/A|-|
