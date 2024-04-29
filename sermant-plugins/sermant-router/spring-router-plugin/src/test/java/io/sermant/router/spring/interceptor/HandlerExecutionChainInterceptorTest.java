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

package io.sermant.router.spring.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.service.ServiceManager;
import io.sermant.router.spring.service.SpringConfigService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.servlet.HandlerExecutionChain;

import java.util.Collections;
import java.util.Set;

/**
 * Test HandlerExecutionChainInterceptor
 *
 * @author provenceee
 * @since 2022-09-07
 */
public class HandlerExecutionChainInterceptorTest {
    private final HandlerExecutionChainInterceptor interceptor;

    private final ExecuteContext context;

    private static MockedStatic<ServiceManager> mockServiceManager;

    /**
     * Perform mock before the UT is executed
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(SpringConfigService.class)).thenReturn(
                new SpringConfigService() {
                    @Override
                    public void init(String cacheName, String serviceName) {
                    }

                    @Override
                    public Set<String> getMatchKeys() {
                        return Collections.emptySet();
                    }

                    @Override
                    public Set<String> getInjectTags() {
                        return Collections.emptySet();
                    }
                });
    }

    /**
     * Release the mock object after the UT is executed
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public HandlerExecutionChainInterceptorTest() {
        interceptor = new HandlerExecutionChainInterceptor();
        context = ExecuteContext.forMemberMethod(new HandlerExecutionChain(new Object()), null, null, null, null);
    }

    /**
     * Test the before method
     */
    @Test
    public void testBefore() {
        interceptor.before(context);
        HandlerExecutionChain chain = (HandlerExecutionChain) context.getObject();
        Assert.assertNotNull(chain.getInterceptors());
        Assert.assertEquals(1, chain.getInterceptors().length);
        Assert.assertEquals(RouteHandlerInterceptor.class, chain.getInterceptors()[0].getClass());
    }
}