/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.hercules.fallback;

import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 功能描述：feign失败回调
 *
 * @author z30009938
 * @since 2021-11-22
 */
public abstract class BaseFeignFallbackFactory<T> implements FallbackFactory<T> {
    /**
     * 日志工具
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFeignFallbackFactory.class);

    /**
     * feign调用失败，记录调用异常信息，不然只能看到feign失败的信息
     *
     * @param throwable 调用失败抛出的异常
     */
    protected T error(Throwable throwable) {
        if (throwable != null) {
            LOGGER.error("Http request fail by feign client, message:{}", throwable.getMessage(), throwable);
        }
        throw new RuntimeException("Http request fail by feign client.");
    }
}
