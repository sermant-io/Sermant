/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.service;

import com.huawei.flowre.flowreplay.config.Const;
import com.huawei.flowre.flowreplay.datasource.EsDataSource;
import com.huawei.flowre.flowreplay.datasource.EsIndicesInit;
import com.huawei.flowre.flowreplay.domain.Field;
import com.huawei.flowre.flowreplay.domain.FieldCompare;
import com.huawei.flowre.flowreplay.domain.IgnoreFieldEntity;
import com.huawei.flowre.flowreplay.domain.ReplayResultEntity;
import com.huawei.flowre.flowreplay.domain.message.ReplayResultMessage;
import com.huawei.flowre.flowreplay.utils.WorkerStatusUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 对回放结果进行处理
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-28
 */
@Service
@Order(1)
public class ResultCompareService implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultCompareService.class);

    @Autowired
    EsDataSource esDataSource;

    @Autowired
    EsIndicesInit esIndicesInit;

    @Autowired
    Environment environment;

    @Autowired
    Properties kafkaConsumerProperties;

    /**
     * consumer 一次拉取的时长
     */
    @Value("${kafka.consumer.duration}")
    long consumerDuration;

    /**
     * 在没有回放结果数据时的等待时间
     */
    @Value("${kafka.consumer.wait}")
    long waitTime;

    /**
     * 回放线程池的最大线程数
     */
    @Value("${replay.thread.pool.max}")
    int maxPoolSize;

    /**
     * 回放线程池的核心线程数
     */
    @Value("${compare.thread.pool.core}")
    int coreThreadSize;

    /**
     * 回放线程池阻塞队列大小
     */
    @Value("${replay.thread.pool.block}")
    int blockQueueSize;

    /**
     * 回放线程池线程存活时间
     */
    @Value("${replay.thread.pool.alive}")
    long aliveTime;

    /**
     * 回放结果比对
     *
     * @param replayResultMessage 从kafka拉取的回放结果
     */
    public void compare(ReplayResultMessage replayResultMessage) {
        // 初始化忽略字段
        // methodName = invocation.getAttachments().get(Const.FIELD_INTERFACE) + '.' + invocation.getMethodName()
        initIgnoreFields(replayResultMessage.getMethodName(), replayResultMessage.getResponseBody());

        // 结果比对
        ReplayResultEntity replayResultEntity;
        try {
            JSONObject record = JSON.parseObject(replayResultMessage.getResponseBody());
            JSONObject replay = JSON.parseObject(replayResultMessage.getReplayResult());
            replayResultEntity = compareResult(record, replay);
        } catch (JSONException jsonException) {
            replayResultEntity = compareResult(replayResultMessage.getResponseBody(),
                replayResultMessage.getReplayResult());
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        replayResultEntity.setTraceId(replayResultMessage.getTraceId());
        replayResultEntity.setMethod(replayResultMessage.getMethodName());
        replayResultEntity.setRecordTime(simpleDateFormat.format(replayResultMessage.getRecordTime()));
        replayResultEntity.setReplayTime(simpleDateFormat.format(replayResultMessage.getReplayTime()));
        replayResultEntity.setResponseTime(replayResultMessage.getResponseTime());
        replayResultEntity.setStatusCode(replayResultMessage.getStatusCode());

        // 比对结果放入Es
        try {
            synchronized (ResultCompareService.class) {
                if (!esDataSource.checkIndexExistence(Const.REPLAY_RESULT_INDEX_PREFIX
                    + replayResultMessage.getReplayJobId())) {
                    esIndicesInit.replayResult(Const.REPLAY_RESULT_INDEX_PREFIX + replayResultMessage.getReplayJobId());
                }
            }
            esDataSource.addData(Const.REPLAY_RESULT_INDEX_PREFIX + replayResultMessage.getReplayJobId(),
                replayResultEntity);
        } catch (IOException ioException) {
            LOGGER.error("Add replay result to es error , {}", ioException.getMessage());
        }
    }

    /**
     * 对录制和回放结果进行结果比对
     *
     * @param record 录制结果
     * @param replay 回放结果
     * @return 返回一个结果的详细比对结果
     */
    private ReplayResultEntity compareResult(JSONObject record, JSONObject replay) {
        ReplayResultEntity replayResultEntity = new ReplayResultEntity();
        replayResultEntity.setCompareResult(true);
        List<FieldCompare> filedCompare = new ArrayList<>();
        for (String str : record.keySet()) {
            FieldCompare compare = new FieldCompare();

            // 初始化忽略状态为false
            compare.setName(str);
            compare.setIgnore(false);
            compare.setRecord(record.get(str).toString());
            compare.setReplay(replay.get(str) == null ? Const.NULL_STRING : replay.get(str).toString());
            compare.setCompare(record.get(str).equals(replay.get(str)));
            if (!record.get(str).equals(replay.get(str))) {
                replayResultEntity.setCompareResult(false);
            }
            filedCompare.add(compare);
        }
        replayResultEntity.setFieldCompare(filedCompare);
        return replayResultEntity;
    }

    /**
     * 对录制和回放结果进行结果比对
     *
     * @param record 录制结果
     * @param replay 回放结果
     * @return 返回一个结果的详细比对结果
     */
    private ReplayResultEntity compareResult(String record, String replay) {
        FieldCompare compare = new FieldCompare();
        compare.setName("result");
        compare.setIgnore(false);
        compare.setRecord(record);
        compare.setReplay(replay);
        compare.setCompare(record.equals(replay));
        List<FieldCompare> filedCompare = new ArrayList<>();
        ReplayResultEntity replayResultEntity = new ReplayResultEntity();
        filedCompare.add(compare);
        replayResultEntity.setFieldCompare(filedCompare);
        replayResultEntity.setCompareResult(record.equals(replay));
        return replayResultEntity;
    }

    /**
     * 初始化忽略字段
     *
     * @param method 需要初始化的接口名
     * @param record 录制数据
     */
    private void initIgnoreFields(String method, String record) {
        try {
            if (!esDataSource.checkIndexExistence(Const.IGNORE_FIELDS_INDEX)) {
                esIndicesInit.fieldsIgnore();
            }
            String docId = esDataSource.getDocId(Const.IGNORE_FIELDS_INDEX,
                Const.METHOD_KEYWORD, method);
            if (Const.BLANK.equals(docId)) {
                IgnoreFieldEntity ignoreFieldEntity = new IgnoreFieldEntity();
                ignoreFieldEntity.setMethod(method);
                List<Field> fields = new ArrayList<>();
                try {
                    JSONObject jsonRecord = JSON.parseObject(record);
                    for (String str : jsonRecord.keySet()) {
                        Field field = new Field();
                        field.setName(str);
                        field.setIgnore(false);
                        fields.add(field);
                    }
                } catch (JSONException jsonException) {
                    Field field = new Field();
                    field.setName("result");
                    field.setIgnore(false);
                    fields.add(field);
                }
                ignoreFieldEntity.setFields(fields);
                esDataSource.addData(Const.IGNORE_FIELDS_INDEX, ignoreFieldEntity);
            }
        } catch (IOException ioException) {
            LOGGER.error("Init ignore fields to es error , {}", ioException.getMessage());
        }
    }

    @Override
    public void run(ApplicationArguments args) throws UnknownHostException {
        LOGGER.info("Start result compare service.");
        InetAddress address = InetAddress.getLocalHost();
        String replayResultTopic = Const.REPLAY_RESULT_TOPIC + address.getHostAddress() + Const.UNDERLINE
            + environment.getProperty(Const.SERVER_PORT);
        List<String> topicList = Arrays.asList(replayResultTopic.split(Const.COMMA));
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProperties);
        kafkaConsumer.subscribe(topicList);
        ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(blockQueueSize);
        ThreadPoolExecutor compareThreadPool = new ThreadPoolExecutor(coreThreadSize, maxPoolSize,
            aliveTime, TimeUnit.SECONDS, workQueue);
        new Thread(() -> {
            while (true) {
                try {
                    ConsumerRecords<String, String> consumerRecords
                        = kafkaConsumer.poll(Duration.ofMillis(consumerDuration));
                    if (consumerRecords.isEmpty()) {
                        WorkerStatusUtil.getInstance().setComparing(false);
                        Thread.sleep(waitTime);
                        continue;
                    }
                    WorkerStatusUtil.getInstance().setComparing(true);
                    while (compareThreadPool.getQueue().size() >= blockQueueSize) {
                        // 阻塞任务下发
                        Thread.sleep(waitTime);
                    }
                    compareThreadPool.execute(new Thread(() -> {
                        for (ConsumerRecord<String, String> record : consumerRecords) {
                            ReplayResultMessage replayResultMessage = JSON.parseObject(record.value(),
                                ReplayResultMessage.class);
                            compare(replayResultMessage);
                        }
                    }));
                } catch (InterruptedException interruptedException) {
                    LOGGER.error("Stop to wait error:{}", interruptedException.getMessage());
                }
            }
        }).start();
    }
}
