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

package com.huawei.dubbo.register.declarer;

import com.huawei.sermant.core.plugin.agent.declarer.InterceptDeclarer;

/**
 * 增强MigrationRule类的parse方法
 *
 * @author provenceee
 * @since 2022年1月26日
 */
public class MigrationRuleDeclarer extends AbstractDeclarer {
    private static final String[] ENHANCE_CLASS = {"org.apache.dubbo.rpc.cluster.support.migration.MigrationRule"};

    private static final String INTERCEPT_CLASS = "com.huawei.dubbo.register.interceptor.MigrationRuleInterceptor";

    private static final String METHOD_NAME = "parse";

    public MigrationRuleDeclarer() {
        super(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
            InterceptDeclarer.build(getStaticMethod(METHOD_NAME), INTERCEPT_CLASS)
        };
    }
}