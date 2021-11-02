/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.label;

import com.huawei.apm.bootstrap.boot.CoreServiceManager;
import com.huawei.apm.bootstrap.boot.heartbeat.HeartbeatInterval;
import com.huawei.apm.bootstrap.boot.heartbeat.HeartbeatService;
import com.huawei.apm.bootstrap.lubanops.config.IdentityConfigManager;
import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;
import com.huawei.route.common.factory.NamedThreadFactory;
import com.huawei.route.common.label.heartbeat.HeartbeatInfoProvider;
import com.huawei.route.common.utils.IpUtil;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * netty接收插件初始化
 *
 * @author zhouss
 * @since 2021-11-01
 */
public enum LabelValidService {
    /**
     * 标签库初始化实例
     */
    INSTANCE;
    /**
     * 线程空闲时间
     */
    private static final int KEEP_ALIVE_TIME_MS = 1000;

    /**
     * 标签库心跳名称
     */
    private static final String HEARTBEAT_NAME = "TAG_HEARTBEAT";

    /**
     * 是否初始化
     */
    private volatile boolean isInit = false;

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1,
            KEEP_ALIVE_TIME_MS, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new NamedThreadFactory("Thread-LabelValid-Service"));

    public synchronized void start(final int port) {
        if (isInit) {
            return;
        }
        isInit = true;
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                new LabelValidServer().start(port);
            }
        });
        initHeartbeat(port);
    }

    public synchronized void stop() {
        threadPoolExecutor.shutdown();
    }

    private void initHeartbeat(int port) {
        HeartbeatService heartbeatService = CoreServiceManager.INSTANCE.getService(HeartbeatService.class);
        heartbeatService.heartbeat(HEARTBEAT_NAME, HeartbeatInfoProvider.getInstance(),
                HeartbeatInterval.OFTEN);
        // 新增心跳内容
        HeartbeatInfoProvider.getInstance()
                .registerHeartMsg("netty.ip", IpUtil.getIpV4())
                .registerHeartMsg("netty.port", String.valueOf(port))
                .registerHeartMsg("serviceName", IdentityConfigManager.getAppName())
                .registerHeartMsg("instanceName", getInstanceName());
    }

    private String getInstanceName() {
        String instanceName = IdentityConfigManager.getInstanceName();
        return StringUtils.isBlank(instanceName)
                ? UUID.randomUUID().toString().replaceAll("-", "") + "@" + IpUtil.getIpV4()
                : instanceName;
    }

}
