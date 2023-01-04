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

package com.huaweicloud.sermant.router.dubbo.declarer;

import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 增强AbstractConfig类的appendParameters方法，增加路由标签
 *
 * @author chengyouling
 * @since 2022-12-28
 */
public class AbstractConfigDeclarer extends AbstractDeclarer {
    private static final String[] ENHANCE_CLASS = {"com.alibaba.dubbo.config.AbstractConfig"};

    private static final String INTERCEPT_CLASS
        = "com.huaweicloud.sermant.router.dubbo.interceptor.AbstractConfigInterceptor";

    private static final String METHOD_NAME = "appendParameters";

    private static final int PARAMETER_COUNT = 2;

    /**
     * 构造方法
     */
    public AbstractConfigDeclarer() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }

    /**
     * 获取方法匹配器
     *
     * @return 方法匹配器
     */
    @Override
    public MethodMatcher getMethodMatcher() {
        return super.getMethodMatcher().and(MethodMatcher.paramCountEquals(PARAMETER_COUNT));
    }
}