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
import com.huawei.registry.interceptors.health.NacosGrpcDeRegisterInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * nacos 2.x
 * 拦截反注册方法, 在反注册之前，判断RpcClient是否可用; 因为此处当双注册下发关闭原注册中心配置时，会将RpcClient关闭掉,
 * 若关闭了，则无需进行反注册, 因为此时当前客户端已经主动从注册中心下线了，且若处于Shutdown调用, 会直接抛出异常，因此做前置判断
 *
 * @author zhouss
 * @since 2022-12-20
 */
public class NacosGrpcProxyDeregisterDeclarer extends AbstractDoubleRegistryDeclarer {
    /**
     * nacos心跳发送类
     */
    private static final String[] ENHANCE_CLASSES = new String[] {
        "com.alibaba.nacos.client.naming.remote.gprc.NamingGrpcClientProxy"
    };

    /**
     * 拦截类的全限定名
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
