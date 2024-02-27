/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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
 * 增强dubbo3.x AbstractDirectory的子类的doList方法，筛选标签应用的地址
 *
 * @author chengyouling
 * @since 2024-02-20
 */
public class Dubbo3AbstractDirectoryInterceptor extends AbstractInterceptor {
    private static final int INDEX = 2;
    private final AbstractDirectoryService abstractDirectoryService;

    /**
     * 构造方法
     */
    public Dubbo3AbstractDirectoryInterceptor() {
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
        context.changeResult(abstractDirectoryService.selectInvokers(context.getObject(), arguments[INDEX],
                context.getResult()));
        LogUtils.printDubboRequestAfterPoint(context);
        return context;
    }
}