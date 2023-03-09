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

import com.huaweicloud.sermant.backend.common.conf.CommonConst;
import com.huaweicloud.sermant.backend.common.conf.EventConfig;
import com.huaweicloud.sermant.backend.common.util.StringUtils;
import com.huaweicloud.sermant.backend.dao.EventDao;
import com.huaweicloud.sermant.backend.entity.EventInfoEntity;
import com.huaweicloud.sermant.backend.entity.EventsRequestEntity;
import com.huaweicloud.sermant.backend.entity.InstanceMeta;
import com.huaweicloud.sermant.backend.entity.QueryResultEventInfoEntity;

import com.alibaba.fastjson.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * redis数据库数据处理
 *
 * @author xuezechao
 * @since 2023-03-02
 */
@Component
public class EventDaoForRedis implements EventDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDaoForRedis.class);

    private Jedis jedis;

    /**
     * 构造函数
     *
     * @param eventConfig 配置
     */
    public EventDaoForRedis(EventConfig eventConfig) {
        try {
            jedis = new Jedis(eventConfig.getUrl(), Integer.parseInt(eventConfig.getPort()));
            jedis.auth(eventConfig.getPassword());
        } catch (JedisConnectionException e) {
            LOGGER.error(String.format(Locale.ROOT, "connect redis failed, error message: [%s]", e.getMessage()));
        }
    }

    /**
     * 插入事件
     *
     * @param eventEntity 事件
     * @return true/false
     */
    @Override
    public boolean addEvent(EventInfoEntity eventEntity) {
        String event = JSONObject.toJSONString(eventEntity);
        try {
            // 获取事件字段
            String field = getEventField(eventEntity);
            if (StringUtils.isEmpty(field)) {
                return false;
            }

            // 写入事件
            jedis.hset(CommonConst.REDIS_EVENT_KEY, field, event);

            // 写入事件索引，以时间计数
            jedis.zadd(CommonConst.REDIS_EVENT_FIELD_SET_KEY, eventEntity.getTime(), field);
            return true;
        } catch (IllegalStateException e) {
            LOGGER.error(String.format(Locale.ROOT, "add event failed, event:[%s], error message:[%s]",
                    event, e.getMessage()));
            return false;
        }
    }

    /**
     * 插入agent实例
     *
     * @param instanceMeta agent元数据
     * @return true/false
     */
    @Override
    public boolean addInstanceMeta(InstanceMeta instanceMeta) {
        String meta = JSONObject.toJSONString(instanceMeta);
        try {
            // 写入实例信息
            jedis.hset(CommonConst.REDIS_HASH_KEY_OF_INSTANCE_META, instanceMeta.getMetaHash(), meta);
            return true;
        } catch (IllegalStateException e) {
            LOGGER.error(String.format(Locale.ROOT, "add instance meta failed, instance meta:[%s], error message:[%s]",
                    instanceMeta, e.getMessage()));
            return false;
        }
    }

    /**
     * 删除事件
     *
     * @param eventInfoEntity 事件
     * @return true/false
     */
    @Override
    public boolean deleteEvent(EventInfoEntity eventInfoEntity) {
        String event = JSONObject.toJSONString(eventInfoEntity);
        try {

            // 获取事件字段
            String field = getEventField(eventInfoEntity);
            if (StringUtils.isEmpty(field)) {
                return false;
            }

            // 删除事件
            jedis.hdel(CommonConst.REDIS_EVENT_KEY, field);

            // 删除事件索引
            jedis.zrem(CommonConst.REDIS_EVENT_FIELD_SET_KEY, field);
            return true;
        } catch (IllegalStateException e) {
            LOGGER.error(String.format(Locale.ROOT, "delete event failed, event:[%s], error message:[%s]",
                    event, e.getMessage()));
            return false;
        }
    }

    /**
     * 删除agent实例
     *
     * @param instanceMeta agent实例元数据
     * @return true/false
     */
    @Override
    public boolean deleteInstanceMeta(InstanceMeta instanceMeta) {
        String meta = JSONObject.toJSONString(instanceMeta);
        try {

            // 删除实例信息
            jedis.hdel(CommonConst.REDIS_HASH_KEY_OF_INSTANCE_META, instanceMeta.getMetaHash());
            return true;
        } catch (IllegalStateException e) {
            LOGGER.error(String.format(Locale.ROOT,
                    "delete instance meta failed, instance meta:[%s], error message:[%s]",
                    meta, e.getMessage()));
            return false;
        }
    }

    /**
     * 事件查询
     *
     * @param eventsRequestEntity 查询条件
     * @return 查询结果
     */
    @Override
    public List<QueryResultEventInfoEntity> queryEvent(EventsRequestEntity eventsRequestEntity) {
        List<QueryResultEventInfoEntity> queryResultEventInfoEntities = new ArrayList<>();

        // 查询符合时间条件的事件
        List<String> queryResultByTime = jedis.zrangeByScore(
                CommonConst.REDIS_EVENT_FIELD_SET_KEY,
                eventsRequestEntity.getStartTime(),
                eventsRequestEntity.getEndTime());

        // 查询符合实例条件的事件
        ScanResult<Map.Entry<String, String>> firstScanResult = jedis.hscan(
                CommonConst.REDIS_EVENT_KEY,
                String.valueOf(0),
                new ScanParams().match(getPattern(eventsRequestEntity)));
        int cursor = Integer.parseInt(firstScanResult.getCursor());
        aggregationQueryResult(queryResultEventInfoEntities, firstScanResult.getResult(), queryResultByTime);
        while (cursor > 0) {
            ScanResult<Map.Entry<String, String>> scanResult = jedis.hscan(
                    CommonConst.REDIS_EVENT_KEY,
                    String.valueOf(cursor),
                    new ScanParams().match(getPattern(eventsRequestEntity)));
            cursor = Integer.parseInt(scanResult.getCursor());
            aggregationQueryResult(queryResultEventInfoEntities, scanResult.getResult(), queryResultByTime);
        }
        return queryResultEventInfoEntities;
    }

    /**
     * 获取事件field
     *
     * @param eventInfoEntity 事件
     * @return field
     */
    private String getEventField(EventInfoEntity eventInfoEntity) {
        String event = JSONObject.toJSONString(eventInfoEntity);

        // 查询事件对应的实例
        String instanceMeta = jedis.hget(CommonConst.REDIS_HASH_KEY_OF_INSTANCE_META, eventInfoEntity.getMeta());
        if (StringUtils.isEmpty(instanceMeta)) {
            LOGGER.error(String.format(Locale.ROOT, "add event failed, event:[%s], error message:[instance not exist]",
                    event));
            return "";
        }
        InstanceMeta agentInstanceMeta = JSONObject.parseObject(instanceMeta, InstanceMeta.class);
        String field = String.join("_",
                agentInstanceMeta.getInstanceId(),
                agentInstanceMeta.getApplication(),
                agentInstanceMeta.getNode().getIp(),
                agentInstanceMeta.getCluster().getCluster(),
                agentInstanceMeta.getEnvironment().getEnv(),
                agentInstanceMeta.getAz(),
                eventInfoEntity.getMeta(),
                eventInfoEntity.getType().toString(),
                eventInfoEntity.getLevel().toString(),
                eventInfoEntity.getScope(),
                String.valueOf(eventInfoEntity.getTime()));
        return field;
    }

    /**
     * 拼接查询条件
     *
     * @param event 查询条件
     * @return 事件查询模版
     */
    private String getPattern(EventsRequestEntity event) {
        List<String> patterns = new ArrayList<>();
        patterns.add(!StringUtils.isEmpty(event.getApplication())
                ? event.getApplication() : CommonConst.FULL_MATCH_KEY);
        patterns.add(!StringUtils.isEmpty(event.getApplication())
                ? event.getIp() : CommonConst.FULL_MATCH_KEY);
        patterns.add(!StringUtils.isEmpty(event.getType())
                ? event.getType() : CommonConst.FULL_MATCH_KEY);
        patterns.add(!StringUtils.isEmpty(event.getLevel())
                ? event.getLevel() : CommonConst.FULL_MATCH_KEY);
        patterns.add(!StringUtils.isEmpty(event.getScope())
                ? event.getScope() : CommonConst.FULL_MATCH_KEY);
        return patterns.stream().map(String::valueOf).collect(Collectors.joining("_"));
    }

    /**
     * 聚合查询结果
     *
     * @param result          查询结果
     * @param aggregationData 需要聚合的数据
     * @param timeFilter      时间过滤列表
     */
    private void aggregationQueryResult(List<QueryResultEventInfoEntity> result,
                                        List<Map.Entry<String, String>> aggregationData,
                                        List<String> timeFilter) {
        for (Map.Entry<String, String> entry : aggregationData) {
            if (timeFilter.contains(entry.getKey())) {
                QueryResultEventInfoEntity q = new QueryResultEventInfoEntity();
                q.setEventInfoEntity(JSONObject.parseObject(entry.getValue(), EventInfoEntity.class));
                String[] s = entry.getKey().split("_");
                q.setAppName(s[1]);
                q.setIp(s[2]);
                result.add(q);
            }
        }
    }
}
