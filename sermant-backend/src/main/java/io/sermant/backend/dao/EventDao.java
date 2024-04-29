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

package io.sermant.backend.dao;

import io.sermant.backend.entity.InstanceMeta;
import io.sermant.backend.entity.event.Event;
import io.sermant.backend.entity.event.EventsRequestEntity;
import io.sermant.backend.entity.event.QueryCacheSizeEntity;
import io.sermant.backend.entity.event.QueryResultEventInfoEntity;

import java.util.List;

/**
 * Database interface
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public interface EventDao {
    /**
     * Add event
     *
     * @param event event
     * @return true/false
     */
    boolean addEvent(Event event);

    /**
     * Add agent instance
     *
     * @param agentInstanceMeta agent instance
     * @return true/false
     */
    boolean addInstanceMeta(InstanceMeta agentInstanceMeta);

    /**
     * Query event
     *
     * @param eventsRequestEntity Query condition
     * @return Query result
     */
    List<QueryResultEventInfoEntity> queryEvent(EventsRequestEntity eventsRequestEntity);

    /**
     * Query data with specific page
     *
     * @param sessionId session id
     * @param page page number
     * @return query result
     */
    List<QueryResultEventInfoEntity> queryEventPage(String sessionId, int page);

    /**
     * Get the cache size in the query result
     *
     * @param eventsRequestEntity Event request entity
     * @return the cache size in query result
     */
    QueryCacheSizeEntity getQueryCacheSize(EventsRequestEntity eventsRequestEntity);

    /**
     * Get webhook notify event
     *
     * @param event event
     * @return Event information entity
     */
    QueryResultEventInfoEntity getDoNotifyEvent(Event event);
}
