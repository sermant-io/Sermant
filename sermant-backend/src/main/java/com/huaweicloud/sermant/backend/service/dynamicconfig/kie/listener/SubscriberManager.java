/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.backend.service.dynamicconfig.kie.listener;

import com.huaweicloud.sermant.backend.service.dynamicconfig.kie.client.ClientUrlManager;
import com.huaweicloud.sermant.backend.service.dynamicconfig.kie.client.kie.KieClient;
import com.huaweicloud.sermant.backend.service.dynamicconfig.kie.client.kie.KieListenerWrapper;
import com.huaweicloud.sermant.backend.service.dynamicconfig.kie.client.kie.KieRequest;
import com.huaweicloud.sermant.backend.service.dynamicconfig.kie.client.kie.KieResponse;
import com.huaweicloud.sermant.backend.service.dynamicconfig.kie.client.kie.KieSubscriber;
import com.huaweicloud.sermant.backend.service.dynamicconfig.service.ConfigurationListener;
import com.huaweicloud.sermant.backend.service.dynamicconfig.utils.LabelGroupUtils;
import com.huaweicloud.sermant.backend.util.BackendThreadFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 监听器管理
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class SubscriberManager {
    /**
     * 基础时间
     */
    public static final long BASE_MS = 3000L;

    /**
     * 最大线程数
     */
    public static final int MAX_THREAD_SIZE = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriberManager.class);

    /**
     * 线程数
     */
    private static final int THREAD_SIZE = 5;

    /**
     * 秒转换为毫秒
     */
    private static final int SECONDS_UNIT = 1000;

    /**
     * 定时请求间隔
     */
    private static final int SCHEDULE_REQUEST_INTERVAL_MS = 5000;

    /**
     * 等待时间
     */
    private static final String WAIT = "20";

    /**
     * 长连接拉取间隔
     */
    private static final long LONG_CONNECTION_REQUEST_INTERVAL_MS = 2000L;

    /**
     * 当前长连接请求数 要求最大连接数必须小于 MAX_THREAD_SIZE
     */
    private final AtomicInteger curLongConnectionRequestCount = new AtomicInteger(0);

    /**
     * map 监听键, 监听该键的监听器列表
     */
    private final Map<KieSubscriber, List<KieListenerWrapper>> listenerMap = new ConcurrentHashMap<>();

    /**
     * kie客户端
     */
    private final KieClient kieClient;

    /**
     * 订阅执行器 最大支持MAX_THREAD_SIZE个任务 由于是长连接请求，必然会占用线程，因此这里不考虑将任务存在队列中
     */
    private final ThreadPoolExecutor longRequestExecutor = new ThreadPoolExecutor(THREAD_SIZE, MAX_THREAD_SIZE,
        0, TimeUnit.MILLISECONDS, new SynchronousQueue<>(),
        new BackendThreadFactory("kie-subscribe-long-task"));

    /**
     * 快速返回的请求
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 指定KIE地址
     *
     * @param urls KIE地址列表
     */
    public SubscriberManager(String urls) {
        kieClient = new KieClient(new ClientUrlManager(urls));
    }

    /**
     * 添加组监听
     *
     * @param group    标签组
     * @param listener 监听器
     * @return 是否添加成功
     */
    public boolean addGroupListener(String group, ConfigurationListener listener) {
        try {
            return subscribe(new KieRequest().setLabelCondition(LabelGroupUtils.getLabelCondition(group)).setWait(WAIT),
                listener);
        } catch (Exception ex) {
            LOGGER.warn(String.format(Locale.ENGLISH, "Add group listener failed, %s", ex.getMessage()));
            return false;
        }
    }

    /**
     * 移除组监听
     *
     * @param group    标签组
     * @param listener 监听器
     * @return 是否添加成功
     */
    public boolean removeGroupListener(String group, ConfigurationListener listener) {
        try {
            return unSubscribe(
                new KieRequest().setLabelCondition(LabelGroupUtils.getLabelCondition(group)).setWait(WAIT), listener);
        } catch (Exception ex) {
            LOGGER.warn(String.format(Locale.ENGLISH, "Removed group listener failed, %s", ex.getMessage()));
            return false;
        }
    }

    /**
     * 发布配置
     *
     * @param key     配置键
     * @param group   分组
     * @param content 配置内容
     * @return 是否发布成功
     */
    public boolean publishConfig(String key, String group, String content) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        final Map<String, String> labels = LabelGroupUtils.resolveGroupLabels(group);
        return kieClient.publishConfig(key, labels, content, true);
    }

    /**
     * 注册监听器
     *
     * @param kieRequest            请求
     * @param configurationListener 监听器
     * @return 订阅成功返回true
     */
    public boolean subscribe(KieRequest kieRequest, ConfigurationListener configurationListener) {
        final KieSubscriber kieSubscriber = new KieSubscriber(kieRequest);
        Task task;
        KieListenerWrapper kieListenerWrapper = new KieListenerWrapper(kieRequest.getLabelCondition(),
            configurationListener, new KvDataHolder());
        if (!kieSubscriber.isLongConnectionRequest()) {
            task = new ShortTimerTask(kieSubscriber, kieListenerWrapper);
        } else {
            if (exceedMaxLongRequestCount()) {
                LOGGER.warn(String.format(Locale.ENGLISH,
                    "Exceeded max long connection request subscribers, the max number is %s, it will be discarded!",
                    curLongConnectionRequestCount.get()));
                return false;
            }
            buildRequestConfig(kieRequest);
            task = new LoopPullTask(kieSubscriber, kieListenerWrapper);
            firstRequest(kieRequest, kieListenerWrapper);
        }
        List<KieListenerWrapper> configurationListeners = listenerMap.get(kieSubscriber);
        if (configurationListeners == null) {
            configurationListeners = new ArrayList<>();
        }
        kieListenerWrapper.setTask(task);
        configurationListeners.add(kieListenerWrapper);
        listenerMap.put(kieSubscriber, configurationListeners);
        executeTask(task);
        return true;
    }

    /**
     * 是否超过最大限制长连接任务数
     *
     * @return boolean
     */
    private boolean exceedMaxLongRequestCount() {
        return curLongConnectionRequestCount.incrementAndGet() > MAX_THREAD_SIZE;
    }

    /**
     * 针对长请求的场景需要做第一次拉取，获取已有的数据
     *
     * @param kieRequest         请求体
     * @param kieListenerWrapper 监听器
     */
    public void firstRequest(KieRequest kieRequest, KieListenerWrapper kieListenerWrapper) {
        try {
            final KieRequest cloneRequest = new KieRequest().setRevision(kieRequest.getRevision())
                .setLabelCondition(kieRequest.getLabelCondition());
            final KieResponse kieResponse = kieClient.queryConfigurations(cloneRequest);
            if (kieResponse != null && kieResponse.isChanged()) {
                tryPublishEvent(kieResponse, kieListenerWrapper);
                kieRequest.setRevision(kieResponse.getRevision());
            }
        } catch (Exception ex) {
            LOGGER.warn(String.format(Locale.ENGLISH, "Pull the first request failed! %s", ex.getMessage()));
        }
    }

    /**
     * 取消订阅
     *
     * @param kieRequest            请求体
     * @param configurationListener 监听器
     * @return 取消订阅成功
     */
    public boolean unSubscribe(KieRequest kieRequest, ConfigurationListener configurationListener) {
        for (Map.Entry<KieSubscriber, List<KieListenerWrapper>> next : listenerMap.entrySet()) {
            if (!next.getKey().getKieRequest().equals(kieRequest)) {
                continue;
            }
            final Iterator<KieListenerWrapper> iterator = next.getValue().iterator();
            while (iterator.hasNext()) {
                final KieListenerWrapper listenerWrapper = iterator.next();
                if (listenerWrapper.getConfigurationListener() == configurationListener) {
                    iterator.remove();
                    listenerWrapper.getTask().stop();
                    LOGGER.info(String.format(Locale.ENGLISH, "%s has been stopped!",
                        configurationListener.getClass().getName()));
                    return true;
                }
            }
        }
        LOGGER.warn(String.format(Locale.ENGLISH, "The subscriber of group %s not found!",
            kieRequest.getLabelCondition()));
        return false;
    }

    private void buildRequestConfig(KieRequest kieRequest) {
        int wait = (Integer.parseInt(kieRequest.getWait()) + 1) * SECONDS_UNIT;
        if (kieRequest.getRequestConfig() == null) {
            kieRequest.setRequestConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(wait)
                .setConnectTimeout(wait)
                .setSocketTimeout(wait)
                .build());
        }
    }

    private void executeTask(final Task task) {
        try {
            if (task.isLongConnectionRequest()) {
                longRequestExecutor.execute(new TaskRunnable(task));
            } else {
                if (scheduledExecutorService == null) {
                    synchronized (SubscriberManager.class) {
                        scheduledExecutorService = new ScheduledThreadPoolExecutor(THREAD_SIZE,
                            new BackendThreadFactory("kie-subscribe-task"));
                    }
                }
                scheduledExecutorService.scheduleAtFixedRate(
                    new TaskRunnable(task), 0, SCHEDULE_REQUEST_INTERVAL_MS, TimeUnit.MILLISECONDS);
            }
        } catch (RejectedExecutionException ex) {
            LOGGER.warn(String.format(Locale.ENGLISH, "Rejected the task %s, %s",
                task.getClass(), ex.getMessage()));
        }
    }

    private void tryPublishEvent(KieResponse kieResponse, KieListenerWrapper kieListenerWrapper) {
        final KvDataHolder kvDataHolder = kieListenerWrapper.getKvDataHolder();
        final KvDataHolder.EventDataHolder eventDataHolder = kvDataHolder.analyzeLatestData(kieResponse);
        if (eventDataHolder.isChanged()) {
            kieListenerWrapper.notifyListener(eventDataHolder);
        }
    }

    /**
     * 请求任务
     *
     * @since 2021-11-17
     */
    static class TaskRunnable implements Runnable {
        private final Task task;

        TaskRunnable(Task task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                task.execute();
            } catch (Exception ex) {
                LOGGER.warn(String.format(Locale.ENGLISH, "The error occurred when execute task , %s",
                    ex.getMessage()));
            }
        }
    }

    /**
     * 任务定义
     *
     * @since 2021-11-17
     */
    public interface Task {
        /**
         * 任务执行
         */
        void execute();

        /**
         * 是否为长时间保持连接的请求
         *
         * @return boolean
         */
        boolean isLongConnectionRequest();

        /**
         * 任务终止
         */
        void stop();
    }

    /**
     * 抽象任务
     *
     * @since 2021-11-17
     */
    abstract static class AbstractTask implements Task {
        protected volatile boolean isContinue = true;

        @Override
        public void execute() {
            if (!isContinue) {
                return;
            }
            executeInner();
        }

        @Override
        public void stop() {
            isContinue = false;
        }

        /**
         * 子类执行方法
         */
        public abstract void executeInner();
    }

    /**
     * 定时短期任务
     *
     * @since 2021-11-17
     */
    class ShortTimerTask extends AbstractTask {
        private final KieSubscriber kieSubscriber;

        private final KieListenerWrapper kieListenerWrapper;

        ShortTimerTask(KieSubscriber kieSubscriber, KieListenerWrapper kieListenerWrapper) {
            this.kieSubscriber = kieSubscriber;
            this.kieListenerWrapper = kieListenerWrapper;
        }

        @Override
        public void executeInner() {
            final KieResponse kieResponse = kieClient.queryConfigurations(kieSubscriber.getKieRequest());
            if (kieResponse != null && kieResponse.isChanged()) {
                tryPublishEvent(kieResponse, kieListenerWrapper);
                kieSubscriber.getKieRequest().setRevision(kieResponse.getRevision());
            }
        }

        @Override
        public boolean isLongConnectionRequest() {
            return false;
        }
    }

    /**
     * 长连接任务
     *
     * @since 2021-11-17
     */
    class LoopPullTask extends AbstractTask {
        private final KieSubscriber kieSubscriber;

        private final KieListenerWrapper kieListenerWrapper;

        private int failCount;

        LoopPullTask(KieSubscriber kieSubscriber, KieListenerWrapper kieListenerWrapper) {
            this.kieSubscriber = kieSubscriber;
            this.kieListenerWrapper = kieListenerWrapper;
        }

        @Override
        public void executeInner() {
            try {
                final KieResponse kieResponse = kieClient.queryConfigurations(kieSubscriber.getKieRequest());
                if (kieResponse != null && kieResponse.isChanged()) {
                    tryPublishEvent(kieResponse, kieListenerWrapper);
                    kieSubscriber.getKieRequest().setRevision(kieResponse.getRevision());
                }

                // 间隔一段时间拉取，减轻服务压力;如果在间隔时间段内有键变更，服务可以通过传入的revision判断是否需要将最新的数据立刻返回，不会存在键监听不到的问题
                this.failCount = 0;
                SubscriberManager.this.executeTask(new SleepCallBackTask(this, LONG_CONNECTION_REQUEST_INTERVAL_MS));
            } catch (Exception ex) {
                LOGGER.warn(String.format(Locale.ENGLISH, "pull kie config failed, %s, it will rePull",
                    ex.getMessage()));
                ++failCount;
                SubscriberManager.this.executeTask(new SleepCallBackTask(this, failCount));
            }
        }

        @Override
        public boolean isLongConnectionRequest() {
            return kieSubscriber.isLongConnectionRequest();
        }
    }

    /**
     * 等待执行任务, 等待一定的时间执行
     *
     * @since 2021-11-17
     */
    class SleepCallBackTask extends AbstractTask {
        private final Task nextTask;

        private int failedCount;

        private long waitTimeMs;

        SleepCallBackTask(Task nextTask, int failedCount) {
            this.nextTask = nextTask;
            this.failedCount = failedCount;
        }

        SleepCallBackTask(Task nextTask, long waitTimeMs) {
            this.nextTask = nextTask;
            this.waitTimeMs = waitTimeMs;
        }

        @Override
        public void executeInner() {
            long maxWaitMs = Duration.ofHours(1L).toMillis();
            long wait;
            if (waitTimeMs != 0) {
                wait = Math.min(waitTimeMs, maxWaitMs);
            } else {
                wait = Math.min(maxWaitMs, BASE_MS * failedCount * failedCount);
            }
            try {
                Thread.sleep(wait);
                SubscriberManager.this.executeTask(nextTask);
            } catch (InterruptedException ignored) {
                // ignored
            }
        }

        @Override
        public boolean isLongConnectionRequest() {
            return nextTask.isLongConnectionRequest();
        }

        @Override
        public void stop() {
            nextTask.stop();
        }
    }
}
