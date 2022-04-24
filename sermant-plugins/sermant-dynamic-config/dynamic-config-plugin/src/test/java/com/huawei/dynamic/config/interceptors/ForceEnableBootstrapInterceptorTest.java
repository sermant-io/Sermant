/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config.interceptors;

import com.huawei.dynamic.config.DynamicConfiguration;
import com.huawei.dynamic.config.sources.MockEnvironment;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 测试强制开启启动配置
 *
 * @author zhouss
 * @since 2022-04-16
 */
public class ForceEnableBootstrapInterceptorTest {
    @Before
    public void init() {
        final DynamicConfiguration configuration = new DynamicConfiguration();
        configuration.setForceEnableBootstrap(true);
        Mockito.mockStatic(PluginConfigManager.class)
            .when(() -> PluginConfigManager.getPluginConfig(DynamicConfiguration.class
            )).thenReturn(configuration);
    }

    @Test
    public void testDoAfter() {
        final ForceEnableBootstrapInterceptor interceptor = new ForceEnableBootstrapInterceptor();
        final MockEnvironment mockEnvironment = new MockEnvironment();
        interceptor.doAfter(ExecuteContext.forMemberMethod(this, null, new Object[]{mockEnvironment},
            null, null));
        Assert.assertTrue(mockEnvironment.getPropertySources().iterator().hasNext());
    }
}
