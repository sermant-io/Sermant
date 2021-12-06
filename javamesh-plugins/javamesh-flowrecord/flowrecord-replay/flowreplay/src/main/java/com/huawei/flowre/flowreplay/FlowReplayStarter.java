/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
