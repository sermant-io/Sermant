/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.dto;

import com.huawei.route.server.labels.group.LabelGroup;
import com.huawei.route.server.labels.label.Label;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 返回给前端的标签组信息
 *
 * @author Zhang Hu
 * @since 2021-04-09
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LabelGroupDto extends LabelGroup implements Serializable {
    private static final long serialVersionUID = -8732122911406826599L;

    private List<Label> labels;
}
