/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * mysql request entity
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-04-13
 */
@Getter
@Setter
public class MysqlRequestEntity {
    private String sql;

    private Object[] parameters;
}
