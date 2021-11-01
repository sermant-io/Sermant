package com.huawei.route.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huawei.route.server.constants.RouteConstants;
import com.huawei.route.server.rules.InstanceTagConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;

/**
 * 实例信息
 *
 * @author zhouss
 * @since 2021-10-08
 */
@Getter
public abstract class AbstractInstance {

    /**
     * 该实例的ldc
     */
    private String ldc = RouteConstants.DEFAULT_LDC;

    /**
     * 所属服务名
     */
    @Setter
    private String serviceName;

    /**
     * 标签配置
     */
    @JsonIgnore
    private InstanceTagConfiguration instanceTagConfiguration;

    /**
     * 当前实例IP
     */
    @Setter
    private String ip;

    /**
     * 端口
     */
    @Setter
    private int port;

    /**
     * 上次心跳时间戳
     */
    @Setter
    @JsonIgnore
    private long lastHeartbeat;

    /**
     * 该服务是否健康
     */
    @Setter
    @JsonIgnore
    private boolean isHealth;

    /**
     * 额外一些信息
     */
    @Setter
    private Map<String, String> metadata;

    /**
     * 当前实例标签
     */
    private Tag currentTag = Tag.getDefaultTag();

    /**
     * 获取holder的键
     *
     * @return holdKey
     */
    @JsonIgnore
    public abstract String getHolderKey();

    @JsonIgnore
    public String getInstanceKey() {
        return String.format("%s@%d", ip , port);
    }

    public void setInstanceTagConfiguration(InstanceTagConfiguration instanceTagConfiguration) {
        this.instanceTagConfiguration = instanceTagConfiguration;
        this.currentTag = instanceTagConfiguration == null ? null : instanceTagConfiguration.getCurrentTag();
        if (this.currentTag != null) {
            this.ldc = this.currentTag.get("ldc");
            if (this.ldc == null) {
                this.currentTag.addTag("ldc", RouteConstants.DEFAULT_LDC);
                this.ldc = RouteConstants.DEFAULT_LDC;
            }
            if (this.currentTag.get(RouteConstants.TAG_NAME_KEY) == null) {
                this.currentTag.addTag(RouteConstants.TAG_NAME_KEY, RouteConstants.DEFAULT_TAG_NAME);
            }
        } else {
            // 使用缺省值
            this.ldc = RouteConstants.DEFAULT_LDC;
            this.currentTag = Tag.getDefaultTag();
        }
    }

    @JsonIgnore
    public String getTagName() {
        if (currentTag == null) {
            return null;
        }
        return currentTag.getTagName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractInstance instance = (AbstractInstance) o;
        return port == instance.port && ip.equals(instance.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
