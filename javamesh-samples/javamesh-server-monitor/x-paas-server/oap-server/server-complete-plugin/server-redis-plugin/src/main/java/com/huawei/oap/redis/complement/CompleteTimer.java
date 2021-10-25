/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.redis.complement;

import com.huawei.oap.redis.module.RedisOperationModule;
import com.huawei.oap.redis.service.IRedisService;

import com.google.protobuf.InvalidProtocolBufferException;

import io.lettuce.core.ScoredValue;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.network.common.v3.KeyStringValuePair;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentReference;
import org.apache.skywalking.apm.network.language.agent.v3.SpanObject;
import org.apache.skywalking.apm.network.language.agent.v3.SpanType;
import org.apache.skywalking.oap.server.analyzer.module.AnalyzerModule;
import org.apache.skywalking.oap.server.analyzer.provider.trace.parser.ISegmentParserService;
import org.apache.skywalking.oap.server.core.analysis.manual.segment.SegmentRecord;
import org.apache.skywalking.oap.server.core.storage.StorageModule;
import org.apache.skywalking.oap.server.core.storage.query.ITraceQueryDAO;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.server.grpc.CustomThreadFactory;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 补全定时器
 *
 * @author hefan
 * @since 2021-06-21
 */
@Slf4j
public enum CompleteTimer {
    /**
     * 实例
     */
    INSTANCE;

    private static final String ROOT_SEGMENT_KEY = "rootSegment";

    private static final String OTHERS_SEGMENT_KEY = "others";

    private static final String DEFAULT_PORT = ":80";

    private static final String PLACEHOLDER = "-";

    private static final String DOWNSTREAM_SERVICE = "downstreamService";

    private static final int TOTAL_RETRY_TIMES = 3;

    private static final String INCOMPLETE_TRACE_KEY = "INCOMPLETE_TRACE";

    private static final String HANDLER_MARK = "OAP_SEGMENT_HANDLER_MARK";

    private static final int HANDLER_MARK_EXPIRE = 60;

    private static final double ONE_MINUTE_MILLS = 60 * 1000L;

    public static final ScheduledExecutorService COMPLETE_EXECUTOR =
        new ScheduledThreadPoolExecutor(1, new CustomThreadFactory("Complete-scheduler"));

    public static final ScheduledExecutorService HANDLER_MARK_EXECUTOR =
        new ScheduledThreadPoolExecutor(1, new CustomThreadFactory("Handler-mark"));

    private IRedisService redisService;

    private ISegmentParserService iSegmentParserService;

    private ITraceQueryDAO traceQueryDao;

    private boolean isHandler = false;

    public void start(ModuleManager moduleManager) {
        this.redisService = moduleManager.find(RedisOperationModule.NAME).provider().getService(IRedisService.class);
        this.iSegmentParserService = moduleManager.find(AnalyzerModule.NAME).provider()
            .getService(ISegmentParserService.class);
        this.traceQueryDao = moduleManager.find(StorageModule.NAME).provider().getService(ITraceQueryDAO.class);

        HANDLER_MARK_EXECUTOR.scheduleAtFixedRate(this::getSegmentsHandleRight,
            2, 30, TimeUnit.SECONDS);
        COMPLETE_EXECUTOR.scheduleWithFixedDelay(this::completeSegments,
            5, 3, TimeUnit.SECONDS);
    }

    private void getSegmentsHandleRight() {
        if (isHandler) {
            if (!redisService.expire(HANDLER_MARK, HANDLER_MARK_EXPIRE)) {
                isHandler = false;
            }
            log.debug("Set key:{} expire:{}.", HANDLER_MARK, HANDLER_MARK_EXPIRE);
        } else {
            isHandler = redisService.setIfNotExist(HANDLER_MARK, StringUtils.EMPTY);
            if (!isHandler) {
                if (redisService.ttl(HANDLER_MARK) < 0) {
                    redisService.del(HANDLER_MARK);
                }
            }
            log.debug("Set key:{} successfully:{}", isHandler);
        }
    }

