/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.oap.redis.module;

import com.huawei.oap.redis.service.IRedisService;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;

/**
 * redis集群查询模型类
 *
 * @author zhouss
 * @since 2020-11-28
 */
public class RedisOperationModule extends ModuleDefine {
    /**
     * 模块名
     */
    public static final String NAME = "redis";

    public RedisOperationModule() {
        super(NAME);
    }

    @Override
    public Class[] services() {
        return new Class[] {
            IRedisService.class
        };
    }
}
