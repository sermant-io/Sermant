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

package com.huawei.dynamic.config.source;

import com.huawei.dynamic.config.ConfigHolder;

import org.springframework.core.env.EnumerablePropertySource;

/**
 * 动态配置
 *
 * @author zhouss
 * @since 2022-04-20
 */
public class DynamicConfigPropertySource extends EnumerablePropertySource<ConfigHolder> {
    /**
     * 构造器
     *
     * @param name 配置名
     */
    public DynamicConfigPropertySource(String name) {
        super(name);
    }

    @Override
    public String[] getPropertyNames() {
        return ConfigHolder.INSTANCE.getConfigNames().toArray(new String[0]);
    }

    @Override
    public boolean containsProperty(String name) {
        return ConfigHolder.INSTANCE.getConfigNames().contains(name);
    }

    @Override
    public Object getProperty(String name) {
        return ConfigHolder.INSTANCE.getConfig(name);
    }
}
