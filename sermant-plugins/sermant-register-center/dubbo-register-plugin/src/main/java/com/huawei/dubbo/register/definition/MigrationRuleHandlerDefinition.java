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

package com.huawei.dubbo.register.definition;

/**
 * MigrationRuleHandler增强类
 *
 * @author provenceee
 * @date 2022/1/26
 */
public class MigrationRuleHandlerDefinition extends AbstractDefinition {
    private static final String ENHANCE_CLASS = "org.apache.dubbo.registry.client.migration.MigrationRuleHandler";

    private static final String INTERCEPT_CLASS
            = "com.huawei.dubbo.register.interceptor.MigrationRuleHandlerInterceptor";

    private static final String METHOD_NAME = "doMigrate";

    public MigrationRuleHandlerDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }
}
