/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol;

import com.alibaba.csp.sentinel.init.InitExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

/**
 * 文件名：DashboardApplication
 * 版权：
 * 描述：启动类
 * 修改人：Gaofang Wu
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 * 修改时间：2020/12/2
 * 跟踪单号：
 * 修改单号：
 * 修改内容：添加zuul
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(value = {"com.huawei.flowcontrol.console"})
public class DashboardApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(DashboardApplication.class);
    }

    private void triggerSentinelInit() {
        new Thread(() -> InitExecutor.doInit()).start();
    }

    /**
     * 初始化告警配置和sentinel
     */
    @PostConstruct
    public void init() {
        triggerSentinelInit();
    }
}
