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

package com.huawei.flowcontrol.retry.cluster;

import com.huawei.flowcontrol.DubboApplicationCache;
import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.context.FlowControlContext;
import com.huawei.flowcontrol.common.entity.DubboRequestEntity;
import com.huawei.flowcontrol.common.entity.RequestEntity.RequestType;
import com.huawei.flowcontrol.common.handler.retry.AbstractRetry;
import com.huawei.flowcontrol.common.handler.retry.Retry;
import com.huawei.flowcontrol.common.handler.retry.RetryContext;
import com.huawei.flowcontrol.common.util.ConvertUtils;
import com.huawei.flowcontrol.retry.handler.RetryHandlerV2;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.ClassUtils;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.vavr.CheckedFunction0;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import org.apache.dubbo.rpc.service.GenericException;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * apache dubbo invoker
 *
 * @param <T> 返回类型
 * @author zhouss
 * @since 2022-03-04
 */
public class ApacheDubboClusterInvoker<T> extends AbstractClusterInvoker<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Retry retry = new ApacheDubboRetry();

    private final RetryHandlerV2 retryHandler = new RetryHandlerV2();

    private final Invoker<T> delegate;

    /**
     * apache dubbo 集群调用
     *
     * @param directory service
     */
    public ApacheDubboClusterInvoker(Directory<T> directory) {
        this(directory, null);
    }

    /**
     * apache dubbo 集群调用
     *
     * @param directory service
     * @param delegate 原始调用器, 需开启配置{@link FlowControlConfig#isUseOriginInvoker()}
     */
    public ApacheDubboClusterInvoker(Directory<T> directory, Invoker<T> delegate) {
        super(directory);
        this.delegate = delegate;
    }

    @Override
    protected Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance)
            throws RpcException {
        RetryContext.INSTANCE.markRetry(retry);
        checkInvokers(invokers, invocation);
        final List<io.github.resilience4j.retry.Retry> handlers = retryHandler
                .getHandlers(convertToApacheDubboEntity(invocation, invokers.get(0)));
        DecorateCheckedSupplier<Result> dcs = Decorators.ofCheckedSupplier(buildFunc(invocation, invokers,
                loadbalance));
        io.github.resilience4j.retry.Retry retryRule = null;
        if (!handlers.isEmpty()) {
            // 重试仅支持一种策略
            retryRule = handlers.get(0);
            dcs.withRetry(retryRule);
        }
        try {
            return dcs.get();
        } catch (RpcException ex) {
            log(retryRule, invocation);
            throw ex;
        } catch (Throwable ex) {
            log(retryRule, invocation);
            throw formatEx(ex);
        } finally {
            RetryContext.INSTANCE.remove();
            FlowControlContext.INSTANCE.clear();
        }
    }

    private RuntimeException formatEx(Throwable ex) {
        if (ex instanceof GenericException) {
            return (GenericException) ex;
        }

        // 注意这里新版本可能会逐渐将该类淘汰掉, dubbo3.1.0目前还在使用
        final Optional<Class<?>> isExist = ClassUtils
                .loadClass("com.alibaba.dubbo.rpc.service.GenericException", Thread.currentThread()
                        .getContextClassLoader());
        if (isExist.isPresent() && ex instanceof com.alibaba.dubbo.rpc.service.GenericException) {
            return (com.alibaba.dubbo.rpc.service.GenericException) ex;
        }
        return new RpcException(ex.getMessage(), ex);
    }

    private void log(io.github.resilience4j.retry.Retry retryRule, Invocation invocation) {
        if (retryRule != null) {
            LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH,
                    "Retry %d times failed for interface %s.%s", retryRule.getRetryConfig().getMaxAttempts() - 1,
                    invocation.getInvoker().getInterface().getName(), invocation.getMethodName()));
        }
    }

    private CheckedFunction0<Result> buildFunc(Invocation invocation, List<Invoker<T>> invokers,
            LoadBalance loadbalance) {
        if (this.delegate == null) {
            return () -> {
                checkInvokers(invokers, invocation);
                Invoker<T> invoker = select(loadbalance, invocation, invokers, null);
                Result result = invoker.invoke(invocation);
                checkThrowEx(result);
                return result;
            };
        }
        return () -> {
            Result result = delegate.invoke(invocation);
            checkThrowEx(result);
            return result;
        };
    }

    private void checkThrowEx(Result result) throws Throwable {
        if (result != null && result.hasException() && !FlowControlContext.INSTANCE.isFlowControl()) {
            throw result.getException();
        }
    }

    /**
     * 转换apache dubbo 注意，该方法不可抽出，由于宿主依赖仅可由该拦截器加载，因此抽出会导致找不到类
     *
     * @param invocation 调用信息
     * @return DubboRequestEntity
     */
    private DubboRequestEntity convertToApacheDubboEntity(Invocation invocation, Invoker<T> invoker) {
        String interfaceName = invoker.getInterface().getName();
        String methodName = invocation.getMethodName();
        String version = invocation.getAttachment(ConvertUtils.DUBBO_ATTACHMENT_VERSION);
        final URL url = invoker.getUrl();
        boolean isGeneric = false;
        if (version == null) {
            version = url.getParameter(CommonConst.URL_VERSION_KEY, ConvertUtils.ABSENT_VERSION);
        }
        if (ConvertUtils.isGenericService(interfaceName, methodName)) {
            // 针对泛化接口, 实际接口、版本名通过url获取, 方法名基于参数获取, 为请求方法的第一个参数
            isGeneric = true;
            interfaceName = url.getParameter(CommonConst.GENERIC_INTERFACE_KEY, interfaceName);
            final Object[] arguments = invocation.getArguments();
            if (arguments != null && arguments.length > 0 && arguments[0] instanceof String) {
                methodName = (String) invocation.getArguments()[0];
            }
        }

        // 高版本使用api invocation.getTargetServiceUniqueName获取路径，此处使用版本加接口，达到的最终结果一致
        String apiPath = ConvertUtils.buildApiPath(interfaceName, version, methodName);
        return new DubboRequestEntity(apiPath, Collections.unmodifiableMap(invocation.getAttachments()),
                RequestType.CLIENT, getRemoteApplication(url, interfaceName), isGeneric);
    }

    private String getRemoteApplication(URL url, String interfaceName) {
        return DubboApplicationCache.INSTANCE.getApplicationCache()
                .getOrDefault(interfaceName, url.getParameter(CommonConst.DUBBO_REMOTE_APPLICATION));
    }

    /**
     * apache dubbo重试
     *
     * @since 2022-02-21
     */
    public static class ApacheDubboRetry extends AbstractRetry {
        @Override
        public boolean needRetry(Set<String> statusList, Object result) {
            // dubbo不支持状态码
            return false;
        }

        @Override
        public Class<? extends Throwable>[] retryExceptions() {
            return getRetryExceptions();
        }

        @Override
        public RetryFramework retryType() {
            return RetryFramework.APACHE_DUBBO;
        }
    }
}
