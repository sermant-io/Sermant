/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse;

import com.alibaba.fastjson.JSONObject;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.plugin.service.PluginService;
import com.huawei.apm.core.service.ServiceManager;
import com.huawei.apm.core.service.dynamicconfig.Config;
import com.huawei.apm.core.service.dynamicconfig.DynamicConfigurationFactoryServiceImpl;
import com.huawei.apm.core.service.dynamicconfig.kie.KieDynamicConfigurationServiceImpl;
import com.huawei.apm.core.service.dynamicconfig.kie.listener.KvDataHolder;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigType;
import com.huawei.flowcontrol.adapte.cse.entity.CseServiceMeta;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static com.huawei.apm.core.service.dynamicconfig.kie.kie.KieRequestFactory.buildKieRequest;
import static com.huawei.apm.core.service.dynamicconfig.kie.kie.KieRequestFactory.buildLabel;
import static com.huawei.apm.core.service.dynamicconfig.kie.kie.KieRequestFactory.buildLabels;

/**
 * kie同步
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class KieConfigSyncer implements PluginService {
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 等待响应时间间隔
     * 单位S
     */
    private static final String WAIT = "10";

    private static final int WAIT_INTERVAL_MS = 2000;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Map<String, ConfigurationListener> listenerCache = new LinkedHashMap<String, ConfigurationListener>();

    private final DynamicConfigurationFactoryServiceImpl dynamicConfigurationFactoryService =
            ServiceManager.getService(DynamicConfigurationFactoryServiceImpl.class);

    /**
     * 初始化通知
     */
    @Override
    public void start() {
        // 此块只会适配KIE配置中心
        if (Config.getDynamicConfigType() != DynamicConfigType.KIE) {
            return;
        }
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!CseServiceMeta.getInstance().isReady()) {
                    try {
                        Thread.sleep(WAIT_INTERVAL_MS);
                    } catch (InterruptedException ignored) {
                        // ignored
                    }
                }
                initRequests();

            }
        });
    }

    @Override
    public void stop() {
        for (Map.Entry<String, ConfigurationListener> entry : listenerCache.entrySet()) {
            dynamicConfigurationFactoryService.getDynamicConfigurationService().addListener(entry.getKey(), entry.getValue());
        }
    }

    public void initRequests() {
        buildAppRequest();
        buildServiceRequest();
        buildCustomRequest();
        for (Map.Entry<String, ConfigurationListener> entry : listenerCache.entrySet()) {
            dynamicConfigurationFactoryService.getDynamicConfigurationService().addListener(entry.getKey(), entry.getValue());
        }
    }

    private void buildServiceRequest() {
        final String serviceRequest = buildKieRequest(WAIT, null, buildLabels(
                                buildLabel("app", CseServiceMeta.getInstance().getApp()),
                                buildLabel("service", CseServiceMeta.getInstance().getServiceName()),
                                buildLabel("environment", CseServiceMeta.getInstance().getEnvironment())));
        listenerCache.put(serviceRequest, new KieConfigListener());
    }

    private void buildAppRequest() {
        final String appEnvironmentRequest = buildKieRequest(WAIT, null, buildLabels(
                                buildLabel("app", CseServiceMeta.getInstance().getApp()),
                                buildLabel("environment", CseServiceMeta.getInstance().getEnvironment())));
        listenerCache.put(appEnvironmentRequest, new KieConfigListener());
    }

    private void buildCustomRequest() {
        final String customRequest = buildKieRequest(WAIT, null, buildLabels(
                                buildLabel(CseServiceMeta.getInstance().getCustomLabel(),
                                        CseServiceMeta.getInstance().getCustomLabelValue())));
        listenerCache.put(customRequest, new KieConfigListener());
    }

    static class KieConfigListener implements ConfigurationListener {

        @Override
        public void process(ConfigChangedEvent event) {
            KvDataHolder.EventDataHolder eventDataHolder = JSONObject.parseObject(event.getContent(),
                    KvDataHolder.EventDataHolder.class);;
            LOGGER.config(String.format(Locale.ENGLISH,
                    "Received config success! added keys:[%s], modified keys:[%s], deleted keys:[%s]",
                    eventDataHolder.getAdded().keySet(), eventDataHolder.getModified().keySet(),
                    eventDataHolder.getDeleted().keySet()));
            ResolverManager.INSTANCE.resolve(eventDataHolder.getAdded());
            ResolverManager.INSTANCE.resolve(eventDataHolder.getModified());
            ResolverManager.INSTANCE.resolve(eventDataHolder.getDeleted(), true);
        }
    }
}
