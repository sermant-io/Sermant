/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

import org.apache.dubbo.registry.client.migration.MigrationInvoker;
import org.apache.dubbo.registry.client.migration.MigrationRuleHandler;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhance the doMigrate method of the MigrationRuleHandler class
 *
 * @author provenceee
 * @since 2022-01-26
 */
public class MigrationRuleHandlerInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String MIGRATION_INVOKER_FIELD_NAME = "migrationInvoker";

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (context.getObject() instanceof MigrationRuleHandler<?>) {
            MigrationRuleHandler<?> handler = (MigrationRuleHandler<?>) context.getObject();
            MigrationInvoker<?> migrationInvoker;
            try {
                Field field = handler.getClass().getDeclaredField(MIGRATION_INVOKER_FIELD_NAME);
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    field.setAccessible(true);
                    return field;
                });
                migrationInvoker = (MigrationInvoker<?>) field.get(handler);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOGGER.log(Level.SEVERE, "Cannot get the migrationInvoker.");
                return context;
            }

            // Only the SC protocol is intercepted
            if (!Constant.SC_REGISTRY_PROTOCOL.equals(migrationInvoker.getRegistryUrl().getProtocol())) {
                return context;
            }

            // Markings for sc MigrationRule, applicable from 2.7.10 to 2.7.15
            context.getArguments()[0] = Constant.SC_INIT_MIGRATION_RULE;
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
