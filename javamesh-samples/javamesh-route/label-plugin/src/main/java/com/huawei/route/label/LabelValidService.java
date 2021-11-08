/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.label;

import com.huawei.apm.core.lubanops.bootstrap.config.IdentityConfigManager;
import com.huawei.apm.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.apm.core.service.CoreServiceManager;
import com.huawei.apm.core.service.heartbeat.HeartbeatInterval;
import com.huawei.apm.core.service.heartbeat.HeartbeatService;
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

    /**
     * 服务启动入口
     *
     * @param port 指定的netty端口
     */
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

    /**
     * 服务停止
     */
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
        if (StringUtils.isBlank(instanceName) || StringUtils.equals(instanceName, LabelConstants.DEFAULT_INSTANCE_NAME)) {
            // 为确保实例名不同，如果未默认名称,则使用uuid生成实例名称
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            return uuid.substring(uuid.length() - LabelConstants.INSTANCE_NAME_PREFIX_LEN) + "@" + IpUtil.getIpV4();
        }
        return instanceName;
    }
}
