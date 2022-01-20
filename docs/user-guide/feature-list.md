# 当前功能列表

|功能名称|子功能|状态|宿主的必要环境|支持的配置中心|
|:-:|:-:|:-:|:-|:-:|
|[限流降级](flowcontrol/flowcontrol.md)|接口限流<br>应用降级<br>隔离仓|稳定|SpringBoot 1.2.0.RELEASE - 2.6.x & SpringWebMvc 4.1.3.RELEASE - 5.3.x<br>ApacheDubbo 2.7.3 - 3.0.x<br>AlibabaDubbo 2.1.x - 2.6.x|Kie<br>ZooKeeper|
|[流量录制回放](flowrecord/document.md)||实验|||
|[全链路压测](hercules/document.md)|-|实验|-|-
|[影子库压测](online-stresstest/document.md)||实验|||
|[注册中心](register/document.md)|注册中心迁移<br>双注册中心|实验|SpringBoot 1.5.x, 2.0.0 - 2.6.2 & SpringCloud Edgware.x - 2021.0.0<br>- Eureka 1.4.x - 3.1.0<br>- Nacos 1.5.x - 2021.1<br>- Zookeeper 1.x.x - 3.1.0<br>- Consul 1.x.x - 3.1.0<br>ApacheDubbo 2.7.0 - 2.7.8, 2.7.11 - 2.7.15<br>- DubboRegistryZookeeper 2.7.0 - 2.7.8, 2.7.11 - 2.7.15<br>- DubboRegistryNacos  2.7.1 - 2.7.8, 2.7.11 - 2.7.15|Kie<br>ZooKeeper|
|[灰度发布](route/document.md)|-|实验|ApacheDubbo 2.7.5 - 2.7.8|Kie|
|[服务监控](server-monitor/document.md)|数据库连接池监控|实验|AlibabaDruid 1.0.x, 1.1.x, 1.2.0 - 1.2.8|-|
|[线程变量插件](threadlocal/document.md)||实验|||