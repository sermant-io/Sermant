/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.label;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 标签信息实体类
 *
 * @author Zhang Hu
 * @since 2021-04-09
 */
@Data
public class Label implements Serializable {
    private static final long serialVersionUID = 2862579048955874173L;

    /**
     * 标签名
     */
    @NotBlank(message = "标签名不能为空")
    private String labelName;

    /**
     * 标签描述
     */
    @NotBlank(message = "标签描述不能为空")
    private String description;

    /**
     * 标签信息
     */
    private String value;

    /**
     * 所属服务数组
     */
    @NotEmpty(message = "所属服务不能为空")
    private String[] serviceNames;

    private String on;
}
