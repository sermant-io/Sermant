/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.metricserver.dao.influxdb;

import com.huawei.javamesh.metricserver.config.InfluxConfig;
import com.huawei.javamesh.metricserver.dao.influxdb.common.FluxBuilder;
import com.huawei.javamesh.metricserver.dao.influxdb.common.FluxTableResolver;
import com.huawei.javamesh.metricserver.dao.influxdb.request.InfluxInsertRequest;
import com.huawei.javamesh.metricserver.dao.influxdb.request.InfluxQueryRequest;
import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.internal.FluxResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Influxdb持久层
 */
@Repository
public class InfluxDao {

    private final String bucket;

    private final String org;

    private final WriteApi writeApi;

    private final QueryApi queryApi;

    private final DeleteApi deleteApi;

    private final FluxTableResolver defaultResolver = CommonFluxTableResolver.getInstance();

    @Autowired
    public InfluxDao(InfluxConfig config, InfluxDBClient influxDBClient) {
        this.bucket = config.getBucket();
        this.org = config.getOrg();
        this.writeApi = influxDBClient.makeWriteApi();
        this.queryApi = influxDBClient.getQueryApi();
        this.deleteApi = influxDBClient.getDeleteApi();
    }

    /**
     * 异步添加数据
     *
     * @param measurement 指标实体，需要包含Influxdb POJO相关注解
     */
    public void asyncInsert(Object measurement) {
        writeApi.writeMeasurement(WritePrecision.NS, measurement);
    }

    /**
     * 异步添加数据
     *
     * @param request {@link InfluxInsertRequest}实体
     */
    public void asyncInsert(InfluxInsertRequest request) {
        Point point = Point.measurement(request.getMeasurement())
            .time(request.getTime().toEpochMilli(), WritePrecision.MS);
        Map<String, String> tags = request.getTags();
        if (!CollectionUtils.isEmpty(tags)) {
            point.addTags(tags);
        }
        Map<String, Object> fields = request.getFields();
        if (!CollectionUtils.isEmpty(fields)) {
            point.addFields(fields);
        }
        writeApi.writePoint(point);
    }

    /**
     * 查询数据，使用默认的结果解析器
     *
     * @param request     {@link InfluxQueryRequest}实体
     * @param targetClass 目标类型Class
     * @param <M>         目标类型
     * @return 解析后的查询结果实体列表
     */
    public <M> List<M> query(InfluxQueryRequest request, Class<M> targetClass) {
        return query(request, defaultResolver, targetClass);
    }

    /**
     * 查询数据
     *
     * @param request        {@link InfluxQueryRequest}实体
     * @param resultResolver 结果解析器
     * @param targetClass    目标类型Class
     * @param <M>            目标类型
     * @return 解析后的查询结果实体列表
     */
    public <M> List<M> query(InfluxQueryRequest request, FluxTableResolver resultResolver, Class<M> targetClass) {
        return query(buildFlux(request), resultResolver, targetClass);
    }

    /**
     * 查询数据
     *
     * @param flux           flux查询语句
     * @param resultResolver 结果解析器
     * @param targetClass    目标类型Class
     * @param <M>            目标类型
     * @return 解析后的查询结果实体列表
     */
    public <M> List<M> query(String flux, FluxTableResolver resultResolver, Class<M> targetClass) {
        return resultResolver.resolve(query(flux), targetClass);
    }

    /**
     * 删除指定时间段的数据（谨慎使用）
     *
     * @param start 开始时间
     * @param stop  结束时间
     */
    public void delete(OffsetDateTime start, OffsetDateTime stop) {
        deleteApi.delete(start, stop, "", bucket, org);
    }

    private List<FluxTable> query(String flux) {
        return queryApi.query(flux);
    }

    private String buildFlux(InfluxQueryRequest request) {
        FluxBuilder fluxBuilder = FluxBuilder.from(bucket)
            .measurement(request.getMeasurement())
            .range(request.getStart(), request.getEnd());
        if (!CollectionUtils.isEmpty(request.getTags())) {
            for (Map.Entry<String, String> entry : request.getTags().entrySet()) {
                fluxBuilder.addFilter(FluxBuilder.Filter.newEquals(entry.getKey(), entry.getValue()));
            }
        }
        return fluxBuilder.build();
    }

    /**
     * 通用结果解析器
     * 以一种不太主流的方式避免了FluxResultMapper无法解析field的问题
     */
    static class CommonFluxTableResolver implements FluxTableResolver {

        private static final CommonFluxTableResolver INSTANCE = new CommonFluxTableResolver();

        private CommonFluxTableResolver() {
        }

        public static CommonFluxTableResolver getInstance() {
            return INSTANCE;
        }

        private final FluxResultMapper mapper = new FluxResultMapper();

        @Override
        public <M> List<M> resolve(List<FluxTable> fluxTables, Class<M> targetClass) {
            final List<M> metrics = new LinkedList<>();
            Map<Instant, List<FluxRecord>> recordGroupByTime = fluxTables.stream()
                .map(FluxTable::getRecords)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(FluxRecord::getTime));
            for (List<FluxRecord> records : recordGroupByTime.values()) {
                FluxRecord firstRecord = null;
                for (FluxRecord record : records) {
                    if (firstRecord == null) {
                        firstRecord = record;
                        Map<String, Object> values = firstRecord.getValues();
                        values.put(record.getField(), record.getValue());
                        values.put("_value", null);
                        values.put("_field", null);
                    } else {
                        firstRecord.getValues().put(record.getField(), record.getValue());
                    }
                }
                if (firstRecord != null) {
                    metrics.add(mapper.toPOJO(firstRecord, targetClass));
                }
            }
            return metrics;
        }
    }
}
