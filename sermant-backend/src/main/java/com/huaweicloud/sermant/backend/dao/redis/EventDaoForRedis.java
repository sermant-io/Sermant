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

package com.huaweicloud.sermant.backend.dao.redis;

import com.huaweicloud.sermant.backend.common.conf.BackendConfig;
import com.huaweicloud.sermant.backend.dao.EventDao;
import com.huaweicloud.sermant.backend.entity.InstanceMeta;
import com.huaweicloud.sermant.backend.entity.event.Event;
import com.huaweicloud.sermant.backend.entity.event.EventsRequestEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryCacheSizeEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryResultEventInfoEntity;

import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Redis database for event
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public class EventDaoForRedis implements EventDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventDaoForRedis.class);

    private EventDao jedis;

    /**
     * Constructor
     *
     * @param backendConfig configuration
     */
    public EventDaoForRedis(BackendConfig backendConfig) {
        try {
            jedis = new RedisClientImpl(backendConfig);
        } catch (JedisConnectionException | JedisDataException e) {
            LOGGER.error("connect redis failed, error message: {}", e.getMessage());
        }
    }

    /**
     * Add event
     *
     * @param event event
     * @return true/false
     */
    @Override
    public boolean addEvent(Event event) {
        return jedis.addEvent(event);
    }

    /**
     * Add agent instance
     *
     * @param instanceMeta agent metadata
     * @return true/false
     */
    @Override
    public boolean addInstanceMeta(InstanceMeta instanceMeta) {
        return jedis.addInstanceMeta(instanceMeta);
    }

    /**
     * Event query
     *
     * @param eventsRequestEntity query condition
     * @return query result
     */
    @Override
    public List<QueryResultEventInfoEntity> queryEvent(EventsRequestEntity eventsRequestEntity) {
        return jedis.queryEvent(eventsRequestEntity);
    }

    /**
     * Query data with specific page
     *
     * @param sessionId session id
     * @param page page number
     * @return query result
     */
    @Override
    public List<QueryResultEventInfoEntity> queryEventPage(String sessionId, int page) {
        return jedis.queryEventPage(sessionId, page);
    }

    /**
     * Query the number of data types
     *
     * @param eventsRequestEntity Event request entity
     * @return the cache size in query result
     */
    @Override
    public QueryCacheSizeEntity getQueryCacheSize(EventsRequestEntity eventsRequestEntity) {
        return jedis.getQueryCacheSize(eventsRequestEntity);
    }

    @Override
    public QueryResultEventInfoEntity getDoNotifyEvent(Event event) {
        return jedis.getDoNotifyEvent(event);
    }
}