    private void completeSegments() {
        log.debug("Is complete handler:{}", isHandler);
        if (isHandler) {
            final long now = System.currentTimeMillis();
            List<ScoredValue<String>> scoredValues = redisService.zRangeWithScore(INCOMPLETE_TRACE_KEY, 0, 500);
            List<IncompletedTrace> incompletedTraces = new ArrayList<>();
            for (ScoredValue<String> scoredValue : scoredValues) {
                IncompletedTrace incompletedTrace = IncompletedTrace.parse(scoredValue.getValue(), scoredValue.getScore());
                if ( now - incompletedTrace.getUpdateTime() < ONE_MINUTE_MILLS) {
                    break;
                }
                incompletedTraces.add(incompletedTrace);
            }
            if (CollectionUtils.isNotEmpty(incompletedTraces)) {
                incompletedTraces.stream().parallel().forEach(this::complete);
            }
        }
        log.debug("scheduler completed!");
    }

    private void complete(IncompletedTrace incompletedTrace) {
        try {
            String traceId = incompletedTrace.getTraceId();
            List<SegmentRecord> segmentRecords = traceQueryDao.queryByTraceId(traceId);
            long retryTimes = incompletedTrace.increaseRetryTimes();
            if (CollectionUtils.isNotEmpty(segmentRecords)) {
                Set<SegmentObject> segments = recordToSegment(segmentRecords);
                if (!handleTraceSegments(segments) && retryTimes <= TOTAL_RETRY_TIMES) {
                    redisService.zAdd(INCOMPLETE_TRACE_KEY,
                        ScoredValue.fromNullable(incompletedTrace.getNewScore(), traceId));
                    log.debug("Complete trace id:[{}] failed! because segments are not completed. Add to retry",
                        traceId);
                } else {
                    redisService.zRemove(INCOMPLETE_TRACE_KEY, traceId);
                    log.debug("Delete trace id:[{}]. retry times:{}", traceId, retryTimes);
                }
            } else if (retryTimes <= TOTAL_RETRY_TIMES){
                redisService.zAdd(INCOMPLETE_TRACE_KEY,
                    ScoredValue.fromNullable(incompletedTrace.getNewScore(), traceId));
                log.debug("Complete trace id:[{}] failed! because query result has no segment. Add to retry",
                    traceId);
            } else {
                redisService.zRemove(INCOMPLETE_TRACE_KEY, traceId);
                log.debug("Delete trace id:{}. retry times:{},query by trace id has no segments", traceId, retryTimes);
            }
        } catch (IOException e) {
            log.error("", e);
        }
    }

    private boolean handleTraceSegments(Set<SegmentObject> segments) {
        if (CollectionUtils.isEmpty(segments)) {
            return false;
        }
        // 把segment convert to tree
        Map<String, Set<SegmentObject>> segmentMap = getSegmentMap(segments);
        Optional<SegmentNode> tree = buildTree(segmentMap);

        boolean isCompleted = tree.map(this::completeSegment).isPresent();
        if (isCompleted) {
            LinkedList<SegmentNode> queue = new LinkedList<>();
            queue.add(tree.get());
            while (CollectionUtils.isNotEmpty(queue)) {
                SegmentNode currentNode = queue.poll();
                if (Objects.requireNonNull(currentNode).getChangedSpanSize() != 0) {
                    // 发送到解析模块
                    iSegmentParserService.send(currentNode.buildSegment());
                }
                queue.addAll(currentNode.getChildNode());
            }
        }
        return isCompleted;
    }

    private Optional<SegmentNode> buildTree(Map<String, Set<SegmentObject>> segmentMap) {
        Set<SegmentObject> currentSegments = segmentMap.get(ROOT_SEGMENT_KEY);
        if (CollectionUtils.isEmpty(currentSegments)) {
            return Optional.empty();
        }
        LinkedList<SegmentNode> queue = new LinkedList<>();
        SegmentObject currentSegment = currentSegments.iterator().next();
        SegmentNode rootNode = new SegmentNode(currentSegment);
        queue.add(rootNode);

        SegmentNode currentNode;
        while ((currentNode = queue.poll()) != null) {
            Set<SegmentObject> childSegments = segmentMap.remove(
                currentNode.getSegment().getTraceSegmentId());
            if (CollectionUtils.isNotEmpty(childSegments)) {
                List<SegmentNode> segmentNodes = childSegments.stream()
                    .map(SegmentNode::new)
                    .peek(queue::add)
                    .collect(Collectors.toList());
                currentNode.addChildren(segmentNodes);
            } else if (currentNode.getExitSpanCount() != 0) {
                return Optional.empty();
            }
        }
        return Optional.of(rootNode);
    }

