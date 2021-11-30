/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */
package com.lubanops.stresstest.redis.redisson.config;

import com.lubanops.stresstest.core.Reflection;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 生产影子config的基类
 *
 * @author yiwei
 * @since 2021/11/4
 */
public abstract class ShadowConfig {
    static final String ADDRESS = "address";
    static final String NODE_ADDRESS = "nodeAddresses";
    private String serverConfig;
    private String field;

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
     */
    protected Optional<Object> update(Object shadowObject){
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
        Reflection.getDeclaredValue(serverConfig, shadowObject).flatMap(configValue -> Reflection.getDeclaredValue(field, configValue)).ifPresent(address -> {
            if (address.equals(getAddress())) {
                value.set(true);
            }
        });
        return value.get();
    }
}
