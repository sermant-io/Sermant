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

package com.huaweicloud.sermant.router.spring.declarer;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * Definition of interception points for org.springframework.web.client.RestTemplate<br>
 *
 * @author yuzl Yu Zhenlong
 * @since 2022-10-27
 */
public class RestTemplateDeclarer extends BaseRegistryPluginAdaptationDeclarer {
    private static final String ENHANCE_CLASS = "org.springframework.web.client.RestTemplate";

    private static final String ENHANCE_METHOD = "doExecute";

    private static final String INTERCEPTOR_CLASS =
            "com.huaweicloud.sermant.router.spring.interceptor.RestTemplateInterceptor";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals(ENHANCE_METHOD), INTERCEPTOR_CLASS)};
    }
}
