/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.fowcontrol.res4j.chain;

import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.fowcontrol.res4j.chain.context.ChainContext;
import com.huawei.fowcontrol.res4j.chain.context.RequestContext;
import com.huawei.fowcontrol.res4j.util.FlowControlExceptionUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 请求链入口类
 *
 * @author zhouss
 * @since 2022-07-11
 */
public enum HandlerChainEntry {
    /**
     * 单例
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 处理链
     */
    private final HandlerChain chain = HandlerChainBuilder.INSTANCE.build();

    /**
     * 前置方法
     *
     * @param sourceName 发起源
     * @param requestEntity 请求体
     * @param flowControlResult 流控结果
     */
    public void onBefore(String sourceName, RequestEntity requestEntity, FlowControlResult flowControlResult) {
        try {
            final RequestContext threadLocalContext = ChainContext.getThreadLocalContext(sourceName);
            threadLocalContext.setRequestEntity(requestEntity);
            chain.onBefore(threadLocalContext, null);
        } catch (Exception ex) {
            flowControlResult.setRequestType(requestEntity.getRequestType());
            FlowControlExceptionUtils.handleException(ex, flowControlResult);
            ChainContext.getThreadLocalContext(sourceName).save(HandlerConstants.OCCURRED_FLOW_EXCEPTION, ex);
            LOGGER.log(Level.FINE, ex, ex::getMessage);
        }
    }

    /**
     * dubbo前置方法, 此处区分生产端与消费端
     *
     * @param sourceName 发起源
     * @param requestEntity 请求体
     * @param flowControlResult 流控结果
     * @param isProvider 是否为生产端
     */
    public void onDubboBefore(String sourceName, RequestEntity requestEntity, FlowControlResult flowControlResult,
            boolean isProvider) {
        String formatSourceName = formatSourceName(sourceName, isProvider);
        configPrefix(formatSourceName, isProvider);
        onBefore(formatSourceName, requestEntity, flowControlResult);
    }

    private String formatSourceName(String sourceName, boolean isProvider) {
        return String.format(Locale.ENGLISH, "%s%s",
                isProvider ? HandlerConstants.THREAD_LOCAL_DUBBO_PROVIDER_PREFIX
                        : HandlerConstants.THREAD_LOCAL_DUBBO_CONSUMER_PREFIX, sourceName);
    }

    /**
     * 后置方法
     *
     * @param sourceName 发起源
     * @param result 执行结果
     */
    public void onResult(String sourceName, Object result) {
        try {
            chain.onResult(ChainContext.getThreadLocalContext(sourceName), null, result);
        } finally {
            ChainContext.remove(sourceName);
        }
    }

    private void configPrefix(String sourceName, boolean isProvider) {
        if (isProvider) {
            ChainContext.setKeyPrefix(sourceName, HandlerConstants.THREAD_LOCAL_DUBBO_PROVIDER_PREFIX);
        } else {
            ChainContext.setKeyPrefix(sourceName, HandlerConstants.THREAD_LOCAL_DUBBO_CONSUMER_PREFIX);
        }
    }

    /**
     * 后置方法
     *
     * @param sourceName 发起源
     * @param result 执行结果
     * @param isProvider 是否为生产端
     */
    public void onDubboResult(String sourceName, Object result, boolean isProvider) {
        final String formatSourceName = formatSourceName(sourceName, isProvider);
        configPrefix(formatSourceName, isProvider);
        onResult(formatSourceName, result);
    }

    /**
     * 异常方法
     *
     * @param sourceName 发起源
     * @param throwable 异常信息
     */
    public void onThrow(String sourceName, Throwable throwable) {
        final RequestContext context = ChainContext.getThreadLocalContext(sourceName);
        context.save(HandlerConstants.OCCURRED_REQUEST_EXCEPTION, throwable);
        chain.onThrow(context, null, throwable);
    }

    /**
     * 异常方法
     *
     * @param sourceName 发起源
     * @param throwable 异常信息
     * @param isProvider 是否为生产端
     */
    public void onDubboThrow(String sourceName, Throwable throwable, boolean isProvider) {
        final String formatSourceName = formatSourceName(sourceName, isProvider);
        configPrefix(formatSourceName, isProvider);
        onThrow(formatSourceName, throwable);
    }
}
