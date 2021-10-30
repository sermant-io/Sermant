/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.register.nacos.sync;

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import lombok.Data;

import java.util.List;

/**
 * nacos服务信息
 *
 * @author zhouss
 * @since 2021-10-28
 */
@Data
public class ServiceHosts {
    /**
     * nacos服务列表
     */
    private List<ServiceInfo> serviceList;

    /**
     * 服务个数
     */
    private int count;
}
