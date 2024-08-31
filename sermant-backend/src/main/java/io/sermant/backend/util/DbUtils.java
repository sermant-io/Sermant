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

package io.sermant.backend.util;

import io.sermant.backend.common.conf.BackendConfig;
import io.sermant.backend.common.conf.CommonConst;
import io.sermant.backend.entity.InstanceMeta;
import io.sermant.backend.entity.event.Event;
import io.sermant.backend.entity.event.EventLevel;
import io.sermant.backend.entity.event.EventType;
import io.sermant.backend.entity.event.EventsRequestEntity;
import io.sermant.backend.entity.event.QueryCacheSizeEntity;
import io.sermant.backend.entity.event.QueryResultEventInfoEntity;
import redis.clients.jedis.resps.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * String utility class
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public class DbUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbUtils.class);

    private static final int EVENT_LEVEL_INDEX = 3;

    /**
     * Constructor
     */
    private DbUtils() {

    }

    /**
     * String empty check
     *
     * @param val string
     * @return is empty
     */
    public static boolean isEmpty(String val) {
        return val == null || "".equals(val.trim());
    }

    /**
     * String to int, empty returns 0
     *
     * @param val string
     * @return int value
     */
    public static int filterStr(String val) {
        return val == null ? 0 : Integer.parseInt(val);
    }

    /**
     * Get event field
     *
     * @param agentInstanceMeta agent instance meta
     * @param event event
     * @return event correspondence field
     */
    public static String getEventField(InstanceMeta agentInstanceMeta, Event event) {
        String name = getEventDisplayName(event);
        String field = String.join(CommonConst.JOIN_REDIS_KEY,
                getField(agentInstanceMeta.getService()),
                getField(agentInstanceMeta.getNode().getIp()),
                getField(String.valueOf(event.getEventType().getDescription()).toLowerCase(Locale.ROOT)),
                getField(event.getEventLevel().toString().toLowerCase(Locale.ROOT)),
                getField(event.getScope()),
                getField(agentInstanceMeta.getInstanceId()),
                getField(name),
                agentInstanceMeta.getCluster() != null ? getField(agentInstanceMeta.getCluster().getCluster()) : "",
                agentInstanceMeta.getEnvironment() != null ? getField(agentInstanceMeta.getEnvironment().getEnv()) : "",
                getField(agentInstanceMeta.getAz()),
                getField(event.getMetaHash()),
                getField(String.valueOf(event.getTime())));
        return field;
    }

    /**
     * Obtain event display name, Regarding log events, the event name displayed on the front end is the log level
     *
     * @param event event
     * @return event display name
     */
    private static String getEventDisplayName(Event event) {
        String name = "";
        if (event.getEventType() == EventType.LOG && event.getLogInfo() != null) {
            name = event.getLogInfo().getLogLevel();
        } else if (event.getEventType() != EventType.LOG && event.getEventInfo() != null) {
            name = event.getEventInfo().getName();
        }
        return name;
    }

    /**
     * Filter the string of field concatenations
     *
     * @param str string
     * @return filter result
     */
    public static String getField(String str) {
        return !DbUtils.isEmpty(str) ? str : "";
    }

    /**
     * Aggregate event
     *
     * @param event event
     * @param agentInstanceMeta the instance to which the event belongs
     * @return QueryResultEventInfoEntity
     */
    public static QueryResultEventInfoEntity aggregationEvent(Event event, InstanceMeta agentInstanceMeta) {
        QueryResultEventInfoEntity queryResultEventInfoEntity = new QueryResultEventInfoEntity();
        queryResultEventInfoEntity.setTime(event.getTime());
        queryResultEventInfoEntity.setScope(event.getScope());
        queryResultEventInfoEntity.setLevel(event.getEventLevel().toString().toLowerCase(Locale.ROOT));
        queryResultEventInfoEntity.setType(event.getEventType().getDescription());
        if (event.getEventType().getDescription().equals(EventType.LOG.getDescription())) {
            queryResultEventInfoEntity.setInfo(event.getLogInfo());
        } else {
            queryResultEventInfoEntity.setInfo(event.getEventInfo());
        }
        HashMap<String, String> meta = new HashMap<>();
        meta.put("service", agentInstanceMeta.getService());
        meta.put("ip", agentInstanceMeta.getNode().getIp());
        meta.put("instanceId", agentInstanceMeta.getInstanceId());
        queryResultEventInfoEntity.setMeta(meta);
        return queryResultEventInfoEntity;
    }

    /**
     * Concatenated query condition
     *
     * @param event Query condition
     * @return Event query pattern
     */
    public static String getPattern(EventsRequestEntity event) {
        List<String> patterns = new ArrayList<>();
        patterns.add(getListPattern(event.getService()));
        patterns.add(getListPattern(event.getIp()));
        patterns.add(getListPattern(event.getType() == null ? new ArrayList<>() : event.getType()));
        patterns.add(getListPattern(event.getLevel() == null ? new ArrayList<>() : event.getLevel()));
        patterns.add(getListPattern(event.getScope()));
        patterns.add(getListPattern(event.getInstanceIds()));
        patterns.add(getListPattern(event.getName()));
        patterns.add(CommonConst.FULL_MATCH_KEY);
        return patterns.stream().map(String::valueOf).collect(Collectors.joining(CommonConst.JOIN_REDIS_KEY));
    }

    /**
     * Multi-condition judgment
     *
     * @param strings condition list
     * @return multi-condition query string
     */
    public static String getListPattern(List<String> strings) {
        return strings.size() == 0 ? CommonConst.FULL_MATCH_KEY : "(" + String.join("|", strings) + ")";
    }

    /**
     * Filter query result
     *
     * @param backendConfig backend configuration
     * @param queryResultByTime query the result by time
     * @param pattern filter rule
     * @return filter result
     */
    public static List<Tuple> filterQueryResult(BackendConfig backendConfig,
            List<Tuple> queryResultByTime, String pattern) {
        List<Tuple> fanList = new ArrayList<>();
        if (queryResultByTime.size() <= 0) {
            return fanList;
        }
        int threadSize = Integer.parseInt(backendConfig.getFilterThreadNum());
        int dataSize = queryResultByTime.size();
        int threadNum = 0;
        if (dataSize % threadSize == 0) {
            threadNum = dataSize / threadSize;
        } else {
            threadNum = dataSize / threadSize + 1;
        }
        ExecutorService exc = Executors.newFixedThreadPool(threadNum);
        List<Callable<List<Tuple>>> tasks = new ArrayList<>();
        Callable<List<Tuple>> task = null;
        List<Tuple> cutList = null;
        for (int i = 0; i < threadNum; i++) {

            // cut list
            if (i == threadNum - 1) {
                cutList = queryResultByTime.subList(i * threadSize, queryResultByTime.size());
            } else {
                cutList = queryResultByTime.subList(i * threadSize, (i + 1) * threadSize);
            }

            final List<Tuple> finalCutList = cutList;
            task = new Callable<List<Tuple>>() {
                @Override
                public List<Tuple> call() throws Exception {
                    return getTuples(finalCutList, pattern);
                }
            };
            tasks.add(task);
        }

        try {
            List<Future<List<Tuple>>> results = exc.invokeAll(tasks);
            for (Future<List<Tuple>> result : results) {
                fanList.addAll(result.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("filter query result failed, error message:{}", e.getMessage());
        }
        exc.shutdown();
        return fanList;
    }

    private static List<Tuple> getTuples(List<Tuple> finalCutList, String pattern) {
        List<Tuple> newList = new ArrayList<>();
        for (Tuple tuple : finalCutList) {
            if (tuple.getElement().matches(pattern)) {
                newList.add(tuple);
            }
        }
        return newList;
    }

    /**
     * get the query result data quantity
     *
     * @param keyList key list of query event
     * @return Query result data quantity
     */
    public static QueryCacheSizeEntity getQueryCacheSize(List<String> keyList) {
        QueryCacheSizeEntity queryCacheSize = new QueryCacheSizeEntity();
        int emergency = 0;
        int important = 0;
        int normal = 0;
        for (String key : keyList) {
            EventLevel level = EventLevel.valueOf(
                    key.split(CommonConst.JOIN_REDIS_KEY)[EVENT_LEVEL_INDEX].toUpperCase(Locale.ROOT));
            switch (level) {
                case EMERGENCY:
                    emergency++;
                    break;
                case IMPORTANT:
                    important++;
                    break;
                case NORMAL:
                    normal++;
                    break;
                default:
                    break;
            }
        }
        queryCacheSize.setEmergencyNum(emergency);
        queryCacheSize.setImportantNum(important);
        queryCacheSize.setNormalNum(normal);
        queryCacheSize.setTotal(emergency + important + normal);
        return queryCacheSize;
    }
}
