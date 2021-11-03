/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.observers;

import com.huawei.route.report.common.LdcConfiguration;
import com.huawei.route.report.common.ReporterConstants;

import java.util.Map;
import java.util.Properties;

/**
 * ldc在标签库修改或时新增时的监听类
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-08-06
 */
public class LdcConfigurationObserver extends AbstractReporterObserver {
    @Override
    public void notify(Map<String, Properties> properties) {
        if (!properties.containsKey(ReporterConstants.LDC_CONFIGURATION_KEY)) {
            return;
        }
        final LdcConfiguration ldcConfiguration = LdcConfiguration.resolveConfiguration(properties);
        if (ldcConfiguration.getBusinesses() != null && !ldcConfiguration.getBusinesses().isEmpty()) {
            comingMessage();
        }
    }

    @Override
    public String getLabelName() {
        return ReporterConstants.LDC_CONFIGURATION_KEY;
    }

}
