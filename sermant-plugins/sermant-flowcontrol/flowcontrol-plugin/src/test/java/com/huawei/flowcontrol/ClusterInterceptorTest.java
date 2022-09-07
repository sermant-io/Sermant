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

package com.huawei.flowcontrol;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.apache.dubbo.common.URL;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * 集群拦截点上下文测试
 *
 * @author zhouss
 * @since 2022-09-13
 */
public class ClusterInterceptorTest {
    @Test
    public void doBefore() throws Exception {
        String interfaceName = "com.huaweicloud.test";
        String application = "test";
        String protocol = "dubbo";
        String host = "localhost";
        int port = 8080;
        final ClusterInterceptor clusterInterceptor = new ClusterInterceptor();
        final HashMap<String, String> params = new HashMap<>();
        params.put("interface", interfaceName);
        params.put("application", application);
        final URL url = new URL(protocol, host, port, params);
        clusterInterceptor.doBefore(buildContext(new Object[]{url}));
        Assert.assertEquals(DubboApplicationCache.INSTANCE.getApplicationCache().get(interfaceName), application);
        DubboApplicationCache.INSTANCE.getApplicationCache().clear();
        final com.alibaba.dubbo.common.URL aliUrl = new com.alibaba.dubbo.common.URL(protocol, host, port, params);
        clusterInterceptor.doBefore(buildContext(new Object[]{aliUrl}));
        Assert.assertEquals(DubboApplicationCache.INSTANCE.getApplicationCache().get(interfaceName), application);
        DubboApplicationCache.INSTANCE.getApplicationCache().clear();
    }

    private ExecuteContext buildContext(Object[] args) throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(this, String.class.getDeclaredMethod("trim"), args,
                null, null);
    }
}
