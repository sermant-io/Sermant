# 统一数据发送

[简体中文](service_send-zh.md) | [English](service_send.md)

本文档主要介绍**核心模块**的[统一网关数据发送服务](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/send)

`GatewayClient`：统一数据发送服务，简称统一发送服务，用于把Agent数据发往sermant-backend，再由sermant-backend转发给Kafka。`GatewayClient` 是数据发送的客户端。

用户通过调用服务提供的`send(byte[] data, int typeNum)`方法来发送数据。
```java
ServiceManager.getService(GatewayClient.class).send(data, typeNum);
```
其中参数`data`为待发送数据的字节，参数`typeNum`为数据的类型编号，它是某特定类型数据在所对接的sermant-backend的唯一标识编号，即同一个类型编号在一个sermant-backend实例或集群内只能表示一种类型数据。sermant-backend通过该编号来定位到相应的Kafka topic，以正确地把数据转发到该数据类型对应的topic当中。数据类型以及编号定义在[Message.proto文件](../../sermant-agentcore/sermant-agentcore-core/src/main/proto/Message.proto)的`ServiceData`元素的`DataType`当中。格式为``DATA_TYPE = typeNum``，其中`DATA_TYPE`为数据类型，`typeNum`为类型编号。

```protobuf
message ServiceData {
  enum DataType {
    DATA_TYPE_1 = 0,
    DATA_TYPE_2 = 1,
    ...
    DATA_TYPE_N = typeNumN;
  }
}
```

相应的，在sermant-backend模块的[Message.proto文件](../../sermant-backend/src/main/proto/Message.proto)也要添加相应的数据类型和编号，内容和格式与上面相同。

数据类型与Kafka topic之间映射关系的配置在sermant-backend模块的配置文件[application.properties](../../sermant-backend/src/main/resources/application.properties)的`datatype.topic.mapping`元素当中，配置规则为``datatype.topic.mapping.${typeNum} = kafka-topic``。

```properties
datatype.topic.mapping.0=topic-zero
datatype.topic.mapping.1=topic-one
...
datatype.topic.mapping.n=topic-n
```

[返回**Sermant**说明文档](../README-zh.md)
