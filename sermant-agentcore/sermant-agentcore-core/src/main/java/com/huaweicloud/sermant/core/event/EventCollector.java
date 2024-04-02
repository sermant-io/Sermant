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
import com.huaweicloud.sermant.core.event.config.EventConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Event Collector
 *
 * @author luanwenfei
 * @since 2023-03-02
 */
public class EventCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    // BlockingQueue for event cache. If the queue is not full, event is reported periodically. If
    // the queue is full, event is reported automatically
    private final BlockingQueue<Event> eventQueue = new ArrayBlockingQueue<>(100);

    private final EventConfig eventConfig = ConfigManager.getConfig(EventConfig.class);

    private final ConcurrentHashMap<EventInfo, Long> eventInfoOfferTimeCache = new ConcurrentHashMap<>();

    /**
     * constructor
     */
    protected EventCollector() {
    }

    /**
     * It is used by the event collection manager to obtain current cache events and clear the cache of logs and
     * event reporting events during collection
     *
     * @return cache queue
     */
    public final BlockingQueue<Event> collect() {
        cleanOfferTimeCacheMap();
        return eventQueue;
    }

    /**
     * It is used to add events to the event collector. If the collector is full, it sends events
     *
     * @param event event
     * @return result
     */
    public boolean offerEvent(Event event) {
        if (!eventConfig.isEnable()) {
            return false;
        }
        if (event.getEventInfo() != null) {
            if (checkEventInfoOfferInterval(event.getEventInfo())) {
                eventInfoOfferTimeCache.put(event.getEventInfo(), System.currentTimeMillis());
            } else {
                return false;
            }
        }
        return doOffer(event);
    }

    private boolean doOffer(Event event) {
        if (eventQueue.offer(event)) {
            return true;
        }
        LOGGER.info("Event queue is full when offer new event, send events initiative.");
        sendEventInitiative();
        return eventQueue.offer(event);
    }

    private void sendEventInitiative() {
        if (eventQueue.isEmpty()) {
            LOGGER.severe("Event queue is empty when send events initiative.");
            return;
        }
        List<Event> events = new ArrayList<>();
        eventQueue.drainTo(events);
        EventSender.sendEvent(new EventMessage(BootArgsIndexer.getInstanceId(), events));
    }

    /**
     * Check whether the event can be reported again based on the reporting interval
     *
     * @param eventInfo event information
     * @return boolean result
     */
    private boolean checkEventInfoOfferInterval(EventInfo eventInfo) {
        Long lastOfferTime = eventInfoOfferTimeCache.get(eventInfo);
        if (lastOfferTime == null) {
            return true;
        }
        return System.currentTimeMillis() - lastOfferTime > eventConfig.getOfferInterval();
    }

    /**
     * Periodically clear the event reporting time cache
     */
    protected void cleanOfferTimeCacheMap() {
        long currentTime = System.currentTimeMillis();
        for (EventInfo eventInfo : eventInfoOfferTimeCache.keySet()) {
            if (currentTime - eventInfoOfferTimeCache.get(eventInfo) > eventConfig.getOfferInterval()) {
                eventInfoOfferTimeCache.remove(eventInfo);
            }
        }
    }
}
