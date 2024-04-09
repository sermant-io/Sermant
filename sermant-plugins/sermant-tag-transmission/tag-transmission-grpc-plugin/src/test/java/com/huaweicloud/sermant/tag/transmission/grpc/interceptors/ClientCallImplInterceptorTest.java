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

package com.huaweicloud.sermant.tag.transmission.grpc.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficTag;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;

import io.grpc.Metadata;
import io.grpc.Metadata.Key;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * ClientCallImplInterceptorTest
 *
 * @author daizhenyu
 * @since 2023-08-31
 **/
public class ClientCallImplInterceptorTest extends AbstractRpcInterceptorTest {
    private final ClientCallImplInterceptor interceptor = new ClientCallImplInterceptor();

    public ClientCallImplInterceptorTest() {
    }

    @Override
    public void doBefore(TrafficTag trafficTag) {
        TrafficUtils.setTrafficTag(trafficTag);
    }

    @Test
    public void testGrpcConsumer() {
        Object[] arguments;
        ExecuteContext context;
        ExecuteContext returnContext;
        Metadata metadata;
        Key<String> name = Key.of("name", Metadata.ASCII_STRING_MARSHALLER);
        Key<String> id = Key.of("id", Metadata.ASCII_STRING_MARSHALLER);
        Key<String> key = Key.of("key", Metadata.ASCII_STRING_MARSHALLER);

        // Metadata is null
        arguments = new Object[]{null, null};
        context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
        returnContext = interceptor.before(context);
        Assert.assertNull(returnContext.getArguments()[1]);

        // traffic label off
        tagTransmissionConfig.setEnabled(false);
        metadata = new Metadata();
        arguments = new Object[]{null, metadata};
        context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
        returnContext = interceptor.before(context);
        Assert.assertNull(((Metadata) returnContext.getArguments()[1]).get(name));
        Assert.assertNull(((Metadata) returnContext.getArguments()[1]).get(id));
        tagTransmissionConfig.setEnabled(true);

        // Metadata is not null, TrafficTag is all matching traffic tags
        metadata = new Metadata();
        arguments = new Object[]{null, metadata};
        context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
        interceptor.before(context);
        Assert.assertEquals("001", metadata.get(id));
        Assert.assertEquals("test001", metadata.get(name));

        // Metadata is not null, TrafficTag contains unmatched traffic tags
        TrafficUtils.getTrafficTag().getTag().put("key", Collections.singletonList("value"));
        metadata = new Metadata();
        arguments = new Object[]{null, metadata};
        context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
        interceptor.before(context);
        Assert.assertEquals("001", metadata.get(id));
        Assert.assertEquals("test001", metadata.get(name));
        Assert.assertNull(metadata.get(key));
    }
}