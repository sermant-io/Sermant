/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.interceptors.http.server;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.interceptors.BaseInterceptorTest;
import com.huaweicloud.sermant.tag.transmission.service.ServiceCombHeaderParseService;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.tomcat.util.http.MimeHeaders;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试HttpServletInterceptor
 *
 * @author tangle
 * @since 2023-07-27
 */
public class HttpServletInterceptorTest extends BaseInterceptorTest {
    private final ServiceCombHeaderParseService parseService = Mockito.mock(ServiceCombHeaderParseService.class);

    private final HttpServletInterceptor interceptor;

    private final Object[] arguments;

    public MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic;

    public HttpServletInterceptorTest() {
        pluginServiceManagerMockedStatic = Mockito.mockStatic(PluginServiceManager.class);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(ServiceCombHeaderParseService.class))
                .thenReturn(parseService);
        interceptor = new HttpServletInterceptor();
        arguments = new Object[2];
    }

    @Test
    public void testSevlet() throws NoSuchFieldException, IllegalAccessException {
        ExecuteContext context;
        Map<String, List<String>> addHeaders = new HashMap<>();
        TrafficUtils.removeTrafficTag();

        // Headers包含tag的请求
        List<String> ids = new ArrayList<>();
        ids.add("testId001");
        ids.add("testId002");
        addHeaders.put("id", ids);
        context = buildContext(addHeaders);
        interceptor.before(context);
        Map<String, List<String>> tags1 = new HashMap<>();
        tags1.put("id", ids);
        Assert.assertEquals(TrafficUtils.getTrafficTag().getTag(), tags1);
        interceptor.after(context);

        // 第二次请求（不同tag）,测试tag数据不会污染其他请求
        addHeaders.remove("id");
        List<String> names = new ArrayList<>();
        names.add("testName001");
        names.add("testName001");
        addHeaders.put("name", names);
        context = buildContext(addHeaders);
        interceptor.before(context);
        Map<String, List<String>> tags2 = new HashMap<>();
        tags2.put("name", names);
        Assert.assertEquals(TrafficUtils.getTrafficTag().getTag(), tags2);
        interceptor.after(context);

        // 测试TagTransmissionConfig开关关闭时
        tagTransmissionConfig.setEnabled(false);
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag());
        interceptor.after(context);
    }

    public ExecuteContext buildContext(Map<String, List<String>> addHeaders)
            throws NoSuchFieldException, IllegalAccessException {
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        Field headers = coyoteRequest.getClass().getDeclaredField("headers");
        headers.setAccessible(true);
        MimeHeaders o2 = (MimeHeaders) headers.get(coyoteRequest);
        for (Map.Entry<String, List<String>> entry : addHeaders.entrySet()) {
            for (String val : entry.getValue()) {
                o2.addValue(entry.getKey()).setString(val);
            }
        }
        Request request = new Request(new Connector());
        request.setCoyoteRequest(coyoteRequest);
        arguments[0] = new RequestFacade(request);
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }

    @After
    public void afterTest() {
        pluginServiceManagerMockedStatic.close();
    }
}
