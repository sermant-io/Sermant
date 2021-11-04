/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report;

import com.huawei.apm.core.service.PluginService;
import com.huawei.apm.core.config.ConfigLoader;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.route.common.factory.NamedThreadFactory;
import com.huawei.route.common.label.observers.LabelObservers;
import com.huawei.route.common.report.acquire.TargetAddrAcquire;
import com.huawei.route.common.report.cache.ServiceRegisterCache;
import com.huawei.route.common.report.common.entity.ServiceRegisterMessage;
import com.huawei.route.report.observers.GrayConfigurationObserver;
import com.huawei.route.report.observers.LdcConfigurationObserver;
import com.huawei.route.common.report.print.LoggerPrintManager;
import com.huawei.route.report.send.ServiceRegistrarMessageSender;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 上报插件初始化
 *
 * @author zhouss
 * @since 2021-11-02
 */
public class ReporterPluginInitServiceImpl implements PluginService {
    private static final Logger LOGGER = LogFactory.getLogger();
    private static ServiceRegistrarMessageSender httpSender;
    private volatile ScheduledFuture<?> sendFuture;

    public void run() {
        int sendMessageSize = ServiceRegisterCache.getInstance().needSendMessageSize();
        if (sendMessageSize > 0) {
            Set<ServiceRegisterMessage> list = ServiceRegisterCache.getInstance().getServiceRegisterMessageList();
            try {
                Set<ServiceRegisterMessage> registerMessages = httpSender.sendServiceRegisterMessage(list);
                ServiceRegisterCache.getInstance().addServiceRegisterMessage(registerMessages);
                // 将发送成功的缓存下来为后面ldc变动的时候再一次的发送准备
                ServiceRegisterCache.getInstance().addOldServiceRegisterMessage(list);
            } catch (Throwable t) {
                ServiceRegisterCache.getInstance().addServiceRegisterMessage(list);

                // 判断错误日志是否打印，如果接口出现问题频繁打印影响其他问题定位
                if (LoggerPrintManager.getInstance().shouldPrintLogger()) {
                    LOGGER.log(Level.WARNING, "send ServiceRegistrarMessage to route server fail. {}",
                            t.getMessage());
                }
                // 上报
            }
        }
    }

    @Override
    public void init() {
        final ReporterConfig config = ConfigLoader.getConfig(ReporterConfig.class);
        // 注册标签库修改监听
        LabelObservers.INSTANCE.registerLabelObservers(new LdcConfigurationObserver());
        LabelObservers.INSTANCE.registerLabelObservers(new GrayConfigurationObserver());
        // 启动定时器
        sendFuture = Executors.newSingleThreadScheduledExecutor(
                new NamedThreadFactory("registerMessage-sender"))
                .scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ReporterPluginInitServiceImpl.this.run();
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "registerMessage sender and upload failure.", e);
                        }
                    }
                }, config.getReportIntervalMs(), config.getReportIntervalMs(), TimeUnit.MICROSECONDS);

        // 初始化上报器
        TargetAddrAcquire.initAcquire(config.getServerUrls());
        // 初始化发送器
        httpSender = new DefaultServiceRegisterMessageSender();
    }

    @Override
    public void stop() {
            sendFuture.cancel(true);
    }
}
