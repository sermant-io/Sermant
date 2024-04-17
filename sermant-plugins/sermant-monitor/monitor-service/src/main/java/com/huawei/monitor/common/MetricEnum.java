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
 * performance indicator enumeration class
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public enum MetricEnum {
    /**
     * jvm memory indicator information
     */
    CPU_USED("cpu_used", "the number is cpu used of jvm"),

    /**
     * service start time
     */
    START_TIME("start_time", "the number is start time of jvm"),

    /**
     * heap memory initialization
     */
    HEAP_MEMORY_INIT("heap_memory_init", "the number is init of heap memory"),

    /**
     * maximum heap memory
     */
    HEAP_MEMORY_MAX("heap_memory_max", "the number is max of heap memory"),

    /**
     * the heap memory is in use
     */
    HEAP_MEMORY_USED("heap_memory_used", "the number is used of heap memory"),

    /**
     * heap memory commit value
     */
    HEAP_MEMORY_COMMITTED("heap_memory_committed", "the number is committed of heap memory"),

    /**
     * non heap memory initialization
     */
    NON_HEAP_MEMORY_INIT("non_heap_memory_init", "the number is init of non heap memory"),

    /**
     * maximum non heap memory
     */
    NON_HEAP_MEMORY_MAX("non_heap_memory_max", "the number is max of non heap memory"),

    /**
     * non heap memory usage
     */
    NON_HEAP_MEMORY_USED("non_heap_memory_used", "the number is used of non heap memory"),

    /**
     * non heap memory commit
     */
    NON_HEAP_MEMORY_COMMITTED("non_heap_memory_committed", "the number is committed of non heap memory"),

    /**
     * code cache initial value
     */
    CODE_CACHE_INIT("code_cache_init", "the number is init of code cache"),

    /**
     * codec cache maximum
     */
    CODE_CACHE_MAX("code_cache_max", "the number is max of code cache"),

    /**
     * code cache usage value
     */
    CODE_CACHE_USED("code_cache_used", "the number is used of code cache"),

    /**
     * value submitted by code cache
     */
    CODE_CACHE_COMMITTED("code_cache_committed", "the committed is init of code cache"),

    /**
     * meta space initial value
     */
    META_SPACE_INIT("meta_space_init", "the number is init of meta space"),

    /**
     * meta space maximum
     */
    META_SPACE_MAX("meta_space_max", "the number is max of meta space"),

    /**
     * meta space usage value
     */
    META_SPACE_USED("meta_space_used", "the number is used of meta space"),

    /**
     * value submitted by meta space
     */
    META_SPACE_COMMITTED("meta_space_committed", "the committed is init of meta space"),

    /**
     * class loading initial value
     */
    COMPRESSED_CLASS_SPACE_INIT("compressed_class_space_init", "the number is init of compressed class space"),

    /**
     * class load maximum
     */
    COMPRESSED_CLASS_SPACE_MAX("compressed_class_space_max", "the number is max of compressed class space"),

    /**
     * the class loader has used memory
     */
    COMPRESSED_CLASS_SPACE_USED("compressed_class_space_used", "the number is used of compressed class space"),

    /**
     * the class loader has committed memory
     */
    COMPRESSED_CLASS_SPACE_COMMITTED("compressed_class_space_committed", "the committed is init of compressed class "
            + "space"),

    /**
     * the eden area initializes memory
     */
    EDEN_INIT("eden_init", "the number is init of eden"),

    /**
     * the maximum memory usage of the eden area
     */
    EDEN_MAX("eden_max", "the number is max of eden"),

    /**
     * memory is used in the eden area
     */
    EDEN_USED("eden_used", "the number is used of eden"),

    /**
     * eden area committed memory
     */
    EDEN_COMMITTED("eden_committed", "the committed is init of eden"),

    /**
     * memory initialized by the survivor section
     */
    SURVIVOR_INIT("survivor_init", "the number is init of survivor"),

    /**
     * maximum memory usage of the survivor area
     */
    SURVIVOR_MAX("survivor_max", "the number is max of survivor"),

    /**
     * used memory in the survivor area
     */
    SURVIVOR_USED("survivor_used", "the number is used of survivor"),

    /**
     * The survivor area has been submitted for occupation
     */
    SURVIVOR_COMMITTED("survivor_committed", "the committed is init of survivor"),

    /**
     * the memory usage is initialized in the old age
     */
    OLD_GEN_INIT("old_gen_init", "the number is init of old gen"),

    /**
     * the maximum memory usage in the old age
     */
    OLD_GEN_MAX("old_gen_max", "the number is max of old gen"),

    /**
     * memory used in the old age
     */
    OLD_GEN_USED("old_gen_used", "the number is used of old gen"),

    /**
     * the old age has committed memory usage
     */
    OLD_GEN_COMMITTED("old_gen_committed", "the committed is init of old gen"),

    /**
     * activity thread
     */
    THREAD_LIVE("thread_live", "the number is live of thread"),

    /**
     * non daemon thread
     */
    THREAD_PEAK("thread_peak", "the number is peak of thread"),

    /**
     * daemon thread
     */
    THREAD_DAEMON("thread_daemon", "the number is daemon of thread"),

    /**
     * The number of collections in the younger generation
     */
    NEW_GEN_COUNT("new_gen_count", "the number is count of new gen"),

    /**
     * the younger generation takes time to collect
     */
    NEW_GEN_SPEND("new_gen_spend", "the number is spend of new gen"),

    /**
     * the number of collections in the old generation
     */
    OLD_GEN_COUNT("old_gen_count", "the number is count of new gen"),

    /**
     * it takes time to collect in the old generation
     */
    OLD_GEN_SPEND("old_gen_spend", "the number is spend of new gen"),

    /**
     * cpu user mode
     */
    CPU_USER("cpu_user", "the number is user of cpu"),

    /**
     * cpu system
     */
    CPU_SYS("cpu_sys", "the number is sys of cpu"),

    /**
     * cpu wait
     */
    CPU_WAIT("cpu_wait", "the number is wait of cpu"),

    /**
     * cpu idle
     */
    CPU_IDLE("cpu_idle", "the number is idle of cpu"),

    /**
     * number of cpu cores
     */
    CPU_CORES("cpu_cores", "the number is cores of cpu"),

    /**
     * total memory value
     */
    MEMORY_TOTAL("memory_total", "the number is total of memory"),

    /**
     * swap memory size
     */
    MEMORY_SWAP("memory_swap", "the number is swap of memory"),

    /**
     * buffer memory size
     */
    MEMORY_BUFFER("memory_buffer", "the number is buffer of jvm memory"),

    /**
     * used memory size
     */
    MEMORY_USED("memory_used", "the number is used of jvm memory"),

    /**
     * cache memory size
     */
    MEMORY_CACHE("memory_cached", "the number is cached of memory"),

    /**
     * network read speed
     */
    NETWORK_READ_BYTE_SPEED("network_readBytesPerSec", "the number is read speed of network"),

    /**
     * network write speed
     */
    NETWORK_WRITE_BYTE_SPEED("network_writeBytesPerSec", "the number is write speed of network"),

    /**
     * network packet reading speed
     */
    NETWORK_READ_PACKAGE_SPEED("network_readPackagePerSec", "the number is read package speed of network"),

    /**
     * network write packet speed
     */
    NETWORK_WRITE_PACKAGE_SPEED("network_writePackagePerSec", "the number is cached of memory"),

    /**
     * disk read speed
     */
    DISK_READ_BYTE_SPEED("disk_readBytesPerSec", "the number is read byte speed of disk"),

    /**
     * disk write speed
     */
    DISK_WRITE_BYTE_SPEED("disk_writeBytesPerSec", "the number is buffer of memory"),

    /**
     * disk busy condition
     */
    DISK_IO_SPENT("disk_ioSpentPercentage", "the number is used of memory"),

    /**
     * requests per second
     */
    QPS("qps", "the number is request number of per second"),

    /**
     * number of transactions per second
     */
    TPS("tps", "the number is number of transactions processed per second"),

    /**
     * average response time
     */
    AVG_RESPONSE_TIME("avg_response_time", "the number is number of response time"),

    /**
     * JDK11 stores codeCache initial value for other types of compiled code
     */
    NON_NMETHODS_INIT("non_nmethods_init", "the number is init of non_nmethods"),

    /**
     * JDK11 stores codeCache usage values for other types of compiled code
     */
    NON_NMETHODS_USED("non_nmethods_used", "the number is used of non_nmethods"),

    /**
     * JDK11 maximum codeCache value for storing other types of compiled code
     */
    NON_NMETHODS_MAX("non_nmethods_max", "the number is max of non_nmethods"),

    /**
     * JDK11 stores codeCache commit values for other types of compiled code
     */
    NON_NMETHODS_COMMITTED("non_nmethods_committed", "the number is committed of non_nmethods"),

    /**
     * JDK11 stores codeCache initial value of compiled code after profiling
     */
    PROFILED_NMETHODS_INIT("profiled_nmethods_init", "the number is init of profiled nmethods"),

    /**
     * JDK11 stores codeCache usage values for profiled compiled code
     */
    PROFILED_NMETHODS_USED("profiled_nmethods_used", "the number is used of profiled nmethods"),

    /**
     * JDK11 maximum codeCache size for storing profiled compiled code
     */
    PROFILED_NMETHODS_MAX("profiled_nmethods_max", "the number is max of profiled nmethods"),

    /**
     * JDK11 stores codeCache commit values for profiled compiled code
     */
    PROFILED_NMETHODS_COMMITTED("profiled_nmethods_committed", "the number is committed of profiled nmethods"),

    /**
     * JDK11 stores codeCache initial value for compiled code that has not been profiled
     */
    NON_PROFILED_NMETHODS_INIT("non_profiled_nmethods_init", "the number is init of non-profiled nmethods"),

    /**
     * JDK11 stores codeCache usage values for compiled code that has not been profiled
     */
    NON_PROFILED_NMETHODS_USED("non_profiled_nmethods_used", "the number is used of non-profiled nmethods"),

    /**
     * JDK11 maximum codeCache size for storing compiled code that has not been profiled
     */
    NON_PROFILED_NMETHODS_MAX("non_profiled_nmethods_max", "the number is max of non-profiled nmethods"),

    /**
     * JDK11 stores codeCache commit values for compiled code that has not been profiled
     */
    NON_PROFILED_NMETHODS_COMMITTED("non_profiled_nmethods_committed",
            "the number is committed of non-profiled nmethods"),

    /**
     * the epsilon collector initial value for jdk 11
     */
    EPSILON_HEAP_INIT("epsilon_heap_init", "the number is init of Epsilon Heap"),

    /**
     * epsilon collector usage values for jdk 11
     */
    EPSILON_HEAP_USED("epsilon_heap_used", "the number is used of Epsilon Heap"),

    /**
     * the maximum epsilon collector value of jdk 11
     */
    EPSILON_HEAP_MAX("epsilon_heap_max", "the number is max of Epsilon Heap"),

    /**
     * epsilon collector commit values for jdk 11
     */
    EPSILON_HEAP_COMMITTED("epsilon_heap_committed", "the number is committed of Epsilon Heap"),

    /**
     * the shenandoah collector initial value for jdk 17
     */
    SHENANDOAH_INIT("shenandoah_init", "the number is init of Shenandoah"),

    /**
     * shenandoah collector usage values for jdk 17
     */
    SHENANDOAH_USED("shenandoah_used", "the number is used of Shenandoah"),

    /**
     * maximum shenandoah collector value for jdk 17
     */
    SHENANDOAH_MAX("shenandoah_max", "the number is max of Shenandoah"),

    /**
     * shenandoah collector commit values for jdk 17
     */
    SHENANDOAH_COMMITTED("shenandoah_committed", "the number is committed of Shenandoah");

    /**
     * name
     */
    private String name;

    /**
     * description
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
