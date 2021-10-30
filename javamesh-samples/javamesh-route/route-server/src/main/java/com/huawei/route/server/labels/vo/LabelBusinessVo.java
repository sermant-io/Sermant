/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 标签受用业务
 *
 * @author zhanghu
 * @since 2021-05-24
 */
@Data
public class LabelBusinessVo implements Serializable {
    private static final long serialVersionUID = -6543541410869612978L;

    @NotBlank(message = "服务名不能为空")
    private String serviceName;

    @NotBlank(message = "实例名不能为空")
    private String instanceName;

    private String value;

    @NotBlank(message = "标签组名不能为空")
    private String labelGroupName;

    @NotBlank(message = "标签名不能为空")
    private String labelName;
}
