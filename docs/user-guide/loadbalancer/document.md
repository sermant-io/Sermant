# 负载均衡

[插件目录](../../../sermant-plugins/sermant-loadbalancer)

## 功能:

可根据配置中心的配置，无侵入地动态修改宿主应用（支持dubbo和spring cloud）的负载均衡策略。

## 使用方式:

按需修改负载均衡插件的[配置文件](../../../sermant-plugins/sermant-loadbalancer/config/config.yaml)以读取配置中心的配置：

```yaml
loadbalancer.plugin:
  #负载均衡配置的key，可自定义，必填
  key: loadbalancer
  #负载均衡配置的组，public、default可自定义，必填
  group: public=default
  # dubbo默认负载均衡策略，随机，非必填
  dubboType: RANDOM
  # spring-cloud-loadbalancer默认负载均衡策略，轮询，非必填
  springType: ROUND_ROBIN
  # spring-cloud-netflix-ribbon默认负载均衡策略，区域权重，非必填
  ribbonType: ZONE_AVOIDANCE
```

然后需要到配置中心配置如下负载均衡策略：

### 配置中心新增配置方式如下：

**URL**

POST /publishConfig

**请求Body**

|参数|是否必填|参数类型|描述
|---|---|---|---|
|key|是|String|配置的key|
|group|是|String|配置的组|
|content|是|String|配置文本|

其中，对于kie配置中心（暂时只支持kie）来说，group的格式为：**key=value**，key、value为自定义的值。具体参数的描述请参考[动态配置服务介绍](../../dev-guide/service_dynamicconfig.md)。

对于上述[配置文件](#使用方式)来说，新增配置接口请求的参数中，key为loadbalancer，group为public=default，content为负载均衡策略。

### 负载均衡策略示例及说明如下：

```yaml
# dubbo负载均衡策略，随机
dubboType: RANDOM
# spring-cloud-loadbalancer负载均衡策略，轮询
springType: ROUND_ROBIN
# spring-cloud-netflix-ribbon负载均衡策略，区域权重
ribbonType: ZONE_AVOIDANCE
```

### 配置说明

配置中心的负载均衡策略的优先级高于配置文件的负载均衡策略，用户可通过修改配置中心的负载均衡策略动态修改宿主应用的负载均衡策略（宿主无需重启）。

### 负载均衡策略支持列表

|框架类型|策略名|策略值|版本支持|
|---|---|---|---|
|dubbo|随机（dubbo默认）|RANDOM|2.6.x, 2.7.x|
|dubbo|轮询|ROUNDROBIN|2.6.x, 2.7.x|
|dubbo|最少活跃|LEASTACTIVE|2.6.x, 2.7.x|
|dubbo|一致性HASH|CONSISTENTHASH|2.6.x, 2.7.x|
|dubbo|最短响应时间|SHORTESTRESPONSE|2.7.7+|
|spring-cloud-netflix-ribbon|区域权重（ribbon默认）|ZONE_AVOIDANCE|spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x|
|spring-cloud-netflix-ribbon|随机|RANDOM|spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x|
|spring-cloud-netflix-ribbon|轮询|ROUND_ROBIN|spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x|
|spring-cloud-netflix-ribbon|重试|RETRY|spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x|
|spring-cloud-netflix-ribbon|最低并发|BEST_AVAILABLE|spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x|
|spring-cloud-netflix-ribbon|可用过滤|AVAILABILITY_FILTERING|spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x|
|spring-cloud-netflix-ribbon|响应时间加权重（Deprecated）|RESPONSE_TIME_WEIGHTED|spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x|
|spring-cloud-netflix-ribbon|响应时间加权重|WEIGHTED_RESPONSE_TIME|spring cloud Edgware.x, spring cloud Finchley.x, spring cloud Greenwich.x, spring cloud Hoxton.x|
|spring-cloud-loadbalancer|轮询（loadbalancer默认）|ROUND_ROBIN|spring cloud Hoxton.SR10+, spring cloud 2020.0.x, spring cloud 2021.0.x|
|spring-cloud-loadbalancer|随机|RANDOM|spring cloud Hoxton.SR10+, spring cloud 2020.0.x, spring cloud 2021.0.x|

### 版本说明

- 在spring cloud 2020.0.x之前，spring cloud负载均衡默认使用的核心组件为spring-cloud-netflix-ribbon（宿主应用可通过排除ribbon相关的组件使用spring-cloud-loadbalancer组件），从spring cloud 2020.0.x开始，负载均衡的核心组件为spring-cloud-loadbalancer。

- 在spring cloud Hoxton.SR10之前，spring-cloud-loadbalancer的负载均衡策略只有轮询（ROUND_ROBIN），所以插件并不支持修改Hoxton. SR10之前的spring-cloud-loadbalancer组件的负载均衡策略，spring cloud Hoxton.SR10之前版本建议使用spring-cloud-netflix-ribbon组件进行负载均衡。

## 结果验证

- 前提条件[正确打包Sermant](../../README.md)

- 注册中心使用Service Center，下载[Service Center](https://github.com/apache/servicecomb-service-center) ，按照官网说明进行启动。

- 编译demo应用

```shell
mvn clean package
```

- 启动消费者

```shell
java -javaagent:${path}\sermant-agent-x.x.x\agent\sermant-agent.jar=appName=dubbo-a,instanceName=dubboA -jar dubbo-a.jar
```

其中path需要替换为Sermant实际打包路径

- 启动生产者第一个实例

```shell
java -jar dubbo-b.jar
```

- 启动生产者第二个实例

```shell
java -jar dubbo-b2.jar
```

- 测试

当启动以上3个应用并正确配置负载均衡策略后，通过浏览器访问<http://localhost:28020/test27>，即可根据配置的负载均衡策略对生产者进行访问。

[返回**Sermant**说明文档](../../README.md)
