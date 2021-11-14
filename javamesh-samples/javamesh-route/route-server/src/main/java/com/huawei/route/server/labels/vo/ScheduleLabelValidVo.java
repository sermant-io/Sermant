/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签定时生效实体类
 *
 * @author pengyuyi
 * @since 2021-07-12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScheduleLabelValidVo extends LabelValidVo {
    private String ip;

    private Integer port;
}
