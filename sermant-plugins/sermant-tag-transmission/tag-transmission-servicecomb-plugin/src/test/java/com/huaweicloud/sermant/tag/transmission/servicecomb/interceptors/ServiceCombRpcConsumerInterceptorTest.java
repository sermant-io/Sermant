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

package com.huaweicloud.sermant.tag.transmission.servicecomb.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficTag;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;

import org.apache.servicecomb.core.Invocation;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * ServiceCombRpcConsumerInterceptorTest
 *
 * @author daizhenyu
 * @since 2023-08-30
 **/
public class ServiceCombRpcConsumerInterceptorTest extends AbstractRpcInterceptorTest {
    private final ServiceCombRpcConsumerInterceptor interceptor = new ServiceCombRpcConsumerInterceptor();

    public ServiceCombRpcConsumerInterceptorTest() {
    }

    @Override
    public void doBefore(TrafficTag trafficTag) {
        TrafficUtils.setTrafficTag(trafficTag);
    }

    @Test
    public void testServiceCombRpcConsumer() {
        // defineParameter
        ExecuteContext context;
        ExecuteContext returnContext;
        Map<String, String> expectContext;

        // Invocation is null
        context = buildContext(null);
        returnContext = interceptor.before(context);
        Assert.assertNull(returnContext.getArguments()[0]);

        // The traffic tag transmission switch is turned off
        tagTransmissionConfig.setEnabled(false);
        context = buildContext(new Invocation());
        returnContext = interceptor.before(context);
        expectContext = buildExpectContext();
        Assert.assertEquals(((Invocation) returnContext.getArguments()[0]).getContext(), expectContext);
        tagTransmissionConfig.setEnabled(true);

        // the TrafficTag contains complete tag information
        context = buildContext(new Invocation());
        returnContext = interceptor.before(context);
        expectContext = buildExpectContext("id", "name");
        Assert.assertEquals(((Invocation) returnContext.getArguments()[0]).getContext(), expectContext);

        // TrafficTag indicates only partial tag information
        context = buildContext(new Invocation());
        TrafficUtils.getTrafficTag().getTag().remove("id");
        returnContext = interceptor.before(context);
        expectContext = buildExpectContext("name");
        Assert.assertEquals(((Invocation) returnContext.getArguments()[0]).getContext(), expectContext);

        // there is no tag information in TrafficTag
        TrafficUtils.removeTrafficTag();
        context = buildContext(new Invocation());
        returnContext = interceptor.before(context);
        expectContext = buildExpectContext();
        Assert.assertEquals(((Invocation) returnContext.getArguments()[0]).getContext(), expectContext);
    }

    private Map<String, String> buildExpectContext(String... keys) {
        Map<String, String> expectContext = new HashMap<>();
        for (String key : keys) {
            expectContext.put(key, fullTrafficTag.get(key).get(0));
        }
        return expectContext;
    }

    private ExecuteContext buildContext(Invocation invocation) {
        Object[] arguments = new Object[]{invocation};
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }
}