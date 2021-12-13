# resource-monitor

资源监控模块用于监控宿主应用所在服务器的CPU、内存、磁盘IO和网络IO等硬件资源的使用情况，以及宿主应用Java虚拟机和其所使用Druid数据库连接池的使用情况。

*环境依赖*

connection-pool-collect-plugin需要宿主应用使用Druid数据库连接池。

server-monitor-service服务包含三个监控采集子功能，分别为Linux资源监控采集、IBM JDK JVM 内存监控采集和OpenJDK JVM 内存监控采集，其中：                        
- Linux资源监控采集功能需要宿主应用部署在Linux环境。
- IBM JDK JVM 内存监控采集功能需要宿主应用使用IBM JDK。
- OpenJDK JVM 内存监控采集功能需要宿主应用使用OpenJDK或者基于OpenJDK的JDK版本。

### 后台启动方式

项目打包完成后，打开 `项目输出目录/server/server-monitor` 目录，然后打开控制台执行以下命令启动后台。
```shell
java -jar metric-server-x.x.x.jar
```

### 启动后台前准备事项

- 启动InfluxDB数据库(2.x版本)。有关InfluxDB数据库，请查阅[InfluxDB官方文档](https://docs.influxdata.com/influxdb/v2.1/)。
- 启动zookeeper和kafka。
- 启动sermant-backend。
- 正确配置后台信息，包括InfluxDB和kafka连接信息。

### 后台配置文件：
配置文件application.yml在metric-server-x.x.x.jar包的BOOT-INF/classes目录下
```yaml
#Kafka配置
spring:
  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.ByteArrayDeserializer
      max-poll-records: 1000
      auto-offset-reset: latest
#指标Rest查询接口配置
server:
  port: 9998 #端口号
#InfluxDB配置
influx:
  token: yourTocken
  bucket: yourBucket
  url: http://localhost:9096
  org: yourOrg
```

[说明文档](../../docs/user-guide/server-monitor/document.md)