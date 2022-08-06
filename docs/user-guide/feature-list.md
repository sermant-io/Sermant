# 当前功能列表

|功能名称|状态|微服务框架组件支持列表|配置中心支持列表|注册中心支持列表|
|:-:|:-:|:-:|:-:|:-:|
|[限流降级](flowcontrol/flowcontrol.md)|实验|SpringBoot 1.2.x - 2.6.x & SpringWebMvc 4.1.3.RELEASE - 5.3.x<br>Dubbo 2.1.x - 2.6.x & 2.7.3 - 3.0.x|servicecomb-kie<br>ZooKeeper|N/A|
|[服务注册](registry/document.md)|GA|SpringBoot 1.5.x - 2.6.2 & SpringCloud Edgware.x - 2021.0.0<br>Dubbo 2.6.x-2.7.x|N/A|servicecomb-service-center|
|[服务双注册迁移](registry/spring-cloud-registry-migiration.md)|实验|SpringBoot 1.5.x - 2.6.2 & SpringCloud Edgware.x - 2021.0.0<br>Dubbo 2.6.x-2.7.x|N/A|**目标注册中心**支持：servicecomb-service-center<br/>**SpringCloud原注册中心**支持：Eureka、Nacos、Zookeeper、Consul<br/>**Dubbo原注册中心**支持：Nacos、Zookeeper|
|[负载均衡](loadbalancer/document.md)|开发中|-|-|-|
|[灰度发布](router/document.md)|实验|SpringBoot 1.5.x - 2.6.2 & SpringCloud Edgware.SR2 - 2021.0.0<br/>Dubbo 2.6.x-2.7.x|servicecomb-kie|servicecomb-service-center|
|[服务监控](server-monitor/document.md)|开发中|-|-|-|
|[流量录制回放](flowrecord/document.md)|实验|-|-|-|
|[全链路压测](hercules/document.md)|实验|-|-|-|
|[影子库压测](online-stresstest/document.md)|实验|-|-|-|
|[线程变量插件](threadlocal/document.md)|实验|-|-|-|
