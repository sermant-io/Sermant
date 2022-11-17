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

package com.huawei.discovery.interceptors;

import com.huawei.discovery.service.RegistryService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Collections;

/**
 * 关闭事件测试
 *
 * @author zhouss
 * @since 2022-11-16
 */
public class SpringCloseEventInterceptorTest extends BaseTest {
    private RegistryService registryService;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        registryService = Mockito.mock(RegistryService.class);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(RegistryService.class))
                .thenReturn(registryService);
    }

    @Test
    public void before() throws Exception {
        final SpringCloseEventInterceptor springCloseEventInterceptor = new SpringCloseEventInterceptor();
        springCloseEventInterceptor.before(buildContext(new Object[]{"test"}));
        Mockito.verify(registryService, Mockito.times(0)).shutdown();
        springCloseEventInterceptor.before(buildContext(new Object[]{new ContextClosedEvent(Mockito.mock(
                AnnotationConfigApplicationContext.class))}));
        Mockito.verify(registryService, Mockito.times(0)).shutdown();
        springCloseEventInterceptor.before(buildContext(new Object[]{new ContextClosedEvent(Mockito.mock(
                AnnotationConfigServletWebApplicationContext.class))}));
        Mockito.verify(registryService, Mockito.times(1)).shutdown();
        springCloseEventInterceptor.after(buildContext(new Object[]{"test"}));
    }

    private ExecuteContext buildContext(Object[] arguments) throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(new Object(), String.class.getDeclaredMethod("trim"), arguments,
                Collections.emptyMap(), Collections.emptyMap());
    }
}
