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

import java.util.ArrayList;
import java.util.List;

/**
 * base consume mode entity
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class BaseMessage {
    /**
     * default value
     */
    private static final long DEFAULT = 15L;

    private ConsumeModeEnum consumeMode = ConsumeModeEnum.AUTO;

    private long autoCheckDelayTime = DEFAULT;

    private List<String> excludeGroupTags = new ArrayList<>();

    public ConsumeModeEnum getConsumeMode() {
        return consumeMode;
    }

    public void setConsumeMode(ConsumeModeEnum consumeMode) {
        this.consumeMode = consumeMode;
    }

    public long getAutoCheckDelayTime() {
        return autoCheckDelayTime;
    }

    public void setAutoCheckDelayTime(long autoCheckDelayTime) {
        this.autoCheckDelayTime = autoCheckDelayTime;
    }

    public List<String> getExcludeGroupTags() {
        return excludeGroupTags;
    }

    public void setExcludeGroupTags(List<String> excludeGroupTags) {
        this.excludeGroupTags = excludeGroupTags;
    }
}
