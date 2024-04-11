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

package com.huaweicloud.sermant.tag.transmission.sofarpc.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficTag;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;

import com.alipay.sofa.rpc.core.request.SofaRequest;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SofaRpcServerInterceptorTest
 *
 * @author daizhenyu
 * @since 2023-08-30
 **/
public class SofaRpcServerInterceptorTest extends AbstractRpcInterceptorTest {
    private final SofaRpcServerInterceptor interceptor = new SofaRpcServerInterceptor();

    public SofaRpcServerInterceptorTest() {
    }

    @Override
    public void doBefore(TrafficTag trafficTag) {
    }

    @Test
    public void testSofaRpcServer() {
        // defineParameter
        Map<String, Object> requestProp;
        ExecuteContext context;
        ExecuteContext returnContext;
        Map<String, List<String>> expectTrafficTag;

        // SofaRequest is null
        context = buildContext(null, null);
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag());

        // The traffic tag tranmission switch is turned off
        tagTransmissionConfig.setEnabled(false);
        context = buildContext(new SofaRequest(), new HashMap<>());
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag());
        tagTransmissionConfig.setEnabled(true);

        // SofaRequest includes full tag
        requestProp = new HashMap<>();
        requestProp.put("id", "001");
        requestProp.put("name", "test001");
        context = buildContext(new SofaRequest(), requestProp);
        returnContext = interceptor.before(context);
        expectTrafficTag = buildExpectTrafficTag("id", "name");
        Assert.assertEquals(TrafficUtils.getTrafficTag().getTag(), expectTrafficTag);
        interceptor.after(returnContext);

        // SofaRequest contains partial tag
        requestProp = new HashMap<>();
        requestProp.put("id", "001");
        context = buildContext(new SofaRequest(), requestProp);
        interceptor.before(context);
        expectTrafficTag = buildExpectTrafficTag("id");
        Assert.assertEquals(TrafficUtils.getTrafficTag().getTag(), expectTrafficTag);
        interceptor.after(returnContext);
    }

    private ExecuteContext buildContext(SofaRequest sofaRequest, Map<String, Object> headers) {
        if (sofaRequest != null) {
            sofaRequest.addRequestProps(headers);
        }
        Object[] arguments = new Object[]{sofaRequest};
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }
}