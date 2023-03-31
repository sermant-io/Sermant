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

package com.huawei.discovery.service.util;

import com.huawei.discovery.entity.Recorder;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.retry.Retry;
import com.huawei.discovery.retry.RetryException;
import com.huawei.discovery.service.lb.DiscoveryManager;
import com.huawei.discovery.service.lb.LbConstants;
import com.huawei.discovery.service.lb.stats.InstanceStats;
import com.huawei.discovery.service.lb.stats.ServiceStatsManager;
import com.huawei.discovery.service.retry.policy.PolicyContext;
import com.huawei.discovery.service.retry.policy.RetryPolicy;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * 增加异常信息采集
 *
 * @author zhp
 * @since 2023-03-08
 */
public class ApplyUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private ApplyUtil() {
    }

    /**
     * 服务调用
     *
     * @param invokeFunc 调用方法
     * @param invokerContext 上下文
     * @return 调用结果
     */
    public static Object apply(Function<InvokerContext, Object> invokeFunc, InvokerContext invokerContext) {
        return invokeFunc.apply(invokerContext);
    }

    /**
     * 服务调用
     *
     * @param invokeFunc 具体调用实现
     * @param serviceName 服务名称
     * @param retry 重试信息
     * @param invokerContext 服务调用上下文
     * @param retryPolicy 重试策略
     * @return 调用结果
     * @throws Exception 调用异常信息
     */
    public static Optional<Object> invokeWithEx(Function<InvokerContext, Object> invokeFunc, String serviceName,
             Retry retry, InvokerContext invokerContext, RetryPolicy retryPolicy) throws Exception {
        final Retry.RetryContext<Recorder> context = retry.context();
        final PolicyContext policyContext = new PolicyContext();
        boolean isInRetry = false;
        do {
            final long start = System.currentTimeMillis();
            policyContext.setServiceInstance(invokerContext.getServiceInstance());
            final Optional<ServiceInstance> instance = choose(serviceName, isInRetry, policyContext, retryPolicy);
            if (!instance.isPresent()) {
                LOGGER.warning("Can not find provider service named : " + serviceName);
                return Optional.empty();
            }
            invokerContext.setServiceInstance(instance.get());
            final InstanceStats stats = ServiceStatsManager.INSTANCE.getInstanceStats(instance.get());
            context.onBefore(stats);
            long consumeTimeMs;
            try {
                final Object result = ApplyUtil.apply(invokeFunc, invokerContext);
                consumeTimeMs = System.currentTimeMillis() - start;
                isInRetry = true;
                if (invokerContext.getEx() != null) {
                    // 此处调用器, 若调用出现异常, 则以异常结果返回
                    context.onError(stats, invokerContext.getEx(), consumeTimeMs);
                    invokerContext.setEx(null);
                    continue;
                }
                final boolean isNeedRetry = context.onResult(stats, result, consumeTimeMs);
                if (!isNeedRetry) {
                    context.onComplete(stats);
                    return Optional.ofNullable(result);
                }
            } catch (Exception ex) {
                handleEx(ex, context, stats, System.currentTimeMillis() - start);
            }
        } while (true);
    }

    /**
     * 异常处理
     *
     * @param ex 异常信息
     * @param context 上下文信息
     * @param stats 实例指标数据
     * @param consumeTimeMs 调用事件
     * @throws Exception 服务调用异常信息
     */
    private static void handleEx(Exception ex, Retry.RetryContext<Recorder> context, InstanceStats stats,
                                 long consumeTimeMs) throws Exception {
        if (ex instanceof RetryException) {
            throw ex;
        }
        context.onError(stats, ex, consumeTimeMs);
    }

    /**
     * 服务选择
     *
     * @param serviceName 服务名称
     * @param isInRetry 是否重试
     * @param policyContext 重试策略的上下文
     * @param retryPolicy 重试策略
     * @return 服务选择结果
     */
    private static Optional<ServiceInstance> choose(String serviceName, boolean isInRetry, PolicyContext policyContext,
                                                    RetryPolicy retryPolicy) {
        if (isInRetry) {
            final Optional<ServiceInstance> select = retryPolicy.select(serviceName, policyContext);
            select.ifPresent(instance -> LOGGER.info(String.format(Locale.ENGLISH,
                    "Start retry for invoking instance [id: %s] of service [%s] at time %s",
                    instance.getMetadata().get(LbConstants.SERMANT_DISCOVERY), serviceName, LocalDateTime.now())));
            return select;
        }
        return DiscoveryManager.INSTANCE.choose(serviceName);
    }
}