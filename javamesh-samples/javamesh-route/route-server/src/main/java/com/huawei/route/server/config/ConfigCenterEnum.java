/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.config;

import lombok.Getter;

/**
 * 定义使用那种配置中心
 *
 * @author zhouss
 * @since 2021-10-12
 */
@Getter
public enum ConfigCenterEnum {
    /**
     * 配置中心类型, 目前仅支持ZOOKEEPER
     */
    ZOOKEEPER("zookeeper");

    private final String configCenter;

    ConfigCenterEnum(String configCenter) {
        this.configCenter = configCenter;
    }
}
