# send

本文档主要介绍**核心模块**的[统一网关数据发送服务](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huawei/sermant/core/service/send)

`GatewayClient`：统一网关（sermant-backend）数据发送服务，简称统一发送服务，用于把Agent数据发往sermant-backend，再由sermant-backend转发给Kafka。

用户通过调用服务提供的send(byte[] data, int typeNum)方法来发送数据。
```java
ServiceManager.getService(GatewayClient.class).send(data, typeNum);
```
其中参数data为待发送数据的字节，参数typeNum为数据的类型编号，它是某特定类型数据在所对接的sermant-backend的唯一标识编号，即同一个类型编号在一个sermant-backend实例或集群内只能表示一种类型数据。sermant-backend通过该编号来定位到相应的Kafka topic，以正确地把数据转发到该数据类型对应的topic当中。数据类型以及编号定义在[MessageProto.proto文件](../../sermant-agentcore/sermant-agentcore-core/src/main/proto/MessageProto.proto)的ServiceData元素的DataType当中。格式为``DATA_TYPE = typeNum``，其中DATA_TYPE为数据类型，typeNum为类型编号，多条数据之间用英文逗号“,”隔开，最后一条数据后要加上英文分号“;”。

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

数据类型与Kafka topic之间映射关系的配置在sermant-backend模块的配置文件[application.properties](../../sermant-backend/src/main/resources/application.properties)的datatype.topic.mapping元素当中，配置规则为``datatype.topic.mapping.${typeNum} = kafka-topic``。

```properties
datatype.topic.mapping.0=topic-zero
datatype.topic.mapping.1=topic-one
...
datatype.topic.mapping.n=topic-n
```

该服务的默认实现为NettyGatewayClient，它使用Netty实现与sermant-backend的交互。服务启动时，会调用ClientManager#getNettyClientFactory获取NettyClientFactory实例，然后通过NettyClientFactory#getNettyClient创建NettyClient实例。发送数据时调用NettyClient#sendData(byte[] data, int typeNum)方法来把数据发送到sermant-backend。

[返回**Sermant**说明文档](../README.md)
