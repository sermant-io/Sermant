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

package com.huaweicloud.sermant.implement.service.dynamicconfig.kie.listener;

import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.kie.KieConfigEntity;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.kie.KieResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Listen to response data of key，used to compare old and new data and retain old data
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class KvDataHolder {
    /**
     * Current data
     */
    private Map<String, String> currentData;

    /**
     * Analyze the latest data
     *
     * @param isFirst Whether it is the first notification
     * @param response Latest data
     * @return EventDataHolder
     */
    public EventDataHolder analyzeLatestData(KieResponse response, boolean isFirst) {
        if (isFirst) {
            clear();
        }
        final Map<String, String> latestData = formatKieResponse(response);
        final EventDataHolder eventDataHolder = new EventDataHolder(formatRevision(response.getRevision()), latestData);
        if (currentData != null) {
            if (latestData.isEmpty()) {
                eventDataHolder.deleted.putAll(currentData);
            } else {
                Map<String, String> temp = new HashMap<String, String>(currentData);
                for (Map.Entry<String, String> entry : latestData.entrySet()) {
                    final String value = currentData.get(entry.getKey());
                    if (value == null) {
                        // Added key
                        eventDataHolder.added.put(entry.getKey(), entry.getValue());
                    } else {
                        // If the key exists, then compare the value
                        if (!value.equals(entry.getValue())) {
                            // modify
                            eventDataHolder.modified.put(entry.getKey(), entry.getValue());
                        }
                    }
                    temp.remove(entry.getKey());
                }

                // The keys left by temp are deleted
                eventDataHolder.deleted.putAll(temp);
            }
        } else {
            eventDataHolder.added.putAll(latestData);
        }
        currentData = latestData;
        return eventDataHolder;
    }

    private void clear() {
        if (currentData != null) {
            currentData.clear();
            currentData = null;
        }
    }

    private long formatRevision(String revision) {
        if (revision == null) {
            return 0L;
        }
        try {
            return Long.parseLong(revision);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private Map<String, String> formatKieResponse(KieResponse response) {
        final HashMap<String, String> latestData = new HashMap<String, String>();
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            return latestData;
        }
        for (KieConfigEntity entity : response.getData()) {
            latestData.put(entity.getKey(), entity.getValue());
        }
        return latestData;
    }

    /**
     * Changed data
     *
     * @since 2021-11-18
     */
    public static class EventDataHolder {
        /**
         * version
         */
        private final long version;

        /**
         * The latest full data
         */
        private final Map<String, String> latestData;

        /**
         * modified key
         */
        private Map<String, String> modified;

        /**
         * deleted key
         */
        private Map<String, String> deleted;

        /**
         * added key
         */
        private Map<String, String> added;

        /**
         * Constructor.
         *
         * @param version version
         * @param latestData Latest full data
         */
        public EventDataHolder(long version, Map<String, String> latestData) {
            modified = new HashMap<String, String>();
            deleted = new HashMap<String, String>();
            added = new HashMap<String, String>();
            this.version = version;
            this.latestData = latestData;
        }

        public Map<String, String> getLatestData() {
            return latestData;
        }

        public Map<String, String> getModified() {
            return modified;
        }

        public void setModified(Map<String, String> modified) {
            this.modified = modified;
        }

        public Map<String, String> getDeleted() {
            return deleted;
        }

        public void setDeleted(Map<String, String> deleted) {
            this.deleted = deleted;
        }

        public Map<String, String> getAdded() {
            return added;
        }

        public void setAdded(Map<String, String> added) {
            this.added = added;
        }

        /**
         * whether the behavior changes
         *
         * @return boolean
         */
        public boolean isChanged() {
            return !added.isEmpty() || !deleted.isEmpty() || !modified.isEmpty();
        }

        public long getVersion() {
            return version;
        }
    }
}
