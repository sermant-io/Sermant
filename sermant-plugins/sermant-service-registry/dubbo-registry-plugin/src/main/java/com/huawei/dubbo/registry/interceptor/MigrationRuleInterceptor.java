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

package com.huawei.dubbo.registry.interceptor;

import com.huawei.dubbo.registry.constants.Constant;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

import org.apache.dubbo.rpc.cluster.support.migration.MigrationRule;
import org.apache.dubbo.rpc.cluster.support.migration.MigrationStep;

/**
 * Enhance the parse method of the MigrationRule class
 *
 * @author provenceee
 * @since 2022-01-26
 */
public class MigrationRuleInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (Constant.SC_INIT_MIGRATION_RULE.equals(context.getArguments()[0])) {
            // 2.7.10-2.7.15, if the rule is scInit, set the MigrationRule to FORCE-INTERFACE to block sc application
            // level registration
            MigrationRule migrationRule = new MigrationRule();
            migrationRule.setStep(MigrationStep.FORCE_INTERFACE);
            context.skip(migrationRule);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
