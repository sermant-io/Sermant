/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.group;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 标签组实体类
 *
 * @author Zhang Hu
 * @since 2021-04-09
 */
@Data
public class LabelGroup implements Serializable {
    private static final long serialVersionUID = 3718374294863564756L;

    /**
     * 标签组名
     */
    @NotBlank(message = "标签组名不能为空")
    private String labelGroupName;

    /**
     * 标签组描述
     */
    @NotBlank(message = "标签组描述不能为空")
    private String description;
}
