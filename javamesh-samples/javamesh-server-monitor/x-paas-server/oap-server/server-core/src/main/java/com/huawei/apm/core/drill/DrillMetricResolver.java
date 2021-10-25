/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.drill;

import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 无损演练复制指标cpm/sla处理
 *
 * @author qinfurong
 * @since 2021-08-12
 */
public class DrillMetricResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DrillMetricResolver.class);

    private static final Map<String, String> allMetricLabelKeys = new HashMap<>();
    private static final Map<String, Map<String, Field>> allMetricFileds = new HashMap<>();

    private static final String TOTAL_FIELD_NAME = "total";
    private static final String MATCH_FIELD_NAME = "match";

    static {
        loadLabelKeys();
    }

    private static void loadLabelKeys() {
        allMetricLabelKeys.put("ServiceSlaMetrics", "SERVICE_SLA");
        allMetricLabelKeys.put("ServiceCpmMetrics", "SERVICE_CPM");
        allMetricLabelKeys.put("ServiceRelationClientCpmMetrics", "SERVICE_RELATION_CLIENT_CPM");
        allMetricLabelKeys.put("ServiceRelationServerCpmMetrics", "SERVICE_RELATION_SERVER_CPM");
        allMetricLabelKeys.put("ServiceRelationClientCallSlaMetrics", "SERVICE_RELATION_CLIENT_CALL_SLA");
        allMetricLabelKeys.put("ServiceRelationServerCallSlaMetrics", "SERVICE_RELATION_SERVER_CALL_SLA");
        allMetricLabelKeys.put("ServiceInstanceSlaMetrics", "SERVICE_INSTANCE_SLA");
        allMetricLabelKeys.put("ServiceInstanceCpmMetrics", "SERVICE_INSTANCE_CPM");
        allMetricLabelKeys.put("EndpointCpmMetrics", "ENDPOINT_CPM");
        allMetricLabelKeys.put("EndpointSlaMetrics", "ENDPOINT_SLA");
        allMetricLabelKeys.put("DatabaseAccessSlaMetrics", "DATABASE_ACCESS_SLA");
        allMetricLabelKeys.put("DatabaseAccessCpmMetrics", "DATABASE_ACCESS_CPM");
    }

    public static void toParse(Metrics metric) {
        String simpleName = metric.getClass().getSimpleName();
        if (BaseReplicator.getCopyFlagValue(metric) != BaseReplicator.TO_COPY || !allMetricLabelKeys.containsKey(simpleName)) {
            return;
        }
        // 1.读取标签指标配置值
        String labelMetricValue = DrillThreadLocal.get().toString();
        Map<String, Object> labelMetricValues = BaseReplicator.parseLabelMetricValueToMap(labelMetricValue);
        Object labelValue = labelMetricValues.get(allMetricLabelKeys.get(simpleName));
        if (labelValue == null) {
            return;
        }
        // 2.大于0才进行修改
        BigDecimal metricLabelValueBigDecimal = new BigDecimal(labelValue.toString());
        if (metricLabelValueBigDecimal.compareTo(new BigDecimal(0)) <= 0) {
            return;
        }
        // 3.设置属性值
        if (simpleName.endsWith("SlaMetrics")) {
            toCopySlaValue(metric, metricLabelValueBigDecimal.longValue());
        } else {
            toCopyCpmValue(metric, metricLabelValueBigDecimal.longValue());
        }
    }

    private static void toCopySlaValue(Metrics metric, long value) {
        Field matchField = getField(metric, MATCH_FIELD_NAME);
        Field totalField = getField(metric, TOTAL_FIELD_NAME);
        if (matchField == null || totalField == null) {
            return;
        }

        try {
            matchField.set(metric, value);
            totalField.set(metric, 100);
        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to set {}.{} or {}", metric.getClass().getSimpleName(), MATCH_FIELD_NAME, TOTAL_FIELD_NAME);
        }
    }

    private static void toCopyCpmValue(Metrics metric, long value) {
        Field totalField = getField(metric, TOTAL_FIELD_NAME);
        if (totalField == null) {
            return;
        }
        try {
            totalField.set(metric, value);
        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to set {}.{} or {}", metric.getClass().getSimpleName(), MATCH_FIELD_NAME, TOTAL_FIELD_NAME);
        }
    }

    private static Map<String, Field> getFieldMap(String classSimpleName) {
        Map<String, Field> fieldMap = allMetricFileds.get(classSimpleName);
        if (fieldMap == null) {
            fieldMap = new HashMap<>();
            allMetricFileds.put(classSimpleName, fieldMap);
        }
        return fieldMap;
    }

    private static Field getField(Metrics metric, String fieldName) {
        Map<String, Field> fieldMap = getFieldMap(metric.getClass().getSimpleName());
        Field field = fieldMap.get(fieldName);
        if (field == null) {
            try {
                field = metric.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
            } catch (NoSuchFieldException e) {
                // 获取父类的属性
                try {
                    field = metric.getClass().getSuperclass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                } catch (NoSuchFieldException noSuchFieldException) {
                    LOGGER.error("Class property not found. info:" + metric.getClass().getName() + "." + fieldName);
                }
            }
        }
        fieldMap.put(fieldName, field);
        return field;
    }
}
