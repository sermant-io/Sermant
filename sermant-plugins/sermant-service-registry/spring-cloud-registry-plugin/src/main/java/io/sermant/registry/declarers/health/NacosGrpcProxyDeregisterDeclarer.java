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

package io.sermant.registry.declarers.health;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.registry.declarers.AbstractDoubleRegistryDeclarer;
import io.sermant.registry.interceptors.health.NacosGrpcDeRegisterInterceptor;

/**
 * nacos 2.x
 * Intercept the anti-registration method, and determine whether the RpcClient is available before the
 * anti-registration; Because here, when the dual registration is delivered to close the original registry
 * configuration, the RpcClient will be closed.
 * If it is closed, there is no need to deregister, because the current
 * client has been actively removed from the registry, and if it is called by Shutdown, an exception will be thrown
 * directly, so make a pre-judgment
 *
 * @author zhouss
 * @since 2022-12-20
 */
public class NacosGrpcProxyDeregisterDeclarer extends AbstractDoubleRegistryDeclarer {
    /**
     * Nacos heartbeat sending class
     */
    private static final String[] ENHANCE_CLASSES = new String[]{
            "com.alibaba.nacos.client.naming.remote.gprc.NamingGrpcClientProxy"
    };

    /**
     * The fully qualified name of the interception class
     */
    private static final String INTERCEPT_CLASS = NacosGrpcDeRegisterInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASSES);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("deregisterService"), INTERCEPT_CLASS)
        };
    }
}
