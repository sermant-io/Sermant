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

package com.huawei.fowcontrol.res4j.handler;

import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.common.handler.listener.HandlerRequestListener;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 处理器包装类
 *
 * @author zhouss
 * @since 2022-01-22
 */
public enum HandlerFacade {
    /**
     * 单例
     */
    INSTANCE;

    private final RateLimitingHandler rateLimitingHandler = new RateLimitingHandler();

    private final BulkheadHandler bulkheadHandler = new BulkheadHandler();

    private final CircuitBreakerHandler circuitBreakerHandler = new CircuitBreakerHandler();

    private final Processor httpProcessor = new Processor();

    private final Processor dubboProviderProcessor = new Processor();

    private final Processor dubboConsumerProcessor = new Processor();

    private final Map<RequestEntity, HandlerWrapper> requestWrapperCache = new ConcurrentHashMap<>();

    HandlerFacade() {
        registerListener();
    }

    /**
     * 注册管理请求体匹配的业务场景更新 确保在流控规则更新后及时更新缓存，确保最新规则生效
     */
    private void registerListener() {
        final FacadeRequestListener facadeRequestListener = new FacadeRequestListener();
        rateLimitingHandler.registerListener(facadeRequestListener);
        bulkheadHandler.registerListener(facadeRequestListener);
        circuitBreakerHandler.registerListener(facadeRequestListener);
    }

    public void injectHandlers(RequestEntity entity, boolean isProvider) {
        if (isProvider) {
            dubboProviderProcessor.injectHandlers(entity);
        } else {
            dubboConsumerProcessor.injectHandlers(entity);
        }
    }

    public void injectHandlers(RequestEntity entity) {
        httpProcessor.injectHandlers(entity);
    }

    public void removeHandlers(boolean isProvider) {
        if (isProvider) {
            dubboProviderProcessor.removeHandlers();
        } else {
            dubboConsumerProcessor.removeHandlers();

            // 消费者移除后，再次判断生产者是否移除干净, 确保线程变量清空
            if (dubboProviderProcessor.handlerThreadLocal.get() != null) {
                dubboProviderProcessor.removeHandlers();
            }
        }
    }

    public void removeHandlers() {
        httpProcessor.removeHandlers();
    }

    public void onDubboThrow(Throwable throwable) {
        dubboProviderProcessor.onThrow(throwable);
        dubboConsumerProcessor.onThrow(throwable);
    }

    public void onDubboResult(Object result) {
        dubboProviderProcessor.onResult(result);
        dubboConsumerProcessor.onResult(result);
    }

    public void releaseDubboPermit() {
        dubboProviderProcessor.releasePermit();
        dubboConsumerProcessor.releasePermit();
    }

    public void releasePermit() {
        httpProcessor.releasePermit();
    }

    public void onThrow(Throwable throwable) {
        httpProcessor.onThrow(throwable);
    }

    public void onResult(Object result) {
        httpProcessor.onResult(result);
    }

    class Processor {
        private final ThreadLocal<HandlerWrapper> handlerThreadLocal = new ThreadLocal<>();

        public void injectHandlers(RequestEntity entity) {
            // 判断缓存是否存在该请求的wrapper, 再去实际匹配创建
            final HandlerWrapper handlerWrapper = requestWrapperCache.computeIfAbsent(entity, fn -> {
                injectRateLimitingHandlers(entity);
                injectBulkheadHandlers(entity);
                injectCircuitBreakerHandlers(entity);
                return handlerThreadLocal.get();
            });
            handlerThreadLocal.set(handlerWrapper);
            handlerWrapper.tryAcquirePermission();
        }

        private void injectRateLimitingHandlers(RequestEntity entity) {
            HandlerWrapper handlerWrapper = handlerThreadLocal.get();
            if (handlerWrapper == null) {
                final List<RateLimiter> handlers = rateLimitingHandler.getHandlers(entity);
                handlerWrapper = new HandlerWrapper(handlers);
                handlerThreadLocal.set(handlerWrapper);
            }
        }

