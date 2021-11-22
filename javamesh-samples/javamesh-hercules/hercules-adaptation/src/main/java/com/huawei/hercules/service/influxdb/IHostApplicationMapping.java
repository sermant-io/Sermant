/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb;

import java.util.Map;

/**
 * 功能描述：主机信息和应用部署服务实例之间的映射
 *
 * @author z30009938
 * @since 2021-11-22
 */
public interface IHostApplicationMapping {
    /**
     * 获取主机和应用映射关系信息
     *
     * @return 主机应用和映射关系
     */
    Map<String, Object> getHostApplicationMapping();
}
