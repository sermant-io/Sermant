/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.vo;

import com.huawei.route.server.labels.label.Label;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 前端请求实体类
 *
 * @author Zhang Hu
 * @since 2021-04-13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LabelVo extends Label implements Serializable {
    private static final long serialVersionUID = -1941940514869089174L;

    /**
     * 标签组名
     */
    @NotBlank(message = "标签组名不能为空")
    private String labelGroupName;

    /**
     * 修改时间时间戳
     */
    private long updateTimeStamp;
}
