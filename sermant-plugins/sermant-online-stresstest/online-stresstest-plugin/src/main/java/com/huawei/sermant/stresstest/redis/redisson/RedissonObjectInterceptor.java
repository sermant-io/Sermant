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

package com.huawei.sermant.stresstest.redis.redisson;

import static com.huawei.sermant.stresstest.redis.redisson.RedissonUtils.getSetShadowConnectionManager;

import com.huawei.sermant.core.agent.interceptor.ConstructorInterceptor;
import com.huawei.sermant.stresstest.config.ConfigFactory;
import com.huawei.sermant.stresstest.core.Reflection;
import com.huawei.sermant.stresstest.core.Tester;

/**
 * Redisson Object 拦截器, redisson 分库
 *
 * @author yiwei
 * @since 2021-11-02
 */
public class RedissonObjectInterceptor implements ConstructorInterceptor {
    private static final String CONNECTION_MANAGER = "connectionManager";

    @Override
    public void onConstruct(Object obj, Object[] allArguments) {
        if (Tester.isTest() && ConfigFactory.getConfig().isRedisShadowRepositories()) {
            Reflection.getDeclaredValue(CONNECTION_MANAGER, obj).ifPresent(connectionManager -> Reflection
                .setDeclaredValue(CONNECTION_MANAGER, obj, getSetShadowConnectionManager(connectionManager), true));
        }
    }

}
