/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 标签生效、失效实体类
 *
 * @author zhanghu
 * @since 2021-05-24
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LabelValidVo extends LabelBusinessVo implements Serializable {
    private static final long serialVersionUID = 7114531682617920636L;

    private String on;
}
