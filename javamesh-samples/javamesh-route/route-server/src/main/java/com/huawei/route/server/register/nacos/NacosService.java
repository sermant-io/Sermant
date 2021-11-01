package com.huawei.route.server.register.nacos;

import com.huawei.route.server.entity.AbstractService;
import com.huawei.route.server.register.RegisterCenterTypeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * nacos服务
 *
 * @author zhouss
 * @since 2021-10-08
 */
@Getter
@Setter
public class NacosService extends AbstractService<NacosInstance> {
    /**
     * 分组信息
     */
    private String group;

    /**
     * nacos命名空间
     */
    private String namespaceId;

    /**
     * 所属集群
     */
    private String clusters;

    /**
     * nacos服务标志
     */
    private String dom;

    public NacosService() {
        super(RegisterCenterTypeEnum.NACOS);
    }
}
