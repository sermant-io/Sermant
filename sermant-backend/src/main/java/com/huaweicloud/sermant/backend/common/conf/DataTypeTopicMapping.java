/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.backend.common.conf;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * web configuration
 *
 * @author beetlemen
 * @since 2021-12-08
 */
@Data
@ConfigurationProperties(
        prefix = "datatype.topic"
)
public class DataTypeTopicMapping {
    private final Map<Integer, String> mapping = new HashMap<>();

    /**
     * 获取topic类型
     *
     * @param type 类型
     * @return topic
     */
    public String getTopicOfType(Integer type) {
        return mapping.get(type);
    }
}
