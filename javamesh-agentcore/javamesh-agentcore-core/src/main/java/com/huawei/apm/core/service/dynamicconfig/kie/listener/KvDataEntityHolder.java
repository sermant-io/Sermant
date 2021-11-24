/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.service.dynamicconfig.kie.listener;

import com.huawei.apm.core.service.dynamicconfig.kie.client.kie.KieConfigEntity;
import com.huawei.apm.core.service.dynamicconfig.kie.client.kie.KieResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * 监听键响应数据
 * 用于对比新旧数据并保留旧数据
 *
 * @author zhouss
 * @since 2021-11-18
 */
@Deprecated
public class KvDataEntityHolder {
    /**
     * 当前数据
     */
    private Map<String, SimpleConfigEntity> currentData;

    /**
     * 分析最新的数据
     *
     * @param response 最新数据
     * @return EventDataHolder
     */
    public EventDataHolder analyzeLatestData(KieResponse response) {
        final Map<String, SimpleConfigEntity> latestData = formatKieResponse(response);
        final EventDataHolder eventDataHolder = new EventDataHolder();
        if (currentData != null) {
            if (latestData.isEmpty()) {
                currentData.clear();
            } else {
                Map<String, SimpleConfigEntity> temp = new HashMap<String, SimpleConfigEntity>(currentData);
                for (Map.Entry<String, SimpleConfigEntity> entry : latestData.entrySet()) {
                    final SimpleConfigEntity oldData = currentData.get(entry.getKey());
                    if (oldData == null) {
                        // 增加的键
                        eventDataHolder.added.put(entry.getKey(), entry.getValue());
                    } else {
                        // 如果存在该键，则比对值是否相等
                        if (!oldData.getValue().equals(entry.getValue().getValue())) {
                            // 修改
                            eventDataHolder.modified.put(entry.getKey(), entry.getValue());
                        }
                    }
                    temp.remove(entry.getKey());
                }
                // temp留下的键即为删除的
                for (String key : temp.keySet()) {
                    eventDataHolder.deleted.remove(key);
                }
            }
        } else {
            eventDataHolder.added.putAll(latestData);
        }
        currentData = latestData;
        return eventDataHolder;
    }

    private Map<String, SimpleConfigEntity> formatKieResponse(KieResponse response) {
        final HashMap<String, SimpleConfigEntity> latestData = new HashMap<String, SimpleConfigEntity>();
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            return latestData;
        }
        for (KieConfigEntity entity : response.getData()) {
            latestData.put(entity.getKey(), new SimpleConfigEntity(entity.getValue(), entity.getLabels()));
        }
        return latestData;
    }

    public static class SimpleConfigEntity {
        private String value;

        private Map<String, String> labels;

        public SimpleConfigEntity(String value, Map<String, String> labels) {
            this.value = value;
            this.labels = labels;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public void setLabels(Map<String, String> labels) {
            this.labels = labels;
        }
    }

    /**
     * 数据变更
     */
    public static class EventDataHolder {
        /**
         * 修改的key
         */
        private Map<String, SimpleConfigEntity> modified;

        /**
         * 删除的key
         */
        private Map<String, SimpleConfigEntity> deleted;

        /**
         * 新增key
         */
        private Map<String, SimpleConfigEntity> added;

        public EventDataHolder() {
            modified = new HashMap<String, SimpleConfigEntity>();
            deleted = new HashMap<String, SimpleConfigEntity>();
            added = new HashMap<String, SimpleConfigEntity>();
        }

        public Map<String, SimpleConfigEntity> getModified() {
            return modified;
        }

        public void setModified(Map<String, SimpleConfigEntity> modified) {
            this.modified = modified;
        }

        public Map<String, SimpleConfigEntity> getDeleted() {
            return deleted;
        }

        public void setDeleted(Map<String, SimpleConfigEntity> deleted) {
            this.deleted = deleted;
        }

        public Map<String, SimpleConfigEntity> getAdded() {
            return added;
        }

        public void setAdded(Map<String, SimpleConfigEntity> added) {
            this.added = added;
        }

        public boolean isChanged() {
            return !added.isEmpty() || !deleted.isEmpty() || !modified.isEmpty();
        }
    }
}
