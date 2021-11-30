/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.db.mongodb;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;
import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.core.Tester;

import java.lang.reflect.Method;

import static com.lubanops.stresstest.core.Tester.addTestMongodb;

/**
 * Mongo collection 增强实现
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class MongoCollectionInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (Tester.isTest() && !ConfigFactory.getConfig().isMongoShadowRepositories() ) {
            arguments[0] = addTestMongodb((String) arguments[0]);
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }
}
