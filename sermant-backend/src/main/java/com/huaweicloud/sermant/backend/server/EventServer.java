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

package com.huaweicloud.sermant.backend.server;

import com.huaweicloud.sermant.backend.common.conf.BackendConfig;
import com.huaweicloud.sermant.backend.dao.DatabaseType;
import com.huaweicloud.sermant.backend.dao.EventDao;
import com.huaweicloud.sermant.backend.dao.memory.MemoryClientImpl;
import com.huaweicloud.sermant.backend.dao.redis.EventDaoForRedis;
import com.huaweicloud.sermant.backend.entity.InstanceMeta;
import com.huaweicloud.sermant.backend.entity.event.Event;
import com.huaweicloud.sermant.backend.entity.event.EventsRequestEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryCacheSizeEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryResultEventInfoEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

/**
 * Event server
 *
 * @author xuezechao
 * @since 2023-03-02
 */
@Component
public class EventServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventServer.class);

    private static EventServer eventServer;

    @Autowired
    private BackendConfig backendConfig;

    private EventDao daoService;

    private EventServer() {
    }

    /**
     * Initialize the server
     */
    @PostConstruct
    public void init() {
        eventServer = this;
        if (Objects.requireNonNull(backendConfig.getDatabase()) == DatabaseType.REDIS) {
            eventServer.daoService = new EventDaoForRedis(backendConfig);
        } else {
            eventServer.daoService = new MemoryClientImpl(backendConfig);
        }
    }

    /**
     * Add event
     *
     * @param event event
     * @return add result
     */
    public boolean addEvent(Event event) {
        return daoService.addEvent(event);
    }

    /**
     * Add agent Instance
     *
     * @param instanceMeta agent instance
     * @return add result
     */
    public boolean addEvent(InstanceMeta instanceMeta) {
        return daoService.addInstanceMeta(instanceMeta);
    }

    /**
     * Query event
     *
     * @param eventsRequestEntity Query condition
     * @return Query result
     */
    public List<QueryResultEventInfoEntity> queryEvent(EventsRequestEntity eventsRequestEntity) {
        return daoService.queryEvent(eventsRequestEntity);
    }

    /**
     * Query a page of event
     *
     * @param sessionId Session id
     * @param page Page number
     * @return Query result
     */
    public List<QueryResultEventInfoEntity> queryEventPage(String sessionId, int page) {
        return daoService.queryEventPage(sessionId, page);
    }

    /**
     * Get the size of the query result
     *
     * @param eventsRequestEntity Query condition
     * @return Get number of query result and number of different types of events
     */
    public QueryCacheSizeEntity getQueryCacheSize(EventsRequestEntity eventsRequestEntity) {
        return daoService.getQueryCacheSize(eventsRequestEntity);
    }

    /**
     * Get webhook notification event
     *
     * @param event event
     * @return webhook notification event
     */
    public QueryResultEventInfoEntity getDoNotifyEvent(Event event) {
        return daoService.getDoNotifyEvent(event);
    }
}
