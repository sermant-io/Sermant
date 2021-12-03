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

package com.huawei.javamesh.core.lubanops.integration.access.inbound;

import java.util.Map;

import com.huawei.javamesh.core.lubanops.integration.access.Header;
import com.huawei.javamesh.core.lubanops.integration.utils.JSON;

/**
 * 监控数据的头部信息
 * @author
 * @since 2020/4/30
 **/
public class MonitorDataHeader extends Header {

    /**
     * 环境的ID
     */
    private long envId;

    /**
     * 实例ID
     */

    private long instanceId;

    /**
     * 应用ID
     */
    private long appId;

    /**
     * 业务ID
     */
    private long bizId;

    /**
     * 租户的ID
     */
    private int domainId;

    /**
     * 这个字段access的服务器时间，由access设置
     */
    private long accessTime;

    /**
     * 注册信息里面的attachment
     */
    private Map<String, String> attachment;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public long getEnvId() {
        return envId;
    }

    public void setEnvId(long envId) {
        this.envId = envId;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getBizId() {
        return bizId;
    }

    public void setBizId(long bizId) {
        this.bizId = bizId;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }

    public Map<String, String> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, String> attachment) {
        this.attachment = attachment;
    }
}
