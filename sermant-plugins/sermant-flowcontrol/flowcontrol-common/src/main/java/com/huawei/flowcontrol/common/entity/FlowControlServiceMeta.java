/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.entity;

/**
 * service source information class - If the singleton relies on the sdk, consider replacing
 * it directly with MicroserviceMeta {@see org.apache.servicecomb.governance.MicroserviceMeta}
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class FlowControlServiceMeta {
    private static final FlowControlServiceMeta INSTANCE = new FlowControlServiceMeta();

    /**
     * sc appName
     */
    private String app;

    /**
     * service name
     */
    private String serviceName;

    /**
     * version
     */
    private String version;

    /**
     * environment
     */
    private String environment;

    /**
     * sc custom value
     */
    private String customLabelValue;

    /**
     * sc custom tag
     */
    private String customLabel;

    /**
     * kie-project
     */
    private String project;

    /**
     * whether it is a dubbo application
     */
    private boolean isDubboService;

    private FlowControlServiceMeta() {
    }

    public boolean isDubboService() {
        return isDubboService;
    }

    public void setDubboService(boolean dubboService) {
        isDubboService = dubboService;
    }

    public static FlowControlServiceMeta getInstance() {
        return INSTANCE;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
     * whether all current data is obtained
     *
     * @return boolean
     */
    public boolean isReady() {
        return isCustomInfoReady() && isServiceInfoReady();
    }

    private boolean isServiceInfoReady() {
        return serviceName != null && environment != null && app != null;
    }

    private boolean isCustomInfoReady() {
        return project != null && customLabel != null && customLabelValue != null;
    }
}
