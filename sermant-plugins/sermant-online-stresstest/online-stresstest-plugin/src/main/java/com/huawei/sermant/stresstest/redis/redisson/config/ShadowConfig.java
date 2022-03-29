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

package com.huawei.sermant.stresstest.redis.redisson.config;

import com.huawei.sermant.stresstest.core.Reflection;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 生产影子config的基类
 *
 * @author yiwei
 * @since 2021-11-04
 */
public abstract class ShadowConfig {
    static final String ADDRESS = "address";
    static final String NODE_ADDRESS = "nodeAddresses";
    private String serverConfig;
    private String field;

    /**
     * 构造方法
     *
     * @param serverConfig serverConfig
     * @param field field
     */
    public ShadowConfig(String serverConfig, String field) {
        this.serverConfig = serverConfig;
        this.field = field;
    }

    /**
     * 获取要配置的地址信息
     *
     * @return 影子配置信息
     */
    protected abstract Object getAddress();

    /**
     * 更新影子配置的地址信息
     *
     * @param shadowObject 影子配置
     * @return Optional < Object >
     */
    protected Optional<Object> update(Object shadowObject) {
        return Reflection.getDeclaredValue(serverConfig, shadowObject).map(configValue -> {
            Reflection.setDeclaredValue(field, configValue, getAddress());
            return configValue;
        });
    }

    /**
     * 检查当前配置是否为影子配置
     *
     * @param shadowObject 待检查配置
     * @return 影子配置返回true，否则返回false
     */
    protected boolean isShadowObject(Object shadowObject) {
        AtomicBoolean value = new AtomicBoolean(false);
        Reflection.getDeclaredValue(serverConfig, shadowObject)
            .flatMap(configValue -> Reflection.getDeclaredValue(field, configValue)).ifPresent(address -> {
                if (address.equals(getAddress())) {
                    value.set(true);
                }
            });
        return value.get();
    }
}
