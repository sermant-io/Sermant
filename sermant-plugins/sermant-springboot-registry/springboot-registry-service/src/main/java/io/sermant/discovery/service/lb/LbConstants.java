/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.discovery.service.lb;

import io.sermant.discovery.config.LbConfig;

/**
 * Load balancing constants
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class LbConstants {
    /**
     * Registration tags
     */
    public static final String SERMANT_DISCOVERY = "sermant-discovery";

    /**
     * The unit in which minutes are converted to seconds
     */
    public static final int MIN_TO_SEC = 60;

    /**
     * The unit in which seconds are converted into milliseconds
     */
    public static final int SEC_TO_MS = 1000;

    /**
     * How long before the instance expires to start refreshing the instance list
     */
    public static final long GAP_MS_BEFORE_EXPIRE_MS = 10000L;

    /**
     * The policy of executing GAP_MS_BEFORE_EXPIRE_SEC only when {@link LbConfig#getInstanceCacheExpireTime()} * 1000
     * is greater than MIN_GAP_MS_BEFORE_EXPIRE_SEC
     */
    public static final long MIN_GAP_MS_BEFORE_EXPIRE_MS = 50000L;

    /**
     * If the refresh time is less than GAP_MS_BEFORE_EXPIRE_SEC, the instance list is refreshed before
     * 0.1*{@link LbConfig#getInstanceCacheExpireTime()} * 1000
     */
    public static final float GAP_MS_BEFORE_EXPIRE_DELTA = 0.1f;

    private LbConstants() {
    }
}
