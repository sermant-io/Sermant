/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.auto.sc.ServiceCombServiceInstance;
import com.huawei.registry.config.grace.GraceHelper;
import com.huawei.registry.entity.MicroServiceInstance;

import com.huaweicloud.sermant.core.utils.ReflectUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * 测试预热能力-loadbalancer
 *
 * @author zhouss
 * @since 2022-06-30
 */
public class SpringLoadbalancerWarmUpInterceptorTest extends WarmUpTest {
    /**
     * 测试预热
     *
     * @throws NoSuchMethodException 找不到反复抛出
     */
    @Test
    public void testWarmUp() throws NoSuchMethodException {
        before();
        final SpringLoadbalancerWarmUpInterceptor springRibbonWarmUpInterceptor = new SpringLoadbalancerWarmUpInterceptor();
        final List<ServiceCombServiceInstance> serviceInstances = buildServiceInstances();
        final ExecuteContext executeContext = ExecuteContext.forMemberMethod(springRibbonWarmUpInterceptor,
                SpringRibbonWarmUpInterceptor.class.getDeclaredMethod("doAfter", ExecuteContext.class),
                new Object[] {serviceInstances}
                , null,
                null);
        executeContext.changeResult(new DefaultResponse(serviceInstances.get(0)));
        process(springRibbonWarmUpInterceptor, executeContext, serviceInstances);
    }

    /**
     * 执行操作
     *
     * @param interceptor 拦截器
     * @param executeContext 上下文
     * @param argument 参数
     */
    private void process(GraceSwitchInterceptor interceptor, ExecuteContext executeContext, Object argument) {
        final HashMap<String, Integer> statResult = new HashMap<>();
        for (int i = 0; i < REQUEST_COUNT; i++) {
            ReflectUtils.setFieldValue(executeContext, "arguments", new Object[] {argument});
            interceptor.before(executeContext);
            interceptor.after(executeContext);
            final Object result = executeContext.getResult();
            final Optional<Object> serviceInstance = ReflectUtils.getFieldValue(result, "serviceInstance");
            Assert.assertTrue(serviceInstance.isPresent());
            stat(Collections.singletonList(serviceInstance.get()), statResult);
        }
        Assert.assertTrue(statResult.get(DISABLE_WARM_UP_IP) * 1.0d / statResult.get(ENABLE_WARM_UP_IP) >= RATE);
    }

    private List<ServiceCombServiceInstance> buildServiceInstances() {
        final MicroServiceInstance microServiceInstance = microServiceInstance(DISABLE_WARM_UP_IP);
        final MicroServiceInstance warmUpMicroServiceInstance = microServiceInstance(ENABLE_WARM_UP_IP);
        GraceHelper.configWarmUpParams(warmUpMicroServiceInstance.getMetadata(), graceConfig);
        return Arrays.asList(new ServiceCombServiceInstance(microServiceInstance),
                new ServiceCombServiceInstance(warmUpMicroServiceInstance));
    }
}
