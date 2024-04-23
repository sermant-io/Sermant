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

package io.sermant.monitor.common;

import java.util.Optional;

/**
 * jvm memory performance type
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public enum MemoryType {
    /**
     * heap memory
     */
    HEAP_MEMORY("heap_memory", MetricEnum.HEAP_MEMORY_INIT, MetricEnum.HEAP_MEMORY_USED, MetricEnum.HEAP_MEMORY_MAX,
            MetricEnum.HEAP_MEMORY_COMMITTED),

    /**
     * non heap memory
     */
    NON_HEAP_MEMORY("non_heap_memory", MetricEnum.NON_HEAP_MEMORY_INIT, MetricEnum.NON_HEAP_MEMORY_USED,
            MetricEnum.NON_HEAP_MEMORY_MAX, MetricEnum.NON_HEAP_MEMORY_COMMITTED),

    /**
     * Code Cache memory pool
     */
    CODE_CACHE("Code Cache", MetricEnum.CODE_CACHE_INIT, MetricEnum.CODE_CACHE_USED, MetricEnum.CODE_CACHE_MAX,
            MetricEnum.CODE_CACHE_COMMITTED),

    /**
     * Metaspace memory pointer information
     */
    META_SPACE("Metaspace", MetricEnum.META_SPACE_INIT, MetricEnum.META_SPACE_USED, MetricEnum.META_SPACE_MAX,
            MetricEnum.META_SPACE_COMMITTED),

    /**
     * class loading
     */
    CLASS_SPACE("Compressed Class Space", MetricEnum.COMPRESSED_CLASS_SPACE_INIT,
            MetricEnum.COMPRESSED_CLASS_SPACE_USED, MetricEnum.COMPRESSED_CLASS_SPACE_MAX,
            MetricEnum.COMPRESSED_CLASS_SPACE_COMMITTED),
    /**
     * eden zone metric enumeration
     */
    EDEN_SPACE("PS Eden Space", MetricEnum.EDEN_INIT, MetricEnum.EDEN_USED, MetricEnum.EDEN_MAX,
            MetricEnum.EDEN_COMMITTED),

    /**
     * survivor metric enumeration information
     */
    SURVIVOR_SPACE("PS Survivor Space", MetricEnum.SURVIVOR_INIT, MetricEnum.SURVIVOR_USED, MetricEnum.SURVIVOR_MAX,
            MetricEnum.SURVIVOR_COMMITTED),

    /**
     * old generation metric information
     */
    OLD_GEN_SPACE("PS Old Gen", MetricEnum.OLD_GEN_INIT, MetricEnum.OLD_GEN_USED, MetricEnum.OLD_GEN_MAX,
            MetricEnum.OLD_GEN_COMMITTED),

    /**
     * young generation indicator information
     */
    EDEN_SPACE_CMS("Par Eden Space", MetricEnum.EDEN_INIT, MetricEnum.EDEN_USED, MetricEnum.EDEN_MAX,
            MetricEnum.EDEN_COMMITTED),

    /**
     * cms young generation metric information
     */
    SURVIVOR_SPACE_CMS("Par Survivor Space", MetricEnum.SURVIVOR_INIT, MetricEnum.SURVIVOR_USED,
            MetricEnum.SURVIVOR_MAX, MetricEnum.SURVIVOR_COMMITTED),

    /**
     * enumeration of old generation metric
     */
    OLD_EDEN_SPACE_CMS("CMS Old Gen", MetricEnum.OLD_GEN_INIT, MetricEnum.OLD_GEN_USED, MetricEnum.OLD_GEN_MAX,
            MetricEnum.OLD_GEN_COMMITTED),

    /**
     * SERIAL young generation Eden metric information
     */
    EDEN_SPACE_SERIAL("Eden Space", MetricEnum.EDEN_INIT, MetricEnum.EDEN_USED, MetricEnum.EDEN_MAX,
            MetricEnum.EDEN_COMMITTED),

    /**
     * SERIAL young generation Survivor metric information
     */
    SURVIVOR_SPACE_SERIAL("Survivor Space", MetricEnum.SURVIVOR_INIT, MetricEnum.SURVIVOR_USED,
            MetricEnum.SURVIVOR_MAX, MetricEnum.SURVIVOR_COMMITTED),

    /**
     * SERIAL old generation metric enumeration
     */
    OLD_EDEN_SPACE_SERIAL("Tenured Gen", MetricEnum.OLD_GEN_INIT, MetricEnum.OLD_GEN_USED, MetricEnum.OLD_GEN_MAX,
            MetricEnum.OLD_GEN_COMMITTED),
    /**
     * G1 young generation Eden metric information
     */
    EDEN_SPACE_G1("G1 Eden Space", MetricEnum.EDEN_INIT, MetricEnum.EDEN_USED, MetricEnum.EDEN_MAX,
            MetricEnum.EDEN_COMMITTED),

    /**
     * G1 young generation Survivor metric information
     */
    SURVIVOR_SPACE_G1("G1 Survivor Space", MetricEnum.SURVIVOR_INIT, MetricEnum.SURVIVOR_USED,
            MetricEnum.SURVIVOR_MAX, MetricEnum.SURVIVOR_COMMITTED),

    /**
     * G1 old generation metric enum
     */
    OLD_EDEN_SPACE_G1("G1 Old Gen", MetricEnum.OLD_GEN_INIT, MetricEnum.OLD_GEN_USED, MetricEnum.OLD_GEN_MAX,
            MetricEnum.OLD_GEN_COMMITTED),
    /**
     * CodeCache memory pool
     */
    CODE_CACHE_G1("CodeCache", MetricEnum.CODE_CACHE_INIT, MetricEnum.CODE_CACHE_USED, MetricEnum.CODE_CACHE_MAX,
            MetricEnum.CODE_CACHE_COMMITTED),

    /**
     * JDK11 codeCache memory pool that stores profiled compiled code
     */
    PROFILED_NMETHODS("CodeHeap 'profiled nmethods'", MetricEnum.PROFILED_NMETHODS_INIT,
            MetricEnum.PROFILED_NMETHODS_USED,
            MetricEnum.PROFILED_NMETHODS_MAX,
            MetricEnum.PROFILED_NMETHODS_COMMITTED),

    /**
     * JDK11 codeCache memory pool that stores compiled code that has not been profiled
     */
    NON_PROFILED_NMETHODS("CodeHeap 'non-profiled nmethods'", MetricEnum.NON_PROFILED_NMETHODS_INIT,
            MetricEnum.NON_PROFILED_NMETHODS_USED, MetricEnum.NON_PROFILED_NMETHODS_MAX,
            MetricEnum.NON_PROFILED_NMETHODS_COMMITTED),

    /**
     * JDK11 codeCache memory pool that stores other types of compiled code
     */
    NON_NMETHODS("CodeHeap 'non-nmethods'", MetricEnum.NON_NMETHODS_INIT, MetricEnum.NON_NMETHODS_USED,
            MetricEnum.NON_NMETHODS_MAX,
            MetricEnum.NON_NMETHODS_COMMITTED),

    /**
     * Epsilon collector metric enumeration
     */
    EPSILON_HEAP("Epsilon Heap", MetricEnum.EPSILON_HEAP_INIT, MetricEnum.EPSILON_HEAP_USED,
            MetricEnum.EPSILON_HEAP_MAX,
            MetricEnum.EPSILON_HEAP_COMMITTED),

    /**
     * Enumeration of Shenandoah collector metric for JDK 17
     */
    SHENANDOAH("Shenandoah", MetricEnum.SHENANDOAH_INIT, MetricEnum.SHENANDOAH_USED,
            MetricEnum.SHENANDOAH_MAX,
            MetricEnum.SHENANDOAH_COMMITTED);

    /**
     * type
     */
    private String type;

    /**
     * initialization enumeration
     */
    private MetricEnum initEnum;

    /**
     * enumeration used
     */
    private MetricEnum usedEnum;

    /**
     * enumeration corresponding to the maximum value
     */
    private MetricEnum maxEnum;

    /**
     * submit the enumeration corresponding to the value
     */
    private MetricEnum committedEnum;

    /**
     * construction method
     *
     * @param type type
     * @param initEnum initialization enumeration
     * @param usedEnum the corresponding enumeration has been used
     * @param maxEnum enumeration corresponding to the maximum value
     * @param committedEnum submit the enumeration corresponding to the value
     */
    MemoryType(String type, MetricEnum initEnum, MetricEnum usedEnum, MetricEnum maxEnum, MetricEnum committedEnum) {
        this.type = type;
        this.initEnum = initEnum;
        this.usedEnum = usedEnum;
        this.maxEnum = maxEnum;
        this.committedEnum = committedEnum;
    }

    public String getType() {
        return type;
    }

    public MetricEnum getInitEnum() {
        return initEnum;
    }

    public MetricEnum getUsedEnum() {
        return usedEnum;
    }

    public MetricEnum getMaxEnum() {
        return maxEnum;
    }

    public MetricEnum getCommittedEnum() {
        return committedEnum;
    }

    /**
     * gets enumeration by type
     *
     * @param type type
     * @return corresponding enumeration
     */
    public static Optional<MemoryType> getEnumByType(String type) {
        for (MemoryType memoryType : MemoryType.values()) {
            if (memoryType.type.equals(type)) {
                return Optional.of(memoryType);
            }
        }
        return Optional.empty();
    }
}
