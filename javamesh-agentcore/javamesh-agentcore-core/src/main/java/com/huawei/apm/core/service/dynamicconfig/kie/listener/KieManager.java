/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.service.dynamicconfig.kie.listener;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.apm.core.lubanops.integration.utils.APMThreadFactory;
import com.huawei.apm.core.service.dynamicconfig.kie.utils.KieGroupUtils;
import com.huawei.apm.core.service.dynamicconfig.kie.client.ClientUrlManager;
import com.huawei.apm.core.service.dynamicconfig.kie.constants.KieConstants;
import com.huawei.apm.core.service.dynamicconfig.kie.client.kie.KieClient;
import com.huawei.apm.core.service.dynamicconfig.kie.client.kie.KieRequest;
import com.huawei.apm.core.service.dynamicconfig.kie.client.kie.KieResponse;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangeType;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Kie配置管理
 *
 * @author zhouss
 * @since 2021-11-23
 */
@Deprecated
public class KieManager {
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 长连接监听20s
     */
    private static final String WAIT = "20";

    /**
     * 全局kv数据持有
     */
    private static final KvDataEntityHolder GLOBAL_KV_DATA_HOLDER = new KvDataEntityHolder();

    private final KieListenerManager listenerManager = new KieListenerManager();

    private final ThreadPoolExecutor taskExecutor =
            new ThreadPoolExecutor(
                    KieConstants.CORE_THREAD_SIZE,
                    KieConstants.MAX_THREAD_SIZE, 0, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(KieConstants.QUEUE_SIZE),
                    new APMThreadFactory("KIE-KV-DATA-LISTEN-TASK"));

    /**
     * kie客户端
     */
    private final KieClient kieClient;

    public KieManager(String urls) {
        this.kieClient = new KieClient(new ClientUrlManager(urls));
        init();
    }

    /**
     * 添加监听器
     *
     * @param key      监听键或者是标签组
     * @param group    分组
     * @param listener 监听器
     */
    public void addListener(String key, String group, ConfigurationListener listener) {
        final KieListener kieListener = new KieListener(key, group, listener);
        listenerManager.addListener(kieListener);
    }

    /**
     * 移除监听器
     *
     * @param key      监听键或者是标签组
     * @param group    分组
     * @param listener 监听器
     */
    public void removeListener(String key, String group, ConfigurationListener listener) {
        listenerManager.removeListener(key, group, listener);
    }


    /**
     * 初始化任务，第一次查询kie的所有数据
     */
    private void init() {
        updateLocalData(new KieRequest(), false);
        executeTask(new LoopPullTask(new KieRequest().setWait(WAIT)));
    }

    /**
     * 开启监听任务
     */
    private void executeTask(Task task) {
        try {
            taskExecutor.execute(new TaskRunnable(task));
        } catch (RejectedExecutionException exception) {
            LOGGER.warning("Exceeded max task num that kie can listen!");
        }
    }

    /**
     * 更新本地数据
     *
     * @param kieRequest  请求
     * @param forPublish 是否是为了发布时间更新
     */
    private void updateLocalData(KieRequest kieRequest, boolean forPublish) {
        try {
            final KieResponse kieResponse = kieClient.queryConfigurations(kieRequest);
            final KvDataEntityHolder.EventDataHolder eventDataHolder = GLOBAL_KV_DATA_HOLDER.analyzeLatestData(kieResponse);
            if (forPublish && kieResponse != null && kieResponse.isChanged()) {
                tryPublishEvent(eventDataHolder);
            }
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Updated local kie data failed! %s", ex.getMessage()));
        }
    }

    /**
     * 尝试发布事件
     *
     * @param eventDataHolder 响应数据
     */
    private void tryPublishEvent(KvDataEntityHolder.EventDataHolder eventDataHolder) {
        if (!eventDataHolder.isChanged()) {
            return;
        }
        if (!eventDataHolder.getAdded().isEmpty()) {
            // 新增事件
            listenerManager.notify(eventDataHolder.getAdded(), ConfigChangeType.ADDED);
        }
        if (!eventDataHolder.getDeleted().isEmpty()) {
            // 删除事件
            listenerManager.notify(eventDataHolder.getDeleted(), ConfigChangeType.DELETED);
        }
        if (!eventDataHolder.getModified().isEmpty()) {
            // 修改事件
            listenerManager.notify(eventDataHolder.getModified(), ConfigChangeType.MODIFIED);
        }
    }

    /**
     * kie监听管理器
     */
    static class KieListenerManager {
        private List<KieListener> listeners;

        public void addListener(KieListener kieListener) {
            if (listeners == null) {
                listeners = new ArrayList<KieListener>();
            }
            listeners.add(kieListener);
        }

