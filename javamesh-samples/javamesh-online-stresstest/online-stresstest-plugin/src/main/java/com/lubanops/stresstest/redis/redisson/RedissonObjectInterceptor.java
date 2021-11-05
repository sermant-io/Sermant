/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */
package com.lubanops.stresstest.redis.redisson;


import com.huawei.apm.core.agent.interceptor.ConstructorInterceptor;
import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.core.Reflection;
import com.lubanops.stresstest.core.Tester;


import static com.lubanops.stresstest.redis.redisson.RedissonUtils.getSetShadowConnectionManager;

/**
 * Redisson Object 拦截器, redisson 分库
 *
 * @author yiwei
 * @since 2021/11/2
 */
public class RedissonObjectInterceptor implements ConstructorInterceptor {
    private static final String CONNECTION_MANAGER = "connectionManager";

    @Override
    public void onConstruct(Object obj, Object[] allArguments) {
        if (Tester.isTest() && ConfigFactory.getConfig().isRedisShadowRepositories()) {
            Reflection.getDeclaredValue(CONNECTION_MANAGER, obj).ifPresent(connectionManager -> Reflection.setDeclaredValue(
                    CONNECTION_MANAGER, obj, getSetShadowConnectionManager(connectionManager), true));
        }
    }

}
