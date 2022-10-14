/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.huaweicloud.sermant.message.common.config;

import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;

/**
 * 禁消费的启用配置类<br>
 *
 * @author yuzl 俞真龙
 * @since 2022-10-13
 */
@ConfigTypeKey("deny.consume.plugin")
public class DenyConsumeConfig implements PluginConfig {
    /**
     * 是否开启rabbitmq禁消费
     */
    private boolean enableRabbitmqDeny;

    /**
     * 是否开启kafka禁消费
     */
    private boolean enableKafkaDeny;

    public boolean isEnableRabbitmqDeny() {
        return enableRabbitmqDeny;
    }

    public void setEnableRabbitmqDeny(boolean enableRabbitmqDeny) {
        this.enableRabbitmqDeny = enableRabbitmqDeny;
    }

    public boolean isEnableKafkaDeny() {
        return enableKafkaDeny;
    }

    public void setEnableKafkaDeny(boolean enableKafkaDeny) {
        this.enableKafkaDeny = enableKafkaDeny;
    }
}
