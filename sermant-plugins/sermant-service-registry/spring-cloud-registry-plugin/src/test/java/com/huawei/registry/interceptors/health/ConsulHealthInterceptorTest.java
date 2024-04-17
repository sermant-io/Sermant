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

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.config.RegisterDynamicConfig;
import com.huawei.registry.context.RegisterContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.cloud.consul.discovery.ConsulCatalogWatch;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.ScheduledTask;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Test consul health monitoring
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class ConsulHealthInterceptorTest {
    private final RegisterConfig registerConfig = new RegisterConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private ConsulHealthInterceptor interceptor;

    @Before
    public void setUp() throws Exception {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(registerConfig);
        interceptor = new ConsulHealthInterceptor();
    }

    @After
    public void tearDown() throws Exception {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void close() {
        final ConsulCatalogWatch watch = Mockito.mock(ConsulCatalogWatch.class);
        RegisterContext.INSTANCE.setRegisterWatch(watch);
        interceptor.close();
        Mockito.verify(watch, Mockito.times(1)).stop();
        final ScheduledAnnotationBeanPostProcessor processor = new ScheduledAnnotationBeanPostProcessor();
        final Optional<Object> scheduledTasks = ReflectUtils.getFieldValue(processor, "scheduledTasks");
        Assert.assertTrue(scheduledTasks.isPresent() && scheduledTasks.get() instanceof Map);
        Map<Object, Set<ScheduledTask>> tasks = (Map<Object, Set<ScheduledTask>>) scheduledTasks.get();
        final ScheduledTask task = Mockito.mock(ScheduledTask.class);
        RegisterContext.INSTANCE.setRegisterWatch(new Object());
        tasks.put(RegisterContext.INSTANCE.getRegisterWatch(), Collections.singleton(task));
        RegisterContext.INSTANCE.setScheduleProcessor(processor);
        interceptor.close();
        Mockito.verify(task, Mockito.times(1)).cancel();
        RegisterContext.INSTANCE.setScheduleProcessor(null);
        RegisterContext.INSTANCE.setRegisterWatch(null);
    }

    @Test
    public void doBefore() throws NoSuchMethodException {
        final ExecuteContext context = interceptor.doBefore(buildContext());
        Assert.assertFalse(context.isSkip());
        registerConfig.setOpenMigration(true);
        registerConfig.setEnableSpringRegister(true);
        RegisterDynamicConfig.INSTANCE.setClose(true);
        final ExecuteContext context1 = interceptor.doBefore(buildContext());
        Assert.assertTrue(context1.isSkip());
        RegisterDynamicConfig.INSTANCE.setClose(false);
    }

    @Test
    public void doAfter() throws NoSuchMethodException {
        final ExecuteContext context = buildContext();
        context.changeResult(new Object());
        interceptor.doAfter(context);
        Assert.assertTrue(RegisterContext.INSTANCE.isAvailable());
        RegisterContext.INSTANCE.compareAndSet(true, false);
    }

    @Test
    public void doThrow() throws NoSuchMethodException {
        RegisterContext.INSTANCE.setAvailable(true);
        interceptor.doThrow(buildContext());
        Assert.assertFalse(RegisterContext.INSTANCE.isAvailable());
    }

    private ExecuteContext buildContext() throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(this, String.class.getDeclaredMethod("trim"),
                null, null, null);
    }
}
