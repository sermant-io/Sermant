/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.LogUtils;
import com.huaweicloud.sermant.router.common.service.AbstractDirectoryService;

/**
 * The doList method of the AbstractDirectory subclass is enhanced to filter the addresses to which the label is
 * applied
 *
 * @author provenceee
 * @since 2021-06-28
 */
public class AbstractDirectoryInterceptor extends AbstractInterceptor {
    private final AbstractDirectoryService abstractDirectoryService;

    /**
     * Constructor
     */
    public AbstractDirectoryInterceptor() {
        abstractDirectoryService = PluginServiceManager.getPluginService(AbstractDirectoryService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        LogUtils.printDubboRequestBeforePoint(context);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        Object[] arguments = context.getArguments();

        // DUBBO 2.x and DUBBO 3.O.x doList method is one parameter/3.1.x two parameter/3.2.x three parameters
        // all version invocation parameter at last
        Object invocation = arguments[arguments.length - 1];
        context.changeResult(abstractDirectoryService.selectInvokers(context.getObject(), invocation,
                context.getResult()));
        LogUtils.printDubboRequestAfterPoint(context);
        return context;
    }
}