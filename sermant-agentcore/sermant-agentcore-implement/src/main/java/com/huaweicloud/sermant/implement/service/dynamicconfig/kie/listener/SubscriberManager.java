/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/servicecomb/config/kie/client/KieConfigManager.java
 * from the Apache ServiceComb Java Chassis project.
 */

package com.huaweicloud.sermant.implement.service.dynamicconfig.kie.listener;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huaweicloud.sermant.core.utils.LabelGroupUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.core.utils.ThreadFactoryUtils;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.ClientUrlManager;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.kie.KieClient;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.kie.KieConfigEntity;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.kie.KieListenerWrapper;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.kie.KieRequest;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.kie.KieResponse;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.kie.KieSubscriber;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.kie.ResultHandler;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.constants.KieConstants;

import org.apache.http.client.config.RequestConfig;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Subscriber Manager
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class SubscriberManager {
    /**
     * Maximum number of threads
     */
    public static final int MAX_THREAD_SIZE = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Thread size
     */
    private static final int THREAD_SIZE = 5;

    /**
     * Seconds are converted to milliseconds
     */
    private static final int SECONDS_UNIT = 1000;

    /**
     * request interval
     */
    private static final int SCHEDULE_REQUEST_INTERVAL_MS = 5000;

    /**
     * Wait time
     */
    private static final String WAIT = "20";

    /**
     * Long connection pull interval
     */
    private static final long LONG_CONNECTION_REQUEST_INTERVAL_MS = 2000L;

    /**
     * Number of current long connection requests. The maximum number of connections must be smaller than
     * MAX_THREAD_SIZE
     */
    private final AtomicInteger curLongConnectionRequestCount = new AtomicInteger(0);

    /**
     * map< listener key, list of listeners listening for the key >. A group has only one KieListenerWrapper
     */
    private final Map<KieRequest, KieListenerWrapper> listenerMap =
            new ConcurrentHashMap<>();

    /**
     * Kie client
     */
    private final KieClient kieClient;

    /**
     * Receive all data without filtering disabled data
     */
    private final ResultHandler<KieResponse> receiveAllDataHandler = new ResultHandler.DefaultResultHandler(false);

    /**
     * LongRequestExecutor for subscription. A maximum of MAX_THREAD_SIZE tasks are supported. Because it is a long
     * connection request, it will inevitably occupy threads, so the task is not considered to be stored in the queue
     */
    private final ThreadPoolExecutor longRequestExecutor = new ThreadPoolExecutor(THREAD_SIZE, MAX_THREAD_SIZE, 0,
            TimeUnit.MILLISECONDS, new SynchronousQueue<>(), new ThreadFactoryUtils("kie-subscribe-long-task"));

    /**
     * Used for quick return requests
     */
    private volatile ScheduledExecutorService scheduledExecutorService;

    /**
     * consortruct
     *
     * @param serverAddress serverAddress
     * @param timeout timeout
     */
    public SubscriberManager(String serverAddress, int timeout) {
        kieClient = new KieClient(new ClientUrlManager(serverAddress), timeout);
    }

    /**
     * SubscriberManager
     *
     * @param serverAddress serverAddress
     * @param project namespace
     * @param timeout timeout
     */
    public SubscriberManager(String serverAddress, String project, int timeout) {
        kieClient = new KieClient(new ClientUrlManager(serverAddress), project, timeout);
    }

    /**
     * Add group listener
     *
     * @param group label group
     * @param listener listener
     * @param ifNotify Whether to return all queried data to the caller on the first addition
     * @return add result
     */
    public boolean addGroupListener(String group, DynamicConfigListener listener, boolean ifNotify) {
        try {
            return subscribe(KieConstants.DEFAULT_GROUP_KEY,
                    new KieRequest().setLabelCondition(LabelGroupUtils.getLabelCondition(group)).setWait(WAIT),
                    listener,
                    ifNotify);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Add group listener failed, %s", ex.getMessage()));
            return false;
        }
    }

    /**
     * Add listener for single key
     *
     * @param key key
     * @param group label group
     * @param listener listener
     * @param ifNotify Whether to return all queried data to the caller on the first addition
     * @return add result
     */
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener, boolean ifNotify) {
        try {
            return subscribe(key,
                    new KieRequest().setLabelCondition(LabelGroupUtils.getLabelCondition(group)).setWait(WAIT),
                    listener,
                    ifNotify);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Add group listener failed, %s", ex.getMessage()));
            return false;
        }
    }

    /**
     * Remove group listener
     *
     * @param group label group
     * @param listener listener
     * @return remove result
     */
    public boolean removeGroupListener(String group, DynamicConfigListener listener) {
        try {
            return unSubscribe(KieConstants.DEFAULT_GROUP_KEY,
                    new KieRequest().setLabelCondition(LabelGroupUtils.getLabelCondition(group)).setWait(WAIT),
                    listener);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Removed group listener failed, %s", ex.getMessage()));
            return false;
        }
    }

    /**
     * Publish the configuration. If the configuration already exists, then update the configuration
     *
     * @param key configuration key
     * @param group configuration group
     * @param content configuration content
     * @return publish result
     */
    public boolean publishConfig(String key, String group, String content) {
        final Optional<String> keyIdOptional = getKeyId(key, group);
        if (!keyIdOptional.isPresent()) {
            // If not exists, then publish
            final Map<String, String> labels = LabelGroupUtils.resolveGroupLabels(group);
            return kieClient.publishConfig(key, labels, content, true);
        } else {
            return kieClient.doUpdateConfig(keyIdOptional.get(), content, true);
        }
    }

    /**
     * Remove configuration
     *
     * @param key configuration key
     * @param group configuration group
     * @return remove result
     */
    public boolean removeConfig(String key, String group) {
        final Optional<String> keyIdOptional = getKeyId(key, group);
        return keyIdOptional.filter(kieClient::doDeleteConfig).isPresent();
    }

    /**
     * Get key_id
     *
     * @param key configuration key
     * @param group configuration group
     * @return key_id, return null if not exists
     */
    private Optional<String> getKeyId(String key, String group) {
        final KieResponse kieResponse = queryConfigurations(null, LabelGroupUtils.getLabelCondition(group), false);
        if (kieResponse == null || kieResponse.getData() == null) {
            return Optional.empty();
        }
        final Map<String, String> labels = LabelGroupUtils.resolveGroupLabels(group);
        for (KieConfigEntity entity : kieResponse.getData()) {
            if (isSameKey(entity, key, labels)) {
                return Optional.of(entity.getId());
            }
        }
        return Optional.empty();
    }

    private boolean isSameKey(KieConfigEntity entity, String targetKey, Map<String, String> targetLabels) {
        if (!StringUtils.equals(entity.getKey(), targetKey)) {
            return false;
        }

        // Compare labels to see if they are the same
        final Map<String, String> sourceLabels = entity.getLabels();
        if (sourceLabels == null || (sourceLabels.size() != targetLabels.size())) {
            return false;
        }
        for (Map.Entry<String, String> entry : sourceLabels.entrySet()) {
            final String labelValue = targetLabels.get(entry.getKey());
            if (!StringUtils.equals(labelValue, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Register listener
     *
     * @param key key
     * @param kieRequest kie request
     * @param dynamicConfigListener dynamic config listener
     * @param ifNotify Whether to return all queried data to the caller on the first addition
     * @return subscribe result
     */
    public boolean subscribe(String key, KieRequest kieRequest, DynamicConfigListener dynamicConfigListener,
            boolean ifNotify) {
        final KieListenerWrapper oldWrapper = listenerMap.get(kieRequest);
        if (oldWrapper == null) {
            return firstSubscribeForGroup(key, kieRequest, dynamicConfigListener, ifNotify);
        } else {
            oldWrapper.addKeyListener(key, dynamicConfigListener, ifNotify);
            tryNotify(oldWrapper.getKieRequest(), oldWrapper, ifNotify);
            return true;
        }
    }

    private void tryNotify(KieRequest request, KieListenerWrapper wrapper, boolean ifNotify) {
        if (ifNotify) {
            firstRequest(request, wrapper);
        }
    }

    private boolean firstSubscribeForGroup(String key, KieRequest kieRequest,
            DynamicConfigListener dynamicConfigListener, boolean ifNotify) {
        final KieSubscriber kieSubscriber = new KieSubscriber(kieRequest);
        Task task;
        KieListenerWrapper kieListenerWrapper =
                new KieListenerWrapper(key, dynamicConfigListener, new KvDataHolder(), kieRequest, ifNotify);
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
        }
        kieListenerWrapper.setTask(task);
        listenerMap.put(kieRequest, kieListenerWrapper);
        tryNotify(kieRequest, kieListenerWrapper, ifNotify);
        executeTask(task);
        return true;
    }

    /**
     * Whether the number of long connection tasks exceeded the upper limit
     *
     * @return boolean
     */
    private boolean exceedMaxLongRequestCount() {
        return curLongConnectionRequestCount.incrementAndGet() > MAX_THREAD_SIZE;
    }

    /**
     * In the scenario of long requests, need to perform the first pull to obtain existing data
     *
     * @param kieRequest kie request
     * @param kieListenerWrapper listener
     */
    public void firstRequest(KieRequest kieRequest, KieListenerWrapper kieListenerWrapper) {
        try {
            KieResponse kieResponse = queryConfigurations(null, kieRequest.getLabelCondition());
            if (kieResponse != null && kieResponse.isChanged()) {
                tryPublishEvent(kieResponse, kieListenerWrapper, true);
                kieRequest.setRevision(kieResponse.getRevision());
            }
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Pull the first request failed! %s", ex.getMessage()));
        }
    }

    /**
     * Query configurations
     *
     * @param revision revision
     * @param label associated label group
     * @return kv configuration
     */
    public KieResponse queryConfigurations(String revision, String label) {
        return queryConfigurations(revision, label, true);
    }

    /**
     * Query configurations
     *
     * @param revision revision
     * @param label associated label group
     * @param onlyEnabled Whether only status=enabled is available
     * @return kv configuration
     */
    public KieResponse queryConfigurations(String revision, String label, boolean onlyEnabled) {
        final KieRequest cloneRequest = new KieRequest().setRevision(revision).setLabelCondition(label);
        if (onlyEnabled) {
            return kieClient.queryConfigurations(cloneRequest);
        }
        return kieClient.queryConfigurations(cloneRequest, receiveAllDataHandler);
    }

    /**
     * UnSubscribe
     *
     * @param key key
     * @param kieRequest kieRequest
     * @param dynamicConfigListener dynamicConfigListener
     * @return boolean
     */
    public boolean unSubscribe(String key, KieRequest kieRequest, DynamicConfigListener dynamicConfigListener) {
        for (Map.Entry<KieRequest, KieListenerWrapper> next : listenerMap.entrySet()) {
            if (!next.getKey().equals(kieRequest)) {
                continue;
            }
            if (dynamicConfigListener == null) {
                listenerMap.remove(next.getKey());
                return true;
            } else {
                if (doUnSubscribe(key, dynamicConfigListener, next)) {
                    return true;
                }
            }
        }
        LOGGER.warning(
                String.format(Locale.ENGLISH, "The subscriber of group %s not found!", kieRequest.getLabelCondition()));
        return false;
    }

    private boolean doUnSubscribe(String key, DynamicConfigListener dynamicConfigListener,
            Entry<KieRequest, KieListenerWrapper> next) {
        final KieListenerWrapper wrapper = next.getValue();
        if (wrapper.removeKeyListener(key, dynamicConfigListener)) {
            if (wrapper.isEmpty()) {
                // If all listeners are cleared, stop the task of changing the label group
                wrapper.getTask().stop();
            }
            return true;
        }
        return false;
    }

    private void buildRequestConfig(KieRequest kieRequest) {
        int wait = (Integer.parseInt(kieRequest.getWait()) + 1) * SECONDS_UNIT;
        if (kieRequest.getRequestConfig() == null) {
            kieRequest.setRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(wait).setConnectTimeout(wait)
                    .setSocketTimeout(wait).build());
        }
    }

    private void executeTask(final Task task) {
        try {
            if (task.isLongConnectionRequest()) {
                longRequestExecutor.execute(new TaskRunnable(task));
            } else {
                executeScheduledTask(task);
            }
        } catch (RejectedExecutionException ex) {
            LOGGER.warning("Rejected the task " + task.getClass() + " " + ex.getMessage());
        }
    }

    private void executeScheduledTask(Task task) {
        if (scheduledExecutorService == null) {
            synchronized (SubscriberManager.class) {
                if (scheduledExecutorService == null) {
                    scheduledExecutorService = new ScheduledThreadPoolExecutor(THREAD_SIZE,
                            new ThreadFactoryUtils("kie-subscribe-task"));
                }
            }
        }
        scheduledExecutorService.scheduleAtFixedRate(new TaskRunnable(task), 0, SCHEDULE_REQUEST_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
    }

    private void tryPublishEvent(KieResponse kieResponse, KieListenerWrapper kieListenerWrapper, boolean isFirst) {
        final KvDataHolder kvDataHolder = kieListenerWrapper.getKvDataHolder();
        final KvDataHolder.EventDataHolder eventDataHolder = kvDataHolder.analyzeLatestData(kieResponse, isFirst);
        if (eventDataHolder.isChanged() || isFirst) {
            kieListenerWrapper.notifyListeners(eventDataHolder, isFirst);
        }
    }

    /**
     * TaskRunnable
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
                LOGGER.warning(
                        String.format(Locale.ENGLISH, "The error occurred when execute task , %s", ex.getMessage()));
            }
        }
    }

    /**
     * Task
     *
     * @since 2021-11-17
     */
    public interface Task {
        /**
         * execute task
         */
        void execute();

        /**
         * Whether it is a request to maintain a connection for a long time
         *
         * @return boolean
         */
        boolean isLongConnectionRequest();

        /**
         * stop task
         */
        void stop();
    }

    /**
     * AbstractTask
     *
     * @since 2021-11-17
     */
    abstract static class AbstractTask implements Task {
        private volatile boolean isContinue = true;

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
         * subclass executive method
         */
        public abstract void executeInner();
    }

    /**
     * Scheduled short-term task
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
                tryPublishEvent(kieResponse, kieListenerWrapper, false);
                kieSubscriber.getKieRequest().setRevision(kieResponse.getRevision());
            }
        }

        @Override
        public boolean isLongConnectionRequest() {
            return false;
        }
    }

    /**
     * LoopPullTask
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
                    tryPublishEvent(kieResponse, kieListenerWrapper, false);
                    kieSubscriber.getKieRequest().setRevision(kieResponse.getRevision());
                }

                // Pull at intervals to reduce service pressure; If there are key changes in the interval, the
                // service can judge whether the latest data needs to be returned immediately through the input
                // revision, and there is no problem that the key cannot be listened to
                this.failCount = 0;
                SubscriberManager.this.executeTask(new SleepCallBackTask(this, LONG_CONNECTION_REQUEST_INTERVAL_MS));
            } catch (Exception ex) {
                LOGGER.warning(
                        String.format(Locale.ENGLISH, "pull kie config failed, %s, it will rePull", ex.getMessage()));
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
     * SleepCallBackTask
     *
     * @since 2021-11-17
     */
    class SleepCallBackTask extends AbstractTask {
        private static final long MAX_WAIT_MS = 60 * 1000 * 60L;

        private static final long BASE_MS = 3000L;

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
            long wait;
            if (waitTimeMs != 0) {
                wait = Math.min(waitTimeMs, MAX_WAIT_MS);
            } else {
                wait = Math.min(MAX_WAIT_MS, BASE_MS * failedCount * failedCount);
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