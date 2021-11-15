/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.controller.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 请求规则实体
 *
 * @author zhouss
 * @since 2021-10-15
 */
@Getter
@Setter
public class RuleRequest {
    /**
     * 服务集合
     */
    private List<String> targetServiceNames;

    /**
     * agent实例IP
     */
    private String ip;

    /**
     * 拦截的注册端口
     */
    private int port;
}
