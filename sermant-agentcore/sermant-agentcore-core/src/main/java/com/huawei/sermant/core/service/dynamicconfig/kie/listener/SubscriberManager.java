/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package com.huawei.sermant.core.service.dynamicconfig.kie.listener;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.integration.utils.APMThreadFactory;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huawei.sermant.core.service.dynamicconfig.kie.client.ClientUrlManager;
import com.huawei.sermant.core.service.dynamicconfig.kie.client.kie.KieClient;
import com.huawei.sermant.core.service.dynamicconfig.kie.client.kie.KieConfigEntity;
import com.huawei.sermant.core.service.dynamicconfig.kie.client.kie.KieListenerWrapper;
import com.huawei.sermant.core.service.dynamicconfig.kie.client.kie.KieRequest;
import com.huawei.sermant.core.service.dynamicconfig.kie.client.kie.KieResponse;
import com.huawei.sermant.core.service.dynamicconfig.kie.client.kie.KieSubscriber;
import com.huawei.sermant.core.service.dynamicconfig.kie.client.kie.ResultHandler;
import com.huawei.sermant.core.service.dynamicconfig.kie.constants.KieConstants;
import com.huawei.sermant.core.service.dynamicconfig.utils.LabelGroupUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;

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
import java.util.logging.Logger;

