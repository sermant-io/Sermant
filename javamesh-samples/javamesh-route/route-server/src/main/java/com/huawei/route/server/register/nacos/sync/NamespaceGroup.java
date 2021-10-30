/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.register.nacos.sync;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * 命名空间的订阅服务名管理
 *
 * @author zhouss
 * @since 2021-10-26
 */
@Data
@Builder
public class NamespaceGroup {
    /**
     * nacos命名空间
     */
    private String namespace;

    /**
     * nacos服务名集合
     * Map<group, set<nacosServiceName>>
     */
    private Map<String, Set<String>> groupServices;

}
