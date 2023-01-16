/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.core.service.visibility.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务基本信息
 *
 * @author zhp
 * @since 2022-11-30
 */
public class ServerInfo extends BaseInfo {
    /**
     * 区域
     */
    private String zone;

    /**
     * 命名空间
     */
    private String project;

    /**
     * 环境
     */
    private String environment;

    /**
     * 服务名称
     */
    private String serverName;

    /**
     * 框架类型
     */
    private String serverType;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 版本号
     */
    private String version;

    /**
     * 血缘关系信息
     */
    private List<Consanguinity> consanguinityList;

    /**
     * 契约信息
     */
    private List<Contract> contractList;

    /**
     * 注册信息
     */
    private Map<String,BaseInfo> registryInfo;

    /**
     * 服务ID
     */
    private String instanceId;

    /**
     * 有效期
     */
    private Date validateDate;

    /**
     * 实例ID集合
     */
    private List<String> instanceIds;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Consanguinity> getConsanguinityList() {
        return consanguinityList;
    }

    public void setConsanguinityList(List<Consanguinity> consanguinityList) {
        this.consanguinityList = consanguinityList;
    }

    public List<Contract> getContractList() {
        return contractList;
    }

    public void setContractList(List<Contract> contractList) {
        this.contractList = contractList;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Map<String, BaseInfo> getRegistryInfo() {
        return registryInfo;
    }

    public void setRegistryInfo(Map<String, BaseInfo> registryInfo) {
        this.registryInfo = registryInfo;
    }

    public Date getValidateDate() {
        return validateDate;
    }

    public void setValidateDate(Date validateDate) {
        this.validateDate = validateDate;
    }

    public List<String> getInstanceIds() {
        return instanceIds;
    }

    public void setInstanceIds(List<String> instanceIds) {
        this.instanceIds = instanceIds;
    }
}
