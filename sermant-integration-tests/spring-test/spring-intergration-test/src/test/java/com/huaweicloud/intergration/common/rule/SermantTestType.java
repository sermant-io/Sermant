/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.intergration.common.rule;

import java.util.Locale;
import java.util.Optional;

/**
 * 集成测试类型
 *
 * @author zhouss
 * @since 2022-08-02
 */
public enum SermantTestType {
    /**
     * 流量治理测试
     */
    FLOW_CONTROL,

    /**
     * 动态配置ZK测试
     */
    DYNAMIC_CONFIG_ZK,

    /**
     * 动态配置nacos测试
     */
    DYNAMIC_CONFIG_NACOS,

    /**
     * 测试所有类型
     */
    ALL;

    /**
     * 获取实际的测试类型
     *
     * @param testType 输入测试类型
     * @return 匹配的测试类型
     */
    public static Optional<SermantTestType> of(String testType) {
        if (testType == null) {
            return Optional.empty();
        }
        for (SermantTestType type : values()) {
            if (type.name().equals(testType.toUpperCase(Locale.ROOT))) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
