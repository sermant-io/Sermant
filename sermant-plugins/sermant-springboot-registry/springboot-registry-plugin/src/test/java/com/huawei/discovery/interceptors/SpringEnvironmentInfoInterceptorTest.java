/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

import com.huawei.discovery.entity.RegisterContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class SpringEnvironmentInfoInterceptorTest extends BaseTest {
    private SpringEnvironmentInfoInterceptor interceptor;

    private final Object[] arguments;

    /**
     * Constructor
     */
    public SpringEnvironmentInfoInterceptorTest() {
        arguments = new Object[1];
    }

    @Override
    public void setUp() {
        super.setUp();
        interceptor = new SpringEnvironmentInfoInterceptor();
    }

    @Test
    public void testSpringEnvironmentInfoInterceptor() throws Exception {
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
        final ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
        Mockito.when(environment.getProperty("server.address")).thenReturn("127.0.0.1");
        Mockito.when(environment.getProperty("server.port")).thenReturn("8010");
        Mockito.when(environment.getProperty("spring.application.name")).thenReturn("zookeeper-provider-demo");
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
        applicationContext.setEnvironment(environment);
        arguments[0] = applicationContext;
        interceptor.after(context);
        Assert.assertEquals(RegisterContext.INSTANCE.getServiceInstance().getServiceName(), "zookeeper-provider-demo");
    }
}
