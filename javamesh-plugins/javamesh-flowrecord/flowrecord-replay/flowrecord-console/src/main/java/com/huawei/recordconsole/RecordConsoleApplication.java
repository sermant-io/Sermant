/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.recordconsole;

import com.huawei.recordconsole.consumer.RecordConsoleConsumer;
import com.huawei.recordconsole.elasticsearch.ElasticSearchIndexCreator;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * Springboot主入口
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-02-19
 */

@SpringBootApplication
public class RecordConsoleApplication implements ApplicationRunner {
    /**
     * 自动注入自定义的kafka消费者consumer对象
     */
    @Autowired
    RecordConsoleConsumer consumer;
    @Autowired
    private ElasticSearchIndexCreator elasticSearchimpl;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public static void main(String[] args) {
        SpringApplication.run(RecordConsoleApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException, IOException {
        startSubscribeKafka();
    }

    /**
     * 启动kafka consumer
     */
    public void startSubscribeKafka() {
        consumer.start();
    }
}