/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.mq.grayscale.config;

import io.sermant.core.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * grayscale item entity
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class GrayTagItem {
    private String consumerGroupTag;

    private Map<String, String> serviceMeta = new HashMap<>();

    private Map<String, String> trafficTag = new HashMap<>();

    public String getConsumerGroupTag() {
        return consumerGroupTag;
    }

    public void setConsumerGroupTag(String consumerGroupTag) {
        this.consumerGroupTag = consumerGroupTag;
    }

    public Map<String, String> getServiceMeta() {
        return serviceMeta;
    }

    public void setServiceMeta(Map<String, String> serviceMeta) {
        this.serviceMeta = serviceMeta;
    }

    public Map<String, String> getTrafficTag() {
        return trafficTag;
    }

    public void setTrafficTag(Map<String, String> trafficTag) {
        this.trafficTag = trafficTag;
    }

    /**
     * match grayscale tag with serviceMeta info
     *
     * @param properties serviceMeta
     * @return isMatch
     */
    public boolean matchPropertiesByServiceMeta(Map<String, String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (serviceMeta.containsKey(entry.getKey())
                    && StringUtils.equals(serviceMeta.get(entry.getKey()), entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * update traffic tags
     *
     * @param grayscale grayscale
     */
    public void updateTrafficTags(List<GrayTagItem> grayscale) {
        for (GrayTagItem item : grayscale) {
            if (StringUtils.equals(consumerGroupTag, item.getConsumerGroupTag())) {
                setTrafficTag(item.getTrafficTag());
                return;
            }
        }
    }
}
