# Resource Monitor

[简体中文](document-zh.md) | [English](document.md)

This document introduces [resource-monitor](../../../sermant-plugins/sermant-server-monitor)

The resource monitoring module is used to monitor the usage of hardware resources such as CPU, memory, disk IO and network IO of the server where the host application is located, as well as the usage of the host application Java virtual machine and the Druid database connection pool used by it. The resource monitoring module will start several monitoring threads, according to the [configuration file](../../../sermant-plugins/sermant-server-monitor/config/config.yaml)  
Collect monitoring data at the specified collection time interval, and then send it to the background at the specified sending interval. For the content of the configuration file, refer to [Configuration file description](#Configuration file content description)

## Directory Description

- `config`: config file directory
- `connection-pool-collect-plugin`: Druid database connection pool collection plugin
- `databasepeer-parse-service`: Database peer parsing service, used to parse database connection URLs into database
  peers in IP:PORT format
- `metric-server`: The indicator server receives the monitoring data collected by the Agent and persists it to the
  InfluxDB time series database, and provides a Restful interface for querying the persisted indicator data.
- `monitor-common`: Common library for monitoring service and plugin dependencies
- `protocol`: protobuf protocol file definition module
- `server-monitor-service`: Server hardware resources and JVM monitoring and collection services

## Configuration File Content Description

```yaml
service.config: #Host Application Service Configuration
  service: your-service                    #service name
  serviceInstance: your-service-instance   #service instance
server.monitor: #Server monitoring configuration
  collectInterval: 1                       #Monitoring data collection interval
  consumeInterval: 60                      #Monitoring data sending interval
  timeunit: SECONDS                        #Acquisition and transmission interval time unit
druid.monitor: #Druid database connection pool monitoring configuration
  collectInterval: 1                       #Monitoring data collection interval
  consumeInterval: 10                      #Monitoring data sending interval
  timeunit: SECONDS                        #Acquisition and transmission interval time unit
```

## Connection-Pool-Collect-Plugin Description

*use background*

This plugin requires the host application to use the Druid database connection pool.

*function Description*

The plugin intercepts the com.alibaba.druid.stat.DruidDataSourceStatManager#addDataSource method and gets the method
parameter com.alibaba.druid.pool.DruidDataSource instance stored in memory. Then, the collection thread periodically
collects all data information of com.alibaba.druid.pool.DruidDataSource and sends it to the metric-server.

*collected content*

```
string name; // data source name
int32 activeCount;
int32 initialSize;
int32 maxActive;
int32 poolingCount;
string databasePeer; // database peer information extracted from url

```

## Databasepeer-Parse-Service Database Service Description

*use background*

Enables the connection-pool-collect-plugin plugin.

*function Description*

org.apache.skywalking.apm.plugin.jdbc.connectionurl.parser using
the [apm-jdbc-commons](https://mvnrepository.com/artifact/org.apache.skywalking/apm-jdbc-commons) package. URLParser
parses the url of DruidDataSource into database peer.

## Server-Monitor-Service Service Description

*use background*

This service includes three monitoring and collection sub-functions, namely, Linux resource monitoring and collection,
IBM JDK JVM memory monitoring and collection, and OpenJDK JVM memory monitoring and collection, among which:

- The Linux resource monitoring and collection function requires the host application to be deployed in the Linux
  environment.
- IBM JDK JVM memory monitoring and collection function requires the host application to use IBM JDK.
- The OpenJDK JVM memory monitoring and collection function requires the host application to use OpenJDK or an
  OpenJDK-based JDK version.

*function Description*

- **Linux resource monitoring and collection**: Periodically collect CPU, memory, disk IO, and network IO resource usage
  data obtained by executing Linux commands and send them to metric-server. The executed commands are shown in the
  following table.
  ```shell
  #CPU
  cat /proc/stat
  #MEMORY
  cat /proc/meminfo
  #DISK
  cat /proc/diskstats
  #NETWORK
  cat /proc/net/dev
  ```

  Collect content

  ```
  CPU
  int32 idlePercentage; // Percentage of idle time, the precision is 0
  int32 ioWaitPercentage; // The percentage of io wait time, the precision is 0
  int32 sysPercentage;  // Sys time percentage, the precision is 0
  int32 userPercentage; // Percentage of user and nice time, the precision is 0
  ```

  ```
  memory usage  
  int64 memoryTotal; // total memory size
  int64 swapCached; // SwapCached corresponding to the cat /proc/meminfo command
  int64 cached; // Cached corresponding to the cat /proc/meminfo command
  int64 buffers; // Buffers corresponding to the cat /proc/meminfo command
  int64 memoryUsed; // used memory size
  ```

  ```
  Disk IO
  string deviceName; // Device name
  int64 readBytesPerSec; // Bytes read per second during the acquisition period
  int64 writeBytesPerSec; // Bytes written per second during the acquisition period
  double ioSpentPercentage; // Percentage of time spent in IO during the collection period, the precision is 2
  ```

  ```
  network IO  
  int64 readBytesPerSec; // Bytes read per second during the acquisition period
  int64 writeBytesPerSec; // Bytes written per second during the acquisition period
  int64 readPackagesPerSec; // The number of packets read per second during the acquisition period
  int64 writePackagesPerSec; // The number of packets written per second during the acquisition period
  ```

- **IBM JDK JVM memory monitoring and collection**: Obtain the usage data of each memory area of JVM from
  java.lang.management.MemoryPoolMXBean regularly and send it to metric-server.

  Collect content

  ```
  MemoryUsage data collected by various types of MemoryPoolMXBean:
  int64 init; // the initial amount of memory in bytes that the Java virtual machine allocates
  int64 max; // the maximum amount of memory in bytes that can be used
  int64 used; // the amount of used memory in bytes
  int64 committed; // the amount of committed memory in bytes

  Types include:
  tenured-SOA
  tenured-LOA
  nursery-allocate
  nursery-survivor
  class storage
  miscellaneous non-heap storage
  JIT code cache
  JIT data cache
  ```

- **OpenJDK JVM memory monitoring collection**
  ：Class org.apache.skywalking.apm.agent.core.jvm.cpu using
  the [apm-agent-core](https://mvnrepository.com/artifact/org.apache.skywalking/apm-agent-core) package. CPUProvider
  collects CPU data regularly, org.apache.skywalking.apm.agent.core.jvm.memory.MemoryProvider collects memory data,
  org.apache.skywalking.apm.agent.core.jvm.memorypool.MemoryPoolProvider collects data in each area of JVM,
  org.apache.skywalking.apm.agent.core.jvm.gc.GCProvider collects GC data, and
  org.apache.skywalking.apm.agent.core.jvm.thread.ThreadProvider collects thread data.

  Collect content

  ```
  CPU
  double usagePercent； // use percentage
  ```

  ```
  Memory
  bool isHeap; // Whether it is heap memory
  int64 init;
  int64 max;
  int64 used;
  int64 committed; 
  ```

  ```
  Memory Pool
  PoolType type; // type
  int64 init;
  int64 max;
  int64 used;
  int64 committed; 

  Types include:
  CODE_CACHE_USAGE;
  NEWGEN_USAGE;
  OLDGEN_USAGE;
  SURVIVOR_USAGE;
  PERMGEN_USAGE;
  METASPACE_USAGE;
  ```

  ```
  GC
  GCPhrase phrase; // GC stage：NEW or OLD
  int64 count; // GC times
  int64 time; //GC time consuming
  ```

  ```
  thread
  int64 liveCount;
  int64 daemonCount;
  int64 peakCount;
  ```

## Metric-Server Background Description

### Background Startup Method:

After the project is packaged, open the `server/server-monitor` directory, then open the console and execute the
following command to start the background.

```shell
java -jar metric-server-x.x.x.jar
```

### Preparation Before Starting The Background:

- Start the InfluxDB database (version 2.x). For the InfluxDB database, please refer
  to [InfluxDB official documentation](https://docs.influxdata.com/influxdb/v2.1/).
- Start zookeeper and kafka.
- Start sermant-backend.
- Correctly configure background information, including InfluxDB and kafka connection information.

### Background Configuration File:

The configuration file application.yml is in the BOOT-INF/classes directory of the metric-server-x.x.x.jar package

```yaml
#Kafka config
spring:
  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.ByteArrayDeserializer
      max-poll-records: 1000
      auto-offset-reset: latest
#Metric Rest Query Interface Configuration
server:
  port: 9998 #port
#InfluxDB config
influx:
  token: yourTocken
  bucket: yourBucket
  url: http://localhost:9096
  org: yourOrg
```

[return **Sermant** Documentation](../../README.md)
