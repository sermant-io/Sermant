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

package com.huawei.flowcontrol.common.util;

import com.huawei.flowcontrol.common.config.ConfigConst;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.config.KafkaConnectBySslSwitch;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 配置信息工具类
 *
 * @author zhouss
 * @since 2022-02-11
 */
public class PluginConfigUtil {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static Properties properties;

    private PluginConfigUtil() {
    }

    static {
        // 待后续接入配置中心
        setPropertiesFromFlowControlConfig(PluginConfigManager.getPluginConfig(FlowControlConfig.class));
    }

    /**
     * 通过Key获取Value
     *
     * @param key key值
     * @return String 通过key获取value
     */
    public static String getValueByKey(String key) {
        final Object property = properties.get(key);
        if (property == null) {
            return "";
        }
        return String.valueOf(property).trim();
    }

    /**
     * 将配置类的配置设置到properties，减少代码修改
     */
    private static void setPropertiesFromFlowControlConfig(FlowControlConfig flowControlConfig) {
        properties = new Properties();
        setValueForProperties(flowControlConfig);
        LOGGER.info("All flow-control config following.");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            LOGGER.info(String.format(Locale.ENGLISH, "%s = %s", entry.getKey(), entry.getValue()));
        }
    }

    private static void setValueForProperties(FlowControlConfig flowControlConfig) {
        properties.put(ConfigConst.SENTINEL_VERSION, flowControlConfig.getSentinelVersion());
        properties.put(ConfigConst.ZOOKEEPER_ADDRESS, flowControlConfig.getZookeeperAddress());
        properties.put(ConfigConst.ZOOKEEPER_PATH, flowControlConfig.getFlowControlZookeeperPath());
        properties.put(ConfigConst.DEFAULT_HEARTBEAT_INTERVAL, flowControlConfig.getHeartbeatInterval());
        properties.put(ConfigConst.DEFAULT_METRIC_INTERVAL, flowControlConfig.getMetricInterval());
        properties.put(ConfigConst.METRIC_INITIAL_DURATION, flowControlConfig.getMetricInitialDuration());
        properties.put(ConfigConst.METRIC_MAX_LINE, flowControlConfig.getMetricMaxLine());
        properties.put(ConfigConst.METRIC_SLEEP_TIME, flowControlConfig.getMetricSleepTime());
        properties.put(ConfigConst.KAFKA_BOOTSTRAP_SERVERS, flowControlConfig.getKafkaBootstrapServers());
        properties.put(ConfigConst.KAFKA_KEY_SERIALIZER, flowControlConfig.getKafkaKeySerializer());
        properties.put(ConfigConst.KAFKA_VALUE_SERIALIZER, flowControlConfig.getKafkaValueSerializer());
        properties.put(ConfigConst.KAFKA_METRIC_TOPIC, flowControlConfig.getKafkaMetricTopic());
        properties.put(ConfigConst.KAFKA_HEARTBEAT_TOPIC, flowControlConfig.getKafkaHeartbeatTopic());
        properties.put(ConfigConst.KAFKA_ACKS, flowControlConfig.getKafkaAcks());
        properties.put(ConfigConst.KAFKA_MAX_REQUEST_SIZE, flowControlConfig.getKafkaMaxRequestSize());
        properties.put(ConfigConst.KAFKA_BUFFER_MEMORY, flowControlConfig.getKafkaBufferMemory());
        properties.put(ConfigConst.KAFKA_RETRIES, flowControlConfig.getKafkaRetries());
        properties.put(ConfigConst.KAFKA_REQUEST_TIMEOUT_MS, flowControlConfig.getKafkaRequestTimeoutMs());
        properties.put(ConfigConst.KAFKA_MAX_BLOCK_MS, flowControlConfig.getKafkaMaxBlockMs());
        properties.put(ConfigConst.KAFKA_JAAS_CONFIG_CONST, flowControlConfig.getKafkaJaasConfig());
        properties.put(ConfigConst.KAFKA_SASL_MECHANISM_CONST, flowControlConfig.getKafkaSaslMechanism());
        properties.put(ConfigConst.KAFKA_SECURITY_PROTOCOL_CONST, flowControlConfig.getKafkaSecurityProtocol());
        properties.put(ConfigConst.KAFKA_SSL_TRUSTSTORE_LOCATION_CONST,
                flowControlConfig.getKafkaSslTruststoreLocation());
        properties.put(ConfigConst.KAFKA_SSL_TRUSTSTORE_PASSWORD_CONST,
                flowControlConfig.getKafkaSslTruststorePassword());
        properties.put(ConfigConst.KAFKA_IDENTIFICATION_ALGORITHM_CONST,
                flowControlConfig.getKafkaIdentificationAlgorithm());
        properties.put(ConfigConst.CONFIG_ZOOKEEPER_PATH, flowControlConfig.getConfigZookeeperPath());
        properties.put(ConfigConst.CONFIG_PROFILE_ACTIVE_DEFAULT, flowControlConfig.getConfigProfileActive());
        properties.put(ConfigConst.CONFIG_KIE_ADDRESS, flowControlConfig.getConfigKieAddress());
        properties.put(ConfigConst.CONFIG_CENTER_TYPE, flowControlConfig.getConfigCenterType());
        KafkaConnectBySslSwitch.delKey(properties);
    }
}
