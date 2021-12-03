/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.javamesh.core.lubanops.core.transfer.dto;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.MonitorItem;
import com.huawei.javamesh.core.lubanops.integration.access.Address;

/**
 * 心跳的结果信息
 * @author
 * @Date $ $
 **/
public class HeartBeatResult extends Result {

    /**
     * 下一次心跳的周期
     */
    private Integer heartBeatInterval;

    /**
     * 附属信息，收到这个信息后，原封不动得通过数据上报
     */
    private Map<String, String> attachment;

    private List<MonitorItem> monitorItemList;

    /**
     * 系统属性
     */
    private Map<String, String> systemProperties;

    /**
     * access的地址描述
     */
    private List<Address> accessAddressList;

    /**
     * 实例状态0 代表ok， 1代表disabled
     */
    private Integer instanceStatus;

    /**
     * 对结果进行md5计算，如果内容变化了就下发新的配置
     */
    private String md5;

    public Integer getHeartBeatInterval() {
        return heartBeatInterval;
    }

    public void setHeartBeatInterval(Integer heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
    }

    public Map<String, String> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, String> attachment) {
        this.attachment = attachment;
    }

    public List<MonitorItem> getMonitorItemList() {
        return monitorItemList;
    }

    public void setMonitorItemList(List<MonitorItem> monitorItemList) {
        this.monitorItemList = monitorItemList;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    public List<Address> getAccessAddressList() {
        return accessAddressList;
    }

    public void setAccessAddressList(List<Address> accessAddressList) {
        this.accessAddressList = accessAddressList;
    }

    public Integer getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(Integer instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
