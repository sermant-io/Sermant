/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.service;

import com.huawei.flowre.flowreplay.config.Const;
import com.huawei.flowre.flowreplay.config.FlowReplayConfig;
import com.huawei.flowre.flowreplay.datasource.EsDataSource;
import com.huawei.flowre.flowreplay.datasource.EsIndicesInit;
import com.huawei.flowre.flowreplay.domain.FlowReplayMetric;
import com.huawei.flowre.flowreplay.invocation.DubboReplayInvocationImpl;
import com.huawei.flowre.flowreplay.invocation.HttpReplayInvocationImpl;
import com.huawei.flowre.flowreplay.invocation.InvokeThread;
import com.huawei.flowre.flowreplay.utils.RpsCalculateUtil;
import com.huawei.flowre.flowreplay.utils.WorkerStatusUtil;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
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
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 回放主体逻辑，根据不同的回放类型进行不同的调用
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-03-19
 */
@Service
@Order(2)
public class FlowReplayService implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowReplayService.class);

    /**
     * 时间戳换算
     */
    private static final long THOUSAND = 1000L;

    @Autowired
    Properties kafkaConsumerProperties;

    @Autowired
    KafkaProducer<String, String> kafkaProducer;

    @Autowired
    HttpReplayInvocationImpl httpReplayInvocation;

    @Autowired
    DubboReplayInvocationImpl dubboReplayInvocation;

    @Autowired
    EsDataSource esDataSource;

    @Autowired
    EsIndicesInit esIndicesInit;

    @Autowired
    Environment environment;

    /**
     * 消费者消费时间
     */
    @Value("${kafka.consumer.duration}")
    long consumerDuration;

    /**
     * 未消费到数据的等待时间
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
    @Value("${replay.thread.pool.core}")
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
     * 回放的默认rps
     */
    @Value("${replay.default.rps}")
    int defaultRps;

    /**
     * 初始化时的等待时间
     */
    @Value("${replay.init.wait.time}")
    int initWaitTime;

    /**
     * 启动控制rps的线程
     *
     * @param replayThreadPool 回放线程池
     * @param lastRps          上一秒的rps
     */
    public void rpsControl(ThreadPoolExecutor replayThreadPool, AtomicInteger lastRps) {
        new Thread(() -> {
            // 用于监控和控制Rps的线程
            int targetRps;
            int maxThreadCount = Integer.MAX_VALUE;
            while (true) {
                try {
                    if (replayThreadPool.getActiveCount() <= 1) {
                        // 空闲时轮训间隔变长 运行时测试单线程rps
                        Thread.sleep(initWaitTime);
                    } else {
                        Thread.sleep(waitTime);
                    }
                } catch (InterruptedException interruptedException) {
                    LOGGER.error("Stop to change thread error:{}", interruptedException.getMessage());
                }
                if (FlowReplayConfig.getInstance().getTestType().equals("baselineTest")) {
                    targetRps = FlowReplayConfig.getInstance().getBaseLineThroughPut();
                    maxThreadCount = FlowReplayConfig.getInstance().getMaxThreadCount();
                } else {
                    targetRps = defaultRps;
                }
                LOGGER.info("Target RPS:{}", targetRps);
                LOGGER.info("Current Rps:{}", lastRps.get());
                LOGGER.info("Pool size:{}", replayThreadPool.getPoolSize());
                LOGGER.info("Active count:{}", replayThreadPool.getActiveCount());
                LOGGER.info("Blocking queue:{}", replayThreadPool.getQueue().size());
                if (lastRps.get() < targetRps && replayThreadPool.getQueue().size() > 0
                    && replayThreadPool.getCorePoolSize() < Math.min(replayThreadPool.getMaximumPoolSize(),
                    maxThreadCount)) {
                    int targetThread = targetRps / lastRps.get();

                    // 如果计算出的线程大于最大线程 直接设置最大线程
                    if (targetThread > replayThreadPool.getMaximumPoolSize()) {
                        targetThread = Math.min(replayThreadPool.getMaximumPoolSize(), maxThreadCount);
                    }
                    replayThreadPool.setCorePoolSize(targetThread);
                    LOGGER.info("Changing core poll size , current size:{}", replayThreadPool.getCorePoolSize());
                }

                // 重置线程
                if (replayThreadPool.getQueue().isEmpty() && replayThreadPool.getActiveCount() == 0
                    && lastRps.get() != 0) {
                    WorkerStatusUtil.getInstance().setReplaying(false);
                    replayThreadPool.setCorePoolSize(coreThreadSize);
                    lastRps.set(0);
                }
            }
        }).start();
    }

    /**
     * 启动回放线程
     *
     * @param replayThreadPool 回放线程池
     * @param currentRps       当前rps
     * @param currentSecond    当前时间
     * @param lastRps          上一秒的rps
     */
    public void taskDeliver(ThreadPoolExecutor replayThreadPool, AtomicInteger currentRps, AtomicLong currentSecond,
                            AtomicInteger lastRps) {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            LOGGER.error("Start task deliver error:{}", e.getMessage());
        }
        String workerName = Const.REPLAY_DATA_TOPIC + address.getHostAddress() + Const.UNDERLINE
            + environment.getProperty(Const.SERVER_PORT);
        String replayResultTopic = Const.REPLAY_RESULT_TOPIC + address.getHostAddress() + Const.UNDERLINE
            + environment.getProperty(Const.SERVER_PORT);
        List<String> topicList = Arrays.asList(workerName.split(Const.COMMA));
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProperties);
        kafkaConsumer.subscribe(topicList);

        // 包含一个同步方法 用于计算rps
        RpsCalculateUtil rpsCalculateUtil = new RpsCalculateUtil(lastRps, currentRps, currentSecond);

        // 用于执行回放任务的线程
        new Thread(() -> {
            while (true) {
                if (replayThreadPool.getQueue().size() >= blockQueueSize) {
                    continue;
                }
                ConsumerRecords<String, String> consumerRecords
                    = kafkaConsumer.poll(Duration.ofMillis(consumerDuration));
                if (consumerRecords.isEmpty()) {
                    try {
                        Thread.sleep(waitTime);
                        continue;
                    } catch (InterruptedException interruptedException) {
                        LOGGER.error("Stop to wait replay data error:{}", interruptedException.getMessage());
                    }
                    LOGGER.info("Consumer records(Replay Data) is empty!");
                }
                InvokeThread invokeThread = InvokeThread.builder()
                    .records(consumerRecords)
                    .kafkaProducer(kafkaProducer)
                    .rpsCalculateUtil(rpsCalculateUtil)
                    .replayResultTopic(replayResultTopic)
                    .httpReplayInvocation(httpReplayInvocation)
                    .dubboReplayInvocation(dubboReplayInvocation)
                    .build();
                replayThreadPool.execute(invokeThread);
            }
        }).start();
    }

    /**
     * 上报回放节点信息
     *
     * @param lastRps          RPS
     * @param replayThreadPool 回放线程池
     */
    public void sendReplayMetric(AtomicInteger lastRps, ThreadPoolExecutor replayThreadPool) {
        new Thread(() -> {
            while (true) {
                try {
                    if (replayThreadPool.getActiveCount() == 0) {
                        Thread.sleep(initWaitTime);
                    } else {
                        // rps是前一秒的rps 所以这里也需要修改时间为前一秒
                        long lastTime = new Date().getTime() / THOUSAND - 1;
                        FlowReplayMetric flowReplayMetric =
                            new FlowReplayMetric(FlowReplayConfig.getInstance().getReplayWorkerName(),
                                FlowReplayConfig.getInstance().getReplayJobId(),
                                lastTime * THOUSAND, lastRps.get(), replayThreadPool.getActiveCount());
                        esDataSource.addData(Const.REPLAY_METRIC_INDEX, flowReplayMetric);
                    }
                } catch (InterruptedException interruptedException) {
                    LOGGER.error("Stop to send replay metric error:{}", interruptedException.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!esDataSource.checkIndexExistence(Const.REPLAY_METRIC_INDEX)) {
                esIndicesInit.replayMetric();
            }
        } catch (IOException ioException) {
            LOGGER.error("Init replay_metric index error:{}", ioException.getMessage());
        }
        AtomicInteger lastRps = new AtomicInteger(0);
        AtomicInteger currentRps = new AtomicInteger(0);
        AtomicLong currentSecond = new AtomicLong(new Date().getTime() / THOUSAND);
        ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(blockQueueSize);
        ThreadPoolExecutor replayThreadPool = new ThreadPoolExecutor(coreThreadSize, maxPoolSize,
            aliveTime, TimeUnit.SECONDS, workQueue);
        rpsControl(replayThreadPool, lastRps);
        taskDeliver(replayThreadPool, currentRps, currentSecond, lastRps);
        sendReplayMetric(lastRps, replayThreadPool);
    }
}
