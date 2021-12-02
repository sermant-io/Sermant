/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.service;

import com.huawei.javamesh.core.plugin.config.PluginConfigManager;
import com.huawei.javamesh.core.plugin.service.PluginService;
import com.huawei.javamesh.core.service.ServiceManager;
import com.huawei.javamesh.core.service.dynamicconfig.DynamicConfigurationFactoryServiceImpl;
import com.huawei.javamesh.core.service.dynamicconfig.utils.LabelGroupUtils;
import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.route.common.gray.config.GrayConfig;
import com.huawei.route.common.gray.listener.GrayConfigListener;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 配置服务
 *
 * @author pengyuyi
 * @date 2021/11/24
 */
public class ConfigServiceImpl implements PluginService {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final DynamicConfigurationFactoryServiceImpl dynamicConfigurationFactoryService =
            ServiceManager.getService(DynamicConfigurationFactoryServiceImpl.class);

    private GrayConfig grayConfig;

    private String group;

    private GrayConfigListener listener;

    /**
     * 初始化通知
     */
    @Override
    public void start() {
        grayConfig = PluginConfigManager.getPluginConfig(GrayConfig.class);
        // 此块只会适配KIE配置中心
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                initRequests();
            }
        });
    }

    private void initRequests() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(grayConfig.getDubboCustomLabel(), grayConfig.getDubboCustomLabelValue());
        group = LabelGroupUtils.createLabelGroup(map);
        listener = new GrayConfigListener(DubboCache.getLabelName());
        dynamicConfigurationFactoryService.getDynamicConfigurationService().addGroupListener(group, listener);
    }

    @Override
    public void stop() {
        dynamicConfigurationFactoryService.getDynamicConfigurationService().removeGroupListener(group, group, listener);
    }
}
