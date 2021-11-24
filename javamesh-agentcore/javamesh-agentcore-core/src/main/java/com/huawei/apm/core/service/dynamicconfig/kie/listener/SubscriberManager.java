/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.service.dynamicconfig.kie.listener;

import com.alibaba.fastjson.JSONException;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.integration.utils.APMThreadFactory;
import com.huawei.apm.core.service.dynamicconfig.kie.GroupUtils;
import com.huawei.apm.core.service.dynamicconfig.kie.client.ClientUrlManager;
import com.huawei.apm.core.service.dynamicconfig.kie.client.kie.KieClient;
import com.huawei.apm.core.service.dynamicconfig.kie.client.kie.KieListenerWrapper;
import com.huawei.apm.core.service.dynamicconfig.kie.client.kie.KieRequest;
import com.huawei.apm.core.service.dynamicconfig.kie.client.kie.KieResponse;
import com.huawei.apm.core.service.dynamicconfig.kie.client.kie.KieSubscriber;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import org.apache.http.client.config.RequestConfig;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 监听器管理
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class SubscriberManager {
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 最大线程数
     */
    public static final int MAX_THREAD_SIZE = 100;

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
     * 当前长连接请求数
     * 要求最大连接数必须小于 MAX_THREAD_SIZE
     *
     */
    private final AtomicInteger curLongConnectionRequestCount = new AtomicInteger(0);

    /**
     * map<监听键, 监听该键的监听器列表>
     */
    private final Map<KieSubscriber, List<KieListenerWrapper>> listenerMap = new ConcurrentHashMap<KieSubscriber, List<KieListenerWrapper>>();

    /**
     * kie客户端
     */
    private final KieClient kieClient;

    /**
     * 订阅执行器
     * 最大支持MAX_THREAD_SIZE个任务
     * 由于是长连接请求，必然会占用线程，因此这里不考虑将任务存在队列中
     */
    private final ThreadPoolExecutor longRequestExecutor = new ThreadPoolExecutor(THREAD_SIZE, MAX_THREAD_SIZE,
            0, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(),
            new APMThreadFactory("kie-subscribe-long-task"));

    /**
     * 快速返回的请求
     */
    private final ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(THREAD_SIZE, new APMThreadFactory("kie-subscribe-task"));

    public SubscriberManager(String urls) {
        kieClient = new KieClient(new ClientUrlManager(urls));
    }

    /**
     * 添加组监听
     *
     * @param group 标签组
     * @param listener 监听器
     * @return 是否添加成功
     */
    public boolean addGroupListener(String group, ConfigurationListener listener) {
        if (!GroupUtils.isLabelGroup(group)) {
            return false;
        }
        try {
            return subscribe(new KieRequest().setLabelCondition(GroupUtils.getLabelCondition(group)).setWait(WAIT), listener);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Add group listener failed, %s", ex.getMessage()));
            return false;
        }
    }

    /**
     * 移除组监听
     *
     * @param group 标签组
     * @param listener 监听器
     * @return 是否添加成功
     */
    public boolean removeGroupListener(String group, ConfigurationListener listener) {
        try {
            return unSubscribe(new KieRequest().setLabelCondition(GroupUtils.getLabelCondition(group)).setWait(WAIT), listener);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Removed group listener failed, %s", ex.getMessage()));
            return false;
        }
    }

    /**
     * 注册监听器
     *
     * @param kieRequest            请求
     * @param configurationListener 监听器
     */
    public boolean subscribe(KieRequest kieRequest, ConfigurationListener configurationListener) {
        final KieSubscriber kieSubscriber = new KieSubscriber(kieRequest);
        Task task;
        KieListenerWrapper kieListenerWrapper = new KieListenerWrapper(kieRequest.getLabelCondition(), configurationListener, new KvDataHolder());
        if (!kieSubscriber.isLongConnectionRequest()) {
            task = new ShortTimerTask(kieSubscriber, kieListenerWrapper);
        } else {
            if (exceedMaxLongRequestCount()) {
                LOGGER.warning(String.format(Locale.ENGLISH,
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
            configurationListeners = new ArrayList<KieListenerWrapper>();
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
            LOGGER.warning(String.format(Locale.ENGLISH, "Pull the first request failed! %s", ex.getMessage()));
        }
    }

    /**
     * 取消订阅
     *
     * @param kieRequest 请求体
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
                    LOGGER.log(Level.FINE, String.format(Locale.ENGLISH, "%s has been stopped!",
                            configurationListener.getClass().getName()));
                    return true;
                }
            }
        }
        LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH, "The subscriber of group %s not found!",
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
                scheduledExecutorService.scheduleAtFixedRate(
                        new TaskRunnable(task), 0, SCHEDULE_REQUEST_INTERVAL_MS, TimeUnit.MILLISECONDS);
            }
        } catch (RejectedExecutionException ex) {
            LOGGER.warning("Rejected the task " + task.getClass() + " " + ex.getMessage());
        }
    }

    private void tryPublishEvent(KieResponse kieResponse, KieListenerWrapper kieListenerWrapper) {
        final KvDataHolder kvDataHolder = kieListenerWrapper.getKvDataHolder();
        final KvDataHolder.EventDataHolder eventDataHolder = kvDataHolder.analyzeLatestData(kieResponse);
        if (eventDataHolder.isChanged()) {
            try {
                kieListenerWrapper.notifyListener(eventDataHolder);
            } catch (JSONException ex) {
                LOGGER.warning("Format data to string failed when publish event!");
            }
        }
    }

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
                LOGGER.warning(String.format(Locale.ENGLISH, "The error occurred when execute task , %s", ex.getMessage()));
            }
        }
    }

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
                SubscriberManager.this.executeTask(this);
            } catch (Exception ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "pull kie config failed, %s, it will rePull", ex.getMessage()));
                ++failCount;
                SubscriberManager.this.executeTask(new SleepCallBackTask(this, failCount));
            }
        }

        @Override
        public boolean isLongConnectionRequest() {
            return kieSubscriber.isLongConnectionRequest();
        }
    }

    class SleepCallBackTask extends AbstractTask {
        private final Task nextTask;

        private final int failedCount;

        private long waitTimeMs;

        public SleepCallBackTask(Task nextTask, int failedCount) {
            this.nextTask = nextTask;
            this.failedCount = failedCount;
        }

        @Override
        public void executeInner() {
            long maxWaitMs = 60 * 1000 * 60;
            long wait;
            if (waitTimeMs != 0) {
                wait = Math.min(waitTimeMs, maxWaitMs);
            } else {
                long baseMs = 3000;
                wait = Math.min(maxWaitMs, baseMs * failedCount * failedCount);
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
