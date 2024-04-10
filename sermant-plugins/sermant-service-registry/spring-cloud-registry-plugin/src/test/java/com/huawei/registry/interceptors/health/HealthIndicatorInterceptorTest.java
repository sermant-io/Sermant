/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.interceptors.health;

import com.huawei.registry.config.RegisterDynamicConfig;
import com.huawei.registry.interceptors.BaseRegistryTest;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;

import org.junit.Assert;
import org.junit.Test;

/**
 * Health Interception Test
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class HealthIndicatorInterceptorTest extends BaseRegistryTest {
    @Test
    public void doBefore() throws Exception {
        RegisterDynamicConfig.INSTANCE.setClose(true);
        REGISTER_CONFIG.setOpenMigration(true);
        REGISTER_CONFIG.setEnableSpringRegister(true);
        final ExecuteContext context = interceptor.before(buildContext());
        Assert.assertTrue(context.isSkip());
        RegisterDynamicConfig.INSTANCE.setClose(false);
    }

    @Override
    protected Interceptor getInterceptor() {
        return new HealthIndicatorInterceptor();
    }
}
