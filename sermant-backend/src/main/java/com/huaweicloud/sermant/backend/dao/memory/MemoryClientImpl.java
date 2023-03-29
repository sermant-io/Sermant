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

package com.huaweicloud.sermant.backend.dao.memory;

import com.huaweicloud.sermant.backend.common.conf.BackendConfig;
import com.huaweicloud.sermant.backend.common.conf.CommonConst;
import com.huaweicloud.sermant.backend.dao.DatabaseType;
import com.huaweicloud.sermant.backend.dao.EventDao;
import com.huaweicloud.sermant.backend.entity.InstanceMeta;
import com.huaweicloud.sermant.backend.entity.event.Event;
import com.huaweicloud.sermant.backend.entity.event.EventLevel;
import com.huaweicloud.sermant.backend.entity.event.EventsRequestEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryCacheSizeEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryResultEventInfoEntity;
import com.huaweicloud.sermant.backend.util.DbUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 内存客户端
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public class MemoryClientImpl implements EventDao {

    private static final int EVENT_LEVEL_INDEX = 3;

    private BackendConfig backendConfig;

    private HashMap<String, QueryResultEventInfoEntity> eventMap = new HashMap<>();

    private HashMap<String, InstanceMeta> agentInstanceMap = new HashMap<>();

    private HashMap<String, Long> eventTimeKeyMap = new HashMap<>();

    private HashMap<String, List<String>> sessionMap = new HashMap<>();

    private AtomicInteger emergency = new AtomicInteger();

    private AtomicInteger important = new AtomicInteger();

    private AtomicInteger normal = new AtomicInteger();

    /**
     * 构造函数
     *
     * @param backendConfig 配置
     */
    public MemoryClientImpl(BackendConfig backendConfig) {
        this.backendConfig = backendConfig;
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
                important.incrementAndGet();
                break;
            case EMERGENCY:
                emergency.incrementAndGet();
                break;
            case NORMAL:
                normal.incrementAndGet();
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
                eventsRequestEntity.getStartTime(), eventsRequestEntity.getEndTime());
        queryResultKey = DbUtils.filterQueryResult(backendConfig, queryResultKey, pattern);
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
        QueryCacheSizeEntity queryCacheSize = new QueryCacheSizeEntity();
        queryCacheSize.setEmergencyNum(emergency.get());
        queryCacheSize.setImportantNum(important.get());
        queryCacheSize.setNormalNum(normal.get());
        queryCacheSize.setTotal(queryCacheSize.getEmergencyNum()
                + queryCacheSize.getImportantNum() + queryCacheSize.getNormalNum());
        return queryCacheSize;
    }

    @Override
    public void cleanOverDueEventTimerTask() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -Integer.parseInt(backendConfig.getExpire()));
        List<String> queryResultKey = getQueryEventKey(0, calendar.getTimeInMillis());
        for (String key : queryResultKey) {
            eventMap.remove(key);
            eventTimeKeyMap.remove(key);
            cleanOverDueEventLevel(key);
        }

    }

    @Override
    public QueryResultEventInfoEntity getDoNotifyEvent(Event event) {
        InstanceMeta instanceMeta = agentInstanceMap.get(event.getMetaHash());
        return DbUtils.aggregationEvent(event, instanceMeta);
    }

    /**
     * 获取查询的事件key
     *
     * @param startTime 开始事件
     * @param endTime 截止事件
     * @return 事件key
     */
    public List<String> getQueryEventKey(long startTime, long endTime) {
        List<Map.Entry<String, Long>> queryResultByTime = eventTimeKeyMap.entrySet().stream().filter(
                s -> s.getValue() >= startTime && s.getValue() <= endTime).collect(Collectors.toList());
        return queryResultByTime.stream().map(Map.Entry::getKey)
                .sorted().collect(Collectors.toList());
    }

    /**
     * 删除过期事件同步设置事件级别数量
     *
     * @param key event key
     */
    private void cleanOverDueEventLevel(String key) {
        if (backendConfig.getDatabase().equals(DatabaseType.MEMORY)) {
            EventLevel level = EventLevel.valueOf(
                    key.split(CommonConst.JOIN_REDIS_KEY)[EVENT_LEVEL_INDEX].toUpperCase(Locale.ROOT));
            switch (level) {
                case EMERGENCY:
                    emergency.decrementAndGet();
                    break;
                case IMPORTANT:
                    important.decrementAndGet();
                    break;
                case NORMAL:
                    normal.decrementAndGet();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 获取相同field 数量
     *
     * @param field field
     * @return 相同field 数量
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
