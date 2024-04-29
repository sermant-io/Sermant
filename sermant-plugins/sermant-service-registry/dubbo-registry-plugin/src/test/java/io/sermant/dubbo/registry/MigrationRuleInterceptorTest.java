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
import io.sermant.dubbo.registry.interceptor.MigrationRuleInterceptor;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.rpc.cluster.support.migration.MigrationRule;
import org.apache.dubbo.rpc.cluster.support.migration.MigrationStep;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test MigrationRuleInterceptor
 *
 * @author provenceee
 * @since 2022-02-15
 */
public class MigrationRuleInterceptorTest {
    private final MigrationRuleInterceptor interceptor;

    private final Object[] arguments;

    private final ExecuteContext context;

    /**
     * Constructor
     */
    public MigrationRuleInterceptorTest() throws NoSuchMethodException {
        ApplicationModel.getConfigManager().setApplication(new ApplicationConfig(TestConstant.BAR));
        interceptor = new MigrationRuleInterceptor();
        arguments = new Object[1];
        context = ExecuteContext.forStaticMethod(MigrationRule.class, String.class.getMethod("trim"), arguments, null);
    }

    /**
     * Test MigrationRule
     *
     * @see org.apache.dubbo.rpc.cluster.support.migration.MigrationRule
     */
    @Test
    public void testMigrationRule() {
        // Test null
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());
        Assert.assertNull(context.getResult());

        // Test non-SC
        arguments[0] = "init";
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());
        Assert.assertNull(context.getResult());

        // Test SC
        arguments[0] = Constant.SC_INIT_MIGRATION_RULE;
        interceptor.before(context);
        Assert.assertTrue(context.isSkip());
        Assert.assertNotNull(context.getResult());
        Assert.assertEquals(MigrationStep.FORCE_INTERFACE, ((MigrationRule) context.getResult()).getStep());
    }
}