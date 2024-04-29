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

package io.sermant.discovery.retry.config;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.discovery.config.LbConfig;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Retry the default configuration, which will be customized based on the configuration file or environment variables
 *
 * @author zhouss
 * @since 2022-09-28
 */
public class DefaultRetryConfig implements RetryConfig {
    private static final String SOCKET_CONNECT_TIMEOUT = "connect timed out";

    private static final String SOCKET_READ_TIMEOUT = "Read timed out";

    private static final String NAME = "SERMANT_DEFAULT_RETRY";

    private final List<Class<? extends Throwable>> retryEx = Arrays.asList(
            ConnectException.class,
            NoRouteToHostException.class,
            SocketTimeoutException.class,
            TimeoutException.class);

    private final List<String> rawRetryEx = Arrays.asList(
            "org.apache.http.conn.ConnectTimeoutException",
            "org.springframework.web.reactive.function.client.WebClientRequestException");

    /**
     * Maximum number of retries
     */
    private int maxRetry;

    /**
     * Configuration name
     */
    private String name;

    /**
     * Abnormal judgment
     */
    private Predicate<Throwable> throwablePredicate;

    /**
     * Result judgment
     */
    private Predicate<Object> resultPredicate;

    /**
     * Retry waiting for func
     */
    private Function<Integer, Long> retryWaitFunc;

    /**
     * Retry wait time
     */
    private long retryRetryWaitMs;

    private final LbConfig lbConfig;

    DefaultRetryConfig() {
        this.lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
    }

    /**
     * Create a configuration
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
     * Build an exception judge
     *
     * @return Predicate
     */
    private Predicate<Throwable> buildThrowPredicate() {
        final Predicate<Throwable> assignableEx = ex -> {
            if (ex == null) {
                return false;
            }
            if (isConfigDisable(ex) || isConfigDisable(ex.getCause())) {
                // Do not try again if the scenario is disabled
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
     * Whether to disable the retry scenario for the configuration
     *
     * @param ex Host anomaly
     * @return true: Disabled false: Retryable
     */
    private boolean isConfigDisable(Throwable ex) {
        if (ex instanceof SocketTimeoutException) {
            if (SOCKET_CONNECT_TIMEOUT.equalsIgnoreCase(ex.getMessage())
                    && !lbConfig.isEnableSocketConnectTimeoutRetry()) {
                // The connection timed out
                return true;
            }
            if (SOCKET_READ_TIMEOUT.equalsIgnoreCase(ex.getMessage()) && !lbConfig.isEnableSocketReadTimeoutRetry()) {
                // Response timed out
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
