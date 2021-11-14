/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.rtc.common.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * kafka客户端的配置类
 * <p>
 * 现在只是简单配置：后期可能还有加入用户名密码认证等信息
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@Configuration
public class KafkaAdminClientConfig {
    /**
     * kafka客户端连接kafka服务器请求超时的时间
     */
    private static final int REQUEST_TIMEOUT_MS = 10000;
    /**
     * kafka服务器地址
     */
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String servers;

    /**
     * 该方法主要是用于获取配置信息，整体用properties存起来
     *
     * @return 返回一个properties对象
     */
    public Properties adminConfigs() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, REQUEST_TIMEOUT_MS);
        return props;
    }

    /**
     * 往ioc容器中注入KafkaAdminClient对象
     *
     * @return admin客户端
     */
    @Bean
    public AdminClient kafkaAdminClient() {
        return AdminClient.create(adminConfigs());
    }
}
