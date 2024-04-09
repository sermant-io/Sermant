/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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
 * Event Manager
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
     * Initialize, create scheduled tasks, and report events periodically
     */
    public static void init() {
        EventConfig eventConfig = ConfigManager.getConfig(EventConfig.class);
        if (!eventConfig.isEnable()) {
            LOGGER.info("Event is not enabled.");
            return;
        }

        // Create a thread for periodically collecting events
        executorService = Executors.newScheduledThreadPool(1, new ThreadFactoryUtils("event-collect-task"));

        // Initialize event sending
        EventSender.init();

        // Register the framework event collector
        EventManager.registerCollector(FrameworkEventCollector.getInstance());

        // Register the log event collector
        EventManager.registerCollector(LogEventCollector.getInstance());

        // enable periodic collection of reported events
        executorService.scheduleAtFixedRate(EventManager::collectAll, INITIAL_DELAY, eventConfig.getSendInterval(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * Report events in memory upon program termination
     */
    public static void shutdown() {
        // shutdown the thread for collecting events
        if (executorService != null) {
            executorService.shutdown();
        }

        // collect all events and report them
        collectAll();

        // Clear the registered event collector
        EVENT_COLLECTORS.clear();
    }

    /**
     * Register event collector
     *
     * @param eventCollector event collector
     * @return register result
     */
    public static boolean registerCollector(EventCollector eventCollector) {
        EVENT_COLLECTORS.put(eventCollector.getClass().getCanonicalName(), eventCollector);
        return true;
    }

    /**
     * Unregister event collector
     *
     * @param eventCollector event collector
     * @return unregister result
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
