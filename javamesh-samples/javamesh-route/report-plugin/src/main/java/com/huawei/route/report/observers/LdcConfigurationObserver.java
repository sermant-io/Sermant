/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.observers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;
import com.huawei.route.report.common.ReporterConstants;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ldc在标签库修改或时新增时的监听类
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-08-06
 */
public class LdcConfigurationObserver extends AbstractReporterObserver {
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 标签库配置的ldc的key
     */
    private final String routeConfigurationKey = "LDC_CONFIGURATION";

    @Override
    public void notify(Map<String, Properties> properties) {
        if (!properties.containsKey(routeConfigurationKey)) {
            return;
        }
        Properties routeConfigProperties = properties.get(routeConfigurationKey);
        if (routeConfigProperties == null) {
            routeConfigProperties = new Properties();
        }
        LOGGER.finer(String.format(Locale.ENGLISH, "ldc properties message:%s", routeConfigProperties));

        // 标签名称不为ROUTE_CONFIGURATION或是标签在前端未生效的直接return不做通知
        if (!Boolean.parseBoolean(routeConfigProperties.getProperty(ReporterConstants.ON_KEY))) {
            return;
        }
        JSONObject ldcJson = JSON.parseObject(routeConfigProperties.getProperty(ReporterConstants.VALUE_KEY));
        if (ldcJson != null) {
            // ldc配置信息中标识未生效的不做处理
            if (!Boolean.parseBoolean(ldcJson.getString(ReporterConstants.IS_VALID_KEY))) {
                return;
            }
            String ldc = ldcJson.getString(ReporterConstants.LDC_KEY);
            JSONArray businessArray = new JSONArray();
            try {
                businessArray = ldcJson.getJSONArray(ReporterConstants.BUSINESS_KEY);
            } catch (ClassCastException classCastException) {
                LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH,
                        "ldc businesses label configuration error. ldc configuration is:%s",
                        ldcJson.toJSONString()), classCastException);
            }
            if (StringUtils.isNotBlank(ldc) && businessArray.size() > 0) {
                comingMessage();
            }
        }
    }

    @Override
    public String getLabelName() {
        return routeConfigurationKey;
    }

}
