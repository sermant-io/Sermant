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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 事件采集器
 *
 * @author luanwenfei
 * @since 2023-03-02
 */
public class EventCollector {
    // 有界阻塞队列 缓存事件，未满则定时上报，已满则主动上报
    private final BlockingQueue<Event> eventQueue = new ArrayBlockingQueue<>(10);

    /**
     * 用于事件采集管理器获取当前缓存事件
     */
    public final void collect() {

    }

    /**
     * 用于向事件采集器添加事件
     *
     * @param event 事件
     * @return 事件添加状态
     */
    public boolean offerEvent(Event event) {
        /**
         * todo 判断该收集器中的事件缓存是否已满，满了则主动上报后再添加
         */
        return false;
    }
}
