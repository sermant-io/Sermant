/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.observers;

import java.util.Map;
import java.util.Properties;

/**
 * 灰度配置观察者
 *
 * @author zhouss
 * @since 2021-11-02
 */
public class GrayConfigurationObserver extends AbstractReporterObserver{
    @Override
    public void notify(Map<String, Properties> properties) {
        comingMessage();
    }

    @Override
    public String getLabelName() {
        return "GARY_CONFIGURATION";
    }
}
