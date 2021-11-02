/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.register;

import com.huawei.route.common.RouteThreadFactory;
import com.huawei.route.server.entity.AbstractInstance;
import com.huawei.route.server.entity.AbstractService;
import com.huawei.route.server.entity.ServiceRegistrarMessage;
import com.huawei.route.server.rules.classifier.TagClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 处理注册服务信息同步, 并更新到缓存, 提供agent调用
 *
 * @author zhouss
 * @since 2021-10-09
 */
@Component
public class RegisterSyncManager<S extends AbstractService<T>, T extends AbstractInstance> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterSyncManager.class);

    /**
     * 同步对外接口
     */
    @Autowired
    private RegisterSync<S, T> registerSync;

    /**
     * 注册表数据分类
     */
    @Autowired
    private TagClassifier<S, T> classifier;

    /**
     * 同步间隔 单位MS
     */
    @Value("${register.sync.interval:5000}")
    private long intervalMs;

    /**
     * 缓存的注册信息
     */
    private Map<String, S> registerInfo;

    private ScheduledExecutorService scheduledExecutorService;

    @PostConstruct
    public void init() {
        registerInfo = new HashMap<>();
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1,
                new RouteThreadFactory("REGISTER_SYNC_THREAD"));
        scheduledExecutorService.scheduleAtFixedRate(new RegisterSyncTask(), 0,
                intervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 全量返回服务注册信息, 不考虑LDC
     *
     * @return 服务注册信息
     */
    public Map<String, S> getRegisterInfo() {
        return registerInfo;
    }

    /**
     * 上报数据更新
     *
     * @param serviceRegistrarMessages 上报数据
     */
    public void update(Collection<ServiceRegistrarMessage> serviceRegistrarMessages) {
        registerSync.update(serviceRegistrarMessages);
    }

    @PreDestroy
    public void destroy() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    class RegisterSyncTask implements Runnable {

        @Override
        public void run() {
            try {
                registerInfo = registerSync.sync();
                // 数据归类处理
                classifier.classifier(registerInfo);
            } catch (Exception e) {
                LOGGER.error("sync register info failed!", e);
            }
        }
    }
}
