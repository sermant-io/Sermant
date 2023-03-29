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

package com.huaweicloud.sermant.backend.util;

import com.huaweicloud.sermant.backend.common.conf.BackendConfig;
import com.huaweicloud.sermant.backend.common.conf.CommonConst;
import com.huaweicloud.sermant.backend.entity.InstanceMeta;
import com.huaweicloud.sermant.backend.entity.event.Event;
import com.huaweicloud.sermant.backend.entity.event.EventType;
import com.huaweicloud.sermant.backend.entity.event.EventsRequestEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryResultEventInfoEntity;

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
 * 字符串工具类
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public class DbUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbUtils.class);

    /**
     * 构造函数
     */
    private DbUtils() {

    }

    /**
     * 字符串判空
     *
     * @param val 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String val) {
        return val == null || "".equals(val.trim());
    }

    /**
     * 字符串转int 空返回0
     *
     * @param val 字符串
     * @return 字符串转int值
     */
    public static int filterStr(String val) {
        return val == null ? 0 : Integer.parseInt(val);
    }

    /**
     * 获取事件field
     *
     * @param agentInstanceMeta agent实例
     * @param event 事件
     * @return 事件对应field
     */
    public static String getEventField(InstanceMeta agentInstanceMeta, Event event) {
        String field = String.join(CommonConst.JOIN_REDIS_KEY,
                getField(agentInstanceMeta.getService()),
                getField(agentInstanceMeta.getNode().getIp()),
                getField(String.valueOf(event.getEventType().getDescription()).toLowerCase(Locale.ROOT)),
                getField(event.getEventLevel().toString().toLowerCase(Locale.ROOT)),
                getField(event.getScope()),
                getField(agentInstanceMeta.getInstanceId()),
                agentInstanceMeta.getCluster() != null ? getField(agentInstanceMeta.getCluster().getCluster()) : "",
                agentInstanceMeta.getEnvironment() != null ? getField(agentInstanceMeta.getEnvironment().getEnv()) : "",
                getField(agentInstanceMeta.getAz()),
                getField(event.getMetaHash()),
                getField(String.valueOf(event.getTime())));
        return field;
    }

    /**
     * 过滤filed拼接的字符串
     *
     * @param str 字符串
     * @return 过滤结果
     */
    public static String getField(String str) {
        return !DbUtils.isEmpty(str) ? str : "";
    }

    /**
     * 聚合事件
     *
     * @param event 事件
     * @param agentInstanceMeta 事件归属的实例
     * @return 事件
     */
    public static QueryResultEventInfoEntity aggregationEvent(Event event, InstanceMeta agentInstanceMeta) {
        QueryResultEventInfoEntity queryResultEventInfoEntity = new QueryResultEventInfoEntity();
        queryResultEventInfoEntity.setTime(event.getTime());
        queryResultEventInfoEntity.setScope(event.getScope());
        queryResultEventInfoEntity.setLevel(event.getEventLevel().toString().toUpperCase(Locale.ROOT));
        queryResultEventInfoEntity.setType(event.getEventType().getDescription());
        if (event.getEventType().getDescription().equals(EventType.LOG.getDescription())) {
            queryResultEventInfoEntity.setInfo(event.getLogInfo());
        } else {
            queryResultEventInfoEntity.setInfo(event.getEventInfo());
        }
        HashMap<String, String> meta = new HashMap<>();
        meta.put("service", agentInstanceMeta.getService());
        meta.put("ip", agentInstanceMeta.getNode().getIp());
        queryResultEventInfoEntity.setMeta(meta);
        return queryResultEventInfoEntity;
    }

    /**
     * 拼接查询条件
     *
     * @param event 查询条件
     * @return 事件查询模版
     */
    public static String getPattern(EventsRequestEntity event) {
        List<String> patterns = new ArrayList<>();
        patterns.add(getListPattern(event.getService()));
        patterns.add(getListPattern(event.getIp()));
        patterns.add(getListPattern(event.getType() == null ? new ArrayList<>() : event.getType()));
        patterns.add(getListPattern(event.getLevel() == null ? new ArrayList<>() : event.getLevel()));
        patterns.add(getListPattern(event.getScope()));
        patterns.add(CommonConst.FULL_MATCH_KEY);
        return patterns.stream().map(String::valueOf).collect(Collectors.joining(CommonConst.JOIN_REDIS_KEY));
    }

    /**
     * 多条件判断
     *
     * @param strings 条件列表
     * @return 多条件查询条件
     */
    public static String getListPattern(List<String> strings) {
        return strings.size() == 0 ? CommonConst.FULL_MATCH_KEY : "(" + String.join("|", strings) + ")";
    }

    /**
     * 过滤查询结果
     *
     * @param backendConfig 配置
     * @param queryResultByTime 按时间查询结果
     * @param pattern 过滤规则
     * @return 过滤结果
     */
    public static List<String> filterQueryResult(BackendConfig backendConfig,
                                                 List<String> queryResultByTime, String pattern) {
        List<String> fanList = new ArrayList<>();
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
        List<Callable<List<String>>> tasks = new ArrayList<>();
        Callable<List<String>> task = null;
        List<String> cutList = null;
        for (int i = 0; i < threadNum; i++) {

            // 切割list
            if (i == threadNum - 1) {
                cutList = queryResultByTime.subList(i * threadSize, queryResultByTime.size());
            } else {
                cutList = queryResultByTime.subList(i * threadSize, (i + 1) * threadSize);
            }

            final List<String> finalCutList = cutList;
            task = new Callable<List<String>>() {
                @Override
                public List<String> call() throws Exception {
                    List<String> newList = new ArrayList<>();
                    for (String a : finalCutList) {
                        if (a.matches(pattern)) {
                            newList.add(a);
                        }
                    }
                    return newList;
                }
            };
            tasks.add(task);
        }

        try {
            List<Future<List<String>>> results = exc.invokeAll(tasks);
            for (Future<List<String>> result : results) {
                fanList.addAll(result.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("filter query result failed, error message:{}", e.getMessage());
        }
        exc.shutdown();
        return fanList;
    }
}
