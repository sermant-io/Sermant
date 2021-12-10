/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.javamesh.core.lubanops.bootstrap.trace;

/**
 * 描述可以对 {@link SpanEvent} 进行过滤的过滤器
 */
public interface SpanEventFilter {
    /**
     * 对该 {@link SpanEvent} 进行过滤
     *
     * @param spanEvent {@link SpanEvent} 对象
     * @return true：数据保留；false：数据丢弃
     */
    boolean doFilter(SpanEvent spanEvent);
}
