/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.config;

import java.util.HashSet;
import java.util.Set;

/**
 * Message queue prohibits consumption configuration
 *
 * @author lilai
 * @since 2023-12-07
 */
public class ProhibitionConfig {
    private boolean enableKafkaProhibition = false;

    private Set<String> kafkaTopics = new HashSet<>();

    private boolean enableRocketMqProhibition = false;

    private Set<String> rocketMqTopics = new HashSet<>();

    public boolean isEnableKafkaProhibition() {
        return enableKafkaProhibition;
    }

    public void setEnableKafkaProhibition(boolean enableKafkaProhibition) {
        this.enableKafkaProhibition = enableKafkaProhibition;
    }

    public Set<String> getKafkaTopics() {
        return kafkaTopics;
    }

    public void setKafkaTopics(Set<String> kafkaTopics) {
        this.kafkaTopics = kafkaTopics;
    }

    public boolean isEnableRocketMqProhibition() {
        return enableRocketMqProhibition;
    }

    public void setEnableRocketMqProhibition(boolean enableRocketMqProhibition) {
        this.enableRocketMqProhibition = enableRocketMqProhibition;
    }

    public Set<String> getRocketMqTopics() {
        return rocketMqTopics;
    }

    public void setRocketMqTopics(Set<String> rocketMqTopics) {
        this.rocketMqTopics = rocketMqTopics;
    }

    @Override
    public String toString() {
        return "enableKafkaProhibition=" + enableKafkaProhibition + ", kafkaTopics=" + kafkaTopics + "; "
                + "enableRocketMqProhibition=" + enableRocketMqProhibition + ", rocketMqTopics=" + rocketMqTopics;
    }
}
