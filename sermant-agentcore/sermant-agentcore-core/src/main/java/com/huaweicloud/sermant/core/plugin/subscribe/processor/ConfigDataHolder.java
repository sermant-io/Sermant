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

import java.util.HashMap;
import java.util.Map;

/**
 * 数据持有器
 *
 * @author zhouss
 * @since 2022-04-21
 */
public class ConfigDataHolder implements Comparable<ConfigDataHolder> {
    /**
     * 优先级， 越小优先级越高
     */
    private final int order;

    /**
     * 当前该group的所有数据持有
     * <p>
     * key: 配置键 value: 该配置键所有的值
     * </p>
     */
    private final Map<String, Map<String, Object>> holder = new HashMap<>();

    private final String group;

    /**
     * 构造器
     *
     * @param group 组标签
     * @param order 优先级
     */
    public ConfigDataHolder(String group, int order) {
        this.group = group;
        this.order = order;
    }

    @Override
    public int compareTo(ConfigDataHolder target) {
        return Integer.compare(target.order, this.order);
    }

    public String getGroup() {
        return group;
    }

    public Map<String, Map<String, Object>> getHolder() {
        return holder;
    }
}
