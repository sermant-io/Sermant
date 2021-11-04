/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 标签配置
 *
 * @author zhouss
 * @since 2021-11-03
 */
public class LdcConfiguration {
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 单例
     */
    private static final LdcConfiguration INSTANCE = new LdcConfiguration();
    /**
     * ldc名称
     */
    private String ldc = "LOCAL_LDC";

    /**
     * 是否为网关
     */
    private String isGateWay;

    /**
     * 业务分组
     */
    private List<ServiceGroup> businesses;

    private LdcConfiguration() {

    }

    public String getLdc() {
        return ldc;
    }

    public void setLdc(String ldc) {
        this.ldc = ldc;
    }

    public String getIsGateWay() {
        return isGateWay;
    }

    public void setIsGateWay(String isGateWay) {
        this.isGateWay = isGateWay;
    }

    public List<ServiceGroup> getBusinesses() {
        return businesses;
    }

    public void setBusinesses(List<ServiceGroup> businesses) {
        this.businesses = businesses;
    }

    public static LdcConfiguration resolveConfiguration(Map<String, Properties> labelProperties) {
        Properties routeConfigProperties = labelProperties.get(ReporterConstants.LDC_CONFIGURATION_KEY);
        JSONObject ldcJson;
        if (routeConfigProperties == null
                || !Boolean.parseBoolean(routeConfigProperties.getProperty(ReporterConstants.ON_KEY))
                || (ldcJson = JSON.parseObject(routeConfigProperties.getProperty(ReporterConstants.VALUE_KEY))) == null
                || !Boolean.parseBoolean(ldcJson.getString(ReporterConstants.IS_VALID_KEY))) {
            // 清理数据返回
            clear();
            return INSTANCE;
        }
        INSTANCE.setLdc(ldcJson.getString(ReporterConstants.LDC_KEY));
        INSTANCE.setIsGateWay(ldcJson.getString(ReporterConstants.IS_GATE_WAY_KEY));
        try {
            final JSONArray businessArray = ldcJson.getJSONArray(ReporterConstants.BUSINESS_KEY);
            if (businessArray != null && businessArray.size() > 0) {
                INSTANCE.setBusinesses(JSONArray.parseArray(JSONObject.toJSONString(businessArray), ServiceGroup.class));
            }
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH,
                    "ldc businesses label configuration error. ldc configuration is:%s",
                    ldcJson.toJSONString()), exception);
        }
        return INSTANCE;
    }

    public static LdcConfiguration getInstance() {
        return INSTANCE;
    }

    private static void clear() {
        if (INSTANCE != null) {
            INSTANCE.setBusinesses(null);
            INSTANCE.setLdc("LOCAL_LDC");
            INSTANCE.setIsGateWay(null);
        }
    }

    /**
     * 当前LDC配置是否含有业务分组
     *
     * @return 业务分组
     */
    public boolean hasBusiness() {
        return businesses != null && !businesses.isEmpty();
    }

    public static class ServiceGroup {
        private String groupName;

        private Set<String> services;

        public ServiceGroup() {
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public Set<String> getServices() {
            return services;
        }

        public void setServices(Set<String> services) {
            this.services = services;
        }

        @Override
        public String toString() {
            return "ServiceGroup{" +
                    "groupName='" + groupName +
                    ", services=" + services +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "LdcConfiguration{" +
                "ldc='" + ldc + '\'' +
                ", isGateWay='" + isGateWay + '\'' +
                ", businesses=" + businesses +
                '}';
    }
}
