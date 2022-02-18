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

package com.huawei.dubbo.register;

import com.huawei.dubbo.register.constants.Constant;
import com.huawei.dubbo.register.interceptor.MigrationRuleHandlerInterceptor;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.client.migration.MigrationInvoker;
import org.apache.dubbo.registry.client.migration.MigrationRuleHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * 测试MigrationRuleHandlerInterceptor
 *
 * @author provenceee
 * @since 2022/2/15
 */
public class MigrationRuleHandlerInterceptorTest {
    private static final String INIT = "init";

    private final MigrationRuleHandlerInterceptor interceptor;

    private final Object[] arguments;

    public MigrationRuleHandlerInterceptorTest() {
        interceptor = new MigrationRuleHandlerInterceptor();
        arguments = new Object[1];
        arguments[0] = INIT;
    }

    /**
     * 测试MigrationRuleHandler
     *
     * @see org.apache.dubbo.registry.client.migration.MigrationRuleHandler
     */
    @Test
    public void testMigrationRuleHandler() {
        // obj不为MigrationRuleHandler
        ExecuteContext context = ExecuteContext.forMemberMethod(TestConstant.BAR, null, arguments, null, null);
        interceptor.before(context);
        Assert.assertEquals(INIT, context.getArguments()[0]);

        // 不是sc协议
        MigrationInvoker<?> invoker = new MigrationInvoker<>(null, null, null, null,
            URL.valueOf("foo://localhost:30100"), null);
        MigrationRuleHandler<?> handler = new MigrationRuleHandler<>(invoker);
        context = ExecuteContext.forMemberMethod(handler, null, arguments, null, null);
        interceptor.before(context);
        Assert.assertEquals(INIT, context.getArguments()[0]);

        // sc协议
        invoker = new MigrationInvoker<>(null, null, null, null, URL.valueOf(TestConstant.SC_ADDRESS), null);
        handler = new MigrationRuleHandler<>(invoker);
        context = ExecuteContext.forMemberMethod(handler, null, arguments, null, null);
        interceptor.before(context);
        Assert.assertEquals(Constant.SC_INIT_MIGRATION_RULE, context.getArguments()[0]);
    }
}