        public void removeListener(String key, String group, ConfigurationListener listener) {
            final Iterator<KieListener> iterator = listeners.iterator();
            while (iterator.hasNext()) {
                final KieListener next = iterator.next();
                if (StringUtils.equals(key, next.key) && StringUtils.equals(group, next.group)
                        && listener == next.getListener()) {
                    iterator.remove();
                }
            }
        }

        /**
         * 通知
         *
         * @param data 更新配置数据
         */
        public void notify(Map<String, KvDataEntityHolder.SimpleConfigEntity> data, ConfigChangeType configChangeType) {
            for (Map.Entry<String, KvDataEntityHolder.SimpleConfigEntity> entry : data.entrySet()) {
                // kie配置的键
                final String key = entry.getKey();
                final KvDataEntityHolder.SimpleConfigEntity configEntity = entry.getValue();
                for (KieListener listener : listeners) {
                    // 首先匹配键, 若不是根据标签的监听，支持通过key进行配置
                    if (!listener.isLabelGroup && listener.key.equals(key)) {
                        process(listener, configChangeType, configEntity.getValue());
                        continue;
                    }
                    // 再匹配映射的标签
                    if (listener.containKey(key)) {
                        process(listener, configChangeType, configEntity.getValue());
                        continue;
                    }
                    if (matchLabels(listener.labels, configEntity.getLabels())) {
                        process(listener, configChangeType, configEntity.getValue());
                        listener.addKey(entry.getKey());
                    }
                }
            }
        }

        private void process(KieListener listener, ConfigChangeType configChangeType, String value) {
            try {
                listener.getListener().process(new ConfigChangedEvent(listener.key, listener.group,
                        value, configChangeType));
            } catch (Exception ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Process config data failed, key: [%s], group: [%s]",
                        listener.key, listener.group));
            }
        }

        /**
         * 标签匹配
         *
         * @param listenerLabels 监听器的标签
         * @param configLabels   更新配置的标签
         * @return 是否匹配
         */
        private boolean matchLabels(Map<String, String> listenerLabels, Map<String, String> configLabels) {
            if (listenerLabels == null || configLabels == null) {
                return false;
            }
            if (listenerLabels.size() != configLabels.size()) {
                return false;
            }
            for (Map.Entry<String, String> entry : configLabels.entrySet()) {
                if (!StringUtils.equals(entry.getValue(), listenerLabels.get(entry.getKey()))) {
                    return false;
                }
            }
            return true;
        }
    }

    static class KieListener {
        /**
         * 用户订阅的键
         */
        private final String key;

        /**
         * 用户订阅监听器
         */
        private final ConfigurationListener listener;

        /**
         * 是否为标签组类型
         */
        private final boolean isLabelGroup;

        /**
         * 映射键
         * 当keys属于标签组时，可通过标签组查询多个key
         */
        private final Set<String> mappingKeys = new HashSet<String>();

        /**
         * 针对该key映射的标签
         */
        private Map<String, String> labels;

        /**
         * 分组
         */
        private final String group;

        public boolean containKey(String key) {
            return mappingKeys.add(key);
        }

        public void addKey(String key) {
            mappingKeys.add(key);
        }

        private KieListener(String key, String group, ConfigurationListener listener) {
            this.key = key;
            this.group = group;
            this.listener = listener;
            this.isLabelGroup = KieGroupUtils.isLabelGroup(group);
            resolveGroup();
        }

        private void resolveGroup() {
            if (this.isLabelGroup) {
                // 如果为标签组，则匹配标签组
                labels = KieGroupUtils.resolveGroupLabels(this.key);
            }
        }

        public String getKey() {
            return key;
        }

        public ConfigurationListener getListener() {
            return listener;
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

    class LoopPullTask extends AbstractTask {
        private final KieRequest kieRequest;

        private int failCount;

        LoopPullTask(KieRequest kieRequest) {
            this.kieRequest = kieRequest;
        }

        @Override
        public void executeInner() {
            try {
                final KieResponse kieResponse = kieClient.queryConfigurations(kieRequest);
                if (kieResponse != null && kieResponse.isChanged()) {
                    updateLocalData(kieRequest, true);
                }
                KieManager.this.executeTask(this);
            } catch (Exception ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "pull kie config failed, %s, it will rePull", ex.getMessage()));
                ++failCount;
                KieManager.this.executeTask(new SleepCallBackTask(this, failCount));
            }
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
                KieManager.this.executeTask(nextTask);
            } catch (InterruptedException ignored) {
                // ignored
            }
        }

        @Override
        public void stop() {
            nextTask.stop();
        }
    }
}
