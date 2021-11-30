/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.mockserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Mock Server 启动类
 *
 * @author luanwenfei
 * @version 1.0
 * @since 2021-02-03
 */
@SpringBootApplication
public class MockServerStarter {
    public static void main(String[] args) {
        SpringApplication.run(MockServerStarter.class, args);
    }
}
