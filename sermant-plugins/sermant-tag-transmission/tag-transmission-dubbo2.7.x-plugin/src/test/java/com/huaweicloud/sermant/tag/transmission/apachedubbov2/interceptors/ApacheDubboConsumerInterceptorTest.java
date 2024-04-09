/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.apachedubbov2.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficTag;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;

import org.apache.dubbo.rpc.RpcInvocation;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * ApacheDubboConsumerInterceptorTest
 *
 * @author daizhenyu
 * @since 2023-08-29
 **/
public class ApacheDubboConsumerInterceptorTest extends AbstractRpcInterceptorTest {
    private final ApacheDubboConsumerInterceptor interceptor = new ApacheDubboConsumerInterceptor();

    public ApacheDubboConsumerInterceptorTest() {
    }

    @Override
    public void doBefore(TrafficTag trafficTag) {
        TrafficUtils.setTrafficTag(trafficTag);
    }

    @Test
    public void testApacheDubboConsumer() {
        // defineParameter
        ExecuteContext context;
        ExecuteContext returnContext;
        Map<String, String> expectAttachments;

        // RpcInvocation is null
        context = buildContext(null);
        returnContext = interceptor.before(context);
        Assert.assertNull(returnContext.getArguments()[1]);

        // The traffic tag transmission switch is turned off
        tagTransmissionConfig.setEnabled(false);
        context = buildContext(new RpcInvocation());
        returnContext = interceptor.before(context);
        expectAttachments = buildExpectAttachments();
        Assert.assertEquals(expectAttachments, ((RpcInvocation) returnContext.getArguments()[1]).getAttachments());
        tagTransmissionConfig.setEnabled(true);

        // traffic tag contains a complete traffic tag
        expectAttachments = buildExpectAttachments("id", "name");
        context = buildContext(new RpcInvocation());
        returnContext = interceptor.before(context);
        Assert.assertEquals(((RpcInvocation) returnContext.getArguments()[1]).getAttachments(),
                expectAttachments);

        // traffic tag contains partial traffic tags
        TrafficUtils.getTrafficTag().getTag().remove("id");
        expectAttachments = buildExpectAttachments("name");
        context = buildContext(new RpcInvocation());
        returnContext = interceptor.before(context);
        Assert.assertEquals(((RpcInvocation) returnContext.getArguments()[1]).getAttachments(),
                expectAttachments);

        // traffic tag there is no traffic tag
        TrafficUtils.removeTrafficTag();
        expectAttachments = buildExpectAttachments();
        context = buildContext(new RpcInvocation());
        returnContext = interceptor.before(context);
        Assert.assertEquals(expectAttachments, ((RpcInvocation) returnContext.getArguments()[1]).getAttachments());
    }

    private Map<String, String> buildExpectAttachments(String... keys) {
        Map<String, String> expectAttachments = new HashMap<>();
        for (String key : keys) {
            expectAttachments.put(key, fullTrafficTag.get(key).get(0));
        }
        return expectAttachments;
    }

    private ExecuteContext buildContext(RpcInvocation rpcInvocation) {
        Object[] arguments = new Object[]{null, rpcInvocation};
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }
}