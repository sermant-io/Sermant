package com.huawei.flowcontrol.core.config;

import com.huawei.javamesh.core.plugin.config.PluginConfigManager;

import java.util.Properties;

/**
 * kafka是否通过ssl认证开关类
 */
public class KafkaConnectBySslSwitch {
    /**
     * 删除非ssl认证的配置属性
     *
     * @param properties Properties对象
     */
    public static void delKey(Properties properties) {
        final FlowControlConfig flowControlConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        if (!flowControlConfig.isKafkaIsSsl()) {
            properties.remove(ConfigConst.KAFKA_JAAS_CONFIG_CONST);
            properties.remove(ConfigConst.KAFKA_SASL_MECHANISM_CONST);
            properties.remove(ConfigConst.KAFKA_SECURITY_PROTOCOL_CONST);
            properties.remove(ConfigConst.KAFKA_SSL_TRUSTSTORE_LOCATION_CONST);
            properties.remove(ConfigConst.KAFKA_SSL_TRUSTSTORE_PASSWORD_CONST);
            properties.remove(ConfigConst.KAFKA_IDENTIFICATION_ALGORITHM_CONST);
        }
    }
}
