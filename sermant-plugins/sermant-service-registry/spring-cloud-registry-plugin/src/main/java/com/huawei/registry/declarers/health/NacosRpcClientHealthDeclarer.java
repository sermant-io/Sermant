/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.declarers.health;

import com.huawei.registry.declarers.AbstractDoubleRegistryDeclarer;
import com.huawei.registry.interceptors.health.NacosRpcClientHealthInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * nacos 2.x
 * 拦截com.alibaba.nacos.common.remote.client.RpcClient健康检查方法, 判断注册中心是否存活, 并监听注册订阅配置下发
 *
 * @author zhouss
 * @since 2022-12-20
 */
public class NacosRpcClientHealthDeclarer extends AbstractDoubleRegistryDeclarer {
    /**
     * nacos心跳发送类
     */
    private static final String[] ENHANCE_CLASSES = new String[] {
        "com.alibaba.nacos.common.remote.client.RpcClient"
    };

    /**
     * 拦截类的全限定名
     */
    private static final String INTERCEPT_CLASS = NacosRpcClientHealthInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASSES);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
            InterceptDeclarer.build(MethodMatcher.nameEquals("healthCheck"), INTERCEPT_CLASS)
        };
    }
}
