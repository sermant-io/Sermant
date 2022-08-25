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

import java.util.Optional;

/**
 * JVM内存性能类型
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public enum MemoryType {

    /**
     * 堆内存
     */
    HEAP_MEMORY("heap_memory", MetricEnum.HEAP_MEMORY_INIT, MetricEnum.HEAP_MEMORY_USED, MetricEnum.HEAP_MEMORY_MAX,
            MetricEnum.HEAP_MEMORY_COMMITTED),

    /**
     * 非堆内存
     */
    NON_HEAP_MEMORY("non_heap_memory", MetricEnum.NON_HEAP_MEMORY_INIT, MetricEnum.NON_HEAP_MEMORY_USED,
            MetricEnum.NON_HEAP_MEMORY_MAX, MetricEnum.NON_HEAP_MEMORY_COMMITTED),

    /**
     * CodeCache内存池
     */
    CODE_CACHE("Code Cache", MetricEnum.CODE_CACHE_INIT, MetricEnum.CODE_CACHE_USED, MetricEnum.CODE_CACHE_MAX,
            MetricEnum.CODE_CACHE_COMMITTED),

    /**
     * Metaspace 内存指标信息
     */
    META_SPACE("Metaspace", MetricEnum.META_SPACE_INIT, MetricEnum.META_SPACE_USED, MetricEnum.META_SPACE_MAX,
            MetricEnum.META_SPACE_COMMITTED),

    /**
     * 类加载
     */
    CLASS_SPACE("Compressed Class Space", MetricEnum.COMPRESSED_CLASS_SPACE_INIT,
            MetricEnum.COMPRESSED_CLASS_SPACE_USED, MetricEnum.COMPRESSED_CLASS_SPACE_MAX,
            MetricEnum.COMPRESSED_CLASS_SPACE_COMMITTED),
    /**
     * EDEN区指标枚举
     */
    EDEN_SPACE("PS Eden Space", MetricEnum.EDEN_INIT, MetricEnum.EDEN_USED, MetricEnum.EDEN_MAX,
            MetricEnum.EDEN_COMMITTED),

    /**
     * SURVIVOR指标枚举信息
     */
    SURVIVOR_SPACE("PS Survivor Space", MetricEnum.SURVIVOR_INIT, MetricEnum.SURVIVOR_USED, MetricEnum.SURVIVOR_MAX,
            MetricEnum.SURVIVOR_COMMITTED),

    /**
     * 老年代指标信息
     */
    OLD_GEN_SPACE("PS Old Gen", MetricEnum.OLD_GEN_INIT, MetricEnum.OLD_GEN_USED, MetricEnum.OLD_GEN_MAX,
            MetricEnum.OLD_GEN_COMMITTED),

    /**
     * 年轻代指标信息
     */
    EDEN_SPACE_CMS("Par Eden Space", MetricEnum.EDEN_INIT, MetricEnum.EDEN_USED, MetricEnum.EDEN_MAX,
            MetricEnum.EDEN_COMMITTED),

    /**
     * CMS年轻代指标信息
     */
    SURVIVOR_SPACE_CMS("Par Survivor Space", MetricEnum.SURVIVOR_INIT, MetricEnum.SURVIVOR_USED,
            MetricEnum.SURVIVOR_MAX, MetricEnum.SURVIVOR_COMMITTED),

    /**
     * 老年代指标枚举
     */
    OLD_EDEN_SPACE_CMS("CMS Old Gen", MetricEnum.OLD_GEN_INIT, MetricEnum.OLD_GEN_USED, MetricEnum.OLD_GEN_MAX,
            MetricEnum.OLD_GEN_COMMITTED);

    /**
     * 类型
     */
    private String type;

    /**
     * 初始化枚举
     */
    private MetricEnum initEnum;

    /**
     * 使用的枚举
     */
    private MetricEnum usedEnum;

    /**
     * 最大值对应的枚举
     */
    private MetricEnum maxEnum;

    /**
     * 提交值对应的枚举
     */
    private MetricEnum committedEnum;

    /**
     * 构造方法
     *
     * @param type          类型
     * @param initEnum      初始化枚举
     * @param usedEnum      已使用对应的枚举
     * @param maxEnum       最大值对应的枚举
     * @param committedEnum 提交值对应的枚举
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
     * 获取枚举通过类型
     *
     * @param type 类型
     * @return 对应的枚举
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