        private void injectBulkheadHandlers(RequestEntity entity) {
            HandlerWrapper handlerWrapper = handlerThreadLocal.get();
            handlerWrapper.bulkheads = bulkheadHandler.getHandlers(entity);
        }

        private void injectCircuitBreakerHandlers(RequestEntity entity) {
            HandlerWrapper handlerWrapper = handlerThreadLocal.get();
            handlerWrapper.circuitBreakers = circuitBreakerHandler.getHandlers(entity);
        }

        public void removeHandlers() {
            handlerThreadLocal.remove();
        }

        public void releasePermit() {
            final HandlerWrapper handlerWrapper = handlerThreadLocal.get();
            if (handlerWrapper == null) {
                return;
            }
            handlerWrapper.releasePermit();
        }

        public void onThrow(Throwable throwable) {
            final HandlerWrapper handlerWrapper = handlerThreadLocal.get();
            if (handlerWrapper == null) {
                return;
            }
            handlerWrapper.onThrow(throwable);
        }

        public void onResult(Object result) {
            final HandlerWrapper handlerWrapper = handlerThreadLocal.get();
            if (handlerWrapper == null) {
                return;
            }
            handlerWrapper.onResult(result);
        }
    }

    static class HandlerWrapper {
        private final List<RateLimiter> rateLimiters;

        private List<Bulkhead> bulkheads;

        private List<CircuitBreaker> circuitBreakers;

        /**
         * 请求开始时间 仅熔断请求使用， 由于该对象针对每一个线程仅当有一个，因此针对同一个线程无需对该时间做特殊处理
         */
        private long startTime;

        private HandlerWrapper(List<RateLimiter> rateLimiters) {
            this.rateLimiters = rateLimiters;
        }

        private void tryAcquirePermission() {
            if (rateLimiters != null) {
                rateLimiters.forEach(rateLimiter -> RateLimiter.waitForPermission(rateLimiter, 1));
            }
            if (bulkheads != null) {
                bulkheads.forEach(Bulkhead::acquirePermission);
            }
            if (circuitBreakers != null && !circuitBreakers.isEmpty()) {
                circuitBreakers.forEach(CircuitBreaker::acquirePermission);

                // 这里使用内置方法获取时间, 列表中的每个熔断器时间均一致，因此取第一个
                startTime = circuitBreakers.get(0).getCurrentTimestamp();
            }
        }

        private void releasePermit() {
            if (bulkheads != null) {
                bulkheads.forEach(Bulkhead::onComplete);
            }
        }

        private void onThrow(Throwable throwable) {
            if (rateLimiters != null) {
                rateLimiters.forEach(rateLimiter -> rateLimiter.onError(throwable));
            }
            if (circuitBreakers != null && !circuitBreakers.isEmpty()) {
                long duration = circuitBreakers.get(0).getCurrentTimestamp() - startTime;
                final TimeUnit timestampUnit = circuitBreakers.get(0).getTimestampUnit();
                circuitBreakers.forEach(circuitBreaker -> circuitBreaker.onError(duration, timestampUnit, throwable));
            }
        }

        private void onResult(Object result) {
            if (rateLimiters != null) {
                rateLimiters.forEach(rateLimiter -> rateLimiter.onResult(result));
            }
            if (bulkheads != null) {
                bulkheads.forEach(Bulkhead::onComplete);
            }
            if (circuitBreakers != null && !circuitBreakers.isEmpty()) {
                long duration = circuitBreakers.get(0).getCurrentTimestamp() - startTime;
                final TimeUnit timestampUnit = circuitBreakers.get(0).getTimestampUnit();
                circuitBreakers.forEach(circuitBreaker -> circuitBreaker.onResult(duration, timestampUnit, result));
            }
        }
    }

    class FacadeRequestListener implements HandlerRequestListener {
        @Override
        public void notify(RequestEntity entity, String updateKey) {
            requestWrapperCache.remove(entity);
        }
    }
}
