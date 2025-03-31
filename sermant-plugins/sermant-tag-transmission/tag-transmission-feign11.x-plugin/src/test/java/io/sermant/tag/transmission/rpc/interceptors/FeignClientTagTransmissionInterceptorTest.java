/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.tag.transmission.rpc.interceptors;

import feign.Request;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.tag.TrafficUtils;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FeignClientTagTransmissionInterceptorTest
 *
 * @author chengyouling
 * @since 2025-02-25
 */
public class FeignClientTagTransmissionInterceptorTest extends BaseInterceptorTest {
    private final Object[] arguments;

    public FeignClientTagTransmissionInterceptorTest() {
        arguments = new Object[1];
    }

    @Test
    public void testClient() {
        FeignClientTagTransmissionInterceptor interceptor = new FeignClientTagTransmissionInterceptor();
        ExecuteContext context;
        ExecuteContext resContext;
        Map<String, List<String>> addHeaders = new HashMap<>();
        Map<String, List<String>> tags = new HashMap<>();
        TrafficUtils.removeTrafficTag();

        // no headers no tags
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(0, ((Request) resContext.getArguments()[0]).headers().size());
        TrafficUtils.removeTrafficTag();

        // there are headers but no tags
        addHeaders.put("defaultKey", Collections.singletonList("defaultValue"));
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(1, ((Request) resContext.getArguments()[0]).headers().size());
        Assert.assertEquals("defaultValue",
                ((Request) resContext.getArguments()[0]).headers().get("defaultKey").toArray()[0]);
        TrafficUtils.removeTrafficTag();

        // there are headers and tags
        List<String> ids = new ArrayList<>();
        ids.add("testId001");
        ids.add("testId002");
        tags.put("x_lane_canary", ids);
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(2, ((Request) resContext.getArguments()[0]).headers().size());
        Assert.assertEquals("testId001",
                ((Request) resContext.getArguments()[0]).headers().get("x_lane_canary").toArray()[0]);
        TrafficUtils.removeTrafficTag();
    }

    private ExecuteContext buildContext(Map<String, List<String>> addHeaders) {
        Map<String, Collection<String>> requestHeaders = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : addHeaders.entrySet()) {
            List<String> values = new ArrayList<>(entry.getValue());
            requestHeaders.put(entry.getKey(), values);
        }
        Request request = Request.create("GET", "", requestHeaders, null, StandardCharsets.UTF_8);
        arguments[0] = request;
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }
}
