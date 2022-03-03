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

package com.huawei.flowcontrol;

import static org.mockito.Mockito.mock;

import com.huawei.flowcontrol.apache.ApacheInvocation;
import com.huawei.flowcontrol.apache.ApacheInvoker;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.monitor.support.MonitorFilter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.cluster.directory.StaticDirectory;
import org.apache.dubbo.rpc.cluster.support.FailoverClusterInvoker;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * apache dubbo 测试
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class ApacheDubboInterceptorTest extends BaseTest {
    private ExecuteContext context;

    private Interceptor interceptor;

    @Before
    public void before() {
        interceptor = mock(ApacheDubboInterceptor.class);
        Object proxy = new MonitorFilter();
        Object[] allArguments = new Object[2];
        allArguments[0] = new FailoverClusterInvoker<>(new StaticDirectory<>(URL.valueOf("dubbo://localhost:8080"),
            Collections.singletonList(new ApacheInvoker())));
        allArguments[1] = new ApacheInvocation((Invoker<?>) allArguments[0]);
        context = ExecuteContext.forMemberMethod(proxy, null, allArguments, Collections.emptyMap(),
            Collections.emptyMap());
    }

    @Test
    public void testInterceptor() throws Exception {
        interceptor.before(context);
        interceptor.after(context);
        interceptor.onThrow(context);
    }
}
