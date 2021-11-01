/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.entity;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 上报数据
 *
 * @author zhouss
 * @since 2021-10-13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceRegistrarMessage extends BaseRegistrarMessage{

    /**
     * 下游服务名称
     */
    private String downServiceName;

    /**
     * Zookeeper中代表根路径；nacos中代表分组；serviceComb中代表所属应用
     */
    private String root;

    /**
     * 命名空间 nacos专属
     */
    private String namespaceId;

    /**
     * 注册中心的名称，有ZOOKEEPER、NACOS和SERVICECOMB三种
     */
    private String registry;

    /**
     * 协议，有dubbo和springcloud两种
     */
    private String protocol;

    /**
     * Ldc的businesses信息
     */
    private JSONArray businesses;

    /**
     * 注册中心中服务的名称，serviceComb独有，其余为null
     */
    private String registrarServiceName;

    /**
     * 集群名称，nacos独有，其他为nul
     *
     */
    private String clusterName;

    @Override
    public String getShareKey() {
        return namespaceId;
    }
}
