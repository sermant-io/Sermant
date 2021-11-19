/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.entity;

/**
 * CSE服务原信息类 - 单例
 * 如果依赖sdk的话，可考虑直接替换为MicroserviceMeta
 * {@see org.apache.servicecomb.governance.MicroserviceMeta}
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class CseServiceMeta {
    private static final CseServiceMeta INSTANCE = new CseServiceMeta();

    /**
     * sc app名
     */
    private String app;

    /**
     * 服务名
     */
    private String service;

    /**
     * 版本
     */
    private String version;

    /**
     * 环境
     */
    private String environment;

    /**
     * sc自定义值
     */
    private String customLabelValue;

    /**
     * sc自定义标签
     */
    private String customLabel;

    /**
     * kie-project
     */
    private String project;

    private CseServiceMeta() {

    }

    public static CseServiceMeta getInstance() {
        return INSTANCE;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getCustomLabelValue() {
        return customLabelValue;
    }

    public void setCustomLabelValue(String customLabelValue) {
        this.customLabelValue = customLabelValue;
    }

    public String getCustomLabel() {
        return customLabel;
    }

    public void setCustomLabel(String customLabel) {
        this.customLabel = customLabel;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    /**
     * 当前数据是否都已经获取
     *
     * @return boolean
     */
    public boolean isReady() {
        return service != null && version != null && project != null
                && customLabel != null && customLabelValue != null
                && environment != null && app != null;
    }
}
