package com.huawei.javamesh.core.lubanops.integration.access.trace;

import java.util.Map;

import com.huawei.javamesh.core.lubanops.integration.access.inbound.EventDataBody;

/**
 * event信息上报没继承自 EventDataBody 并且加上header的信息 <br>
 *
 * @author
 * @since 2020年3月4日
 */
public class SpanEventReport extends EventDataBody {
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
     * 注册信息里面的attachment
     */
    private Map<String, String> attachment;

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

    public Map<String, String> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, String> attachment) {
        this.attachment = attachment;
    }
}
