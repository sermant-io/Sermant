# Unified Data Sending

[简体中文](service_send-zh.md) | [English](service_send.md)

This document mainly introduces [Unified Data Sending Service](../../sermant-agentcore/sermant-agentcore-core/src/main/java/com/huaweicloud/sermant/core/service/send) of **sermant-agentcore-core**.

`GatewayClient`：The unified gateway data sending service, is used to send data of sermant-agent to the sermant-backend. And then the sermant-backend send these data to Kafka. `GatewayClient` is used to send data of sermant-agent.

Developers can send data by invoking the `send(byte[] data, int typeNum)` method provided by `GatewayClient`.
```java
ServiceManager.getService(GatewayClient.class).send(data, typeNum);
```
The parameter `data` is the byte of the data to be sent, and the parameter `typeNum` is the type number of the data, which is the unique identification number of a specific type of data in the connected sermant-backend. That is, the same type number can only represent one type of data in sermant-backend instance or cluster. The sermant-backend uses this number to locate the Kafka topic and sends the data to the appropriate topic for that data type. The data types and numbers are defined in the `DataType` of `ServiceData` in [Message.proto](../../sermant-agentcore/sermant-agentcore-core/src/main/proto/Message.proto). The format is `DATA_TYPE = typeNum`, where `DATA_TYPE` is the data type, `typeNum` is the type number.

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

Accordingly, the corresponding data type and number should be added in the [Message.proto](../../sermant-backend/src/main/proto/Message.proto), in which content and format is the same as above.

The mapping between data types and Kafka topics is configured in the element `datatype.topic.mapping` of [application.properties](../../sermant-backend/src/main/resources/application.properties) of sermant-backend. The configuration rule is `datatype.topic.mapping.${typeNum} = kafka-topic`.

```properties
datatype.topic.mapping.0=topic-zero
datatype.topic.mapping.1=topic-one
...
datatype.topic.mapping.n=topic-n
```

[Back to README of **Sermant** ](../README.md)
