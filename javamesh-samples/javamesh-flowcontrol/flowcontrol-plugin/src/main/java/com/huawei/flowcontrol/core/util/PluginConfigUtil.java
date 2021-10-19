/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.util;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.huawei.apm.bootstrap.config.ConfigLoader;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.flowcontrol.core.config.CommonConst;
import com.huawei.flowcontrol.core.config.ConfigConst;
import com.huawei.flowcontrol.core.config.FlowControlConfig;
import com.huawei.flowcontrol.core.config.KafkaConnectBySslSwitch;
import org.apache.curator.framework.CuratorFramework;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 配置信息工具类
 *
 * @author liyi
 * @since 2020-08-26
 */
public class PluginConfigUtil {
    private static final Logger LOGGER = LogFactory.getLogger();
    private static String active;
    private static Properties properties;

    private PluginConfigUtil() {
    }

    static {
        // 待后续接入配置中心
        setPropertiesFromFlowControlConfig(ConfigLoader.getConfig(FlowControlConfig.class));
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
     * 加载配置参数
     *
     * @return Properties对象
     */
    public static Properties loadProp() {
        Properties prop = new Properties();
        Map<String, String> map = loadZk();
        if (map.isEmpty()) {
            RecordLog.error("[PluginConfigUtil] failed to load configuration center file,active=" + active);
            LOGGER.warning(String.format("[PluginConfigUtil] failed to load configuration center file,active=[{%s}]", active));
            return new Properties();
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            prop.put(entry.getKey(), entry.getValue());
        }
        RecordLog.info("[PluginConfigUtil] loading configuration center file succeeded,active=" + active);
        return prop;
    }

    /**
     * 从zookeeper中加载配置信息
     *
     * @return Map 存储从zookeeper中获取的流控规则配置
     */
    private static Map<String, String> loadZk() {
        Map<String, String> configProperties = new HashMap<String, String>();
        CuratorFramework zkClient = ZookeeperConnectionEnum.INSTANCE.getZookeeperConnection();
        if (zkClient == null) {
            return new HashMap<String, String>();
        }
        try {
            String rootInfo = new String(zkClient.getData().forPath(
                ConfigLoader.getConfig(FlowControlConfig.class).getConfigZookeeperPath() + CommonConst.SLASH_SIGN + active), Charset.forName("UTF-8"));
            String[] configData = rootInfo.split(CommonConst.NEWLINE_SIGN);
            for (String line : configData) {
                String[] values = line.split(CommonConst.EQUAL_SIGN);
                if (values.length > 1) {
                    configProperties.put(values[0], values[1]);
                }
            }
        } catch (Exception e) {
            RecordLog.error("[PluginConfigUtil] loadZk " + e);
        }
        return configProperties;
    }

    /**
     * 将配置类的配置设置到properties，减少代码修改
     */
    private static void setPropertiesFromFlowControlConfig(FlowControlConfig flowControlConfig) {
        properties = new Properties();
        setValueForProperties(flowControlConfig);
        RecordLog.info("All flow-control config following.");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            RecordLog.info(entry.getKey() + " = " + entry.getValue());
        }
    }

    private static void setValueForProperties(FlowControlConfig flowControlConfig) {
        properties.put(ConfigConst.SENTINEL_VERSION,
            flowControlConfig.getSentinelVersion());
        properties.put(ConfigConst.SENTINEL_ZOOKEEPER_ADDRESS,
            flowControlConfig.getSentinelZookeeperAddress());
        properties.put(ConfigConst.SENTINEL_ZOOKEEPER_PATH,
            flowControlConfig.getSentinelZookeeperPath());
        properties.put(ConfigConst.DEFAULT_HEARTBEAT_INTERVAL,
            flowControlConfig.getSentinelHeartbeatInterval());
        properties.put(ConfigConst.DEFAULT_METRIC_INTERVAL,
            flowControlConfig.getSentinelMetricInterval());
        properties.put(ConfigConst.METRIC_INITIAL_DURATION,
            flowControlConfig.getMetricInitialDuration());
        properties.put(ConfigConst.METRIC_MAXLINE,
            flowControlConfig.getMetricMaxLine());
        properties.put(ConfigConst.METRIC_SLEEP_TIME,
            flowControlConfig.getMetricSleepTime());
        properties.put(ConfigConst.KAFKA_BOOTSTRAP_SERVERS,
            flowControlConfig.getKafkaBootstrapServers());
        properties.put(ConfigConst.KAFKA_KEY_SERIALIZER,
            flowControlConfig.getKafkaKeySerializer());
        properties.put(ConfigConst.KAFKA_VALUE_SERIALIZER,
            flowControlConfig.getKafkaValueSerializer());
        properties.put(ConfigConst.KAFKA_METRIC_TOPIC,
            flowControlConfig.getKafkaMetricTopic());
        properties.put(ConfigConst.KAFKA_HEARTBEAT_TOPIC,
            flowControlConfig.getKafkaHeartbeatTopic());
        properties.put(ConfigConst.KAFKA_ACKS,
            flowControlConfig.getKafkaAcks());
        properties.put(ConfigConst.KAFKA_MAX_REQUEST_SIZE,
            flowControlConfig.getKafkaMaxRequestSize());
        properties.put(ConfigConst.KAFKA_BUFFER_MEMORY,
            flowControlConfig.getKafkaBufferMemory());
        properties.put(ConfigConst.KAFKA_RETRIES, flowControlConfig.getKafkaRetries());
        properties.put(ConfigConst.KAFKA_REQUEST_TIMEOUT_MS,
            flowControlConfig.getKafkaRequestTimeoutMs());
        properties.put(ConfigConst.KAFKA_MAX_BLOCK_MS,
            flowControlConfig.getKafkaMaxBlockMs());
        properties.put(ConfigConst.KAFKA_JAAS_CONFIG_CONST,
            flowControlConfig.getKafkaJaasConfig());
        properties.put(ConfigConst.KAFKA_SASL_MECHANISM_CONST,
            flowControlConfig.getKafkaSaslMechanism());
        properties.put(ConfigConst.KAFKA_SECURITY_PROTOCOL_CONST,
            flowControlConfig.getKafkaSecurityProtocol());
        properties.put(ConfigConst.KAFKA_SSL_TRUSTSTORE_LOCATION_CONST,
            flowControlConfig.getKafkaSslTruststoreLocation());
        properties.put(ConfigConst.KAFKA_SSL_TRUSTSTORE_PASSWORD_CONST,
            flowControlConfig.getKafkaSslTruststorePassword());
        properties.put(ConfigConst.KAFKA_IDENTIFICATION_ALGORITHM_CONST,
            flowControlConfig.getKafkaIdentificationAlgorithm());
        properties.put(ConfigConst.CONFIG_ZOOKEEPER_PATH,
            flowControlConfig.getConfigZookeeperPath());
        properties.put(ConfigConst.CONFIG_PROFILE_ACTIVE_DEFAULT,
            flowControlConfig.getConfigProfileActive());
        properties.put(ConfigConst.CONFIG_KIE_ADDRESS,
            flowControlConfig.getConfigKieAddress());
        properties.put(ConfigConst.SENTINEL_CONFIG_TYPE,
            flowControlConfig.getSentinelConfigCenterType());
        KafkaConnectBySslSwitch.delKey(properties);
        properties.put(ConfigConst.REDIS_HOST, flowControlConfig.getRedisHost());
        properties.put(ConfigConst.REDIS_PORT, flowControlConfig.getRedisPort());
        properties.put(ConfigConst.REDIS_URIS, flowControlConfig.getRedisUris());
    }
}
