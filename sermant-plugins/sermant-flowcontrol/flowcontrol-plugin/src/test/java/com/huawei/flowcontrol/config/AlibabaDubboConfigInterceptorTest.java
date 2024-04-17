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

package com.huawei.flowcontrol.config;

import static org.junit.Assert.*;

import com.huawei.flowcontrol.EnvUtils;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.core.constants.CseConstants;
import com.huawei.flowcontrol.common.entity.FlowControlServiceMeta;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;

/**
 * testConfiguration
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class AlibabaDubboConfigInterceptorTest {
    private final FlowControlConfig flowControlConfig = new FlowControlConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @After
    public void tearDown() throws Exception {
        pluginConfigManagerMockedStatic.close();
        // reset
        FlowControlServiceMeta.getInstance().setVersion(null);
        EnvUtils.delEnv(Collections.singletonMap(CseConstants.KEY_DUBBO_VERSION, null));
    }

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(flowControlConfig);
    }

    /**
     * Test Close Adaptation
     *
     * @throws Exception willNotThrow
     */
    @Test
    public void testClose() throws Exception {
        final AbstractInterceptor interceptor = getInterceptor();
        interceptor.before(buildContext());
        assertNull(FlowControlServiceMeta.getInstance().getVersion());
    }

    /**
     * test enable adaptation sdk
     *
     * @throws Exception willNotThrow
     */
    @Test
    public void testOpen() throws Exception {
        String version = "1.0.0";
        final AbstractInterceptor interceptor = getInterceptor();
        flowControlConfig.setBaseSdk(true);
        flowControlConfig.setUseCseRule(true);
        final HashMap<String, String> env = new HashMap<>();
        env.put(CseConstants.KEY_DUBBO_VERSION, version);
        EnvUtils.addEnv(env);
        interceptor.before(buildContext());
        assertEquals(version, FlowControlServiceMeta.getInstance().getVersion());
    }

    private ExecuteContext buildContext() throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(
                this,
                String.class.getMethod("trim"),
                new Object[0],
                Collections.emptyMap(),
                Collections.emptyMap());
    }

    /**
     * get the test interceptor
     *
     * @return AbstractInterceptor
     */
    protected AbstractInterceptor getInterceptor() {
        return new AlibabaDubboConfigInterceptor();
    }
}
