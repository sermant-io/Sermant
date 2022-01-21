/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.handler.retry;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.support.ReflectMethodCacheSupport;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 重试抽象，提供公共方法
 *
 * @author zhouss
 * @since 2022-02-10
 */
public abstract class AbstractRetry extends ReflectMethodCacheSupport implements Retry {
    protected Class<? extends Throwable>[] classes;

    /**
     * 根据用户定义的重试异常，尝试加载
     *
     * @param classNames 异常类名
     * @return classes
     */
    protected final Class<? extends Throwable>[] findClass(String[] classNames) {
        if (classNames == null || classNames.length == 0) {
            return new Class[0];
        }
        final List<Class<?>> result = new ArrayList<>(classNames.length);
        for (String className : classNames) {
            try {
                result.add(Class.forName(className, false, Thread.currentThread().getContextClassLoader()));
            } catch (ClassNotFoundException exception) {
                LoggerFactory.getLogger().info(String.format(Locale.ENGLISH,
                        "Can not find retry exception class %s", className));
            }
        }
        return result.toArray(new Class[0]);
    }

    @Override
    public boolean needRetry(Set<String> statusList, Object result) {
        if (result == null) {
            return false;
        }
        final String code = getCode(result);
        if (code == null) {
            return false;
        }
        return statusList.contains(code);
    }

    /**
     * 由子类实现 若子类实现{@link #needRetry(Set, Object)}则无需实现getCode方法
     *
     * @param result 接口响应结果
     * @return 响应状态码
     */
    protected String getCode(Object result) {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取重试异常
     *
     * @return Class<? extends Throwable>[]
     */
    protected final Class<? extends Throwable>[] getRetryExceptions() {
        if (classes != null) {
            return classes;
        }
        synchronized (this) {
            if (classes == null) {
                final RetryFramework retryFramework = retryType();
                final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
                String[] retryExceptions;
                if (retryFramework == RetryFramework.SPRING_CLOUD) {
                    retryExceptions = pluginConfig.getSpringRetryExceptions();
                } else if (retryFramework == RetryFramework.ALIBABA_DUBBO) {
                    retryExceptions = pluginConfig.getAlibabaDubboRetryExceptions();
                } else if (retryFramework == RetryFramework.APACHE_DUBBO) {
                    retryExceptions = pluginConfig.getApacheDubboRetryExceptions();
                } else {
                    throw new IllegalArgumentException("Not supported retry framework! " + retryFramework);
                }
                classes = findClass(retryExceptions);
            }
        }
        return classes;
    }
}
