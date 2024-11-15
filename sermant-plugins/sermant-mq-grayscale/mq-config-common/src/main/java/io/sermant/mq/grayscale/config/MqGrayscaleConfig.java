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

import io.sermant.core.config.common.ConfigTypeKey;
import io.sermant.core.plugin.config.PluginConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * mqGrayscaleConfig entity
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
@ConfigTypeKey("grayscale.mq.config")
public class MqGrayscaleConfig implements PluginConfig {
    /**
     * afa symbol
     */
    private static final String AFA_SYMBOL = "@";

    private boolean enabled = false;

    private List<GrayTagItem> grayscale = new ArrayList<>();

    private BaseMessage base = new BaseMessage();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public BaseMessage getBase() {
        return base;
    }

    public void setBase(BaseMessage base) {
        this.base = base;
    }

    public List<GrayTagItem> getGrayscale() {
        return grayscale;
    }

    public void setGrayscale(List<GrayTagItem> grayscale) {
        this.grayscale = grayscale;
    }

    /**
     * return the corresponding traffic label based on serviceMeta matching result
     *
     * @param microServiceProperties serviceMeta
     * @return traffic tags
     */
    public Map<String, String> getGrayTagsByServiceMeta(Map<String, String> microServiceProperties) {
        Map<String, String> map = new HashMap<>();
        for (GrayTagItem grayTagItem : grayscale) {
            if (grayTagItem.matchPropertiesByServiceMeta(microServiceProperties)
                    && !grayTagItem.getTrafficTag().isEmpty()) {
                // set item traffic tags when serviceMeta match, because all message tag using traffic tags.
                map.putAll(grayTagItem.getTrafficTag());
            }
        }
        return map;
    }

    /**
     * return the traffic tag item based on serviceMeta properties matching result
     *
     * @param properties serviceMeta
     * @return gray tag item
     */
    public Optional<GrayTagItem> getMatchedGrayTagByServiceMeta(Map<String, String> properties) {
        for (GrayTagItem item : grayscale) {
            if (item.matchPropertiesByServiceMeta(properties)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /**
     * return the traffic tag item by grayGroupTag
     *
     * @param grayGroupTag grayGroupTag
     * @return gray tag item
     */
    public Optional<GrayTagItem> getGrayTagByGroupTag(String grayGroupTag) {
        for (GrayTagItem item : grayscale) {
            if (grayGroupTag.equals(item.getConsumerGroupTag())) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /**
     * build traffic tag properties to string
     *
     * @return traffic tag string
     */
    public String buildAllTrafficTagInfoToStr() {
        StringBuilder sb = new StringBuilder();
        for (GrayTagItem item : grayscale) {
            if (sb.length() > 0) {
                sb.append(AFA_SYMBOL);
            }
            sb.append(item.getConsumerGroupTag());
            for (Map.Entry<String, String> entry : item.getTrafficTag().entrySet()) {
                sb.append(entry.getKey())
                        .append(AFA_SYMBOL)
                        .append(entry.getValue());
            }
        }
        return sb.toString();
    }

    /**
     * compare source/target MqGrayscaleConfig excludeGroupTags config whether to change
     *
     * @param target MqGrayscaleConfig
     * @return isChanged
     */
    public boolean isBaseExcludeGroupTagsChanged(MqGrayscaleConfig target) {
        HashSet<String> targetBaseExcludeTags = new HashSet<>(target.getBase().getExcludeGroupTags());
        HashSet<String> sourceBaseExcludeTags = new HashSet<>(base.getExcludeGroupTags());
        return !(targetBaseExcludeTags.size() == sourceBaseExcludeTags.size()
                && targetBaseExcludeTags.containsAll(sourceBaseExcludeTags));
    }

    /**
     * compare source/target MqGrayscaleConfig consumerType config whether to change
     *
     * @param target MqGrayscaleConfig
     * @return isChanged
     */
    public boolean isConsumerModeChanged(MqGrayscaleConfig target) {
        ConsumeModeEnum sourceMode = getBase().getConsumeMode();
        ConsumeModeEnum targetMode = target.getBase().getConsumeMode();
        return sourceMode != targetMode;
    }

    /**
     * update base info/traffic tags
     *
     * @param config config
     */
    public void updateGrayscaleConfig(MqGrayscaleConfig config) {
        setBase(config.getBase());
        setEnabled(config.isEnabled());
        if (config.getGrayscale().isEmpty()) {
            return;
        }
        for (GrayTagItem item : getGrayscale()) {
            item.updateTrafficTags(config.getGrayscale());
        }
    }
}
