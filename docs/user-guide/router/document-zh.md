# 标签路由

[简体中文](document-zh.md) | [English](document.md)

本文档主要介绍[标签路由插件](../../../sermant-plugins/sermant-router)的使用方法。

区域路由见[区域路由](zone-router-zh.md)。

## 功能

在微服务存在多个版本、多个实例的情况下，通过配置路由规则管理服务之间的路由，达到无损升级、应用拨测等业务目的。

## 使用说明

- 配置路由规则

Sermant backend提供api的方式发布配置, 使用前需启动backend后台应用，配置发布接口如下：

**URL**

POST /publishConfig

**请求Body**

|参数|是否必填|参数类型|描述
|---|---|---|---|
|key|√|String|配置的key|
|group|√|String|配置的组|
|content|√|String|配置文本|

其中key值为servicecomb.routeRule.${yourServiceName}，${yourServiceName}为目标应用的微服务名称。

group需要配置为应用级别，即app=${yourApp}&&environment=${yourEnvironment}，其中app默认为default，environment默认为空。

content为具体的路由规则。

### 标签路由规则示例及说明如下：

```yaml
---
- precedence: 2 # 优先级，数字越大，优先级越高。
  match: # 请求匹配规则。0..N个，不配置表示匹配。每条匹配规则只允许存在一个attachments/headers/args。
    attachments: # dubbo attachment匹配。如果是http header匹配，需要配置为headers
      id: # 如果配置了多个key，那么所有的key规则都必须和请求匹配
        exact: '1' # 配置策略，等于1，详细配置策略参考配置策略表
        caseInsensitive: false # false:不区分大小写（默认）,true:区分大小写。配置为false时，将统一转为大写进行比较
    args: # dubbo参数匹配
      args0: # dubbo接口的第0个参数
        type: .id # 取值类型，dubbo应用特有字段，第0个参数为实体，获取其id的属性值，如果参数类型为int，String等普通类型，则无需填写该值，所有的取值类型见取值类型列表
        exact: '2' # 配置策略，等于2，所有的匹配策略见配置策略列表
        caseInsensitive: false # 是否区分大小写，默认为false，区分大小写
  route: # 路由规则
    - weight: 20 # 权重值
      tags:
        version: 1.0.0 # 实例标记。满足标记条件的实例放到这一组。
    - weight: 80 # 权重值
      tags:
        version: 1.0.1 # 实例标记。满足标记条件的实例放到这一组。
- precedence: 1
  route:
    - weight: 20
      tags:
        group: red
    - weight: 80
      tags:
        group: green
```

**注意：新增配置时，请去掉注释，否则会导致新增失败。**

### 配置策略列表

|策略名|策略值|匹配规则|
|---|---|---|
|精确匹配|exact|参数值等于配置值|
|正则|regex|参数值匹配正则表达式，由于部分正则表达式（如\w与\W等）区分大小写，所以使用正则策略时，请谨慎选择caseInsensitive（是否区分大小写）|
|不等于|noEqu|参数值不等于配置值|
|大于等于|noLess|参数值大于等于配置值|
|小于等于|noGreater|参数值小于等于配置值|
|大于|greater|参数值大于配置值|
|小于|less|参数值小于配置值|

### 取值类型列表

|类型|取值方式|适用参数类型|
|---|---|---|
|留空|表示直接取当前参数的值|适用普通参数类型，例如String、int、long等|
|.name|表示取参数的name属性，相当于ARG0.getName()|适用于对象类型|
|.isEnabled()|表示取参数的enabled属性，相当于ARG0.isEnabled()|适用于对象类型|
|[0]|取数组的第一个值，相当于ARG0[0]|适用于普通类型的数组，例如String[]、int[]|
|.get(0)|取List的第一个值，相当于ARG0.get(0)|适用于普通类型的列表，例如List\<String>、List\<Integer>|
|.get("key")|获取key对应的值，相当于ARG0.get("key")|适用于普通类型的map，例如Map<String, String>|

- 启动标签应用

在附带agent启动时，按需加上以下参数：

```
-Dservice_meta_version=${VERSION} -Dservice_meta_parameters=${PARAMETERS}
```

参数说明如下：

- ${VERSION}需替换为服务注册时的版本号（形如a.b.c的格式，其中a,b,c均为数字，默认为1.0.0），标签应用需要修改为不同于正常应用的版本号。
- ${PARAMETERS}需替换为服务注册时的自定义标签（形如tag1:value1,tag2:value2），即标签名与标签值以英文冒号分隔，多个标签之间以英文逗号分隔。
- 一般地，如果用版本号进行路由，则只需配置service_meta_version，如果用自定义标签进行路由，则只需配置service_meta_parameters。

## 结果验证

- 前提条件[正确打包Sermant](../../README.md)

- 注册中心使用华为CSE，下载[Local-CSE](https://support.huaweicloud.com/devg-cse/cse_devg_0036.html) ，解压后按照文档说明进行启动

- 配置路由规则

调用接口`localhost:8900/publishConfig`, 请求参数如下:

```json
{
   "content": "---\n- precedence: 1\n  match:\n    headers:\n        id:\n          exact: '1'\n          caseInsensitive: false\n  route:\n    - tags:\n        group: gray\n      weight: 100\n- precedence: 2\n  match:\n    headers:\n        id:\n          exact: '2'\n          caseInsensitive: false\n  route:\n    - tags:\n        version: 1.0.1\n      weight: 100", 
   "group": "app=default&&environment=", 
   "key": "servicecomb.routeRule.spring-cloud-router-provider"
}
```

- 编译[demo应用](https://github.com/huaweicloud/Sermant-examples/tree/main/router-demo/spring-cloud-router-demo)

```shell
mvn clean package
```

- 启动zuul网关

```shell
java -Dservicecomb_service_enableSpringRegister=true -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar spring-cloud-router-zuul.jar
```

- 启动消费者

```shell
java -Dservicecomb_service_enableSpringRegister=true -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar spring-cloud-router-consumer.jar
```

- 启动生产者

```shell
java -Dservicecomb_service_enableSpringRegister=true -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar spring-cloud-router-provider.jar
```

- 启动标签生产者（版本为1.0.1，标签为group:gray）

```shell
java -Dservicecomb_service_enableSpringRegister=true -Dservice_meta_version=1.0.1 -Dservice_meta_parameters=group:gray -Dserver.port=8163 -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar spring-cloud-router-provider.jar
```

其中path需要替换为Sermant实际安装路径。

- 测试

当启动以上4个应用并正确配置路由规则后，通过http客户端工具访问<http://127.0.0.1:8170/consumer/hello/rest>，可以发现，当请求头为id: 1或者id: 2时，会路由到版本为1.0.1的provider，当不满足以上条件时，会访问到版本为1.0.0的provider

[返回**Sermant**说明文档](../../README.md)
