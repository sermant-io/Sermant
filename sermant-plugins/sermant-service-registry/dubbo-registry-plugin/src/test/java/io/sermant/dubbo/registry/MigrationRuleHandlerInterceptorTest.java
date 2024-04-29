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

package io.sermant.dubbo.registry;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.dubbo.registry.constants.Constant;
import io.sermant.dubbo.registry.interceptor.MigrationRuleHandlerInterceptor;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.client.migration.MigrationInvoker;
import org.apache.dubbo.registry.client.migration.MigrationRuleHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test MigrationRuleHandlerInterceptor
 *
 * @author provenceee
 * @since 2022-02-15
 */
public class MigrationRuleHandlerInterceptorTest {
    private static final String INIT = "init";

    private final MigrationRuleHandlerInterceptor interceptor;

    private final Object[] arguments;

    /**
     * Constructor
     */
    public MigrationRuleHandlerInterceptorTest() {
        interceptor = new MigrationRuleHandlerInterceptor();
        arguments = new Object[1];
        arguments[0] = INIT;
    }

    /**
     * Test MigrationRuleHandler
     *
     * @see org.apache.dubbo.registry.client.migration.MigrationRuleHandler
     */
    @Test
    public void testMigrationRuleHandler() {
        // Obj is not a MigrationRuleHandler
        ExecuteContext context = ExecuteContext.forMemberMethod(TestConstant.BAR, null, arguments, null, null);
        interceptor.before(context);
        Assert.assertEquals(INIT, context.getArguments()[0]);

        // Not the SC protocol
        MigrationInvoker<?> invoker = new MigrationInvoker<>(null, null, null, null,
            URL.valueOf("foo://localhost:30100"), null);
        MigrationRuleHandler<?> handler = new MigrationRuleHandler<>(invoker);
        context = ExecuteContext.forMemberMethod(handler, null, arguments, null, null);
        interceptor.before(context);
        Assert.assertEquals(INIT, context.getArguments()[0]);

        // SC protocol
        invoker = new MigrationInvoker<>(null, null, null, null, URL.valueOf(TestConstant.SC_ADDRESS), null);
        handler = new MigrationRuleHandler<>(invoker);
        context = ExecuteContext.forMemberMethod(handler, null, arguments, null, null);
        interceptor.before(context);
        Assert.assertEquals(Constant.SC_INIT_MIGRATION_RULE, context.getArguments()[0]);
    }
}
