/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.drill;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.skywalking.apm.network.common.v3.KeyStringValuePair;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.apache.skywalking.apm.network.language.agent.v3.SpanObject;
import org.apache.skywalking.apm.util.StringUtil;
import org.apache.skywalking.oap.server.core.CoreModuleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huawei.apm.core.drill.MetricLabelValueType.LONG;

/**
 * SegmentObject复制器
 *
 * @author qinfurong
 * @since 2021-06-29
 */
public class SegmentObjectReplicator extends BaseReplicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SegmentObjectReplicator.class);
    private static Map<SegmentObject, Integer> copySegmentObjectMap = new HashMap<>();
    private static Map<SegmentObject, String> labelSegmentObjectMap = new HashMap<>();

    /**
     * 复制SegmentObject，并按照标签配置值修改
     *
     * @param segmentObject 被复制的采集对象
     */
    public static SegmentObject copyModification(SegmentObject segmentObject) {
        if (segmentObject == null || !CoreModuleConfig.losslessDrillSwitchStatus) {
            return null;
        }

        List<SpanObject> spansList = segmentObject.getSpansList();
        if (spansList == null || spansList.isEmpty()) {
            return null;
        }
        // 解析指标标签值
        Map<String, Object> metricValueMap = new HashMap<>();
        int copy = getLabelMetricValue(spansList, metricValueMap);
        if (NO_COPY == copy) {
            return null;
        }
        SegmentObject copySegmentObject = null;
        try {
            copySegmentObject = SegmentObject.parseFrom(segmentObject.toByteArray());
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("parse segmentObject to segmentObject failed!, message:{}", e);
            return null;
        }
        spansList = copySegmentObject.getSpansList();
        spansList.forEach(span -> updateNumericFiledValue(span, "endTime_", LONG, metricValueMap.get("RESP_TIME")));
        addCashSegmentObjectMap(copySegmentObject, TO_COPY, toJson(metricValueMap));
        return copySegmentObject;
    }

    protected static void updateNumericFiledValue(Object obj, String fieldName, MetricLabelValueType type, Object labelValue) {
        if (obj == null || StringUtil.isEmpty(fieldName)) {
            return;
        }
        String metricLabelValue = String.valueOf(labelValue);
        switch (type) {
            case LONG:
                toCopyModificationRespTime((SpanObject) obj, fieldName, metricLabelValue);
                break;
            case BOOLEAN:
                setFiledValue(obj, fieldName,Boolean.valueOf(metricLabelValue));
                break;
            default:
                break;
        }
    }

    private static void toCopyModificationRespTime(SpanObject span, String fieldName, String metricLabelValue) {
        BigDecimal metricLabelValueBigDecimal = new BigDecimal(metricLabelValue);
        if (metricLabelValueBigDecimal.compareTo(new BigDecimal(0)) <= 0) {
            return;
        }
        long startTime = span.getStartTime();
        setFiledValue(span, fieldName, startTime + metricLabelValueBigDecimal.longValue());
    }

    public static void addCashSegmentObjectMap(SegmentObject segmentObject, int copy, String labelMetricValue) {
        copySegmentObjectMap.put(segmentObject, copy);
        labelSegmentObjectMap.put(segmentObject, labelMetricValue);

    }

    public static void removeCashSegmentObjectMap(SegmentObject segmentObject) {
        copySegmentObjectMap.remove(segmentObject);
        labelSegmentObjectMap.remove(segmentObject);

    }

    public static int getCopy(SegmentObject segmentObject) {
        Integer copy = copySegmentObjectMap.get(segmentObject);
        return copy == null? NO_COPY : copy;
    }

    public static String getLabelMetricValue(SegmentObject segmentObject) {
        return labelSegmentObjectMap.get(segmentObject);
    }

    private static int getLabelMetricValue(List<SpanObject> spansList, Map<String, Object> metricValueMap) {
        SpanObject targetSpan = getTargetSpan(spansList);
        if (targetSpan == null) {
            return NO_COPY;
        }
        // 1.判断copy字段值
        List<KeyStringValuePair> tagsList = targetSpan.getTagsList();
        KeyStringValuePair copyPair = getPair(tagsList, COPY_FILED_NAME);
        KeyStringValuePair labelValuesPair = getPair(tagsList, LABEL_METRIC_VALUE_FILE_NAME);
        if(!isNeedCopy(copyPair, labelValuesPair)) {
            return NO_COPY;
        }

        // 2.解析指标配置值
        Map<String, Object> metricValueToMap = parseLabelMetricValueToMap(labelValuesPair.getValue());
        metricValueMap.putAll(metricValueToMap);
        return TO_COPY;
    }

    private static KeyStringValuePair getPair(List<KeyStringValuePair> tagsList, String filedName) {
        for (KeyStringValuePair pair : tagsList) {
            if (filedName.equals(pair.getKey())) {
                return pair;
            }
        }
        return null;
    }

    private static SpanObject getTargetSpan(List<SpanObject> spansList) {
        SpanObject span0 = null;
        for (SpanObject span : spansList) {
            int spanId = span.getSpanId();
            if (spanId == 0) {
                span0 = span;
                break;
            }
        }
        if (span0 == null) {
            return null;
        }
        List<KeyStringValuePair> tagsList = span0.getTagsList();
        if (tagsList.size() <= 1) {
            return null;
        }
        return span0;
    }

    private static boolean isNeedCopy(KeyStringValuePair copyPair, KeyStringValuePair labelValuesPair) {
        // 1.有copy和labelMetricValue字段
        boolean isNotCopy = copyPair == null || labelValuesPair == null || !COPY_FILED_NAME.equals(copyPair.getKey()) || !LABEL_METRIC_VALUE_FILE_NAME.equals(labelValuesPair.getKey());
        if (isNotCopy) {
            return false;
        }
        // 2.是否需要复制
        return String.valueOf(TO_COPY).equals(copyPair.getValue());
    }
}
