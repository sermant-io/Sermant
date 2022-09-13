/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.interceptors.health;

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.interceptors.BaseRegistryTest;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceWatch;

/**
 * zookeeper健康检查
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class ZookeeperHealthInterceptorTest extends BaseRegistryTest<ZookeeperHealthInterceptor> {
    @Test
    public void close() throws Exception {
        final CuratorFramework client = Mockito.mock(CuratorFramework.class);
        final ZookeeperServiceWatch watch = new ZookeeperServiceWatch(client, null);
        final ExecuteContext context = buildContext(watch, new Object[0]);
        interceptor.doBefore(context);
        interceptor.close();
        Mockito.verify(client, Mockito.times(1)).close();
    }

    @Test
    public void doBefore() throws Exception {
        final TreeCacheEvent event = new TreeCacheEvent(Type.CONNECTION_RECONNECTED, null);
        final ExecuteContext context = buildContext(this, new Object[]{null, event});
        RegisterContext.INSTANCE.setAvailable(false);
        interceptor.doBefore(context);
        Assert.assertTrue(RegisterContext.INSTANCE.isAvailable());

        final TreeCacheEvent loseEvent = new TreeCacheEvent(Type.CONNECTION_LOST, null);
        final ExecuteContext loseContext = buildContext(this, new Object[]{null, loseEvent});
        RegisterContext.INSTANCE.setAvailable(false);
        interceptor.doBefore(loseContext);
        Assert.assertFalse(RegisterContext.INSTANCE.isAvailable());
    }

    @Override
    protected ZookeeperHealthInterceptor getInterceptor() {
        return new ZookeeperHealthInterceptor();
    }
}
