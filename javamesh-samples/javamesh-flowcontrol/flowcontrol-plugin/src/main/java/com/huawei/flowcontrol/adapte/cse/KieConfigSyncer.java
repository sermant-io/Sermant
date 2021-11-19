/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.PluginService;
import com.huawei.config.kie.KieRequest;
import com.huawei.config.kie.KieRequestFactory;
import com.huawei.config.listener.ConfigurationListener;
import com.huawei.config.listener.KvDataHolder;
import com.huawei.config.listener.SubscriberManager;
import com.huawei.flowcontrol.adapte.cse.entity.CseServiceMeta;

import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

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

    private final Map<KieRequest, ConfigurationListener> listenerCache = new LinkedHashMap<KieRequest, ConfigurationListener>();

    /**
     * 初始化通知
     */
    @Override
    public void init() {
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
        for (Map.Entry<KieRequest, ConfigurationListener> entry : listenerCache.entrySet()) {
            SubscriberManager.getInstance().unSubscribe(entry.getKey(), entry.getValue());
        }
    }

    public void initRequests() {
        buildAppRequest();
        buildServiceRequest();
        buildCustomRequest();
        for (Map.Entry<KieRequest, ConfigurationListener> entry : listenerCache.entrySet()) {
            SubscriberManager.getInstance().subscribe(entry.getKey(), entry.getValue());
        }
    }

    private void buildServiceRequest() {
        final KieRequest serviceRequest =
                KieRequestFactory.buildKieRequest(WAIT, null,
                        KieRequestFactory.buildLabels(
                                KieRequestFactory.buildLabel("app", CseServiceMeta.getInstance().getApp()),
                                KieRequestFactory.buildLabel("service", CseServiceMeta.getInstance().getServiceName()),
                                KieRequestFactory.buildLabel("environment", CseServiceMeta.getInstance().getEnvironment())));
        listenerCache.put(serviceRequest, new KieConfigListener());
    }

    private void buildAppRequest() {
        final KieRequest appEnvironmentRequest =
                KieRequestFactory.buildKieRequest(WAIT, null,
                        KieRequestFactory.buildLabels(
                                KieRequestFactory.buildLabel("app", CseServiceMeta.getInstance().getApp()),
                                KieRequestFactory.buildLabel("environment", CseServiceMeta.getInstance().getEnvironment())));
        listenerCache.put(appEnvironmentRequest, new KieConfigListener());
    }

    private void buildCustomRequest() {
        final KieRequest customRequest =
                KieRequestFactory.buildKieRequest(WAIT, null,
                        KieRequestFactory.buildLabels(
                                KieRequestFactory.buildLabel(CseServiceMeta.getInstance().getCustomLabel(),
                                        CseServiceMeta.getInstance().getCustomLabelValue())));
        listenerCache.put(customRequest, new KieConfigListener());
    }

    static class KieConfigListener implements ConfigurationListener {
        @Override
        public void onEvent(EventObject object) {
            final Object source = object.getSource();
            if (source instanceof KvDataHolder.EventDataHolder) {
                KvDataHolder.EventDataHolder eventDataHolder = (KvDataHolder.EventDataHolder) source;
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
}
