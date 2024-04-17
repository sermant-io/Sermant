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

package com.huawei.flowcontrol.retry;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.service.rest4j.HttpRest4jService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;

import feign.Client;
import feign.Request;
import feign.Request.HttpMethod;
import feign.Request.Options;
import feign.Response;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;

/**
 * feign retry test
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class FeignRequestInterceptorTest {
    private ExecuteContext context;

    private Interceptor interceptor;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        serviceManagerMockedStatic.close();
    }


    /**
     * preinitialization
     *
     * @throws Exception initialization failure thrown
     */
    @Before
    public void before() throws Exception {
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(new FlowControlConfig());
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(HttpRest4jService.class))
                .thenReturn(createRestService());
        interceptor = new FeignRequestInterceptor();
        final Client proxy = (request, options) -> createResponse();
        final Object[] allArguments = new Object[2];
        allArguments[0] = createRequest();
        allArguments[1] = new Request.Options();
        context = ExecuteContext.forMemberMethod(
            proxy,
            Client.class.getDeclaredMethod("execute", Request.class, Options.class),
            allArguments,
            Collections.emptyMap(),
            Collections.emptyMap());
    }

    /**
     * test_process
     *
     * @throws Exception execution failure throw
     */
    @Test
    public void test() throws Exception {
        interceptor.before(context);
        context.changeResult(createResponse());
        final ExecuteContext after = interceptor.after(context);
        Assert.assertNotNull(after.getResult());
        interceptor.onThrow(context);
    }

    private Response createResponse() {
        return Response.builder().request(createRequest()).status(HttpStatus.SC_OK).headers(Collections.emptyMap())
            .build();
    }

    private Request createRequest() {
        return Request.create(HttpMethod.GET, "localhost:8080", new HashMap<>(), "test".getBytes(
            StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    private HttpRest4jService createRestService() {
        return new HttpRest4jService() {
            @Override
            public void onBefore(String sourceName, RequestEntity requestEntity,
                    FlowControlResult fixedResult) {

            }

            @Override
            public void onAfter(String sourceName, Object result) {

            }

            @Override
            public void onThrow(String sourceName, Throwable throwable) {

            }
        };
    }
}