/**
 * 监听器管理
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class SubscriberManager {
    private static final Logger LOGGER = LoggerFactory.getLogger();

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
     * 长连接拉取间隔
     */
    private static final long LONG_CONNECTION_REQUEST_INTERVAL_MS = 2000L;

    /**
     * 当前长连接请求数
     * 要求最大连接数必须小于 MAX_THREAD_SIZE
     */
    private final AtomicInteger curLongConnectionRequestCount = new AtomicInteger(0);

    /**
     * map<监听键, 监听该键的监听器列表>  一个group，仅有一个KieListenerWrapper
     */
    private final Map<KieRequest, KieListenerWrapper> listenerMap = new ConcurrentHashMap<KieRequest, KieListenerWrapper>();

    /**
     * kie客户端
     */
    private final KieClient kieClient;

    /**
     * 接收所有数据，不过滤disabled的数据
     */
    private final ResultHandler<KieResponse> receiveAllDataHandler = new ResultHandler.DefaultResultHandler(false);

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
    private ScheduledExecutorService scheduledExecutorService;

    public SubscriberManager(String serverAddress) {
        kieClient = new KieClient(new ClientUrlManager(serverAddress));
    }

    public SubscriberManager(String serverAddress, String project) {
        kieClient = new KieClient(new ClientUrlManager(serverAddress), project);
    }

    /**
     * 添加组监听
     *
     * @param group    标签组
     * @param listener 监听器
     * @param ifNotify 是否在第一次添加时，将所有数据查询返回给调用者
     * @return 是否添加成功
     */
    public boolean addGroupListener(String group, DynamicConfigListener listener, boolean ifNotify) {
        try {
            return subscribe(KieConstants.DEFAULT_GROUP_KEY, new KieRequest().setLabelCondition(
                    LabelGroupUtils.getLabelCondition(group)).setWait(WAIT), listener, ifNotify);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Add group listener failed, %s", ex.getMessage()));
            return false;
        }
    }

    /**
     * 添加单个key监听器
     *
     * @param key      键
     * @param group    标签组
     * @param listener 监听器
     * @param ifNotify 是否在第一次添加时，将所有数据查询返回给调用者
     * @return 是否添加成功
     */
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener, boolean ifNotify) {
        try {
            return subscribe(key, new KieRequest().setLabelCondition(
                    LabelGroupUtils.getLabelCondition(group)).setWait(WAIT), listener, ifNotify);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Add group listener failed, %s", ex.getMessage()));
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
    public boolean removeGroupListener(String group, DynamicConfigListener listener) {
        try {
            return unSubscribe(KieConstants.DEFAULT_GROUP_KEY, new KieRequest().setLabelCondition(
                    LabelGroupUtils.getLabelCondition(group)).setWait(WAIT), listener);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Removed group listener failed, %s", ex.getMessage()));
            return false;
        }
    }

    /**
     * 发布配置, 若配置已存在则转为更新配置
     *
     * @param key     配置键
     * @param group   分组
     * @param content 配置内容
     * @return 是否发布成功
     */
    public boolean publishConfig(String key, String group, String content) {
        final String keyId = getKeyId(key, group);
        if (keyId == null) {
            // 无该配置 执行新增操作
            final Map<String, String> labels = LabelGroupUtils.resolveGroupLabels(group);
            return kieClient.publishConfig(key, labels, content, true);
        } else {
            return kieClient.doUpdateConfig(keyId, content, true);
        }
    }

    /**
     * 移除配置
     *
     * @param key   键名称
     * @param group 分组
     * @return 是否删除成功
     */
    public boolean removeConfig(String key, String group) {
        final String keyId = getKeyId(key, group);
        if (keyId == null) {
            return false;
        }
        return kieClient.doDeleteConfig(keyId);
    }

    /**
     * 获取key_id
     *
     * @param key   键
     * @param group 组
     * @return key_id, 若不存在则返回null
     */
    private String getKeyId(String key, String group) {
        final KieResponse kieResponse = queryConfigurations(null, LabelGroupUtils.getLabelCondition(group), false);
        if (kieResponse == null || kieResponse.getData() == null) {
            return null;
        }
        final Map<String, String> labels = LabelGroupUtils.resolveGroupLabels(group);
        for (KieConfigEntity entity : kieResponse.getData()) {
            if (isSameKey(entity, key, labels)) {
                return entity.getId();
            }
        }
        return null;
    }

    private boolean isSameKey(KieConfigEntity entity, String targetKey, Map<String, String> targetLabels) {
        if (!StringUtils.equals(entity.getKey(), targetKey)) {
            return false;
        }
        // 比较标签是否相同
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
     * 注册监听器
     *
     * @param kieRequest            请求
     * @param dynamicConfigListener 监听器
     * @param ifNotify              是否在第一次添加时，将所有数据查询返回给调用者
     */
    public boolean subscribe(String key, KieRequest kieRequest, DynamicConfigListener dynamicConfigListener, boolean ifNotify) {
        final KieListenerWrapper oldWrapper = listenerMap.get(kieRequest);
        if (oldWrapper == null) {
            return firstSubscribeForGroup(key, kieRequest, dynamicConfigListener, ifNotify);
        } else {
            oldWrapper.addKeyListener(key, dynamicConfigListener);
            tryNotify(oldWrapper.getKieRequest(), oldWrapper, ifNotify);
            return true;
        }
    }

    private void tryNotify(KieRequest request, KieListenerWrapper wrapper, boolean ifNotify) {
        if (ifNotify) {
            firstRequest(request, wrapper);
        }
    }

    private boolean firstSubscribeForGroup(String key,
                                           KieRequest kieRequest,
                                           DynamicConfigListener dynamicConfigListener,
                                           boolean ifNotify) {
        final KieSubscriber kieSubscriber = new KieSubscriber(kieRequest);
        Task task;
        KieListenerWrapper kieListenerWrapper = new KieListenerWrapper(key,
                dynamicConfigListener, new KvDataHolder(), kieRequest);
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
     * 单独查询配置
     *
     * @param revision 版本
     * @param label    关联标签组
     * @return kv配置
     */
    public KieResponse queryConfigurations(String revision, String label) {
        return queryConfigurations(revision, label, true);
    }

    /**
     * 单独查询配置
     *
     * @param revision 版本
     * @param label    关联标签组
     * @param onlyEnabled 是否仅可用status=enabled
     * @return kv配置
     */
    public KieResponse queryConfigurations(String revision, String label, boolean onlyEnabled) {
        final KieRequest cloneRequest = new KieRequest().setRevision(revision).setLabelCondition(label);
        if (onlyEnabled) {
            return kieClient.queryConfigurations(cloneRequest);
        }
        return kieClient.queryConfigurations(cloneRequest, receiveAllDataHandler);
    }

    /**
     * 取消订阅
     *
     * @param kieRequest 请求体
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
                final KieListenerWrapper wrapper = next.getValue();
                if (wrapper.removeKeyListener(key, dynamicConfigListener)) {
                    if (wrapper.isEmpty()) {
                        // 若监听器均已清空，则停止改标签组的任务
                        wrapper.getTask().stop();
                    }
                    return true;
                }
            }
        }
        LOGGER.warning(String.format(Locale.ENGLISH, "The subscriber of group %s not found!",
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
                        if (scheduledExecutorService == null) {
                            scheduledExecutorService = new ScheduledThreadPoolExecutor(THREAD_SIZE,
                                    new APMThreadFactory("kie-subscribe-task"));
                        }
                    }
                }
                scheduledExecutorService.scheduleAtFixedRate(
                        new TaskRunnable(task), 0, SCHEDULE_REQUEST_INTERVAL_MS, TimeUnit.MILLISECONDS);
            }
        } catch (RejectedExecutionException ex) {
            LOGGER.warning("Rejected the task " + task.getClass() + " " + ex.getMessage());
        }
    }

    private void tryPublishEvent(KieResponse kieResponse, KieListenerWrapper kieListenerWrapper, boolean isFirst) {
        final KvDataHolder kvDataHolder = kieListenerWrapper.getKvDataHolder();
        final KvDataHolder.EventDataHolder eventDataHolder = kvDataHolder.analyzeLatestData(kieResponse, isFirst);
        if (eventDataHolder.isChanged() || isFirst) {
            kieListenerWrapper.notifyListener(eventDataHolder);
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
                tryPublishEvent(kieResponse, kieListenerWrapper, false);
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
                    tryPublishEvent(kieResponse, kieListenerWrapper, false);
                    kieSubscriber.getKieRequest().setRevision(kieResponse.getRevision());
                }
                // 间隔一段时间拉取，减轻服务压力;
                // 如果在间隔时间段内有键变更，服务可以通过传入的revision判断是否需要将最新的数据立刻返回，不会存在键监听不到的问题
                this.failCount = 0;
                SubscriberManager.this.executeTask(new SleepCallBackTask(this, LONG_CONNECTION_REQUEST_INTERVAL_MS));
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

        private int failedCount;

        private long waitTimeMs;

        public SleepCallBackTask(Task nextTask, int failedCount) {
            this.nextTask = nextTask;
            this.failedCount = failedCount;
        }

        public SleepCallBackTask(Task nextTask, long waitTimeMs) {
            this.nextTask = nextTask;
            this.waitTimeMs = waitTimeMs;
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
