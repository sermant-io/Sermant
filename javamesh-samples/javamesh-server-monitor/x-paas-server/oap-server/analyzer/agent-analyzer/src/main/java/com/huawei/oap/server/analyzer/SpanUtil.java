/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.server.analyzer;

import org.apache.skywalking.apm.network.common.v3.KeyStringValuePair;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentReference;
import org.apache.skywalking.apm.network.language.agent.v3.SpanObject;
import org.apache.skywalking.apm.network.language.agent.v3.SpanType;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * span的工具类
 *
 * @author hefan
 * @since 2021-04-30
 */
public class SpanUtil {
    /**
     * zipkin中traceId长度
     */
    public static final int TRACE_ID_LENGTH = 32;

    /**
     * 下游服务器名键值
     */
    public static final String DOWNSTREAM_SERVICE = "downstreamService";

    public static final String PLACEHOLDER = "-";


    /**
     * 移除childService标签
     *
     * @param segment  链路信息
     * @return segment 去除DownstreamService标签的链路信息
     */
    public static SegmentObject removeDownstreamServiceTag(SegmentObject segment) {
        // 将segment转换成可操作的对象
        SegmentObject.Builder builder = segment.toBuilder();

        // 获取span列表
        List<SpanObject> spansList = segment.getSpansList();
        if (CollectionUtils.isNotEmpty(spansList)) {
            for (int i = 0; i < spansList.size(); i++) {
                SpanObject span = spansList.get(i);
                List<KeyStringValuePair> tagsList = span.getTagsList();
                SpanObject.Builder spanBuilder = span.toBuilder();

                // 将tag清空
                spanBuilder = spanBuilder.clearTags();
                for (KeyStringValuePair pair : tagsList) {
                    // 非指定键的tag重新添加
                    if (!DOWNSTREAM_SERVICE.equals(pair.getKey())) {
                        spanBuilder.addTags(pair);
                    }
                }

                // 替换指定索引的span
                builder.setSpans(i, spanBuilder);
            }
        }
        return builder.build();
    }

    public static Optional<SegmentObject> isSkip(SegmentObject segment) {
        List<SpanObject> spansList = segment.getSpansList();
        if (spansList.size() == 1) {
            if (spansList.get(0).getSkipAnalysis()) {
                return Optional.empty();
            }
        }
        for (SpanObject span : spansList) {
            if (span.getSpanType().equals(SpanType.Entry)) {
                if (span.getSkipAnalysis()) {
                    return Optional.empty();
                } else {
                    for (SegmentReference ref : span.getRefsList()) {
                        if (ref.getParentService().equals(PLACEHOLDER)) {
                            return Optional.empty();
                        }
                    }
                }
            }
        }
        return Optional.of(segment);
    }
}
