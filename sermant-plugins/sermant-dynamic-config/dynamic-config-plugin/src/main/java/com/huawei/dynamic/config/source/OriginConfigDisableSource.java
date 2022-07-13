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

package com.huawei.dynamic.config.source;

import com.huawei.dynamic.config.ConfigSource;

import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 用于屏蔽原配置中心的开关配置源,当前支持zookeeper/nacos
 *
 * @author zhouss
 * @since 2022-07-12
 */
public class OriginConfigDisableSource extends MapPropertySource implements ConfigSource {
    private static final Map<String, Object> SOURCE = new HashMap<>();

    /**
     * 构造器
     *
     * @param name 资源名称
     */
    public OriginConfigDisableSource(String name) {
        super(name, SOURCE);
        SOURCE.put("spring.cloud.nacos.config.enabled", false);
        SOURCE.put("spring.cloud.zookeeper.config.enabled", false);
    }

    @Override
    public Set<String> getConfigNames() {
        return SOURCE.keySet();
    }

    @Override
    public Object getConfig(String key) {
        return SOURCE.get(key);
    }

    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }
}
