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

/**
 * 重试上下文，用于管理重试策略
 * 基于不同的宿主框架类型
 *
 * @author zhouss
 * @since 2022-01-26
 */
public enum RetryContext {
    /**
     * 单例
     */
    INSTANCE;

    private final ThreadLocal<Retry> retryThreadLocal = new ThreadLocal<>();

    public Retry getRetry() {
        return retryThreadLocal.get();
    }

    public void setRetry(Retry retry) {
        retryThreadLocal.set(retry);
    }

    public void removeRetry() {
        retryThreadLocal.remove();
    }

    public boolean isReady() {
        return retryThreadLocal.get() != null;
    }
}
