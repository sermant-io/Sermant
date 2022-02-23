/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Based on com/alibaba/csp/sentinel/dashboard/controller/MetricController.java from the Alibaba Sentinel project.
 */

package com.huawei.flowcontrol.console.controller;

import com.huawei.flowcontrol.console.entity.MetricEntity;
import com.huawei.flowcontrol.console.entity.MetricVo;
import com.huawei.flowcontrol.console.entity.Result;
import com.huawei.flowcontrol.console.repository.metric.MetricsRepository;

import com.alibaba.csp.sentinel.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指标查询
 *
 * @author openSource
 * @since 2022-02-21
 */
@RestController
@RequestMapping("/metric")
public class MetricController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricController.class);

    private static final long MAX_QUERY_INTERVAL_MS = 1000 * 60 * 60;

    private static final int INIT_MAP_CAPACITY = 16;

    private static final int MAX_PAGE_SIZE = 20;

    private static final int DEFAULT_PAGE_SIZE = 6;

    private static final long ONE_MINUTE = 1000 * 60;

    @Autowired
    private MetricsRepository<MetricEntity> metricStore;

    @ResponseBody
    @RequestMapping("/queryTopResourceMetric.json")
    @SuppressWarnings({"checkstyle:RegexpSingleline", "checkstyle:ParameterNumber"})
    public Result<?> queryTopResourceMetric(final String app, Integer pageIndex, Integer pageSize, Boolean desc,
        Long startTime, Long endTime, String searchKey) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        final QueryCondition queryCondition = new QueryCondition(startTime, endTime, pageIndex, pageSize, desc, null);
        if (queryCondition.endTime - queryCondition.startTime > MAX_QUERY_INTERVAL_MS) {
            return Result.ofFail(-1, "time intervalMs is too big, must <= 1h");
        }
        List<String> resources = metricStore.listResourcesOfApp(app);
        LOGGER.debug("queryTopResourceMetric(), resources.size()={}", resources.size());

        if (resources == null || resources.isEmpty()) {
            return Result.ofSuccess(null);
        }
        if (!queryCondition.desc) {
            Collections.reverse(resources);
        }
        if (StringUtil.isNotEmpty(searchKey)) {
            List<String> searched = new ArrayList<>();
            for (String resource : resources) {
                if (resource.contains(searchKey)) {
                    searched.add(resource);
                }
            }
            resources = searched;
        }
        int totalPage = (resources.size() + queryCondition.pageSize - 1) / queryCondition.pageSize;
        List<String> topResource = new ArrayList<>();
        if (queryCondition.pageIndex <= totalPage) {
            topResource = resources.subList((queryCondition.pageIndex - 1) * queryCondition.pageSize,
                Math.min(queryCondition.pageIndex * queryCondition.pageSize, resources.size()));
        }
        return Result.ofSuccess(queryMetrics(topResource, app, queryCondition, resources,
            totalPage));
    }

    private Map<String, Object> queryMetrics(List<String> topResource, String app, QueryCondition queryCondition,
        List<String> resources, int totalPage) {
        final Map<String, Iterable<MetricVo>> map = new ConcurrentHashMap<>();
        LOGGER.debug("topResource={}", topResource);
        long time = System.currentTimeMillis();
        for (final String resource : topResource) {
            List<MetricEntity> entities = metricStore.queryByAppAndResourceBetween(
                app, resource, queryCondition.startTime, queryCondition.endTime);
            LOGGER.debug("resource={}, entities.size()={}", resource, entities == null ? "null" : entities.size());
            List<MetricVo> vos = MetricVo.fromMetricEntities(entities, resource);
            Iterable<MetricVo> vosSorted = sortMetricVoAndDistinct(vos);
            map.put(resource, vosSorted);
        }
        LOGGER.debug("queryTopResourceMetric() total query time={} ms", System.currentTimeMillis() - time);
        Map<String, Object> resultMap = new HashMap<>(INIT_MAP_CAPACITY);
        resultMap.put("totalCount", resources.size());
        resultMap.put("totalPage", totalPage);
        resultMap.put("pageIndex", queryCondition.pageIndex);
        resultMap.put("pageSize", queryCondition.pageSize);
        Map<String, Iterable<MetricVo>> metricMap = new LinkedHashMap<>();
        for (String identity : topResource) {
            metricMap.put(identity, map.get(identity));
        }
        resultMap.put("metric", metricMap);
        return resultMap;
    }

    @ResponseBody
    @RequestMapping("/queryByAppAndResource.json")
    @SuppressWarnings({"checkstyle:RegexpSingleline"})
    public Result<?> queryByAppAndResource(String app, String identity, Long startTime, Long endTime) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isEmpty(identity)) {
            return Result.ofFail(-1, "identity can't be null or empty");
        }
        final QueryCondition queryCondition = new QueryCondition(startTime, endTime, ONE_MINUTE);
        if (queryCondition.endTime - queryCondition.startTime > MAX_QUERY_INTERVAL_MS) {
            return Result.ofFail(-1, "time intervalMs is too big, must <= 1h");
        }
        List<MetricEntity> entities = metricStore.queryByAppAndResourceBetween(
            app, identity, queryCondition.startTime, queryCondition.endTime);
        List<MetricVo> vos = MetricVo.fromMetricEntities(entities, identity);
        return Result.ofSuccess(sortMetricVoAndDistinct(vos));
    }

    private Iterable<MetricVo> sortMetricVoAndDistinct(List<MetricVo> vos) {
        if (vos == null) {
            return null;
        }
        Map<Long, MetricVo> map = new TreeMap<>();
        for (MetricVo vo : vos) {
            MetricVo oldVo = map.get(vo.getTimestamp());
            if (oldVo == null || vo.getGmtCreate() > oldVo.getGmtCreate()) {
                map.put(vo.getTimestamp(), vo);
            }
        }
        return map.values();
    }

    static class QueryCondition {
        private static final long FIVE_MINUTES = 1000 * 60 * 5;
        @SuppressWarnings("checkstyle:RegexpSingleline")
        private Long startTime;
        private Long endTime;
        @SuppressWarnings("checkstyle:RegexpSingleline")
        private Integer pageIndex;
        private Integer pageSize;
        @SuppressWarnings("checkstyle:RegexpSingleline")
        private Boolean desc;

        @SuppressWarnings({"checkstyle:ParameterNumber", "checkstyle:RegexpSingleline"})
        QueryCondition(Long startTime, Long endTime, Integer pageIndex, Integer pageSize, Boolean desc,
            Long defaultGap) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.pageSize = pageSize;
            this.pageIndex = pageIndex;
            this.desc = desc;
            if (pageIndex == null || pageIndex <= 0) {
                this.pageIndex = 1;
            }
            if (pageSize == null) {
                this.pageSize = DEFAULT_PAGE_SIZE;
            }
            if (this.pageSize >= MAX_PAGE_SIZE) {
                this.pageSize = MAX_PAGE_SIZE;
            }
            if (desc == null) {
                this.desc = true;
            }
            if (endTime == null) {
                this.endTime = System.currentTimeMillis();
            }
            if (startTime == null) {
                this.startTime = this.endTime - (defaultGap == null ? FIVE_MINUTES : defaultGap);
            }
        }

        @SuppressWarnings("checkstyle:RegexpSingleline")
        QueryCondition(Long startTime, Long endTime, long defaultGap) {
            this(startTime, endTime, null, null, null, defaultGap);
        }
    }
}
