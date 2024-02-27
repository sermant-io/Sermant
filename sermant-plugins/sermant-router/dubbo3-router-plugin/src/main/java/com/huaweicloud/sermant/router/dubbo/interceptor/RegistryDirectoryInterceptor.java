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
import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.utils.DubboReflectUtils;

/**
 * 增强RegistryDirectory类的mergeUrl方法
 *
 * @author chengyouling
 * @since 2024-02-20
 */
public class RegistryDirectoryInterceptor extends AbstractInterceptor {
    private static final String APPLICATION_KEY = "application";

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        String application = DubboReflectUtils.getParameter(arguments[0], APPLICATION_KEY);

        // 保存接口与服务名之间的映射
        DubboCache.INSTANCE.putApplication(DubboReflectUtils.getServiceInterface(arguments[0]), application);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}