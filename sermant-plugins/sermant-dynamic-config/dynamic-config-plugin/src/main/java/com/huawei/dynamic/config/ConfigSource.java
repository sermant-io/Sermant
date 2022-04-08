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

package com.huawei.dynamic.config;

import java.util.Set;

/**
 * 配置
 *
 * @author zhouss
 * @since 2022-04-15
 */
public interface ConfigSource extends Comparable<ConfigSource> {
    /**
     * 优先级比较
     *
     * @param target 比较对象
     * @return 优先级
     */
    @Override
    default int compareTo(ConfigSource target) {
        if (target == null) {
            return -1;
        }
        return Integer.compare(this.order(), target.order());
    }

    /**
     * 是否启用
     *
     * @return 是否启用
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * 获取所有配置名称
     *
     * @return 所有配置名称
     */
    Set<String> getConfigNames();

    /**
     * 获取单个配置
     *
     * @param key 配置键
     * @return 配置项
     */
    Object getConfig(String key);

    /**
     * 优先级, 越小优先级越高
     *
     * @return 优先级
     */
    int order();
}
