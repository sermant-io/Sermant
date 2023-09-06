/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.intergration.common;

/**
 * JDK12及以上版本独有性能指标枚举类
 *
 * @author tangle
 * @version 1.0.0
 * @since 2023-09-04
 */
public enum MetricEnumJDK12 {
    /**
     * JDK9存储其他类型编译代码的codeCache初始值
     */
    NON_NMETHODS_INIT("non_nmethods_init", "the number is init of non_nmethods"),

    /**
     * JDK9存储其他类型编译代码的codeCache使用值
     */
    NON_NMETHODS_USED("non_nmethods_used", "the number is used of non_nmethods"),

    /**
     * JDK9存储其他类型编译代码的codeCache最大值
     */
    NON_NMETHODS_MAX("non_nmethods_max", "the number is max of non_nmethods"),

    /**
     * JDK9存储其他类型编译代码的codeCache提交值
     */
    NON_NMETHODS_COMMITTED("non_nmethods_committed", "the number is committed of non_nmethods"),

    /**
     * JDK9存储经过性能分析的编译代码的codeCache初始值
     */
    PROFILED_NMETHODS_INIT("profiled_nmethods_init", "the number is init of profiled nmethods"),

    /**
     * JDK9存储经过性能分析的编译代码的codeCache使用值
     */
    PROFILED_NMETHODS_USED("profiled_nmethods_used", "the number is used of profiled nmethods"),

    /**
     * JDK9存储经过性能分析的编译代码的codeCache最大值
     */
    PROFILED_NMETHODS_MAX("profiled_nmethods_max", "the number is max of profiled nmethods"),

    /**
     * JDK9存储经过性能分析的编译代码的codeCache提交值
     */
    PROFILED_NMETHODS_COMMITTED("profiled_nmethods_committed", "the number is committed of profiled nmethods"),

    /**
     * JDK9存储未经过性能分析的编译代码的codeCache初始值
     */
    NON_PROFILED_NMETHODS_INIT("non_profiled_nmethods_init", "the number is init of non-profiled nmethods"),

    /**
     * JDK9存储未经过性能分析的编译代码的codeCache使用值
     */
    NON_PROFILED_NMETHODS_USED("non_profiled_nmethods_used", "the number is used of non-profiled nmethods"),

    /**
     * JDK9存储未经过性能分析的编译代码的codeCache最大值
     */
    NON_PROFILED_NMETHODS_MAX("non_profiled_nmethods_max", "the number is max of non-profiled nmethods"),

    /**
     * JDK9存储未经过性能分析的编译代码的codeCache提交值
     */
    NON_PROFILED_NMETHODS_COMMITTED("non_profiled_nmethods_committed",
            "the number is committed of non-profiled nmethods"),

    /**
     * JDK9的Epsilon收集器初始值
     */
    EPSILON_HEAP_INIT("epsilon_heap_init", "the number is init of Epsilon Heap"),

    /**
     * JDK9的Epsilon收集器使用值
     */
    EPSILON_HEAP_USED("epsilon_heap_used", "the number is used of Epsilon Heap"),

    /**
     * JDK9的Epsilon收集器最大值
     */
    EPSILON_HEAP_MAX("epsilon_heap_max", "the number is max of Epsilon Heap"),

    /**
     * JDK9的Epsilon收集器提交值
     */
    EPSILON_HEAP_COMMITTED("epsilon_heap_committed", "the number is committed of Epsilon Heap"),
    /**
     * JDK12的Shenandoah收集器初始值
     */
    SHENANDOAH_INIT("shenandoah_init", "the number is init of Shenandoah"),

    /**
     * JDK12的Shenandoah收集器使用值
     */
    SHENANDOAH_USED("shenandoah_used", "the number is used of Shenandoah"),

    /**
     * JDK12的Shenandoah收集器最大值
     */
    SHENANDOAH_MAX("shenandoah_max", "the number is max of Shenandoah"),

    /**
     * JDK12的Shenandoah收集器提交值
     */
    SHENANDOAH_COMMITTED("shenandoah_committed", "the number is committed of Shenandoah");

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String disc;

    MetricEnumJDK12(String name, String disc) {
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
