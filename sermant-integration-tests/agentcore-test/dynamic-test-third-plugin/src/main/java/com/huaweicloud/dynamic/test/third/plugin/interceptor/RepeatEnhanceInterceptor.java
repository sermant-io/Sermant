/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.dynamic.test.third.plugin.interceptor;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 动态安装卸载测试third插件的拦截器
 *
 * @author tangle
 * @since 2023-09-27
 */
public class RepeatEnhanceInterceptor extends AbstractInterceptor {
    /**
     * third测试插件增强参数下标
     */
    private static final int ARGS_INDEX = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext before(ExecuteContext context) {
        context.getArguments()[ARGS_INDEX] = true;
        LOGGER.log(Level.INFO, "Test repeat enhance, third plugin enhance success");
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
