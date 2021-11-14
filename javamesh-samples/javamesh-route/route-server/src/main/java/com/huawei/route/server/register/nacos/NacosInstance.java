package com.huawei.route.server.register.nacos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huawei.route.server.constants.NacosConstants;
import com.huawei.route.server.entity.AbstractInstance;
import lombok.Getter;
import lombok.Setter;

/**
 * nacos服务实例信息
 *
 * @author zhouss
 * @since 2021-10-08
 */
@Getter
@Setter
public class NacosInstance extends AbstractInstance {
    /**
     * 是否有效，取自nacos注册中心
     */
    @JsonIgnore
    private boolean isValid;

    /**
     * 权重，取自nacos注册中心
     */
    @JsonIgnore
    private double weight;

    /**
     * 命名空间
     */
    @JsonIgnore
    private String namespace;

    @Override
    @JsonIgnore
    public String getHolderKey() {
        return namespace + NacosConstants.NACOS_INSTANCE_ID_SEPARATOR + getServiceName();
    }
}
