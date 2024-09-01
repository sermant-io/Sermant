/*
 * Copyright (C) 2023-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.backend.dao.redis;

import com.alibaba.fastjson.JSONObject;

import io.sermant.backend.common.conf.BackendConfig;
import io.sermant.backend.common.conf.CommonConst;
import io.sermant.backend.dao.DatabaseType;
import io.sermant.backend.dao.EventDao;
import io.sermant.backend.entity.InstanceMeta;
import io.sermant.backend.entity.event.Event;
import io.sermant.backend.entity.event.EventsRequestEntity;
import io.sermant.backend.entity.event.QueryCacheSizeEntity;
import io.sermant.backend.entity.event.QueryResultEventInfoEntity;
import io.sermant.backend.util.DbUtils;
import redis.clients.jedis.resps.Tuple;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Redis stand-alone client
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public class RedisClientImpl implements EventDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClientImpl.class);

    private final BackendConfig backendConfig;

    private RedisOperation redisOperation;

    /**
     * Construct the Jedis connection pool
     *
     * @param backendConfig configuration
     */
    public RedisClientImpl(BackendConfig backendConfig) {
        this.backendConfig = backendConfig;
        String url = StringUtils.trim(backendConfig.getUrl());
        String[] addressList = url.split(CommonConst.REDIS_CLUSTER_SPLIT);
        if (addressList.length == 0) {
            LOGGER.error("Redis service address is empty.");
            return;
        }
        if (addressList.length > 1) {
            redisOperation = new RedisClusterOperationImpl(backendConfig, addressList);
            return;
        }
        redisOperation = new RedisStandAloneOperationImpl(backendConfig);
    }

    @Override
    public boolean addEvent(Event event) {
        String instanceMeta = redisOperation.get(event.getMetaHash());
        if (DbUtils.isEmpty(instanceMeta)) {
            LOGGER.error("add event failed, event:{}, error message:[instance not exist]", event);
            return false;
        }
        InstanceMeta agentInstanceMeta = JSONObject.parseObject(instanceMeta, InstanceMeta.class);

        // get event field
        String field = DbUtils.getEventField(agentInstanceMeta, event);

        // check whether there are identical fields
        field = field + CommonConst.JOIN_REDIS_KEY + getSameFieldNum(field);

        // set an existing field cache to expire and delete it periodically
        redisOperation.setex(field, backendConfig.getFieldExpire(), Strings.EMPTY);

        // write event
        redisOperation.hset(CommonConst.REDIS_EVENT_KEY, field,
                JSONObject.toJSONString(DbUtils.aggregationEvent(event, agentInstanceMeta)));

        // write type index
        redisOperation.zadd(CommonConst.REDIS_EVENT_FIELD_SET_KEY, event.getTime(), field);
        return true;
    }

    @Override
    public boolean addInstanceMeta(InstanceMeta instanceMeta) {
        redisOperation.psetex(instanceMeta.getMetaHash(), backendConfig.getHeartbeatEffectiveTime(),
                JSONObject.toJSONString(instanceMeta));
        return true;
    }

    /**
     * Get the number of same fields
     *
     * @param field field
     * @return number of same fields
     */
    private int getSameFieldNum(String field) {
        return redisOperation.keys(field + "*").size();
    }

    @Override
    public List<QueryResultEventInfoEntity> queryEvent(EventsRequestEntity eventsRequestEntity) {
        String pattern = DbUtils.getPattern(eventsRequestEntity);
        List<Tuple> queryResultByTime = queryByTimeRange(CommonConst.REDIS_EVENT_FIELD_SET_KEY,
                eventsRequestEntity.getStartTime(),
                eventsRequestEntity.getEndTime());
        queryResultByTime = DbUtils.filterQueryResult(backendConfig, queryResultByTime, pattern);
        List<String> eventKeys = queryResultByTime.stream().sorted(Comparator.comparing(Tuple::getScore))
                .map(Tuple::getElement).collect(Collectors.toList());
        Collections.reverse(eventKeys);
        redisOperation.setex(eventsRequestEntity.getSessionId(), backendConfig.getSessionTimeout(),
                JSONObject.toJSONString(eventKeys));
        return queryEventPage(eventsRequestEntity.getSessionId(), 1);
    }

    /**
     * Query events by time range
     *
     * @param key key
     * @param startTime start time
     * @param endTime end time
     * @return query result
     */
    public List<Tuple> queryByTimeRange(String key, long startTime, long endTime) {
        return redisOperation.zrangeByScoreWithScores(key, startTime, endTime);
    }

    @Override
    public List<QueryResultEventInfoEntity> queryEventPage(String sessionId, int page) {
        List<QueryResultEventInfoEntity> result = new ArrayList<>();
        String events = redisOperation.get(sessionId);
        if (!DbUtils.isEmpty(events)) {
            List<String> keyList = JSONObject.parseArray(events, String.class);
            int startIndex = (page - 1) * CommonConst.DEFAULT_PAGE_SIZE;
            int endIndex = Math.min(startIndex + CommonConst.DEFAULT_PAGE_SIZE, keyList.size());
            for (String key : keyList.subList(startIndex, endIndex)) {
                result.add(JSONObject.parseObject(
                        redisOperation.hget(CommonConst.REDIS_EVENT_KEY, key), QueryResultEventInfoEntity.class));
            }
        }
        return result;
    }

    @Override
    public QueryCacheSizeEntity getQueryCacheSize(EventsRequestEntity eventsRequestEntity) {
        String events = redisOperation.get(eventsRequestEntity.getSessionId());
        List<String> keyList = JSONObject.parseArray(events, String.class);
        return DbUtils.getQueryCacheSize(keyList);
    }

    /**
     * Scheduled task to clear expired data
     */
    @Scheduled(fixedDelayString = "${database.fixedDelay}")
    public void cleanOverDueEventTimerTask() {
        if (backendConfig.getDatabase().equals(DatabaseType.REDIS)) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -backendConfig.getEventExpire());
            List<Tuple> needCleanEvent = queryByTimeRange(
                    CommonConst.REDIS_EVENT_FIELD_SET_KEY, 0, calendar.getTimeInMillis());
            List<String> eventKeys = needCleanEvent.stream().map(Tuple::getElement).collect(Collectors.toList());
            if (eventKeys.isEmpty()) {
                return;
            }
            redisOperation.hdel(CommonConst.REDIS_EVENT_KEY, eventKeys.toArray(new String[0]));
            redisOperation.zrem(CommonConst.REDIS_EVENT_FIELD_SET_KEY, eventKeys.toArray(new String[0]));
        }
    }

    @Override
    public QueryResultEventInfoEntity getDoNotifyEvent(Event event) {
        QueryResultEventInfoEntity queryResultEventInfoEntity = new QueryResultEventInfoEntity();
        String instanceMeta = redisOperation.hget(CommonConst.REDIS_HASH_KEY_OF_INSTANCE_META, event.getMetaHash());
        if (!DbUtils.isEmpty(instanceMeta)) {
            InstanceMeta agentInstanceMeta = JSONObject.parseObject(instanceMeta, InstanceMeta.class);
            queryResultEventInfoEntity = DbUtils.aggregationEvent(event, agentInstanceMeta);
        }
        return queryResultEventInfoEntity;
    }
}
