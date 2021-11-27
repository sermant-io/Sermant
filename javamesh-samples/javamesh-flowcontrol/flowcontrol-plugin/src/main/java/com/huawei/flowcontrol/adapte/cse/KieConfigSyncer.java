/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.ServiceManager;
import com.huawei.apm.core.service.dynamicconfig.DynamicConfigurationFactoryServiceImpl;
import com.huawei.apm.core.service.dynamicconfig.kie.utils.KieGroupUtils;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangeType;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.flowcontrol.adapte.cse.entity.CseServiceMeta;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * kie同步
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class KieConfigSyncer {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final int WAIT_INTERVAL_MS = 10 * 1000;

    /**
     * 最大等待5分钟
     */
    private static final int MAX_WAIT_INTERVAL_MS = 60 * 5 * 1000;

    private final Map<String, KieConfigListener> listenerCache = new LinkedHashMap<String, KieConfigListener>();

    private final DynamicConfigurationFactoryServiceImpl dynamicConfigurationFactoryService =
            ServiceManager.getService(DynamicConfigurationFactoryServiceImpl.class);

    /**
     * 初始化通知
     */
    public void start() {
        // 此块只会适配KIE配置中心
        long waitTimeMs = 0L;
        while (!CseServiceMeta.getInstance().isReady()) {
            try {
                Thread.sleep(WAIT_INTERVAL_MS);
                waitTimeMs += WAIT_INTERVAL_MS;
                if (waitTimeMs >= MAX_WAIT_INTERVAL_MS) {
                    // 大于最大等待时间，放弃同步
                    return;
                }
            } catch (InterruptedException ignored) {
                // ignored
            }
        }
        initRequests();
    }

    public void initRequests() {
        buildAppRequest();
        buildServiceRequest();
        buildCustomRequest();
        for (Map.Entry<String, KieConfigListener> entry : listenerCache.entrySet()) {
            dynamicConfigurationFactoryService.getDynamicConfigurationService()
                    .addGroupListener(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 停止方法
     */
    public void stop() {
        for (Map.Entry<String, KieConfigListener> entry : listenerCache.entrySet()) {
            dynamicConfigurationFactoryService.getDynamicConfigurationService()
                    .removeGroupListener(entry.getKey(), entry.getKey(), entry.getValue());
        }
    }

    private void buildServiceRequest() {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("app", CseServiceMeta.getInstance().getApp());
        map.put("service", CseServiceMeta.getInstance().getServiceName());
        map.put("environment", CseServiceMeta.getInstance().getEnvironment());
        listenerCache.put(KieGroupUtils.createLabelGroup(map), new KieConfigListener());
    }

    private void buildAppRequest() {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("app", CseServiceMeta.getInstance().getApp());
        map.put("environment", CseServiceMeta.getInstance().getEnvironment());
        listenerCache.put(KieGroupUtils.createLabelGroup(map), new KieConfigListener());
    }

    private void buildCustomRequest() {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put(CseServiceMeta.getInstance().getCustomLabel(), CseServiceMeta.getInstance().getCustomLabelValue());
        listenerCache.put(KieGroupUtils.createLabelGroup(map), new KieConfigListener());
    }

    static class KieConfigListener implements ConfigurationListener {

        @Override
        public void process(ConfigChangedEvent event) {
            ResolverManager.INSTANCE.resolve(event.getKey(), event.getContent(),
                    event.getChangeType() == ConfigChangeType.DELETED);
            LOGGER.log(Level.CONFIG, String.format("Config [%s] has been %s ", event.getKey(), event.getChangeType()));
        }
    }
}
