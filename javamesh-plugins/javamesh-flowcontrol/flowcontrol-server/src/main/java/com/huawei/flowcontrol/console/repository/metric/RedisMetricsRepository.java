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
 * Based on com/alibaba/csp/sentinel/dashboard/repository/metric/MetricsRepository.java
 * from the Alibaba Sentinel project.
 */

package com.huawei.flowcontrol.console.repository.metric;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.huawei.flowcontrol.console.entity.MetricEntity;
import com.huawei.flowcontrol.console.util.DataType;
import com.huawei.flowcontrol.console.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Monitoring data persistence redis
 * redis工具类，完成增删改
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
@Component
@Primary
public class RedisMetricsRepository implements MetricsRepository<MetricEntity> {
    /**
     * 小时数
     */
    private static final int DIFFERENCE_HOURS = 1000 * 3600;
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisMetricsRepository.class);

    @Autowired
    private RedisUtil redisUtil;
    @Value("${redis.view.time}")
    private long viewTime;

    /**
     * 保存数据
     *
     * @param entity 监控实体
     */
    @Override
    public void save(MetricEntity entity) {
        if (entity == null || StringUtil.isBlank(entity.getApp())) {
            return;
        }

        // 将数据存放到redis中
        String app = entity.getApp();
        String value = JSON.toJSONString(entity);
        String source = entity.getResource();

        // 单独保存应用数据
        redisUtil.zSetAdd(app + DataType.SEPARATOR_COLON.getDataType()
            + DataType.REDISMETRICE.getDataType(), source, entity.getTimestamp().getTime());

        // 保存监控数据
        redisUtil.zSetAdd(getMetricResourceKey(app, source, new Date()), value, entity.getTimestamp().getTime());
    }

    @Override
    public void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }
        metrics.forEach(this::save);
    }

    @Override
    public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {
        List<MetricEntity> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }

        // 查询数据
        Set<String> set = getMetricDate(app, resource, startTime, endTime);

        for (String entity : set) {
            try {
                MetricEntity newEntity = JSONObject.parseObject(entity, MetricEntity.class);
                if (resource.equals(newEntity.getResource())) {
                    results.add(newEntity);
                }
            } catch (JSONException e) {
                LOGGER.error("Data error！", e);
            }
        }
        return results;
    }

    @Override
    public List<String> listResourcesOfApp(String app) {
        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }

        // 数据显示结束时间 & 开始时间
        final long end = System.currentTimeMillis();
        final long start = end - viewTime;

        // 根据key获取该数据段的数据
        Set<String> resourcesOfApp = redisUtil.getzSet(app + DataType.SEPARATOR_COLON.getDataType()
            + DataType.REDISMETRICE.getDataType(), start, end);
        if (resourcesOfApp == null) {
            return results;
        }
        for (String filed : resourcesOfApp) {
            results.add(filed);
        }
        Collections.reverse(results);

        // Order by last minute b_qps DESC.
        return results;
    }

    /**
     * 获取监控数据 按小时查询
     *
     * @param app       应用名
     * @param resource  资源名
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 返回所有的监控数据
     */
    private Set<String> getMetricDate(String app, String resource, long startTime, long endTime) {
        // 开始时间
        Date startDate = new Date(startTime);
        Calendar start = Calendar.getInstance();
        start.setTime(startDate);

        // 结束时间
        Date endDate = new Date(endTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        Set<String> metricDate = new HashSet<>();

        // 获取开始时间数据
        metricDate.addAll(redisUtil.getzSet(getMetricResourceKey(app, resource, startDate), startTime, endTime));

        // 判断开始小时与结束小时是否在同一小时，如果不在，则查询结束小时数据。
        if (start.get(Calendar.HOUR_OF_DAY) != end.get(Calendar.HOUR_OF_DAY)) {
            metricDate.addAll(redisUtil.getzSet(getMetricResourceKey(app, resource, endDate), startTime, endTime));
        }

        // 相差小时数
        long betweenHour = (endTime - startTime) / DIFFERENCE_HOURS;

        // 查询中间小时数据
        for (int i = 0; i < betweenHour; i++) {
            start.set(Calendar.HOUR_OF_DAY, start.get(Calendar.HOUR_OF_DAY) + 1);

            // 判断开始小时与结束小时是否相等
            if (start.get(Calendar.HOUR_OF_DAY) != end.get(Calendar.HOUR_OF_DAY)) {
                metricDate.addAll(redisUtil.getzSet(
                    getMetricResourceKey(app, resource, startDate), startTime, endTime));
            }
        }
        return metricDate;
    }

    /**
     * 拼接查询rediskey
     *
     * @param app      应用名
     * @param resource 资源名
     * @param date     日期
     * @return 返回拼接key
     */
    public String getMetricResourceKey(String app, String resource, Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        int year = localDateTime.getYear();
        int monthValue = localDateTime.getMonthValue();
        int dayOfMonth = localDateTime.getDayOfMonth();
        int hour = localDateTime.getHour();
        return app + DataType.SEPARATOR_COLON.getDataType() + DataType.REDISMETRICE.getDataType()
            + DataType.SEPARATOR_COLON.getDataType() + resource + DataType.SEPARATOR_UNDERLINE.getDataType()
            + year + DataType.SEPARATOR_HYPHEN.getDataType() + monthValue + DataType.SEPARATOR_HYPHEN.getDataType()
            + dayOfMonth + DataType.SEPARATOR_UNDERLINE.getDataType() + hour;
    }
}
