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

package com.huawei.dubbo.register.interceptor;

import com.huawei.dubbo.register.constants.Constant;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;

import org.apache.dubbo.registry.client.migration.MigrationInvoker;
import org.apache.dubbo.registry.client.migration.MigrationRuleHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 增强MigrationRuleHandler类的doMigrate方法
 *
 * @author provenceee
 * @date 2022/1/26
 */
public class MigrationRuleHandlerInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String MIGRATION_INVOKER_FIELD_NAME = "migrationInvoker";

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (obj instanceof MigrationRuleHandler<?>) {
            MigrationRuleHandler<?> handler = (MigrationRuleHandler<?>) obj;
            MigrationInvoker<?> migrationInvoker;
            try {
                Field field = handler.getClass().getDeclaredField(MIGRATION_INVOKER_FIELD_NAME);
                field.setAccessible(true);
                migrationInvoker = (MigrationInvoker<?>) field.get(handler);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Cannot get the migrationInvoker.");
                return;
            }
            // 只拦截sc协议的
            if (!Constant.SC_REGISTRY_PROTOCOL.equals(migrationInvoker.getRegistryUrl().getProtocol())) {
                return;
            }
            // sc MigrationRule的标记，适用2.7.10-2.7.15
            arguments[0] = Constant.SC_INIT_MIGRATION_RULE;
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "MigrationRuleHandler is error!", throwable);
    }
}