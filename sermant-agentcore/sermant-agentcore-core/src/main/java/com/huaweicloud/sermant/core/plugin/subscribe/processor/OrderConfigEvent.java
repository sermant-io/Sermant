/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.core.plugin.subscribe.processor;

import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;

import java.util.Map;

/**
 * Order sort event with all data
 *
 * @author zhouss
 * @since 2022-04-21
 */
public class OrderConfigEvent extends DynamicConfigEvent {
    private static final long serialVersionUID = 4990176887738080367L;

    private final Map<String, Object> allData;

    /**
     * constructor
     *
     * @param key Configuration key
     * @param group Configuration group
     * @param content Configuration content
     * @param eventType Event type
     * @param allData All data
     */
    public OrderConfigEvent(String key, String group, String content, DynamicConfigEventType eventType, Map<String,
            Object> allData) {
        super(key, group, content, eventType);
        this.allData = allData;
    }

    /**
     * all data
     *
     * @return all data, overwritten by order
     */
    public Map<String, Object> getAllData() {
        return this.allData;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object target) {
        return super.equals(target);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
