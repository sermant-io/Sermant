/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */
package com.lubanops.stresstest.redis.redisson.config;

import com.lubanops.stresstest.core.Reflection;
import com.lubanops.stresstest.redis.RedisUtils;

import java.util.Optional;

/**
 * 生产影子 single config
 *
 * @author yiwei
 * @since 2021/11/4
 */
public class ShadowMasterSlaveConfig extends ShadowConfig{
    public ShadowMasterSlaveConfig() {
        super("masterSlaveServersConfig", "masterAddress");
    }

    @Override
    protected Object getAddress() {
        return RedisUtils.getMasterAddress();
    }

    @Override
    protected Optional<Object> update(Object shadowObject) {
        return super.update(shadowObject).map(configValue -> {
            Reflection.setDeclaredValue("slaveAddresses", configValue, RedisUtils.getSalveRedisAddress());
            return configValue;
        });
    }
}