    private Map<String, Set<SegmentObject>> getSegmentMap(Set<SegmentObject> segments) {
        return segments.stream().collect(Collectors.groupingBy(segment -> {
            String traceSegmentId = segment.getTraceSegmentId();
            String traceId = segment.getTraceId();
            if (traceId.equals(traceSegmentId)) {
                return ROOT_SEGMENT_KEY;
            }
            for (SpanObject span : segment.getSpansList()) {
                if (span.getSpanType().equals(SpanType.Entry) || span.getSpanType().equals(SpanType.Local)) {
                    List<SegmentReference> refsList = span.getRefsList();
                    if (refsList.size() == 0) {
                        return ROOT_SEGMENT_KEY;
                    } else {
                        return refsList.get(0).getParentTraceSegmentId();
                    }
                }
            }
            return OTHERS_SEGMENT_KEY;
        }, Collectors.toSet()));
    }

    private boolean completeSegment(SegmentNode node) {
        // 根节点的EntrySpan设置为跳过解析，因为oap第一次接收到segment时已处理，防止重复处理导致指标数据不准确
        SpanObject.Builder entrySpan = node.getEntrySpan();
        entrySpan.setSkipAnalysis(true);
        node.addChangedSpan(entrySpan.build());
        entrySpan.clear();

        LinkedList<SegmentNode> queue = new LinkedList<>();
        queue.add(node);

        while (!CollectionUtils.isEmpty(queue)) {
            SegmentNode currentNode = queue.poll();
            Map<String, SpanObject.Builder> currentSpans = Objects.requireNonNull(currentNode).getSpans();
            String currentService = currentNode.getSegment().getService();
            String currentServiceInstance = currentNode.getSegment().getServiceInstance();
            if (currentNode.getChildNode().size() != currentNode.getExitSpanCount()) {
                return false;
            }
            currentNode.getChildNode().forEach(childNode -> {
                queue.add(childNode);
                SpanObject.Builder childNodeEntrySpan = childNode.getEntrySpan();
                List<SegmentReference> refsList = childNodeEntrySpan.getRefsList();
                List<SegmentReference> newRefs = refsList.stream().map(ref -> {
                    if (checkRefsIsNotCompleted(ref)) {
                        String parentSpanId = String.valueOf(ref.getParentSpanId());
                        SpanObject.Builder parentSpan = currentSpans.remove(parentSpanId);

                        parentSpan.addTags(KeyStringValuePair.newBuilder().setKey(DOWNSTREAM_SERVICE).setValue(childNode.getSegment().getService()));
                        currentNode.addChangedSpan(parentSpan.build());

                        if (StringUtils.isBlank(parentSpan.getPeer())) {
                            parentSpan.setPeer(childNode.getSegment().getService() + DEFAULT_PORT);
                        }
                        return ref.toBuilder()
                            .setParentService(currentService)
                            .setParentServiceInstance(currentServiceInstance)
                            .setParentEndpoint(parentSpan.getOperationName())
                            .build();
                    } else {
                        return ref;
                    }
                }).collect(Collectors.toList());
                childNode.addChangedSpan(childNodeEntrySpan.clearRefs().addAllRefs(newRefs).build());
                childNodeEntrySpan.clear();
            });
        }
        return true;
    }

    private Set<SegmentObject> recordToSegment(List<SegmentRecord> segmentRecords) {
        return segmentRecords.stream()
            .map(SegmentRecord::getDataBinary)
            .filter(Objects::nonNull)
            .map(segment -> {
                try {
                    return SegmentObject.parseFrom(segment);
                } catch (InvalidProtocolBufferException e) {
                    log.error("Parse segment failed!", e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private boolean checkRefsIsNotCompleted(SegmentReference ref) {
        return PLACEHOLDER.equals(ref.getParentService())
            || PLACEHOLDER.equals(ref.getParentEndpoint())
            || PLACEHOLDER.equals(ref.getParentServiceInstance());
    }
}
