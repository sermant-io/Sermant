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

import com.huawei.flowcontrol.common.entity.DubboRequestEntity;
import com.huawei.flowcontrol.common.exception.InvokerException;
import com.huawei.flowcontrol.common.handler.retry.AbstractRetry;
import com.huawei.flowcontrol.common.handler.retry.Retry;
import com.huawei.flowcontrol.common.handler.retry.RetryContext;
import com.huawei.flowcontrol.common.util.ConvertUtils;
import com.huawei.flowcontrol.retry.handler.RetryHandlerV2;
import com.huawei.sermant.core.common.LoggerFactory;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.vavr.CheckedFunction0;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

    public ApacheDubboClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    protected Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance)
        throws RpcException {

        RetryContext.INSTANCE.markRetry(retry);
        final List<io.github.resilience4j.retry.Retry> handlers = retryHandler
            .getHandlers(convertToApacheDubboEntity(invocation, invokers.get(0)));
        checkInvokers(invokers, invocation);
        CheckedFunction0<Result> supplier = () -> {
            checkInvokers(invokers, invocation);
            Invoker<T> invoker = select(loadbalance, invocation, invokers, null);
            Result result = invoker.invoke(invocation);
            if (result.hasException()) {
                throw result.getException();
            }
            return result;
        };
        DecorateCheckedSupplier<Result> dcs = Decorators.ofCheckedSupplier(supplier);
        io.github.resilience4j.retry.Retry retryRule = null;
        if (!handlers.isEmpty()) {
            // 重试仅支持一种策略
            retryRule = handlers.get(0);
            dcs.withRetry(retryRule);
        }
        try {
            return dcs.get();
        } catch (Throwable ex) {
            if (retryRule != null) {
                LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH,
                    "Retry %d times failed for interface %s.%s", retryRule.getRetryConfig().getMaxAttempts() - 1,
                    invocation.getInvoker().getInterface().getName(), invocation.getMethodName()));
            }
            throw new InvokerException(ex);
        } finally {
            RetryContext.INSTANCE.removeRetry();
        }
    }

    /**
     * 转换apache dubbo 注意，该方法不可抽出，由于宿主依赖仅可由该拦截器加载，因此抽出会导致找不到类
     *
     * @param invocation 调用信息
     * @return DubboRequestEntity
     */
    private DubboRequestEntity convertToApacheDubboEntity(Invocation invocation, Invoker<T> invoker) {
        // 高版本使用api invocation.getTargetServiceUniqueName获取路径，此处使用版本加接口，达到的最终结果一致
        String apiPath = ConvertUtils.buildApiPath(invoker.getInterface().getName(),
            invocation.getAttachment(ConvertUtils.DUBBO_ATTACHMENT_VERSION), invocation.getMethodName());
        return new DubboRequestEntity(apiPath, Collections.unmodifiableMap(invocation.getAttachments()));
    }

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
