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

package com.huaweicloud.sermant.backend.dao.memory;

import com.huaweicloud.sermant.backend.common.conf.BackendConfig;
import com.huaweicloud.sermant.backend.common.conf.CommonConst;
import com.huaweicloud.sermant.backend.dao.EventDao;
import com.huaweicloud.sermant.backend.entity.InstanceMeta;
import com.huaweicloud.sermant.backend.entity.event.Event;
import com.huaweicloud.sermant.backend.entity.event.EventsRequestEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryCacheSizeEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryResultEventInfoEntity;
import com.huaweicloud.sermant.backend.util.DbUtils;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Memory client
 *
 * @author xuezechao
 * @since 2023-03-02
 */
@Component
public class MemoryClientImpl implements EventDao {
    private ExpiringMap<String, QueryResultEventInfoEntity> eventMap;

    private ExpiringMap<String, InstanceMeta> agentInstanceMap;

    private ExpiringMap<String, Long> eventTimeKeyMap;

    private ExpiringMap<String, List<String>> sessionMap;

    private ExpiringMap<String, String> emergency;

    private ExpiringMap<String, String> important;

    private ExpiringMap<String, String> normal;

    /**
     * Constructor
     *
     * @param backendConfig configuration
     */
    public MemoryClientImpl(BackendConfig backendConfig) {
        this.eventMap = ExpiringMap.builder().expiration(
                        backendConfig.getEventExpire(), TimeUnit.DAYS)
                .expirationPolicy(ExpirationPolicy.CREATED).build();
        this.agentInstanceMap = ExpiringMap.builder()
                .expiration(backendConfig.getEventExpire(), TimeUnit.DAYS)
                .expirationPolicy(ExpirationPolicy.CREATED).build();
        this.eventTimeKeyMap = ExpiringMap.builder()
                .expiration(backendConfig.getEventExpire(), TimeUnit.DAYS)
                .expirationPolicy(ExpirationPolicy.CREATED).build();
        this.sessionMap = ExpiringMap.builder()
                .expiration(backendConfig.getSessionTimeout(), TimeUnit.SECONDS)
                .expirationPolicy(ExpirationPolicy.CREATED).build();
        this.emergency = ExpiringMap.builder()
                .expiration(backendConfig.getEventExpire(), TimeUnit.DAYS)
                .expirationPolicy(ExpirationPolicy.CREATED).build();
        this.important = ExpiringMap.builder()
                .expiration(backendConfig.getEventExpire(), TimeUnit.DAYS)
                .expirationPolicy(ExpirationPolicy.CREATED).build();
        this.normal = ExpiringMap.builder()
                .expiration(backendConfig.getEventExpire(), TimeUnit.DAYS)
                .expirationPolicy(ExpirationPolicy.CREATED).build();
    }

    @Override
    public boolean addEvent(Event event) {
        if (!agentInstanceMap.containsKey(event.getMetaHash())) {
            return false;
        }

        InstanceMeta instanceMeta = agentInstanceMap.get(event.getMetaHash());
        String field = DbUtils.getEventField(instanceMeta, event);
        field = field + CommonConst.JOIN_REDIS_KEY + getSameFieldNum(field);

        eventMap.put(field, DbUtils.aggregationEvent(event, instanceMeta));
        eventTimeKeyMap.put(field, event.getTime());
        switch (event.getEventLevel()) {
            case IMPORTANT:
                important.put(field, "");
                break;
            case EMERGENCY:
                emergency.put(field, "");
                break;
            case NORMAL:
                normal.put(field, "");
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean addInstanceMeta(InstanceMeta agentInstanceMeta) {
        if (!agentInstanceMap.containsKey(agentInstanceMeta.getMetaHash())) {
            agentInstanceMap.put(agentInstanceMeta.getMetaHash(), agentInstanceMeta);
        }
        return true;
    }

    @Override
    public List<QueryResultEventInfoEntity> queryEvent(EventsRequestEntity eventsRequestEntity) {
        String pattern = DbUtils.getPattern(eventsRequestEntity);
        List<String> queryResultKey = getQueryEventKey(
                eventsRequestEntity.getStartTime(), eventsRequestEntity.getEndTime(), pattern);
        Collections.reverse(queryResultKey);
        sessionMap.put(eventsRequestEntity.getSessionId(), queryResultKey);
        return queryEventPage(eventsRequestEntity.getSessionId(), 1);
    }

    @Override
    public List<QueryResultEventInfoEntity> queryEventPage(String sessionId, int page) {
        List<QueryResultEventInfoEntity> result = new ArrayList<>();
        List<String> keyList = sessionMap.get(sessionId);
        int startIndex = (page - 1) * CommonConst.DEFAULT_PAGE_SIZE;
        int endIndex = Math.min(startIndex + CommonConst.DEFAULT_PAGE_SIZE, keyList.size());
        for (String key : keyList.subList(startIndex, endIndex)) {
            result.add(eventMap.get(key));
        }
        return result;
    }

    @Override
    public QueryCacheSizeEntity getQueryCacheSize(EventsRequestEntity eventsRequestEntity) {
        List<String> keyList = sessionMap.get(eventsRequestEntity.getSessionId());
        return DbUtils.getQueryCacheSize(keyList);
    }

    @Override
    public QueryResultEventInfoEntity getDoNotifyEvent(Event event) {
        InstanceMeta instanceMeta = agentInstanceMap.get(event.getMetaHash());
        return DbUtils.aggregationEvent(event, instanceMeta);
    }

    /**
     * Get the key list of the queried event
     *
     * @param startTime start time
     * @param endTime end time
     * @param pattern matching pattern
     * @return event key list
     */
    public List<String> getQueryEventKey(long startTime, long endTime, String pattern) {
        List<Map.Entry<String, Long>> queryResultByTime = eventTimeKeyMap.entrySet().stream().filter(
                        ent -> ent.getValue() >= startTime
                                && ent.getValue() <= endTime
                                && ent.getKey().matches(pattern))
                .collect(Collectors.toList());
        return queryResultByTime.stream().sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    /**
     * Get the number of same fields
     *
     * @param field field
     * @return number of same fields
     */
    private int getSameFieldNum(String field) {
        int result = 0;
        for (String key : eventMap.keySet()) {
            if (key.startsWith(field)) {
                result += 1;
            }
        }
        return result;
    }
}
