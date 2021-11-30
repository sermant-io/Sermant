/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay;

import com.huawei.flowre.flowreplay.service.FlowReplayWorker;
import com.huawei.flowre.flowreplay.service.ResultCompareService;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Flow Replay 启动类
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-03-03
 */
@SpringBootApplication
public class FlowReplayStarter {
    @Autowired
    FlowReplayWorker flowReplayWorker;

    @Autowired
    ResultCompareService resultCompareService;

    @Autowired
    CuratorFramework zkClient;

    public static void main(String[] args) {
        SpringApplication.run(FlowReplayStarter.class);
    }
}
