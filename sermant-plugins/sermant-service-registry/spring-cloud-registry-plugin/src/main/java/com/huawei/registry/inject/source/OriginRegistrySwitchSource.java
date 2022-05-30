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

package com.huawei.registry.inject.source;

import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 关闭原始注册中心开关配置源
 *
 * @author zhouss
 * @since 2022-05-19
 */
public class OriginRegistrySwitchSource extends MapPropertySource {
    private static final Map<String, Object> SWITCHES = new HashMap<>();

    static {
        SWITCHES.put("spring.cloud.zookeeper.enabled", false);
        SWITCHES.put("spring.cloud.nacos.discovery.enabled", false);
        SWITCHES.put("spring.cloud.consul.enabled", false);
        SWITCHES.put("eureka.client.enabled", false);
        SWITCHES.put("ribbon.eureka.enabled", false);
    }

    /**
     * Create a new {@code MapPropertySource} with the given name and {@code Map}.
     *
     * @param name 配置源名称
     * {@link #containsProperty} behavior)
     */
    public OriginRegistrySwitchSource(String name) {
        super(name, SWITCHES);
    }
}
