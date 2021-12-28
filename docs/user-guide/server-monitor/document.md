# resource-monitor

本文档针对[resource-monitor模块](../../../sermant-plugins/sermant-server-monitor)作介绍

 资源监控模块用于监控宿主应用所在服务器的CPU、内存、磁盘IO和网络IO等硬件资源的使用情况，以及宿主应用Java虚拟机和其所使用Druid数据库连接池的使用情况。资源监控模块会启动若干个监控线程，按照[配置文件](../../../sermant-plugins/sermant-server-monitor/config/config.yaml)所指定的采集时间间隔去采集监控数据，再按指定的发送间隔时间发往后台，配置文件内容参照[配置文件说明](#配置文件内容说明)

## 目录说明

- `config`: 配置文件目录
- `connection-pool-collect-plugin`: Druid数据库连接池采集插件
- `databasepeer-parse-service`: Database peer解析服务，用于把数据库连接URL解析成IP:PORT格式的database peer
- `metric-server`: 指标服务器，接收Agent采集的监控数据持久化到InfluxDB时序数据库中，并提供用于查询已持久化指标数据的Restful接口。
- `monitor-common`: 监控服务和插件依赖的公共类库
- `protocol`: protobuf协议文件定义模块
- `server-monitor-service`: 服务器硬件资源和JVM监控采集服务

## 配置文件内容说明
```yaml
service.config:                            #宿主应用服务配置
  service: your-service                    #服务名
  serviceInstance: your-service-instance   #服务实例名
server.monitor:                            #服务器监控配置
  collectInterval: 1                       #监控数据采集间隔
  consumeInterval: 60                      #监控数据发送间隔
  timeunit: SECONDS                        #采集和发送间隔时间单位
druid.monitor:                             #Druid数据库连接池监控配置
  collectInterval: 1                       #监控数据采集间隔
  consumeInterval: 10                      #监控数据发送间隔
  timeunit: SECONDS                        #采集和发送间隔时间单位
```

## connection-pool-collect-plugin插件说明

*使用背景*

该插件需要宿主应用使用Druid数据库连接池。

*功能说明*

插件拦截com.alibaba.druid.stat.DruidDataSourceStatManager#addDataSource方法，拿到方法参数com.alibaba.druid.pool.DruidDataSource实例
存到内存当中。然后由采集线程定时采集所有com.alibaba.druid.pool.DruidDataSource的数据息发往metric-server。

采集内容

```
string name; // 数据源名称
int32 activeCount;
int32 initialSize;
int32 maxActive;
int32 poolingCount;
string databasePeer; // 从url中提取的database peer信息

```

## databasepeer-parse-service Database服务说明

*使用背景*

为connection-pool-collect-plugin插件赋能。

*功能说明*

使用了[apm-jdbc-commons](https://mvnrepository.com/artifact/org.apache.skywalking/apm-jdbc-commons)包的org.apache.skywalking.apm.plugin.jdbc.connectionurl.parser.URLParser把DruidDataSource的url解析成database peer。

## server-monitor-service服务说明

*使用背景*

本服务包含三个监控采集子功能，分别为Linux资源监控采集、IBM JDK JVM 内存监控采集和OpenJDK JVM 内存监控采集，其中：

- Linux资源监控采集功能需要宿主应用部署在Linux环境。
- IBM JDK JVM 内存监控采集功能需要宿主应用使用IBM JDK。
- OpenJDK JVM 内存监控采集功能需要宿主应用使用OpenJDK或者基于OpenJDK的JDK版本。

*功能说明*

- **Linux资源监控采集**：定时采集由执行Linux指令获取的Linux系统CPU、内存、磁盘IO、网络IO资源使用情况数据，发往metric-server，其中被执行的指令如下表所示。
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

  采集内容

  ```
  CPU
  int32 idlePercentage; // idle时间百分占比，精度0
  int32 ioWaitPercentage; // io wait时间百分占比，精度0
  int32 sysPercentage;  // sys时间百分占比，精度0
  int32 userPercentage; // user和nice时间百分占比，精度0
  ```
  
  ```
  内存使用情况  
  int64 memoryTotal; // 总内存大小
  int64 swapCached; // 对应cat /proc/meminfo指令的SwapCached
  int64 cached; // 对应cat /proc/meminfo指令的Cached
  int64 buffers; // 对应cat /proc/meminfo指令的Buffers
  int64 memoryUsed; // 已使用的内存大小
  ```

  ```
  磁盘IO
  string deviceName; // 设备名称
  int64 readBytesPerSec; // 采集周期内的每秒读字节数
  int64 writeBytesPerSec; // 采集周期内的每秒写字节数
  double ioSpentPercentage; // 采集周期内，IO花费的时间百分占比，精度2
  ```

  ```
  网络IO  
  int64 readBytesPerSec; // 采集周期内的每秒读字节数
  int64 writeBytesPerSec; // 采集周期内的每秒写字节数
  int64 readPackagesPerSec; // 采集周期内的每秒读包数
  int64 writePackagesPerSec; // 采集周期内的每秒写包数
  ```

- **IBM JDK JVM 内存监控采集**：定时从java.lang.management.MemoryPoolMXBean获取JVM各内存区域的使用情况数据发往metric-server。

  采集内容

  ```
  各类型MemoryPoolMXBean采集的MemoryUsage数据：
  int64 init; // the initial amount of memory in bytes that the Java virtual machine allocates
  int64 max; // the maximum amount of memory in bytes that can be used
  int64 used; // the amount of used memory in bytes
  int64 committed; // the amount of committed memory in bytes

  类型包含：
  tenured-SOA
  tenured-LOA
  nursery-allocate
  nursery-survivor
  class storage
  miscellaneous non-heap storage
  JIT code cache
  JIT data cache
  ```

- **OpenJDK JVM 内存监控采集**：使用[apm-agent-core](https://mvnrepository.com/artifact/org.apache.skywalking/apm-agent-core)包的类org.apache.skywalking.apm.agent.core.jvm.cpu.CPUProvider定时采集CPU数据，org.apache.skywalking.apm.agent.core.jvm.memory.MemoryProvider采集内存数据，org.apache.skywalking.apm.agent.core.jvm.memorypool.MemoryPoolProvider采集JVM各区域数据，org.apache.skywalking.apm.agent.core.jvm.gc.GCProvider采集GC数据，org.apache.skywalking.apm.agent.core.jvm.thread.ThreadProvider采集线程数据。

  采集内容
  
  ```
  CPU
  double usagePercent； // 使用百分比
  ```

  ```
  内存
  bool isHeap; // 是否为堆内存
  int64 init;
  int64 max;
  int64 used;
  int64 committed; 
  ```

  ```
  Memory Pool
  PoolType type; // 类型
  int64 init;
  int64 max;
  int64 used;
  int64 committed; 

  类型包含：
  CODE_CACHE_USAGE;
  NEWGEN_USAGE;
  OLDGEN_USAGE;
  SURVIVOR_USAGE;
  PERMGEN_USAGE;
  METASPACE_USAGE;
  ```

  ```
  GC
  GCPhrase phrase; // GC阶段：NEW或OLD
  int64 count; // GC次数
  int64 time; //GC耗时
  ```

  ```
  线程
  int64 liveCount;
  int64 daemonCount;
  int64 peakCount;
  ```


## metric-server后台说明

### 后台启动方式：

项目打包完成后，打开 `项目输出目录/server/server-monitor` 目录，然后打开控制台执行以下命令启动后台。
```shell
java -jar metric-server-x.x.x.jar
```

### 启动后台前准备事项：

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

[返回**Sermant**说明文档](../../README.md)
