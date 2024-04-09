/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.discovery.entity;

/**
 * Status Records
 *
 * @author zhouss
 * @since 2022-09-28
 */
public interface Recorder {
    /**
     * Pre-call requests
     */
    void beforeRequest();

    /**
     * Statistics on abnormal calls
     *
     * @param ex The type of exception
     * @param consumeTimeMs The time consumed by the call
     */
    void errorRequest(Throwable ex, long consumeTimeMs);

    /**
     * Result call
     *
     * @param consumeTimeMs The time consumed by the call
     */
    void afterRequest(long consumeTimeMs);

    /**
     * End the request
     */
    void completeRequest();
}
