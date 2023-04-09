/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.interceptor;

import com.huaweicloud.sermant.cache.DubboCache;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * 增强ClusterUtils类的mergeUrl方法
 *
 * @param <T> 参数类型
 * @author zhp
 * @since 2023-02-17
 */
public abstract class AbstractClusterUtilsInterceptor<T> extends AbstractSwitchInterceptor {
    private static final int ARG_LENGTH = 2;

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (context.getArguments() == null || context.getArguments().length < ARG_LENGTH) {
            return context;
        }
        T url = (T) context.getArguments()[0];
        DubboCache.putService(getInterfaceName(url), getServiceName(url));
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * 获取接口名称
     *
     * @param url 链接信息
     * @return 接口名称
     */
    protected abstract String getInterfaceName(T url);

    /**
     * 获取服务名称
     *
     * @param url 链接信息
     * @return 服务名称
     */
    protected abstract String getServiceName(T url);
}
