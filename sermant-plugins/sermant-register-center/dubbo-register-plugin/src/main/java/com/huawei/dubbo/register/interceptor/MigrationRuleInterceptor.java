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
import com.huawei.sermant.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;

import org.apache.dubbo.rpc.cluster.support.migration.MigrationRule;
import org.apache.dubbo.rpc.cluster.support.migration.MigrationStep;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 增强MigrationRule类的parse方法
 *
 * @author provenceee
 * @date 2022年1月26日
 */
public class MigrationRuleInterceptor implements StaticMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        if (Constant.SC_INIT_MIGRATION_RULE.equals(arguments[0])) {
            // 2.7.10-2.7.15，如果规则为scInit，则把MigrationRule设置为FORCE_INTERFACE，以屏蔽sc应用级注册
            MigrationRule migrationRule = new MigrationRule();
            migrationRule.setStep(MigrationStep.FORCE_INTERFACE);
            beforeResult.setResult(migrationRule);
        }
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "MigrationRule is error!", throwable);
    }
}