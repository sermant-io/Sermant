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

package io.sermant.registry.grace.interceptors;

import io.sermant.registry.config.grace.GraceHelper;
import io.sermant.registry.entity.MicroServiceInstance;
import io.sermant.registry.entity.ScServer;

import io.sermant.core.plugin.agent.entity.ExecuteContext;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Warm-up test
 *
 * @author zhouss
 * @since 2022-06-30
 */
public class SpringRibbonWarmUpInterceptorTest extends WarmUpTest {
    /**
     * Initialize
     */
    @Test
    public void testWarmUp() throws NoSuchMethodException {
        final SpringRibbonWarmUpInterceptor springRibbonWarmUpInterceptor = new SpringRibbonWarmUpInterceptor();
        final ExecuteContext executeContext = ExecuteContext.forMemberMethod(springRibbonWarmUpInterceptor,
                SpringRibbonWarmUpInterceptor.class.getDeclaredMethod("doAfter", ExecuteContext.class), null, null, null);
        final List<ScServer> scServers = buildServers();
        process(springRibbonWarmUpInterceptor, executeContext, scServers);
    }

    /**
     * Take action
     *
     * @param interceptor    Interceptor
     * @param executeContext Context
     * @param result         Result
     */
    private void process(GraceSwitchInterceptor interceptor, ExecuteContext executeContext, Object result) {
        final HashMap<String, Integer> statResult = new HashMap<>();
        for (int i = 0; i < REQUEST_COUNT; i++) {
            executeContext.changeResult(result);
            interceptor.before(executeContext);
            interceptor.after(executeContext);
            stat(executeContext.getResult(), statResult);
        }
        Assert.assertTrue(statResult.get(DISABLE_WARM_UP_IP) * 1.0d / statResult.get(ENABLE_WARM_UP_IP) >= RATE);
    }

    private List<ScServer> buildServers() {
        final MicroServiceInstance microServiceInstance = microServiceInstance(DISABLE_WARM_UP_IP);
        final MicroServiceInstance warmUpMicroServiceInstance = microServiceInstance(ENABLE_WARM_UP_IP);
        GraceHelper.configWarmUpParams(warmUpMicroServiceInstance.getMetadata(), graceConfig);
        return Arrays.asList(new ScServer(microServiceInstance, DEFAULT_SERVICE_NAME),
                new ScServer(warmUpMicroServiceInstance, DEFAULT_SERVICE_NAME));
    }
}
