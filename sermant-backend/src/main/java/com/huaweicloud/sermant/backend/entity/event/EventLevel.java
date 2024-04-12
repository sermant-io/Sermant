/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.backend.entity.event;

/**
 * Event level
 *
 * @author luanwenfei
 * @since 2023-03-04
 */
public enum EventLevel {
    /**
     * EMERGENCY event
     */
    EMERGENCY(300),

    /**
     * IMPORTANT event
     */
    IMPORTANT(200),

    /**
     * NORMAL event
     */
    NORMAL(100);

    private final int levelThreshold;

    EventLevel(int levelThreshold) {
        this.levelThreshold = levelThreshold;
    }

    public int getLevelThreshold() {
        return levelThreshold;
    }
}
