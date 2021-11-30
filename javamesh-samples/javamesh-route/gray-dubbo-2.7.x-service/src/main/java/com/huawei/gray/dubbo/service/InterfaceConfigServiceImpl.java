/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.service;

import com.huawei.apm.core.plugin.config.PluginConfigManager;
import com.huawei.apm.core.util.SpiLoadUtil.SpiWeight;
import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.route.common.gray.config.GrayConfig;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;

import org.apache.dubbo.config.ApplicationConfig;

import java.lang.reflect.Method;

/**
 * InterfaceConfigInterceptor的service
 *
 * @author pengyuyi
 * @date 2021/11/24
 */
@SpiWeight(2)
public class InterfaceConfigServiceImpl extends InterfaceConfigService {
    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        if (result instanceof ApplicationConfig) {
            ApplicationConfig config = (ApplicationConfig) result;
            GrayConfig grayConfig = PluginConfigManager.getPluginConfig(GrayConfig.class);
            String version = grayConfig.getGrayVersion(GrayConstant.GRAY_DEFAULT_VERSION);
            config.getParameters().put(GrayConstant.GRAY_VERSION_KEY, version);
            String ldc = grayConfig.getLdc(GrayConstant.GRAY_DEFAULT_LDC);
            config.getParameters().put(GrayConstant.GRAY_LDC_KEY, ldc);
            DubboCache.setAppName(config.getName());
            GrayConfiguration grayConfiguration = LabelCache.getLabel(DubboCache.getLabelName());
            CurrentTag currentTag = grayConfiguration.getCurrentTag();
            if (currentTag == null) {
                currentTag = new CurrentTag();
            }
            currentTag.setVersion(version);
            currentTag.setLdc(ldc);
            grayConfiguration.setCurrentTag(currentTag);
        }
    }
}