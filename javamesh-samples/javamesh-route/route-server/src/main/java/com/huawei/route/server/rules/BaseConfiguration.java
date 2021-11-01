/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.route.server.entity.Tag;
import lombok.Getter;
import lombok.Setter;

/**
 * 基本配置
 *
 * @author zhouss
 * @since 2021-10-23
 */
@Getter
@Setter
public class BaseConfiguration {

    /**
     * 该配置是否生效
     */
    @JsonProperty(value = "isValid")
    private boolean isValid;

    /**
     * 是否为接口层实例
     * 该参数暂时未使用，后续考虑使用
     */
    @JsonProperty(value = "isEntrance")
    private boolean isEntrance;

    /**
     * 当前实例标签
     */
    private Tag currentTag;
}
