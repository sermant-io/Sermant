/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.monitor.common;

/**
 * 性能指标枚举类
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public enum MetricEnum {
    /**
     * JVM内存指标信息
     */
    CPU_USED("cpu_used", "the number is cpu used of jvm"),

    /**
     * 服务启动时间
     */
    START_TIME("start_time", "the number is start time of jvm"),

    /**
     * 堆内存初始化
     */
    HEAP_MEMORY_INIT("heap_memory_init", "the number is init of heap memory"),

    /**
     * 堆内存最大值
     */
    HEAP_MEMORY_MAX("heap_memory_max", "the number is max of heap memory"),

    /**
     * 堆内存已使用
     */
    HEAP_MEMORY_USED("heap_memory_used", "the number is used of heap memory"),

    /**
     * 堆内存提交值
     */
    HEAP_MEMORY_COMMITTED("heap_memory_committed", "the number is committed of heap memory"),

    /**
     * 非堆内存初始化
     */
    NON_HEAP_MEMORY_INIT("non_heap_memory_init", "the number is init of non heap memory"),

    /**
     * 非堆内存最大值
     */
    NON_HEAP_MEMORY_MAX("non_heap_memory_max", "the number is max of non heap memory"),

    /**
     * 非堆内存使用
     */
    NON_HEAP_MEMORY_USED("non_heap_memory_used", "the number is used of non heap memory"),

    /**
     * 非堆内存提交
     */
    NON_HEAP_MEMORY_COMMITTED("non_heap_memory_committed", "the number is committed of non heap memory"),

    /**
     * codeCache初始值
     */
    CODE_CACHE_INIT("code_cache_init", "the number is init of code cache"),

    /**
     * codecCache 最大值
     */
    CODE_CACHE_MAX("code_cache_max", "the number is max of code cache"),

    /**
     * codeCache使用值
     */
    CODE_CACHE_USED("code_cache_used", "the number is used of code cache"),

    /**
     * codeCache提交值
     */
    CODE_CACHE_COMMITTED("code_cache_committed", "the committed is init of code cache"),

    /**
     * meta space 初始值
     */
    META_SPACE_INIT("meta_space_init", "the number is init of meta space"),

    /**
     * meta space 最大值
     */
    META_SPACE_MAX("meta_space_max", "the number is max of meta space"),

    /**
     * meta space 已使用值
     */
    META_SPACE_USED("meta_space_used", "the number is used of meta space"),

    /**
     * meta space 提交值
     */
    META_SPACE_COMMITTED("meta_space_committed", "the committed is init of meta space"),

    /**
     * class加载初始值
     */
    COMPRESSED_CLASS_SPACE_INIT("compressed_class_space_init", "the number is init of compressed class space"),

    /**
     * class加载最大值
     */
    COMPRESSED_CLASS_SPACE_MAX("compressed_class_space_max", "the number is max of compressed class space"),

    /**
     * class加载器已使用内存
     */
    COMPRESSED_CLASS_SPACE_USED("compressed_class_space_used", "the number is used of compressed class space"),

    /**
     * class加载器已提交内存
     */
    COMPRESSED_CLASS_SPACE_COMMITTED("compressed_class_space_committed", "the committed is init of compressed class "
            + "space"),

    /**
     * eden区初始化内存
     */
    EDEN_INIT("eden_init", "the number is init of eden"),

    /**
     * eden区内存最大占用
     */
    EDEN_MAX("eden_max", "the number is max of eden"),

    /**
     * eden区已使用内存
     */
    EDEN_USED("eden_used", "the number is used of eden"),

    /**
     * eden区已提交内存
     */
    EDEN_COMMITTED("eden_committed", "the committed is init of eden"),

    /**
     * survivor区初始化内存
     */
    SURVIVOR_INIT("survivor_init", "the number is init of survivor"),

    /**
     * survivor区最大内存占用
     */
    SURVIVOR_MAX("survivor_max", "the number is max of survivor"),

    /**
     * survivor区已使用内存
     */
    SURVIVOR_USED("survivor_used", "the number is used of survivor"),

    /**
     * survivor区已提交占用
     */
    SURVIVOR_COMMITTED("survivor_committed", "the committed is init of survivor"),

    /**
     * 老年代初始化内存占用
     */
    OLD_GEN_INIT("old_gen_init", "the number is init of old gen"),

    /**
     * 老年代最大内存占用
     */
    OLD_GEN_MAX("old_gen_max", "the number is max of old gen"),

    /**
     * 老年代已使用占用
     */
    OLD_GEN_USED("old_gen_used", "the number is used of old gen"),

    /**
     * 老年代已提交内存占用
     */
    OLD_GEN_COMMITTED("old_gen_committed", "the committed is init of old gen"),

    /**
     * 活动线程
     */
    THREAD_LIVE("thread_live", "the number is live of thread"),

    /**
     * 非守护线程
     */
    THREAD_PEAK("thread_peak", "the number is peak of thread"),

    /**
     * 守护线程
     */
    THREAD_DAEMON("thread_daemon", "the number is daemon of thread"),

    /**
     * 年轻代收集次数
     */
    NEW_GEN_COUNT("new_gen_count", "the number is count of new gen"),

    /**
     * 年轻代收集耗时
     */
    NEW_GEN_SPEND("new_gen_spend", "the number is spend of new gen"),

    /**
     * 老年代收集次数
     */
    OLD_GEN_COUNT("old_gen_count", "the number is count of new gen"),

    /**
     * 老年代收集耗时
     */
    OLD_GEN_SPEND("old_gen_spend", "the number is spend of new gen"),

    /**
     * CPU用户态
     */
    CPU_USER("cpu_user", "the number is user of cpu"),

    /**
     * CPU系统
     */
    CPU_SYS("cpu_sys", "the number is sys of cpu"),

    /**
     * CPU等待
     */
    CPU_WAIT("cpu_wait", "the number is wait of cpu"),

    /**
     * CPU空闲
     */
    CPU_IDLE("cpu_idle", "the number is idle of cpu"),

    /**
     * CPU核心数
     */
    CPU_CORES("cpu_cores", "the number is cores of cpu"),

    /**
     * 内存总值
     */
    MEMORY_TOTAL("memory_total", "the number is total of memory"),

    /**
     * SWAP内存大小
     */
    MEMORY_SWAP("memory_swap", "the number is swap of memory"),

    /**
     * BUFFER内存大小
     */
    MEMORY_BUFFER("memory_buffer", "the number is buffer of jvm memory"),

    /**
     * 已使用内存大小
     */
    MEMORY_USED("memory_used", "the number is used of jvm memory"),

    /**
     * 缓存内存大小
     */
    MEMORY_CACHE("memory_cached", "the number is cached of memory"),

    /**
     * 网络读取速度
     */
    NETWORK_READ_BYTE_SPEED("network_readBytesPerSec", "the number is read speed of network"),

    /**
     * 网络写速度
     */
    NETWORK_WRITE_BYTE_SPEED("network_writeBytesPerSec", "the number is write speed of network"),

    /**
     * 网络读包速度
     */
    NETWORK_READ_PACKAGE_SPEED("network_readPackagePerSec", "the number is read package speed of network"),

    /**
     * 网络写包速度
     */
    NETWORK_WRITE_PACKAGE_SPEED("network_writePackagePerSec", "the number is cached of memory"),

    /**
     * 磁盘读速度
     */
    DISK_READ_BYTE_SPEED("disk_readBytesPerSec", "the number is read byte speed of disk"),

    /**
     * 磁盘写速度
     */
    DISK_WRITE_BYTE_SPEED("disk_writeBytesPerSec", "the number is buffer of memory"),

    /**
     * 磁盘繁忙情况
     */
    DISK_IO_SPENT("disk_ioSpentPercentage", "the number is used of memory"),

    /**
     * 每秒请求数
     */
    QPS("qps", "the number is request number of per second"),

    /**
     * 每秒处理事务数
     */
    TPS("tps", "the number is number of transactions processed per second"),

    /**
     * 平均响应时间
     */
    AVG_RESPONSE_TIME("avg_response_time", "the number is number of response time"),

    /**
     * JDK11存储其他类型编译代码的codeCache初始值
     */
    NON_NMETHODS_INIT("non_nmethods_init", "the number is init of non_nmethods"),

    /**
     * JDK11存储其他类型编译代码的codeCache使用值
     */
    NON_NMETHODS_USED("non_nmethods_used", "the number is used of non_nmethods"),

    /**
     * JDK11存储其他类型编译代码的codeCache最大值
     */
    NON_NMETHODS_MAX("non_nmethods_max", "the number is max of non_nmethods"),

    /**
     * JDK11存储其他类型编译代码的codeCache提交值
     */
    NON_NMETHODS_COMMITTED("non_nmethods_committed", "the number is committed of non_nmethods"),

    /**
     * JDK11存储经过性能分析的编译代码的codeCache初始值
     */
    PROFILED_NMETHODS_INIT("profiled_nmethods_init", "the number is init of profiled nmethods"),

    /**
     * JDK11存储经过性能分析的编译代码的codeCache使用值
     */
    PROFILED_NMETHODS_USED("profiled_nmethods_used", "the number is used of profiled nmethods"),

    /**
     * JDK11存储经过性能分析的编译代码的codeCache最大值
     */
    PROFILED_NMETHODS_MAX("profiled_nmethods_max", "the number is max of profiled nmethods"),

    /**
     * JDK11存储经过性能分析的编译代码的codeCache提交值
     */
    PROFILED_NMETHODS_COMMITTED("profiled_nmethods_committed", "the number is committed of profiled nmethods"),

    /**
     * JDK11存储未经过性能分析的编译代码的codeCache初始值
     */
    NON_PROFILED_NMETHODS_INIT("non_profiled_nmethods_init", "the number is init of non-profiled nmethods"),

    /**
     * JDK11存储未经过性能分析的编译代码的codeCache使用值
     */
    NON_PROFILED_NMETHODS_USED("non_profiled_nmethods_used", "the number is used of non-profiled nmethods"),

    /**
     * JDK11存储未经过性能分析的编译代码的codeCache最大值
     */
    NON_PROFILED_NMETHODS_MAX("non_profiled_nmethods_max", "the number is max of non-profiled nmethods"),

    /**
     * JDK11存储未经过性能分析的编译代码的codeCache提交值
     */
    NON_PROFILED_NMETHODS_COMMITTED("non_profiled_nmethods_committed",
            "the number is committed of non-profiled nmethods"),

    /**
     * JDK11的Epsilon收集器初始值
     */
    EPSILON_HEAP_INIT("epsilon_heap_init", "the number is init of Epsilon Heap"),

    /**
     * JDK11的Epsilon收集器使用值
     */
    EPSILON_HEAP_USED("epsilon_heap_used", "the number is used of Epsilon Heap"),

    /**
     * JDK11的Epsilon收集器最大值
     */
    EPSILON_HEAP_MAX("epsilon_heap_max", "the number is max of Epsilon Heap"),

    /**
     * JDK11的Epsilon收集器提交值
     */
    EPSILON_HEAP_COMMITTED("epsilon_heap_committed", "the number is committed of Epsilon Heap");

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String disc;

    MetricEnum(String name, String disc) {
        this.name = name;
        this.disc = disc;
    }

    public String getName() {
        return name;
    }

    public String getDisc() {
        return disc;
    }
}
