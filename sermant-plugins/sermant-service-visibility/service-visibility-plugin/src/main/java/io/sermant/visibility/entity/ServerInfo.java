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

package io.sermant.visibility.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Basic information about the service
 *
 * @author zhp
 * @since 2022-11-30
 */
public class ServerInfo extends BaseInfo {
    /**
     * Region
     */
    private String zone;

    /**
     * Namespace
     */
    private String project;

    /**
     * Environment
     */
    private String environment;

    /**
     * The name of the service
     */
    private String serviceName;

    /**
     * The type of operation
     */
    private String operateType;

    /**
     * The name of the app
     */
    private String applicationName;

    /**
     * Group name
     */
    private String groupName;

    /**
     * Version number
     */
    private String version;

    /**
     * Consanguinity information
     */
    private List<Consanguinity> consanguinityList;

    /**
     * Contract Information
     */
    private List<Contract> contractList;

    /**
     * Registration Information
     */
    private Map<String, BaseInfo> registryInfo;

    /**
     * Service ID
     */
    private String instanceId;

    /**
     * expiration date
     */
    private Date validateDate;

    /**
     * A collection of instance IDs
     */
    private List<String> instanceIds;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
