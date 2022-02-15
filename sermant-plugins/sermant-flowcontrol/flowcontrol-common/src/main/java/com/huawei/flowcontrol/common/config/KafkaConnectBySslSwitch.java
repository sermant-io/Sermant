/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.common.config;

import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import java.util.Properties;

/**
 * kafka是否通过ssl认证开关类
 *
 * @author zhouss
 * @since 2022-02-10
 */
public class KafkaConnectBySslSwitch {
    private KafkaConnectBySslSwitch() {
    }

    /**
     * 删除非ssl认证的配置属性
     *
     * @param properties Properties对象
     */
    public static void delKey(Properties properties) {
        final FlowControlConfig flowControlConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        if (!flowControlConfig.isKafkaSsl()) {
            properties.remove(ConfigConst.KAFKA_JAAS_CONFIG_CONST);
            properties.remove(ConfigConst.KAFKA_SASL_MECHANISM_CONST);
            properties.remove(ConfigConst.KAFKA_SECURITY_PROTOCOL_CONST);
            properties.remove(ConfigConst.KAFKA_SSL_TRUSTSTORE_LOCATION_CONST);
            properties.remove(ConfigConst.KAFKA_SSL_TRUSTSTORE_PASSWORD_CONST);
            properties.remove(ConfigConst.KAFKA_IDENTIFICATION_ALGORITHM_CONST);
        }
    }
}
