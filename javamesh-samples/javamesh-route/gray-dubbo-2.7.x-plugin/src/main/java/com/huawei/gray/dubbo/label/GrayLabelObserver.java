/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.label;

import com.huawei.apm.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.route.common.gray.addr.AddrCache;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.route.common.label.observers.LabelUpdateObserver;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Properties;

/**
 * 标签观察者
 *
 * @author pengyuyi
 * @date 2021/10/13
 */
public class GrayLabelObserver implements LabelUpdateObserver {
    @Override
    public void notify(Map<String, Properties> labelMap) {
        Properties properties = labelMap.get(GrayConstant.GRAY_CONFIGURATION);
        if (CollectionUtils.isEmpty(properties) || !properties.containsKey(GrayConstant.LABEL_VALUE)) {
            return;
        }
        GrayConfiguration grayConfiguration = JSONObject.parseObject(properties.getProperty(GrayConstant.LABEL_VALUE),
                GrayConfiguration.class, Feature.OrderedField);
        grayConfiguration.setOn(Boolean.parseBoolean(properties.getProperty(GrayConstant.LABEL_SWITCH_FILED_NAME)));
        if (grayConfiguration.isRealValid()) {
            initGrayConfiguration(grayConfiguration);
            AddrCache.start();
        } else {
            AddrCache.shutdown(true);
        }
        LabelCache.setLabel(DubboCache.getAppName(), grayConfiguration);
    }

    @Override
    public String getLabelName() {
        return GrayConstant.GRAY_CONFIGURATION;
    }

    private void initGrayConfiguration(GrayConfiguration grayConfiguration) {
        CurrentTag currentTag = grayConfiguration.getCurrentTag();
        if (currentTag == null) {
            currentTag = new CurrentTag();
        }
        if (StringUtils.isBlank(currentTag.getVersion())) {
            currentTag.setVersion(GrayConstant.GRAY_DEFAULT_VERSION);
        }
        if (StringUtils.isBlank(currentTag.getLdc())) {
            currentTag.setLdc(GrayConstant.GRAY_DEFAULT_LDC);
        }
        grayConfiguration.setCurrentTag(currentTag);
    }
}
