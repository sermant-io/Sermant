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

import com.huawei.registry.config.RegisterDynamicConfig;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.interceptors.BaseRegistryTest;

import com.alibaba.nacos.client.naming.beat.BeatInfo;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * nacos健康检查
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class NacosHealthInterceptorTest extends BaseRegistryTest<NacosHealthInterceptor> {
    @Test
    public void close() throws Exception {
        final BeatInfo beatInfo = new BeatInfo();
        final ExecuteContext context = buildContext(this, new Object[] {beatInfo});
        interceptor.doBefore(context);
        interceptor.close();
        Assert.assertTrue(beatInfo.isStopped());
    }

    @Test
    public void doBefore() throws Exception {
        REGISTER_CONFIG.setEnableSpringRegister(true);
        REGISTER_CONFIG.setOpenMigration(true);
        RegisterDynamicConfig.INSTANCE.setClose(false);
        final ExecuteContext context = interceptor.before(buildContext());
        Assert.assertFalse(context.isSkip());
        RegisterDynamicConfig.INSTANCE.setClose(true);
        final ExecuteContext openContext = interceptor.doBefore(buildContext());
        Assert.assertTrue(openContext.isSkip());
        RegisterDynamicConfig.INSTANCE.setClose(false);
        REGISTER_CONFIG.setEnableSpringRegister(false);
        REGISTER_CONFIG.setOpenMigration(false);
    }

    @Test
    public void doAfter() throws Exception {
        REGISTER_CONFIG.setEnableSpringRegister(true);
        REGISTER_CONFIG.setOpenMigration(true);
        final ExecuteContext context = buildContext();
        RegisterContext.INSTANCE.setAvailable(true);
        interceptor.after(context);
        Assert.assertFalse(RegisterContext.INSTANCE.isAvailable());

        // 心跳可用
        context.changeResult(1L);
        interceptor.after(context);
        Assert.assertTrue(RegisterContext.INSTANCE.isAvailable());

        // 心跳不可用
        context.changeResult(0L);
        interceptor.after(context);
        Assert.assertFalse(RegisterContext.INSTANCE.isAvailable());

        // ObjectNode 可用
        final ObjectNode node = Mockito.mock(ObjectNode.class);
        Mockito.when(node.get("clientBeatInterval")).thenReturn(new LongNode(1L));
        context.changeResult(node);
        interceptor.after(context);
        Assert.assertTrue(RegisterContext.INSTANCE.isAvailable());

        // ObjectNode 不可用
        Mockito.reset(node);
        Mockito.when(node.get("clientBeatInterval")).thenReturn(new LongNode(0L));
        context.changeResult(node);
        interceptor.after(context);
        Assert.assertFalse(RegisterContext.INSTANCE.isAvailable());
        REGISTER_CONFIG.setEnableSpringRegister(false);
        REGISTER_CONFIG.setOpenMigration(false);
        RegisterContext.INSTANCE.setAvailable(false);
    }

    @Override
    protected NacosHealthInterceptor getInterceptor() {
        return new NacosHealthInterceptor();
    }
}
