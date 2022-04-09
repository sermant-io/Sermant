/*
 *   Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *   the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *   an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 */

package com.huawei.example.demo.declarer;

import com.huawei.example.demo.interceptor.DemoMemberInterceptor;
import com.huawei.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huawei.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 通过类名称后缀定位到拦截点的增强定义
 *
 * @author luanwenfei
 * @since 2022-04-07
 */
public class DemoNameSuffixDeclarer extends AbstractPluginDeclarer {
    private static final int PARAMS_COUNT = 2;

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameSuffixedWith("DemoNameService");
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[] {
            InterceptDeclarer.build(MethodMatcher.and(MethodMatcher.paramCountEquals(PARAMS_COUNT),
                MethodMatcher.paramTypesEqual(String.class, int.class)), new DemoMemberInterceptor())};
    }
}
