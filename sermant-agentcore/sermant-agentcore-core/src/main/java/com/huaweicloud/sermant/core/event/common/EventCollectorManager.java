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

package com.huaweicloud.sermant.core.event.common;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理事件收集器
 *
 * @author luanwenfei
 * @since 2023-03-02
 */
public class EventCollectorManager {
    private final ConcurrentHashMap<String, EventCollector> eventCollectors = new ConcurrentHashMap<>();

    /**
     * 注册事件收集器
     *
     * @param eventCollector 事件收集器
     * @return 注册成功｜注册失败
     */
    public boolean registerCollector(EventCollector eventCollector) {
        eventCollectors.put(eventCollector.getClass().getCanonicalName(), eventCollector);
        return false;
    }

    /**
     * 取消注册收集器
     *
     * @param eventCollector 事件收集器
     * @return 取消注册成功｜取消注册失败
     */
    public boolean unRegisterCollector(EventCollector eventCollector) {
        eventCollectors.remove(eventCollector.getClass().getCanonicalName());
        return false;
    }

    private boolean collectAll() {
        for (EventCollector eventCollector : eventCollectors.values()) {
            eventCollector.collect();
        }
        return false;
    }

    private void sendEvent() {

    }
}
