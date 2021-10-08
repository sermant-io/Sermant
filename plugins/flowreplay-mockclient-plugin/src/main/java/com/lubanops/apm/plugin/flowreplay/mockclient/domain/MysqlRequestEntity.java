/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 录制参数实体
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-07
 */
@Getter
@Setter
public class MysqlRequestEntity {
    private String sql;

    private Object[] parameters;
}
