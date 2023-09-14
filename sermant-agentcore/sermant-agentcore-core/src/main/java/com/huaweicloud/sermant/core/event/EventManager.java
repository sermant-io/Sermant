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

package com.huaweicloud.sermant.core.event;

import com.huaweicloud.sermant.core.common.BootArgsIndexer;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.event.collector.FrameworkEventCollector;
import com.huaweicloud.sermant.core.event.collector.LogEventCollector;
import com.huaweicloud.sermant.core.event.config.EventConfig;
import com.huaweicloud.sermant.core.utils.ThreadFactoryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 管理事件收集器
 *
 * @author luanwenfei
 * @since 2023-03-02
 */
public class EventManager {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final ConcurrentHashMap<String, EventCollector> EVENT_COLLECTORS = new ConcurrentHashMap<>();

    private static ScheduledExecutorService executorService;

    private static final long INITIAL_DELAY = 30000L;

    private EventManager() {
    }

    /**
     * 初始化，创建定时任务，定时上报事件
     */
    public static void init() {
        EventConfig eventConfig = ConfigManager.getConfig(EventConfig.class);
        if (!eventConfig.isEnable()) {
            LOGGER.info("Event is not enabled.");
            return;
        }

        // 创建定时采集事件线程
        executorService = Executors.newScheduledThreadPool(1, new ThreadFactoryUtils("event-collect-task"));

        // 初始化事件发送
        EventSender.init();

        // 注册框架事件收集器
        EventManager.registerCollector(FrameworkEventCollector.getInstance());

        // 注册日志事件收集器
        EventManager.registerCollector(LogEventCollector.getInstance());

        // 开启定时采集上报事件消息
        executorService.scheduleAtFixedRate(EventManager::collectAll, INITIAL_DELAY, eventConfig.getSendInterval(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * 在程序终止时上报在内存中的事件
     */
    public static void shutdown() {
        // 关闭定时采集事件线程
        if (executorService != null) {
            executorService.shutdown();
        }

        // 采集全部事件并上报
        collectAll();

        // 清空注册的事件收集器
        EVENT_COLLECTORS.clear();
    }

    /**
     * 注册事件收集器
     *
     * @param eventCollector 事件收集器
     * @return 注册成功｜注册失败
     */
    public static boolean registerCollector(EventCollector eventCollector) {
        EVENT_COLLECTORS.put(eventCollector.getClass().getCanonicalName(), eventCollector);
        return true;
    }

    /**
     * 取消注册收集器
     *
     * @param eventCollector 事件收集器
     * @return 取消注册成功｜取消注册失败
     */
    public static boolean unRegisterCollector(EventCollector eventCollector) {
        if (EVENT_COLLECTORS.remove(eventCollector.getClass().getCanonicalName()) == null) {
            LOGGER.warning("Collector is not in collectors map, name: " + eventCollector.getClass().getCanonicalName());
            return false;
        }
        return true;
    }

    private static void collectAll() {
        List<Event> events = new ArrayList<>();
        for (EventCollector eventCollector : EVENT_COLLECTORS.values()) {
            eventCollector.collect().drainTo(events);
        }
        if (events.isEmpty()) {
            LOGGER.info("No event needs to be reported.");
            return;
        }
        EventSender.sendEvent(new EventMessage(BootArgsIndexer.getInstanceId(), events));
    }
}
