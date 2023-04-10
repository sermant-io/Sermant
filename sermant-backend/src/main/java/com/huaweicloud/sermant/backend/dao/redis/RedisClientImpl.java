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
import com.huaweicloud.sermant.backend.common.conf.CommonConst;
import com.huaweicloud.sermant.backend.dao.DatabaseType;
import com.huaweicloud.sermant.backend.dao.EventDao;
import com.huaweicloud.sermant.backend.entity.InstanceMeta;
import com.huaweicloud.sermant.backend.entity.event.Event;
import com.huaweicloud.sermant.backend.entity.event.EventsRequestEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryCacheSizeEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryResultEventInfoEntity;
import com.huaweicloud.sermant.backend.util.DbUtils;

import com.alibaba.fastjson.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.resps.Tuple;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * redis单机客户端
 *
 * @author xuezechao
 * @since 2023-03-02
 */
@Component
public class RedisClientImpl implements EventDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClientImpl.class);

    private JedisPool jedisPool;

    private BackendConfig backendConfig;

    /**
     * 构造redis 连接池
     *
     * @param backendConfig 配置
     */
    public RedisClientImpl(BackendConfig backendConfig) {
        this.backendConfig = backendConfig;
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(Integer.parseInt(backendConfig.getMaxTotal()));
        config.setMaxIdle(Integer.parseInt(backendConfig.getMaxIdle()));
        if (backendConfig.getVersion().compareTo("6.0") < 0) {
            jedisPool = new JedisPool(
                config,
                Arrays.asList(backendConfig.getUrl().split(CommonConst.REDIS_ADDRESS_SPLIT)).get(0),
                Integer.parseInt(Arrays.asList(backendConfig.getUrl().split(CommonConst.REDIS_ADDRESS_SPLIT)).get(1)),
                Integer.parseInt(backendConfig.getTimeout()),
                backendConfig.getPassword());
        } else {
            jedisPool = new JedisPool(
                config,
                Arrays.asList(backendConfig.getUrl().split(CommonConst.REDIS_ADDRESS_SPLIT)).get(0),
                Integer.parseInt(Arrays.asList(backendConfig.getUrl().split(CommonConst.REDIS_ADDRESS_SPLIT)).get(1)),
                Integer.parseInt(backendConfig.getTimeout()),
                backendConfig.getUser(),
                backendConfig.getPassword());
        }
    }

    @Override
    public boolean addEvent(Event event) {
        try (Jedis jedis = jedisPool.getResource()) {
            String instanceMeta = jedis.get(event.getMetaHash());
            if (DbUtils.isEmpty(instanceMeta)) {
                LOGGER.error("add event failed, event:{}, error message:[instance not exist]", event);
                return false;
            }
            InstanceMeta agentInstanceMeta = JSONObject.parseObject(instanceMeta, InstanceMeta.class);

            // 获取事件字段
            String field = DbUtils.getEventField(agentInstanceMeta, event);

            // 检查是否有相同field
            field = field + CommonConst.JOIN_REDIS_KEY + getSameFieldNum(field);

            // 设置已有field缓存 定时过期删除
            jedis.setex(field, backendConfig.getFieldExpire(), Strings.EMPTY);

            // 写入事件
            jedis.hset(CommonConst.REDIS_EVENT_KEY, field,
                    JSONObject.toJSONString(DbUtils.aggregationEvent(event, agentInstanceMeta)));

            // 写入类型索引
            jedis.zadd(CommonConst.REDIS_EVENT_FIELD_SET_KEY, event.getTime(), field);
            return true;
        } catch (IllegalStateException e) {
            LOGGER.error("add event failed, event:{}, error message:{}", event, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean addInstanceMeta(InstanceMeta instanceMeta) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(
                    instanceMeta.getMetaHash(),
                    backendConfig.getHeartbeatEffectiveTime(),
                    JSONObject.toJSONString(instanceMeta));
            return true;
        } catch (IllegalStateException e) {
            LOGGER.error("add instance meta failed, instance meta:{}, error message:{}", instanceMeta, e.getMessage());
            return false;
        }
    }

    /**
     * 获取相同field 数量
     *
     * @param field field
     * @return 相同field 数量
     */
    private int getSameFieldNum(String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys(field + "*").size();
        } catch (IllegalStateException e) {
            LOGGER.error("query same field failed, field:{}, error message:{}", field, e.getMessage());
            return 0;
        }
    }

    @Override
    public List<QueryResultEventInfoEntity> queryEvent(EventsRequestEntity eventsRequestEntity) {
        String pattern = DbUtils.getPattern(eventsRequestEntity);
        try (Jedis jedis = jedisPool.getResource()) {
            List<Tuple> queryResultByTime = queryByTimeRange(CommonConst.REDIS_EVENT_FIELD_SET_KEY,
                    eventsRequestEntity.getStartTime(),
                    eventsRequestEntity.getEndTime());
            queryResultByTime = DbUtils.filterQueryResult(backendConfig, queryResultByTime, pattern);
            List<String> eventKeys = queryResultByTime.stream().sorted(Comparator.comparing(Tuple::getScore))
                    .map(Tuple::getElement).collect(Collectors.toList());
            Collections.reverse(eventKeys);
            jedis.setex(
                    eventsRequestEntity.getSessionId(),
                    backendConfig.getSessionTimeout(),
                    JSONObject.toJSONString(eventKeys));
            return queryEventPage(eventsRequestEntity.getSessionId(), 1);
        } catch (IllegalStateException e) {
            LOGGER.error("query event failed, error message:{}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 按时间范围查询事件
     *
     * @param key 集合key
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 查询结果
     */
    public List<Tuple> queryByTimeRange(String key, long startTime, long endTime) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrangeByScoreWithScores(key, startTime, endTime);
        } catch (IllegalStateException e) {
            LOGGER.error("query event by time failed, key:{}, startTime:{}, endTime:{}, error message:{}",
                    key, startTime, endTime, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<QueryResultEventInfoEntity> queryEventPage(String sessionId, int page) {
        List<QueryResultEventInfoEntity> result = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            String events = jedis.get(sessionId);
            if (!DbUtils.isEmpty(events)) {
                List<String> keyList = JSONObject.parseArray(events, String.class);
                int startIndex = (page - 1) * CommonConst.DEFAULT_PAGE_SIZE;
                int endIndex = Math.min(startIndex + CommonConst.DEFAULT_PAGE_SIZE, keyList.size());
                for (String key : keyList.subList(startIndex, endIndex)) {
                    result.add(JSONObject.parseObject(
                            jedis.hget(CommonConst.REDIS_EVENT_KEY, key),
                            QueryResultEventInfoEntity.class));
                }
            }
            return result;
        } catch (IllegalStateException e) {
            LOGGER.error("query event by page failed, sessionId:{}, error message:{}",
                    sessionId, e.getMessage());
            return result;
        }
    }

    @Override
    public QueryCacheSizeEntity getQueryCacheSize(EventsRequestEntity eventsRequestEntity) {
        QueryCacheSizeEntity queryCacheSize = new QueryCacheSizeEntity();
        try (Jedis jedis = jedisPool.getResource()) {
            String events = jedis.get(eventsRequestEntity.getSessionId());
            List<String> keyList = JSONObject.parseArray(events, String.class);
            queryCacheSize = DbUtils.getQueryCacheSize(keyList);
            return queryCacheSize;
        } catch (IllegalStateException e) {
            LOGGER.error("query event size failed, sessionId:{}, error message:{}",
                    eventsRequestEntity.getSessionId(), e.getMessage());
            return queryCacheSize;
        }
    }

    /**
     * 定时任务，清理过期数据
     */
    @Scheduled(fixedDelayString = "${database.fixedDelay}")
    public void cleanOverDueEventTimerTask() {
        if (backendConfig.getDatabase().equals(DatabaseType.REDIS)) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -backendConfig.getEventExpire());
            List<Tuple> needCleanEvent = queryByTimeRange(
                    CommonConst.REDIS_EVENT_FIELD_SET_KEY, 0, calendar.getTimeInMillis());
            List<String> eventKeys = needCleanEvent.stream().map(Tuple::getElement).collect(Collectors.toList());
            if (eventKeys.size() <= 0) {
                return;
            }
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.hdel(CommonConst.REDIS_EVENT_KEY, eventKeys.toArray(new String[0]));
                jedis.zrem(CommonConst.REDIS_EVENT_FIELD_SET_KEY, eventKeys.toArray(new String[0]));
            } catch (IllegalStateException e) {
                LOGGER.error("delete over dur event failed, error message:{}", e.getMessage());
            }
        }
    }

    @Override
    public QueryResultEventInfoEntity getDoNotifyEvent(Event event) {
        QueryResultEventInfoEntity queryResultEventInfoEntity = new QueryResultEventInfoEntity();
        try (Jedis jedis = jedisPool.getResource()) {
            String instanceMeta = jedis.hget(CommonConst.REDIS_HASH_KEY_OF_INSTANCE_META, event.getMetaHash());
            if (!DbUtils.isEmpty(instanceMeta)) {
                InstanceMeta agentInstanceMeta = JSONObject.parseObject(instanceMeta, InstanceMeta.class);
                queryResultEventInfoEntity = DbUtils.aggregationEvent(event, agentInstanceMeta);
            }
            return queryResultEventInfoEntity;
        } catch (IllegalStateException e) {
            LOGGER.error("add event failed, event:{}, error message:{}", event, e.getMessage());
            return queryResultEventInfoEntity;
        }
    }
}
