/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.config.listener;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.integration.utils.APMThreadFactory;
import com.huawei.config.client.ClientUrlManager;
import com.huawei.config.kie.KieClient;
import com.huawei.config.kie.KieListenerWrapper;
import com.huawei.config.kie.KieRequest;
import com.huawei.config.kie.KieRequestFactory;
import com.huawei.config.kie.KieResponse;
import com.huawei.config.kie.KieSubscriber;
import com.huawei.config.listener.event.KieConfigurationChangeEvent;
import org.apache.http.client.config.RequestConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

    private static final SubscriberManager INSTANCE = new SubscriberManager();

    /**
     * 线程数
     */
    private static final int THREAD_SIZE = 5;

    /**
     * 最大线程数
     */
    private static final int MAX_THREAD_SIZE = 50;

    /**
     * 秒转换为毫秒
     */
    private static final int SECONDS_UNIT = 1000;

    /**
     * 线程空闲时间
     */
    private static final long KEEP_ALIVE_TIME_MS = 60 * SECONDS_UNIT;

    /**
     * 最大任务数
     */
    private static final int KEEPER_QUEUE_SIZE = 50;

    /**
     * 定时请求间隔
     */
    private static final int SCHEDULE_REQUEST_INTERVAL_MS = 5000;

    /**
     * map<监听键, 监听该键的监听器列表>
     */
    private final Map<KieSubscriber, List<KieListenerWrapper>> listenerMap =
            new ConcurrentHashMap<KieSubscriber, List<KieListenerWrapper>>();

    /**
     * TODO 获取url，从配置获取
     */
    private final KieClient kieClient = new KieClient(new ClientUrlManager("http://172.31.100.55:30110"));

    /**
     * 订阅执行器
     */
    private final ThreadPoolExecutor keeperRequestExecutor = new ThreadPoolExecutor(THREAD_SIZE, MAX_THREAD_SIZE,
            KEEP_ALIVE_TIME_MS, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(KEEPER_QUEUE_SIZE),
            new APMThreadFactory("kie-subscribe-keeper-task"));

    /**
     * 快速返回的请求
     */
    private final ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(THREAD_SIZE, new APMThreadFactory("kie-subscribe-task"));

    /**
     * 注册监听器
     *
     * @param kieRequest 请求
     * @param configurationListener 监听器
     */
    public void subscribe(KieRequest kieRequest, ConfigurationListener configurationListener) {
        if (kieRequest == null || configurationListener == null) {
            return;
        }
        final KieSubscriber kieSubscriber = new KieSubscriber(kieRequest);
        List<KieListenerWrapper> configurationListeners = listenerMap.get(kieSubscriber);
        if (configurationListeners == null) {
            configurationListeners = new ArrayList<KieListenerWrapper>();
        }
        Task task;
        KieListenerWrapper kieListenerWrapper = new KieListenerWrapper(configurationListener, new KvDataHolder());
        if (!kieSubscriber.isKeeperRequest()) {
            task = new ShortTimerTask(kieSubscriber, kieListenerWrapper);
        } else {
            buildRequestConfig(kieRequest);
            task = new LoopPullTask(kieSubscriber, kieListenerWrapper);
            firstRequest(kieRequest, kieListenerWrapper);
        }
        kieListenerWrapper.setTask(task);
        configurationListeners.add(kieListenerWrapper);
        listenerMap.put(kieSubscriber, configurationListeners);
        executeTask(task);
    }

    /**
     * 针对长请求的场景需要做第一次拉取，获取已有的数据
     *
     * @param kieRequest 请求体
     * @param kieListenerWrapper 监听器
     */
    public void firstRequest(KieRequest kieRequest, KieListenerWrapper kieListenerWrapper) {
        final KieRequest cloneRequest = KieRequestFactory
                .buildKieRequest(kieRequest.getRevision(), kieRequest.getLabelCondition());
        final KieResponse kieResponse = kieClient.queryConfigurations(cloneRequest);
        if (kieResponse.isChanged()) {
            publishEvent(kieResponse, kieListenerWrapper);
        }
    }

    /**
     * 取消订阅
     *
     * @param kieRequest 请求体
     */
    public void unSubscribe(KieRequest kieRequest, ConfigurationListener configurationListener) {
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
                    break;
                }
            }
        }
    }

    /**
     * 获取单例
     *
     * @return SubscriberManager
     */
    public static SubscriberManager getInstance() {
        return INSTANCE;
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
        if (task.isKeeperRequest()) {
            keeperRequestExecutor.execute(new TaskRunnable(task));
        } else {
            scheduledExecutorService.scheduleAtFixedRate(
                    new TaskRunnable(task), 0, SCHEDULE_REQUEST_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void publishEvent(KieResponse kieResponse, KieListenerWrapper kieListenerWrapper) {
        final KvDataHolder kvDataHolder = kieListenerWrapper.getKvDataHolder();
        final KvDataHolder.EventDataHolder eventDataHolder = kvDataHolder.analyzeLatestData(kieResponse);
        if (eventDataHolder.isChanged()) {
            kieListenerWrapper.getConfigurationListener().onEvent(new KieConfigurationChangeEvent(eventDataHolder));
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
        boolean isKeeperRequest();

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
            if (kieResponse!= null && kieResponse.isChanged()) {
                publishEvent(kieResponse, kieListenerWrapper);
            }
        }

        @Override
        public boolean isKeeperRequest() {
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
                if (kieResponse!= null && kieResponse.isChanged()) {
                    publishEvent(kieResponse, kieListenerWrapper);
                }
                SubscriberManager.this.executeTask(this);
            } catch (Exception ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "pull kie config failed, %s, it will rePull", ex.getMessage()));
                ++failCount;
                SubscriberManager.this.executeTask(new BackOffSleepTask(this, failCount));
            }
        }

        @Override
        public boolean isKeeperRequest() {
            return kieSubscriber.isKeeperRequest();
        }
    }

    class BackOffSleepTask extends AbstractTask {
        private final Task nextTask;

        private final int failedCount;

        public BackOffSleepTask(Task nextTask, int failedCount) {
            this.nextTask = nextTask;
            this.failedCount = failedCount;
        }

        @Override
        public void executeInner() {
            long baseMs = 3000;
            long maxWaitMs = 60 * 1000 * 60;
            long wait = Math.min(maxWaitMs, baseMs * failedCount * failedCount);
            try {
                Thread.sleep(wait);
                SubscriberManager.this.executeTask(nextTask);
            } catch (InterruptedException ignored) {
                // ignored
            }
        }

        @Override
        public boolean isKeeperRequest() {
            return nextTask.isKeeperRequest();
        }

        @Override
        public void stop() {
            nextTask.stop();
        }
    }
}
