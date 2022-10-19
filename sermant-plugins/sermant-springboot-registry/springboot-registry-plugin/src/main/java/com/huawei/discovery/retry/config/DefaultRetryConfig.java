/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.retry.config;

import com.huawei.discovery.config.LbConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 重试默认配置, 会基于配置文件或者环境变量进行自定义
 *
 * @author zhouss
 * @since 2022-09-28
 */
public class DefaultRetryConfig implements RetryConfig {
    private static final String SOCKET_CONNECT_TIMEOUT = "connect timed out";

    private static final String SOCKET_READ_TIMEOUT = "read timed out";

    private static final String NAME = "SERMANT_DEFAULT_RETRY";

    private final List<Class<? extends Throwable>> retryEx = Arrays.asList(
            ConnectException.class,
            NoRouteToHostException.class,
            SocketTimeoutException.class,
            TimeoutException.class);

    private final List<String> rawRetryEx = Collections.singletonList(
            "org.apache.http.conn.ConnectTimeoutException");

    /**
     * 最大重试次数
     */
    private int maxRetry;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 异常判断
     */
    private Predicate<Throwable> throwablePredicate;

    /**
     * 结果判断
     */
    private Predicate<Object> resultPredicate;

    /**
     * 重试等待func
     */
    private Function<Integer, Long> retryWaitFunc;

    /**
     * 重试等待时间
     */
    private long retryRetryWaitMs;

    private final LbConfig lbConfig;

    DefaultRetryConfig() {
        this.lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
    }

    /**
     * 创建配置
     *
     * @return RetryConfig
     */
    public static RetryConfig create() {
        final DefaultRetryConfig defaultRetryConfig = new DefaultRetryConfig();
        defaultRetryConfig.maxRetry = defaultRetryConfig.lbConfig.getMaxRetry();
        defaultRetryConfig.retryRetryWaitMs = defaultRetryConfig.lbConfig.getRetryWaitMs();
        defaultRetryConfig.retryWaitFunc = count -> defaultRetryConfig.retryRetryWaitMs;
        defaultRetryConfig.name = NAME;
        defaultRetryConfig.resultPredicate = target -> false;
        defaultRetryConfig.throwablePredicate = defaultRetryConfig.buildThrowPredicate();
        return defaultRetryConfig;
    }

    /**
     * 构建异常判断器
     *
     * @return Predicate
     */
    private Predicate<Throwable> buildThrowPredicate() {
        final Predicate<Throwable> assignableEx = ex -> {
            if (ex == null) {
                return false;
            }
            if (isConfigDisable(ex) || isConfigDisable(ex.getCause())) {
                // 配置禁用场景不要重试
                return false;
            }
            for (Class<? extends Throwable> cur : retryEx) {
                if (cur.isAssignableFrom(ex.getClass())) {
                    return true;
                }
                if (ex.getCause() != null && cur.isAssignableFrom(ex.getCause().getClass())) {
                    return true;
                }
            }
            return false;
        };
        return assignableEx.or(buildRawThrowPredicate());
    }

    private Predicate<Throwable> buildRawThrowPredicate() {
        final List<String> specificExceptionsForRetry = this.lbConfig.getSpecificExceptionsForRetry();
        if (specificExceptionsForRetry.isEmpty()) {
            return buildRawThrowPredicate(rawRetryEx);
        }
        final List<String> newRawRetryEx = new ArrayList<>(rawRetryEx);
        newRawRetryEx.addAll(specificExceptionsForRetry);
        return buildRawThrowPredicate(newRawRetryEx);
    }

    private Predicate<Throwable> buildRawThrowPredicate(List<String> targetRawRetryEx) {
        return ex -> {
            if (ex == null) {
                return false;
            }
            return targetRawRetryEx.stream().anyMatch(clazz -> isAssignableFrom(clazz, ex.getClass()));
        };
    }

    /**
     * 是否为配置禁用重试场景
     *
     * @param ex 宿主异常
     * @return true: 已禁用 false: 可重试
     */
    private boolean isConfigDisable(Throwable ex) {
        if (ex instanceof SocketTimeoutException) {
            if (SOCKET_CONNECT_TIMEOUT.equals(ex.getMessage()) && !lbConfig.isEnableSocketConnectTimeoutRetry()) {
                // 连接超时
                return true;
            }
            if (SOCKET_READ_TIMEOUT.equals(ex.getMessage()) && !lbConfig.isEnableSocketReadTimeoutRetry()) {
                // 响应超时
                return true;
            }
        }
        if (ex instanceof TimeoutException) {
            return !lbConfig.isEnableTimeoutExRetry();
        }
        return false;
    }

    private static boolean isAssignableFrom(String clazz, Class<?> ex) {
        if (clazz.equals(ex.getName())) {
            return true;
        }
        final Class<?> superclass = ex.getSuperclass();
        if (superclass == null || superclass == Object.class) {
            return false;
        }
        if (isAssignableFrom(clazz, superclass)) {
            return true;
        }
        final Class<?>[] interfaces = ex.getInterfaces();
        for (Class<?> interfaceClazz : interfaces) {
            if (isAssignableFrom(clazz, interfaceClazz)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getRetryRetryWaitMs() {
        return retryRetryWaitMs;
    }

    @Override
    public Function<Integer, Long> getRetryWaitMs(int retryCount) {
        return this.retryWaitFunc;
    }

    @Override
    public int getMaxRetry() {
        return maxRetry;
    }

    @Override
    public Predicate<Throwable> getThrowablePredicate() {
        return throwablePredicate;
    }

    @Override
    public Predicate<Object> getResultPredicate() {
        return resultPredicate;
    }
}
