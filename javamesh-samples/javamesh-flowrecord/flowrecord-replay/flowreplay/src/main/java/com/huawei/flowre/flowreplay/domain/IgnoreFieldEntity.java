/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 忽略字段实体
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-07
 */
@Getter
@Setter
public class IgnoreFieldEntity {
    /**
     * 忽略字段的接口名
     */
    private String method;

    /**
     * 忽略字段相信信息列表
     */
    private List<Field> fields;
}

