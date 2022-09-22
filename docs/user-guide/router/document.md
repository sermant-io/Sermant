# Tag Router

[简体中文](document-zh.md) | [English](document.md)

This document is used to introduce the usage of [tag router](../../../sermant-plugins/sermant-router)

## Function

In the case of multiple versions and instances of microservices, the routing between services is managed by configuring routing rules to achieve business purposes such as lossless upgrade and application dial test.

## Usage

- Configure Routing Rules

Sermant backend provides api way to publish the configuration, you need to start the backend application before use, the configuration publishing interface is as follows: 

**URL**

POST /publishConfig

**Request Body**

|Params|Mandatory or not|Param type|Description
|---|---|---|---|
|key|√|String|configuration key|
|group|√|String|Configuration group, which is used to configure subscriptions|
|content|√|String|Configuration text, that is, specific routing rules|

The key value needs to be servicecomb.routeRule.${yourServiceName}, ${yourServiceName} is the microservice name of the target application.

The group needs to be configured to application level, i.e. app=${yourApp}&&environment=${yourEnvironment}, app defaults to default, environment defaults to empty.

The content is the specific routing rule.

### Examples of tag routing rules and descriptions are as follows

```yaml
---
- precedence: 2 # Priority, the higher the number, the higher the priority.
  match: # Request match rule. 0..N, not configured to indicate a match. Only one attachments/headers/args are allowed per match rule.
    attachments: # dubbo attachment matches. If it is an http header match, you need to configure it as headers.
      id: # If multiple keys are configured, then all key rules must match the request.
        exact: '1' # Configuration policy, equal to 1, detailed configuration policy refer to the configuration policy table.
        caseInsensitive: false # false: case-insensitive (default), true: case-sensitive. When configured to false, it will be converted to uppercase uniformly for comparison.
    args: # dubbo parameter matches
      args0: # The 0th parameter of the dubbo interface
        type: .id # Take the value type, dubbo application-specific field, the 0th parameter is an entity, get its id property value, if the parameter type is int, String and other common types, it is not necessary to fill in the value, all the value types see the table of value types.
        exact: '2' # Configuration policy, equal to 2, detailed configuration policy refer to the configuration policy table.
        caseInsensitive: false # Whether to be case-sensitive, default is false, case-sensitive.
  route: # Routing Rules
    - weight: 20 # Weight
      tags:
        version: 1.0.0 # Instance tagging. Instances that meet the tagging criteria are placed in this group.
    - weight: 80 # Weight
      tags:
        version: 1.0.1 # Instance tagging. Instances that meet the tagging criteria are placed in this group.
- precedence: 1
  route:
    - weight: 20
      tags:
        group: red
    - weight: 80
      tags:
        group: green
```

**Note: When adding a new configuration, please remove the comment, otherwise it will cause the addition to fail.**

### Configuration Policy Table

|Strategy Name|Strategy Value|Matching Rules|
|---|---|---|
|Exact Match|exact|The parameter value is equal to the configured value|
|Regex Match|regex|Parameter values match regex expressions, Since some regex expressions (such as \w and \W, etc.) are case-sensitive, please choose caseInsensitive (case-sensitive or not) carefully when using regex match|
|Not Equal Match|noEqu|The parameter value is not equal to the configuration value|
|Not Less Match|noLess|The parameter value is not less than the configured value|
|Not Greater Match|noGreater|The parameter value is not greater than the configured value|
|Greater Match|greater|The parameter value is greater than the configured value|
|Less Match|less|The parameter value is less than the configured value|

### Value Types Table

|Type|Fetch Method|Parameter types|
|---|---|---|
|Empty/Null|Indicates that the value of the current parameter is obtained directly|Applicable to common parameter types, such as String, int, long, etc.|
|.name|Denotes the name attribute of the fetch parameter, equivalent to ARG0.getName()|Applicable to object types|
|.isEnabled()|Denotes the enabled attribute of the fetch parameter, equivalent to ARG0.isEnabled()|Applicable to object types|
|[0]|Takes the first value of the array, equivalent to ARG0[0]|For arrays of common types, such as String[], int[]|
|.get(0)|Takes the first value of the list, equivalent to ARG0.get(0)|For list of common types, such as List\<String>, List\<Integer>|
|.get("key")|Get the value corresponding to the key, equivalent to ARG0.get("key")|For map of common types, such as Map<String, String>|

- Start the tag application

Add the following parameters as required at the start of the attached agent: 

```
-Dservice_meta_version=${VERSION} -Dservice_meta_parameters=${PARAMETERS}
```

The parameters are described as follows: 

- ${VERSION} needs to be replaced with the version number at the time of service registration (The format of a.b.c, where a,b,c are numbers and the default is 1.0.0). The tag application needs to be modified to a different version number than the normal application.
- ${PARAMETERS} needs to be replaced with the custom tag from the service registration (Such as tag1:value1, tag2:value2). That is, tag keys and tag values are separated by colons, and multiple tags are separated by commas.
- In general, only service_meta_version needs to be configured if routing by version number, or service_meta_parameters if routing by custom tag.

## Result Verification

- Prerequisites [correctly packed Sermant](../../README.md)

- Registration center using Huawei CSE, download [Local-CSE](https://support.huaweicloud.com/devg-cse/cse_devg_0036.html) ，解压后按照文档说明进行启动

- Configuring Routing Rules

Calling the interface `localhost:8900/publishConfig`, with the following request parameters:

```json
{
   "content": "---\n- precedence: 1\n  match:\n    headers:\n        id:\n          exact: '1'\n          caseInsensitive: false\n  route:\n    - tags:\n        group: gray\n      weight: 100\n- precedence: 2\n  match:\n    headers:\n        id:\n          exact: '2'\n          caseInsensitive: false\n  route:\n    - tags:\n        version: 1.0.1\n      weight: 100", 
   "group": "app=default&&environment=", 
   "key": "servicecomb.routeRule.spring-cloud-router-provider"
}
```

- Compile [demo application](https://github.com/huaweicloud/Sermant-examples/tree/main/router-demo/spring-cloud-router-demo)

```shell
mvn clean package
```

- Start the zuul gateway

```shell
java -Dservicecomb_service_enableSpringRegister=true -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar spring-cloud-router-zuul.jar
```

- Start the consumer

```shell
java -Dservicecomb_service_enableSpringRegister=true -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar spring-cloud-router-consumer.jar
```

- Start the provider

```shell
java -Dservicecomb_service_enableSpringRegister=true -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar spring-cloud-router-provider.jar
```

- Start the provider with tag (version is 1.0.1, tag is group:gray.)

```shell
java -Dservicecomb_service_enableSpringRegister=true -Dservice_meta_version=1.0.1 -Dservice_meta_parameters=group:gray -Dserver.port=8163 -javaagent:${path}\agent\sermant-agent.jar=appName=default -jar spring-cloud-router-provider.jar
```

${path} needs to be replaced with the actual Sermant installation path.

- Testing

After starting the above 4 applications and configuring the routing rules correctly, when accessing <http://127.0.0.1:8170/consumer/hello/rest> through the http client tool, we can find that when the request header is id: 1 or id: 2, it will be routed to the provider of version 1.0.1, and when the above conditions are not met When the above condition is not met, it will visit the provider with version 1.0.0.

[Back to README of **Sermant** ](../../README.md)