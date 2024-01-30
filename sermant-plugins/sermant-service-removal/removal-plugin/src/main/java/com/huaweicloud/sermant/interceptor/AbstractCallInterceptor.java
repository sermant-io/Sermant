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

import com.huaweicloud.sermant.cache.InstanceCache;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.entity.RequestInfo;

import java.util.List;

/**
 * 服务实例摘除抽象类
 *
 * @param <T> 被调用的实例信息
 * @author zhp
 * @since 2023-02-21
 */
public abstract class AbstractCallInterceptor<T> extends AbstractSwitchInterceptor {
    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        saveCallInfo(context);
        return context;
    }

    /**
     * 保存调用信息
     *
     * @param context 上下文信息
     */
    public void saveCallInfo(ExecuteContext context) {
        int index = getIndex();
        if (context.getArguments() != null && context.getArguments().length > index
                && context.getArguments()[index] != null) {
            T instance = (T) context.getArguments()[index];
            RequestInfo requestInfo = new RequestInfo();
            requestInfo.setHost(getHost(instance));
            requestInfo.setPort(getPort(instance));
            requestInfo.setRequestTime(System.currentTimeMillis());
            requestInfo.setSuccess(isSuccess(context));
            InstanceCache.saveInstanceInfo(requestInfo);
        }
    }

    /**
     * 获取实例信息的参数下标
     *
     * @return 例信息的参数下标
     */
    protected abstract int getIndex();

    /**
     * 获取实例IP
     *
     * @param object 实例信息
     * @return 实例IP
     */
    protected abstract String getHost(T object);

    /**
     * 获取实例端口
     *
     * @param object 实例信息
     * @return 实例端口
     */
    protected abstract String getPort(T object);

    /**
     * 判断调用结果
     *
     * @param context 上下文信息
     * @return 调用结果成功或者失败
     */
    protected boolean isSuccess(ExecuteContext context) {
        if (REMOVAL_CONFIG.getExceptions() == null || REMOVAL_CONFIG.getExceptions().isEmpty()) {
            return true;
        }
        if (context.getThrowable() == null) {
            return true;
        }
        List<String> exceptions = REMOVAL_CONFIG.getExceptions();
        Throwable cause = context.getThrowable().getCause();

        // Dubbo或者SpringCloud存在异常封装
        if (cause == null) {
            return !exceptions.contains(context.getThrowable().getClass().getName());
        }
        if (cause.getCause() == null) {
            return !exceptions.contains(cause.getClass().getName());
        }
        return !exceptions.contains(cause.getCause().getClass().getName());
    }
}
