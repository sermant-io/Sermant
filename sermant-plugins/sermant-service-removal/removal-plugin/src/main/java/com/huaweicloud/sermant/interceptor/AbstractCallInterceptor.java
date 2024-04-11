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
 * Abstract class of service instance removal
 *
 * @param <T> Information about the instance that was called
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
     * Save the call information
     *
     * @param context Contextual information
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
     * Obtain the parameter subscript of instance information
     *
     * @return Parameter subscript for example information
     */
    protected abstract int getIndex();

    /**
     * Obtain the IP address of the instance
     *
     * @param object Instance information
     * @return IP address of the instance
     */
    protected abstract String getHost(T object);

    /**
     * Obtain the instance port
     *
     * @param object Instance information
     * @return Instance port
     */
    protected abstract String getPort(T object);

    /**
     * Determine the result of the call
     *
     * @param context Contextual information
     * @return The result of the call is successful or failed
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

        // Dubbo or SpringCloud has abnormal encapsulation
        if (cause == null) {
            return !exceptions.contains(context.getThrowable().getClass().getName());
        }
        if (cause.getCause() == null) {
            return !exceptions.contains(cause.getClass().getName());
        }
        return !exceptions.contains(cause.getCause().getClass().getName());
    }
}
