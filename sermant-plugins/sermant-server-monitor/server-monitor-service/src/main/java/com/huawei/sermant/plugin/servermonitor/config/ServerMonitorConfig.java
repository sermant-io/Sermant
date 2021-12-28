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

package com.huawei.sermant.plugin.servermonitor.config;

import com.huawei.sermant.core.config.common.ConfigTypeKey;
import com.huawei.sermant.core.plugin.config.PluginConfig;
import lombok.Data;

@Data
@ConfigTypeKey("server.monitor")
public class ServerMonitorConfig implements PluginConfig {
    private static final int DEFAULT_COLLECT_INTERVAL_SEC = 1;
    private static final int DEFAULT_CONSUME_INTERVAL_SEC = 60;
    private static final String DEFAULT_TIME_UNIT = "SECONDS";

    private long collectInterval = DEFAULT_COLLECT_INTERVAL_SEC;
    private long consumeInterval = DEFAULT_CONSUME_INTERVAL_SEC;
    private String timeunit = DEFAULT_TIME_UNIT;
}
