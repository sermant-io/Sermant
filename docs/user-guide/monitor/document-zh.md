# monitor

本文档主要用于[monitor模块](../../../sermant-plugins/sermant-monitor)的使用说明

资源监控模块用于监控宿主应用所在服务器的CPU、内存、磁盘IO和网络IO等硬件资源的使用情况，以及宿主应用Java虚拟机的使用情况。
监控模块依赖于prometheus进行指标收集,prometheus定期调用java agent的httpServer服务，获取插件注册的指标信息，并进行存储展示。

## 目录说明

- `config`: 配置文件目录
- `monitor-service`: 服务器硬件资源和JVM监控采集服务

## 配置文件内容说明
```yaml
monitor.config:                       # 监控服务配置
  enable-start-service: false         # 监控服务启动开关
```

## monitor-service插件说明

*使用背景*

本服务包含两个采集子服务，分别为Linux资源监控采集、JVM资源监控

- Linux资源监控采集功能需要宿主应用部署在Linux环境。
- JVM内存监控采集功能需要宿主应用使用OpenJDK或者基于OpenJDK的JDK版本

*功能说明*

- **Linux资源监控采集**：通过执行linux命令获取系统CPU、内存、磁盘IO、网络IO资源使用情况数据，并注册到prometheus的默认注册器。
```shell
  #CPU
  cat /proc/stat
  #MEMORY
  cat /proc/meminfo
  #DISK
  cat /proc/diskstats
  #NETWORK
  cat /proc/net/dev
  #CPU CORE
  lscpu
  ```
- **采集内容**
```
  CPU
  double cpu_user;  // 用户态时间占比
  double cpu_sys;   // 系统时间占比
  double cpu_wait;  // 等待时间百分占比
  double cpu_idle;  // 空闲时间占比
  double cpu_cores; // CPU物理核心数
```

```
  内存使用情况  
  double memory_total; // 总内存大小
  double memory_swap;  // 对应cat /proc/meminfo指令的SwapCached
  double memory_cached; // 对应cat /proc/meminfo指令的Cached
  double memory_buffer; // 对应cat /proc/meminfo指令的Buffers
  double memory_used; // 已使用的内存大小
```

```
  内存使用情况  
  double memory_total; // 总内存大小
  double memory_swap;  // 对应cat /proc/meminfo指令的SwapCached
  double memory_cached; // 对应cat /proc/meminfo指令的Cached
  double memory_buffer; // 对应cat /proc/meminfo指令的Buffers
  double memory_used; // 已使用的内存大小
```

```
  磁盘IO
  double disk_readBytesPerSec;   // 采集周期内的每秒读字节数
  double disk_writeBytesPerSec;  // 采集周期内的每秒写字节数
  double disk_ioSpentPercentage; // 采集周期内，IO花费的时间百分占比
```

```
  网络
  double network_readBytesPerSec;    // 采集周期内的每秒读字节数
  double network_writeBytesPerSec;   // 采集周期内的每秒写字节数
  double network_readPackagePerSec;  // 采集周期内的每秒读包数
  double network_writePackagePerSec; // 采集周期内的每秒写包数
```

- **JVM监控采集**：定时从java.lang.management.ManagementFactory获取JVM的指标情况

```
    JVM内存
    double heap_memory_init;      // 堆内存初始化值
    double heap_memory_max;       // 堆内存最大值
    double heap_memory_used       // 堆内存已使用
    double heap_memory_committed  // 堆内存已提交
    
    double non_heap_memory_init;      // 非堆内存初始化值
    double non_heap_memory_max;       // 非堆内存最大值
    double non_heap_memory_used       // 非堆内存已使用
    double non_heap_memory_committed  // 非堆内存已提交
    
    double code_cache_init;      // 代码缓存区初始化值
    double code_cache_max;       // 代码缓存区最大值
    double code_cache_used       // 代码缓存区已使用
    double code_cache_committed  // 代码缓存区已提交
    
    double meta_sapce_init;      // 元空间初始化值
    double meta_sapce_max;       // 元空间最大值
    double meta_sapce_used       // 元空间已使用
    double meta_sapce_committed  // 元空间已提交
    
    double compressed_class_space_init;      // 压缩类空间初始化值
    double compressed_class_space_max;       // 压缩类空间最大值
    double compressed_class_space_used       // 压缩类空间已使用
    double compressed_class_space_committed  // 压缩类空间已提交
    
    double eden_init;      // eden区内存初始化值
    double eden_max;       // eden区内存最大值
    double eden_used       // eden区内存已使用
    double eden_committed  // eden区内存已提交
    
    double survivor_init;      // survivor区内存初始化值
    double survivor_max;       // survivor区内存最大值
    double survivor_used       // survivor区内存已使用
    double survivor_committed  // survivor区内存已提交
    
    double old_gen_init;      // 老年代内存初始化值
    double old_gen_max;       // 老年代内存最大值
    double old_gen_used       // 老年代内存已使用
    double old_gen_committed  // 老年代内存已提交
```

```
    线程
    double thread_live;   // 活动线程
    double thread_peak;   // 线程峰值
    double thread_daemon; // 守护线程
```

```
    GC
    double new_gen_count;  // 年轻代GC次数
    double new_gen_spend;  // 年轻代GC耗时
    double old_gen_count;  // 老年代GC次数
    double old_gen_spend;  // 老年代GC耗时
```

```
    JVM其他指标
    double cpu_used;   JVM占用CPU情况
    double start_time; JVM已经启动时间，毫秒数
```

*使用说明*

1、修改sermant-agentcore-config监控配置--config.properties。修改对外提供服务的IP 端口 以及开关
```yaml
monitor.service.address=127.0.0.1     // 修改为宿主服务IP
monitor.service.port=12345            // 修改为对外提供服务端口
monitor.service.isStartMonitor=false  // 对外服务开关 -- 开关true时prometheus可以调用服务端口获取指标信息
```

2、修改sermant-monitor的配置文件--config.properties。
```yaml
monitor.config:                       # 监控服务配置
  enable-start-service: false         # 监控服务启动开关
```
修改enable-start-service为true

3、修改prometheus的配置文件. 在scrape_configs下增加对应的job信息（根据第一步配置的内容）

4、宿主应用挂载java agent之后即可在prometheus看到对应的指标信息。







