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

package com.huaweicloud.integration.common;

/**
 * JDK8独有性能指标枚举类
 *
 * @author tangle
 * @version 1.0.0
 * @since 2023-09-04
 */
public enum MetricEnumJDK8 {
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
    CODE_CACHE_COMMITTED("code_cache_committed", "the committed is init of code cache");

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String disc;

    MetricEnumJDK8(String name, String disc) {
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
