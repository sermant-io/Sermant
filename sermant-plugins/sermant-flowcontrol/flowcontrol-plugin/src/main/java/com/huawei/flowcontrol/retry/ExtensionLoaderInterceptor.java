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

package com.huawei.flowcontrol.retry;

import com.huawei.flowcontrol.retry.cluster.ClusterInvokerCreator;
import com.huawei.flowcontrol.service.InterceptorSupporter;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.ClassUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ExtensionLoader 拦截器， 用于注入cluster
 *
 * @author zhouss
 * @since 2022-03-04
 */
public class ExtensionLoaderInterceptor extends InterceptorSupporter {
    private final AtomicBoolean isDefined = new AtomicBoolean();

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        return context;
    }

    /**
     * 后置方法, 注入cluster invoker
     * <p>{@link com.huawei.flowcontrol.retry.cluster.AlibabaDubboCluster}</p>
     * <p>{@link com.huawei.flowcontrol.retry.cluster.AlibabaDubboClusterInvoker}</p>
     * <p>{@link com.huawei.flowcontrol.retry.cluster.ApacheDubboCluster}</p>
     * <p>{@link com.huawei.flowcontrol.retry.cluster.ApacheDubboClusterInvoker}</p>
     *
     * @param context 执行上下文
     * @return 上下文
     */
    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        final Class<?> type = (Class<?>) context.getMemberFieldValue("type");
        if (type == null) {
            return context;
        }
        if (canInjectClusterInvoker(type.getName()) && isDefined.compareAndSet(false, true)) {
            if (!(context.getResult() instanceof Map)) {
                return context;
            }
            final Map<String, Class<?>> classes = (Map<String, Class<?>>) context.getResult();
            final String retryClusterInvoker = flowControlConfig.getRetryClusterInvoker();
            if (classes.get(retryClusterInvoker) != null) {
                return context;
            }
            final Optional<Class<?>> retryInvokerClass;
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (APACHE_DUBBO_CLUSTER_CLASS_NAME.equals(type.getName())) {
                ClassUtils.defineClass(
                    "com.huawei.flowcontrol.retry.cluster.ApacheDubboClusterInvoker", contextClassLoader);
                retryInvokerClass = ClassUtils.defineClass(
                    "com.huawei.flowcontrol.retry.cluster.ApacheDubboCluster", contextClassLoader);
            } else if (ALIBABA_DUBBO_CLUSTER_CLASS_NAME.equals(type.getName())) {
                ClassUtils.defineClass(
                    "com.huawei.flowcontrol.retry.cluster.AlibabaDubboClusterInvoker", contextClassLoader);
                retryInvokerClass = ClassUtils.defineClass(
                    "com.huawei.flowcontrol.retry.cluster.AlibabaDubboCluster", contextClassLoader);
            } else {
                return context;
            }
            retryInvokerClass.ifPresent(invokerClass -> classes.put(retryClusterInvoker, invokerClass));
            ClusterInvokerCreator.INSTANCE.getClusterInvokerMap().putAll(classes);
        }
        return context;
    }
}
