/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.redis.complement;

import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.apache.skywalking.apm.network.language.agent.v3.SpanLayer;
import org.apache.skywalking.apm.network.language.agent.v3.SpanObject;
import org.apache.skywalking.apm.network.language.agent.v3.SpanType;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * segment树
 *
 * @author hefan
 * @since 2021-06-21
 */
public class SegmentNode {

    private SegmentObject.Builder segment;

    private final List<SegmentNode> childNode;

    private SpanObject.Builder entrySpan;

    private Map<String, SpanObject.Builder> spans;

    private Set<SpanObject> changedSpans;

    private int exitSpanCount = 0;

    List<SegmentNode> getChildNode() {
        return childNode;
    }

    public SegmentObject.Builder getSegment() {
        return segment;
    }

    Map<String, SpanObject.Builder> getSpans() {
        return spans;
    }

    SegmentNode(SegmentObject segment) {
        this.segment = segment.toBuilder();
        int spansCount = segment.getSpansCount();
        childNode = new ArrayList<>(spansCount);
        spans = new HashMap<>(spansCount);
        changedSpans = new HashSet<>();
        getSpanBuilders();
    }

    void addChild(SegmentNode node) {
        childNode.add(node);
    }

    void addChildren(List<SegmentNode> nodes) {
        if (nodes == null) {
            return;
        }
        childNode.addAll(nodes);
    }

    int getExitSpanCount() {
        return exitSpanCount;
    }

    SpanObject.Builder getEntrySpan() {
        return entrySpan;
    }

    void addChangedSpan(SpanObject span) {
        this.changedSpans.add(span);
    }

    int getChangedSpanSize(){
        return this.changedSpans.size();
    }

    private void getSpanBuilders() {
        segment.getSpansList().forEach(span -> {
            SpanObject.Builder spanBuilder = span.toBuilder();
            if (span.getSpanType().equals(SpanType.Entry)) {
                entrySpan = spanBuilder;
            } else if (span.getSpanType().equals(SpanType.Exit)){
                if (span.getSpanLayer().equals(SpanLayer.Cache)
                    || span.getSpanLayer().equals(SpanLayer.Database)
                    || span.getSpanLayer().equals(SpanLayer.MQ)) {
                    spanBuilder.setSkipAnalysis(true);
                    changedSpans.add(spanBuilder.build());
                } else {
                    ++exitSpanCount;
                    spans.put(String.valueOf(span.getSpanId()), spanBuilder);
                }
            }
        });
        segment.clearSpans();
    }

    SegmentObject buildSegment() {
        if (CollectionUtils.isNotEmpty(changedSpans)) {
            segment.addAllSpans(changedSpans);
        }
        if (CollectionUtils.isNotEmpty(spans)) {
            segment.addAllSpans(spans.values().stream().map(span -> span.setSkipAnalysis(true).build())
                .collect(Collectors.toSet()));
        }
        if (entrySpan.getStartTime() != 0) {
            segment.addSpans(entrySpan);
        }
        return segment.build();
    }
}
