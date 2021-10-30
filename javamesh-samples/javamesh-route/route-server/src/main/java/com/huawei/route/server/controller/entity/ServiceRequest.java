/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.controller.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 服务注册信息请求实体
 *
 * @author zhouss
 * @since 2021-10-09
 */
@Getter
@Setter
public class ServiceRequest {
    /**
     * ldc属性
     */
    private String ldc;

    /**
     * 服务名集合
     */
    private List<String> serviceNames;

    /**
     * 标签匹配
     */
    private String tagName;
}
