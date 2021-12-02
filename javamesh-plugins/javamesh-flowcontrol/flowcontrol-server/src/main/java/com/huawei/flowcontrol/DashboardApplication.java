/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
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
