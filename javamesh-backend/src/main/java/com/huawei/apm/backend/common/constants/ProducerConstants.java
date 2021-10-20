/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.backend.common.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 生产者常量类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
public class ProducerConstants {
    private ProducerConstants(){
    }

    // producer config map的初始大小
    private static final int PRODUCER_CONFIG_CAPACITY = 16;

    // 生产者需要导入的自定义配置
    public static final Map<String, String> PRODUCER_CONFIG = new HashMap<>(PRODUCER_CONFIG_CAPACITY);

    /**
     * 是否通过ssl认证
     */
    public static final boolean KAFKA_IS_SSL = false;

    /**
     * jaas配置常量
     */
    public static final String KAFKA_JAAS_CONFIG_CONST = "sasl.jaas.config";

    /**
     * SASL鉴权方式常量
     */
    public static final String KAFKA_SASL_MECHANISM_CONST = "sasl.mechanism";

    /**
     * 加密协议常量
     */
    public static final String KAFKA_SECURITY_PROTOCOL_CONST = "security.protocol";

    /**
     * ssl truststore文件存放位置常量
     */
    public static final String KAFKA_SSL_TRUSTSTORE_LOCATION_CONST = "ssl.truststore.location";

    /**
     * ssl truststore密码常量
     */
    public static final String KAFKA_SSL_TRUSTSTORE_PASSWORD_CONST = "ssl.truststore.password";

    /**
     * 域名常量
     */
    public static final String KAFKA_SSL_IDENTIFICATION_ALGORITHM_CONST = "ssl.endpoint.identification.algorithm";
}
