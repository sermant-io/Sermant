/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */
package com.lubanops.stresstest.redis.redisson.config;

import com.lubanops.stresstest.redis.RedisUtils;

/**
 * 生产影子 single config
 *
 * @author yiwei
 * @since 2021/11/4
 */
public class ShadowSingleConfig extends ShadowConfig{
    public ShadowSingleConfig() {
        super("singleServerConfig", ADDRESS);
    }

    @Override
    protected Object getAddress() {
        return RedisUtils.getMasterAddress();
    }
}
