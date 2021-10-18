package com.huawei.apm.core.ext.lubanops.access.inbound;

import java.util.Map;

import com.huawei.apm.core.ext.lubanops.Constants;
import com.huawei.apm.core.ext.lubanops.access.Header;
import com.huawei.apm.core.ext.lubanops.utils.JSON;

/**
 * @author
 * @since 2020/4/30
 **/
public class EventDataHeader extends Header {
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

    /**
     * 将二进制解析成对象
     * @param bb
     * @return
     */
    public static EventDataHeader parse(byte[] bb) {

        return JSON.parseObject(bb, EventDataHeader.class);

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

    public void setBizId(long bizId) {
        this.bizId = bizId;
    }

    public long getBizId() {
        return bizId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setAttachment(Map<String, String> attachment) {
        this.attachment = attachment;
    }

    public Map<String, String> getAttachment() {
        return attachment;
    }

    /**
     * 转成json的二进制
     * @return
     */
    @Override
    public byte[] toBytes() {
        String s = JSON.toJSONString(this);
        return s.getBytes(Constants.DEFAULT_CHARSET);
    }
}
